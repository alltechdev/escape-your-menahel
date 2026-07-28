package com.escapegame.engine

import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Typeface
import com.escapegame.core.GameConfig
import com.escapegame.entities.CandyMan
import com.escapegame.entities.Chalk
import com.escapegame.entities.FelafelBall
import com.escapegame.entities.Mashgiach
import com.escapegame.entities.Menahel
import com.escapegame.entities.PowerUp
import com.escapegame.entities.Rugelach
import com.escapegame.entities.Talmid
import com.escapegame.levels.Levels
import com.escapegame.model.Achievement
import com.escapegame.model.Difficulty
import com.escapegame.model.GamePhase
import com.escapegame.model.Modifier
import com.escapegame.model.LevelDefinition
import com.escapegame.model.Platform
import com.escapegame.model.PowerUpType
import com.escapegame.audio.MusicEngine
import com.escapegame.audio.Sfx
import com.escapegame.net.LeaderboardClient
import com.escapegame.persistence.GamePrefs
import com.escapegame.render.BackgroundRenderer
import com.escapegame.render.HudRenderer
import com.escapegame.render.GameBoyShellRenderer
import com.escapegame.render.OverlayRenderer
import com.escapegame.render.TouchGamepadLayout
import com.escapegame.render.TouchGamepadRenderer
import kotlin.random.Random

/**
 * Owns all game state and rules. The engine simulates in a fixed virtual
 * world ([GameConfig.WORLD_WIDTH] x [GameConfig.WORLD_HEIGHT]) which
 * [draw] scales to whatever surface it is handed — tiny keypad-phone screens
 * included.
 *
 * Not internally synchronized: the hosting view calls every public method
 * under a single lock.
 */
class GameEngine(
    private val prefs: GamePrefs,
    private val music: MusicEngine
) {

    private var worldWidth = GameConfig.WORLD_WIDTH
    private var worldHeight = GameConfig.WORLD_HEIGHT
    private var floorTop = worldHeight * GameConfig.FLOOR_TOP_FRACTION
    private var doorLeft = worldWidth - GameConfig.DOOR_WIDTH - 30f
    private val playerStartX = 40f

    var phase = GamePhase.INTRO
        private set

    private val talmid = Talmid()
    private val menahel = Menahel()
    private val assistants = mutableListOf<Menahel>()
    private val mashgichim = mutableListOf<Mashgiach>()
    private val platforms = mutableListOf<Platform>()
    private val felafelBalls = mutableListOf<FelafelBall>()
    private val chalks = mutableListOf<Chalk>()
    private var candyMan: CandyMan? = null
    private var candyManSpawnedThisLevel = false
    private val rugelach = mutableListOf<Rugelach>()
    private val powerUps = mutableListOf<PowerUp>()
    private val floatingTexts = mutableListOf<FloatingText>()

    private var levelIndex = 0
    private var level: LevelDefinition = Levels.all.first()
    private var score = 0
    private var lives = GameConfig.STARTING_LIVES
    private var levelFrames = 0
    private var phaseFrames = 0
    /** Frames until the van leaves without you; -1 when the level has no timer. */
    private var vanFrames = -1
    private var chalkCooldown = 300
    private var runLivesLost = 0
    // Chazaka: three quick rugelach start a x2 chain
    private var comboCount = 0
    private var comboFrames = 0
    // PA announcement state
    private var paCooldown = 1600
    private var paText: String? = null
    private var paFrames = 0
    private var highScore = prefs.getHighScore()
    private var newBest = false
    private var gameOverLine = Levels.gameOverLines.first()

    private var leftPressed = false
    private var rightPressed = false
    private var runPressed = false

    private var difficulty = loadDifficulty()
    private var difficultySelection = 0
    private var endlessMode = false
    private var modeSelection = 0
    private var endlessSeed = Random.nextLong()
    private val leaderboard = LeaderboardClient()

    private fun loadDifficulty(): Difficulty = try {
        Difficulty.valueOf(prefs.getDifficultyName(Difficulty.BAAL_HABOS.name))
    } catch (e: IllegalArgumentException) {
        Difficulty.BAAL_HABOS
    }

    init {
        // Populate level 1 so the intro screen has a real backdrop
        buildLevel(0)
        music.setMuted(prefs.isMusicMuted())
    }

    /** Flips the funky-klezmer soundtrack on/off and persists the choice. */
    fun toggleMute() {
        val newMuted = !music.isMuted()
        music.setMuted(newMuted)
        prefs.setMusicMuted(newMuted)
    }

    fun isMusicMuted(): Boolean = music.isMuted()

    /**
     * True when the handheld-shell presentation should be used: set by the
     * view when the device has no physical keypad/d-pad or the player touches
     * the screen; cleared again the moment a physical key is used. Touch mode
     * plays in the landscape world inside the shell's LCD; keypad mode plays
     * the portrait world fullscreen.
     */
    var touchControlsEnabled = false
        set(value) {
            if (field == value) return
            field = value
            applyWorldSize()
        }

    private var background = BackgroundRenderer(worldWidth, worldHeight)
    private var hud = HudRenderer(worldWidth)
    private var overlay = OverlayRenderer(worldWidth, worldHeight)
    private val gamepad = TouchGamepadRenderer()
    private val shell = GameBoyShellRenderer()
    private val floatingTextPaint = Paint().apply {
        textSize = 30f
        textAlign = Paint.Align.CENTER
        typeface = Typeface.DEFAULT_BOLD
        style = Paint.Style.FILL
    }
    // DARK modifier: a huge stroked ring leaves a lit circle around the talmid
    private val darknessPaint = Paint().apply {
        color = Color.argb(233, 5, 5, 14)
        style = Paint.Style.STROKE
        strokeWidth = 4200f
    }
    private val paBannerPaint = Paint().apply {
        color = Color.argb(190, 20, 20, 30)
        style = Paint.Style.FILL
    }
    private val paTextPaint = Paint().apply {
        color = Color.rgb(255, 240, 180)
        textSize = 26f
        textAlign = Paint.Align.CENTER
        style = Paint.Style.FILL
    }

    // ---------------------------------------------------------------- input

    fun onLeft(pressed: Boolean) {
        leftPressed = pressed
        if (!pressed) return
        when (phase) {
            GamePhase.DIFFICULTY_SELECT -> moveDifficultySelection(-1)
            GamePhase.MODE_SELECT -> moveModeSelection()
            else -> Unit
        }
    }

    fun onRight(pressed: Boolean) {
        rightPressed = pressed
        if (!pressed) return
        when (phase) {
            GamePhase.DIFFICULTY_SELECT -> moveDifficultySelection(1)
            GamePhase.MODE_SELECT -> moveModeSelection()
            else -> Unit
        }
    }

    fun onJump() {
        when (phase) {
            GamePhase.PLAYING -> if (talmid.jump()) music.playSfx(Sfx.JUMP)
            GamePhase.DIFFICULTY_SELECT -> moveDifficultySelection(-1)
            GamePhase.MODE_SELECT -> moveModeSelection()
            else -> Unit
        }
    }

    /** DOWN / 8: menu navigation, or the leaderboard from the title screen. */
    fun onMenuDown() {
        when (phase) {
            GamePhase.DIFFICULTY_SELECT -> moveDifficultySelection(1)
            GamePhase.MODE_SELECT -> moveModeSelection()
            GamePhase.INTRO -> onLeaderboardKey()
            else -> Unit
        }
    }

    private fun moveModeSelection() {
        modeSelection = 1 - modeSelection
        music.playSfx(Sfx.PICKUP)
    }

    private fun moveDifficultySelection(delta: Int) {
        val count = Difficulty.values().size
        difficultySelection = (difficultySelection + delta + count) % count
        music.playSfx(Sfx.PICKUP)
    }

    /** Key 7 / SELECT on the title screen: show the global leaderboard. */
    fun onLeaderboardKey() {
        if (phase == GamePhase.INTRO) {
            leaderboard.refresh()
            phase = GamePhase.LEADERBOARD
            phaseFrames = 0
        }
    }

    /**
     * The score-submission code shown at the end of a run: score plus a
     * checksum the leaderboard workflow verifies. Honor-system-grade.
     */
    private fun submitCode(): String =
        score.toString() + "-" + ((score.toLong() * 7919L + 5747L) % 99991L)

    /** Fire-and-forget auto-submission; the game never waits on it. */
    private fun submitRunScore() {
        if (score <= 0) return
        leaderboard.submitScore(
            prefs.getPlayerName(),
            score,
            if (endlessMode) "endless" else "story",
            difficulty.name
        )
    }

    /** Status line for the end screens; everything degrades to a manual code. */
    private fun leaderboardStatusLine(): String = when (leaderboard.submitState) {
        LeaderboardClient.SubmitState.SENDING -> "Sending score to the global board..."
        LeaderboardClient.SubmitState.OK -> "On the global board as ${prefs.getPlayerName()}!"
        LeaderboardClient.SubmitState.FAILED ->
            "Offline. Manual code: ${submitCode()} (see README)"
        else -> "Leaderboard code: ${submitCode()} (see README)"
    }

    /** A tap on a menu screen (world coordinates). */
    fun onMenuTap(x: Float, y: Float) {
        when (phase) {
            GamePhase.DIFFICULTY_SELECT -> {
                val row = overlay.difficultyRowAt(x, y)
                if (row >= 0) difficultySelection = row
                difficulty = Difficulty.values()[difficultySelection]
                prefs.setDifficultyName(difficulty.name)
                phase = GamePhase.MODE_SELECT
                phaseFrames = 0
            }
            GamePhase.MODE_SELECT -> {
                val row = overlay.modeRowAt(x, y)
                if (row >= 0) modeSelection = row
                confirmMode()
            }
            else -> Unit
        }
    }

    /** CENTER / 5 / OK pressed: the universal confirm-and-action button. */
    fun onActionDown() {
        when (phase) {
            GamePhase.INTRO -> {
                difficultySelection = difficulty.ordinal
                phase = GamePhase.DIFFICULTY_SELECT
                phaseFrames = 0
            }
            GamePhase.DIFFICULTY_SELECT -> {
                difficulty = Difficulty.values()[difficultySelection]
                prefs.setDifficultyName(difficulty.name)
                phase = GamePhase.MODE_SELECT
                phaseFrames = 0
            }
            GamePhase.MODE_SELECT -> confirmMode()
            GamePhase.LEADERBOARD -> resetToIntro()
            GamePhase.LEVEL_INTRO -> beginPlay()
            GamePhase.PLAYING -> {
                runPressed = true
                talmid.shootFelafel()?.let {
                    felafelBalls.add(it)
                    music.playSfx(Sfx.THROW)
                }
            }
            GamePhase.PAUSED -> resumePlay()
            // Half-second debounce so a thumb still on the controls when a
            // run ends can't skip these screens instantly
            GamePhase.GAME_OVER -> if (phaseFrames > 30) startNewGame()
            GamePhase.VICTORY -> if (phaseFrames > 30) resetToIntro()
        }
    }

    fun onActionUp() {
        runPressed = false
    }

    /**
     * Held state of the touch gamepad's A button: sustains running without
     * firing another felafel (the initial press fires via [onActionDown]).
     */
    fun onRunHeld(held: Boolean) {
        runPressed = held
    }

    fun onPauseToggle() {
        when (phase) {
            GamePhase.PLAYING -> {
                phase = GamePhase.PAUSED
                phaseFrames = 0
            }
            GamePhase.PAUSED -> resumePlay()
            else -> Unit
        }
    }

    // ------------------------------------------------------------ lifecycle

    private fun confirmMode() {
        endlessMode = modeSelection == 1
        startNewGame()
    }

    private fun startNewGame() {
        endlessSeed = Random.nextLong() // fresh bein hazmanim days every run
        leaderboard.resetSubmitState()
        score = 0
        lives = difficulty.lives
        newBest = false
        runLivesLost = 0
        talmid.clearEffects()
        loadLevel(0)
    }

    private fun resetToIntro() {
        phase = GamePhase.INTRO
        phaseFrames = 0
    }

    /**
     * Switches between the portrait and landscape worlds and re-flows the
     * current level into it (levels are fraction-based, so the layout
     * adapts). Mid-run the player gets the level card again as a beat.
     */
    private fun applyWorldSize() {
        if (touchControlsEnabled) {
            worldWidth = GameConfig.LANDSCAPE_WORLD_WIDTH
            worldHeight = GameConfig.LANDSCAPE_WORLD_HEIGHT
        } else {
            worldWidth = GameConfig.WORLD_WIDTH
            worldHeight = GameConfig.WORLD_HEIGHT
        }
        floorTop = worldHeight * GameConfig.FLOOR_TOP_FRACTION
        doorLeft = worldWidth - GameConfig.DOOR_WIDTH - 30f
        background = BackgroundRenderer(worldWidth, worldHeight)
        hud = HudRenderer(worldWidth)
        overlay = OverlayRenderer(worldWidth, worldHeight)
        buildLevel(levelIndex)
        if (phase == GamePhase.PLAYING || phase == GamePhase.PAUSED) {
            phase = GamePhase.LEVEL_INTRO
            phaseFrames = 0
        }
    }

    private fun loadLevel(index: Int) {
        buildLevel(index)
        phase = GamePhase.LEVEL_INTRO
    }

    private fun buildLevel(index: Int) {
        levelIndex = index
        level = if (endlessMode) Levels.endless(index + 1, endlessSeed) else Levels.all[index]

        platforms.clear()
        for (spec in level.platforms) {
            platforms.add(
                Platform(
                    spec.fx * worldWidth,
                    spec.fy * worldHeight,
                    spec.fw * worldWidth,
                    30f
                )
            )
        }

        rugelach.clear()
        for (spec in level.rugelach) {
            rugelach.add(Rugelach(spec.fx * worldWidth, spec.fy * worldHeight))
        }

        powerUps.clear()
        for (spec in level.powerUps) {
            powerUps.add(PowerUp(spec.fx * worldWidth, spec.fy * worldHeight, spec.type))
        }

        mashgichim.clear()
        for (spec in level.patrollers) {
            mashgichim.add(
                Mashgiach(
                    spec.fxStart * worldWidth,
                    spec.fxEnd * worldWidth,
                    spec.fy * worldHeight,
                    GameConfig.MASHGIACH_SPEED + difficulty.mashgiachSpeedBonus
                )
            )
        }

        felafelBalls.clear()
        floatingTexts.clear()
        talmid.resetForLevel(playerStartX, floorTop)
        menahel.resetForLevel(
            worldWidth * 0.72f, floorTop, level.menahelSpeed * difficulty.menahelSpeedFactor
        )

        assistants.clear()
        if (Modifier.ASSISTANT_MENAHEL in level.modifiers) {
            val assistant = Menahel("ASST. MENAHEL")
            // Slightly slower than the boss, but he starts closer
            assistant.resetForLevel(
                worldWidth * 0.45f, floorTop,
                level.menahelSpeed * 0.8f * difficulty.menahelSpeedFactor
            )
            assistants.add(assistant)
        }

        talmid.frictionFactor =
            if (Modifier.SLIPPERY in level.modifiers) 0.975f else GameConfig.PLAYER_FRICTION
        talmid.windEnabled = Modifier.WIND in level.modifiers
        vanFrames = level.timeLimitSeconds?.times(60) ?: -1
        candyMan = null
        candyManSpawnedThisLevel = false
        comboCount = 0
        comboFrames = 0
        chalks.clear()
        chalkCooldown = 300 + Random.nextInt(180)
        paText = null
        paCooldown = 1200 + Random.nextInt(1200)

        levelFrames = 0
        phaseFrames = 0
    }

    private fun beginPlay() {
        phase = GamePhase.PLAYING
        phaseFrames = 0
    }

    private fun resumePlay() {
        phase = GamePhase.PLAYING
        phaseFrames = 0
    }

    // --------------------------------------------------------------- update

    fun update() {
        phaseFrames++
        when (phase) {
            GamePhase.LEVEL_INTRO ->
                if (phaseFrames >= GameConfig.LEVEL_INTRO_FRAMES) beginPlay()
            GamePhase.PLAYING -> updatePlaying()
            else -> Unit
        }
    }

    private fun updatePlaying() {
        levelFrames++

        when {
            leftPressed -> talmid.moveLeft(runPressed)
            rightPressed -> talmid.moveRight(runPressed)
            else -> talmid.stopMoving()
        }

        talmid.update(worldWidth, floorTop, platforms)
        menahel.update(talmid.centerX, worldWidth, floorTop, platforms)
        for (a in assistants) a.update(talmid.centerX, worldWidth, floorTop, platforms)
        for (m in mashgichim) m.update()

        if (vanFrames > 0) {
            vanFrames--
            if (vanFrames == 0) {
                onVanLeft()
            }
        }

        updateChalk()
        updateCandyMan()
        if (comboFrames > 0) {
            comboFrames--
            if (comboFrames == 0) comboCount = 0
        }
        updateAnnouncements()
        updateFelafel()
        updatePickups()
        updateFloatingTexts()
        checkCatches()
        checkExit()
    }

    /** From level 10 the Menahel throws chalk with decades-honed accuracy. */
    private fun updateChalk() {
        if (level.number >= difficulty.chalkFromLevel && !menahel.isStunned) {
            chalkCooldown--
            if (chalkCooldown <= 0) {
                chalkCooldown = 240 + Random.nextInt(200)
                val direction = if (talmid.centerX > menahel.centerX) 1f else -1f
                chalks.add(Chalk(menahel.centerX, menahel.y, direction * (6f + Random.nextFloat() * 3f)))
            }
        }
        val iterator = chalks.iterator()
        while (iterator.hasNext()) {
            val chalk = iterator.next()
            chalk.update(worldWidth, floorTop)
            if (!chalk.active) {
                iterator.remove()
                continue
            }
            if (!talmid.isInvincible && talmid.mussarFrames == 0 &&
                chalk.hits(talmid.centerX, talmid.centerY, 42f)
            ) {
                talmid.receiveMussar()
                music.playSfx(Sfx.STUN)
                addFloatingText("GOT MUSSAR'D! Slowed!", talmid.centerX, talmid.y - 70f, Color.rgb(180, 60, 60))
                iterator.remove()
            }
        }
    }

    /** Rarely, once per level, the Candy Man shuffles through with a treat. */
    private fun updateCandyMan() {
        val current = candyMan
        if (current == null) {
            if (!candyManSpawnedThisLevel && Random.nextInt(3200) == 0) {
                candyManSpawnedThisLevel = true
                candyMan = CandyMan(Random.nextBoolean(), worldWidth, floorTop)
            }
            return
        }
        current.update()
        if (!current.active) {
            candyMan = null
            return
        }
        if (!current.collected &&
            withinDistance(current.centerX, current.centerY, 60f)
        ) {
            current.collect()
            music.playSfx(Sfx.POWERUP)
            if (lives < 5) {
                lives++
                addFloatingText(
                    "THE CANDY MAN! +1 hat!",
                    current.centerX, current.y - 60f, Color.rgb(200, 60, 160)
                )
            } else {
                addScore(300)
                addFloatingText(
                    "THE CANDY MAN! +300!",
                    current.centerX, current.y - 60f, Color.rgb(200, 60, 160)
                )
            }
        }
    }

    private fun updateAnnouncements() {
        if (paFrames > 0) {
            paFrames--
            if (paFrames == 0) {
                paText = null
                paCooldown = 1500 + Random.nextInt(1200)
            }
        } else {
            paCooldown--
            if (paCooldown <= 0) {
                paText = Levels.paAnnouncements[Random.nextInt(Levels.paAnnouncements.size)]
                paFrames = 300
            }
        }
    }

    private fun updateFelafel() {
        val iterator = felafelBalls.iterator()
        while (iterator.hasNext()) {
            val ball = iterator.next()
            ball.update(worldWidth)
            if (!ball.active) {
                iterator.remove()
                continue
            }
            if (ball.hits(menahel.centerX, menahel.centerY, GameConfig.MENAHEL_CATCH_DISTANCE)) {
                menahel.stun()
                recordStun()
                addFloatingText("BULLSEYE!", menahel.centerX, menahel.y - 40f, Color.rgb(255, 160, 40))
                iterator.remove()
                continue
            }
            var hitAssistant = false
            for (a in assistants) {
                if (!a.isStunned &&
                    ball.hits(a.centerX, a.centerY, GameConfig.MENAHEL_CATCH_DISTANCE)
                ) {
                    a.stun()
                    recordStun()
                    addFloatingText("Also him!", a.centerX, a.y - 40f, Color.rgb(255, 160, 40))
                    hitAssistant = true
                    break
                }
            }
            if (hitAssistant) {
                iterator.remove()
                continue
            }
            var hitPatroller = false
            for (m in mashgichim) {
                if (!m.isStunned &&
                    ball.hits(m.centerX, m.centerY, GameConfig.MASHGIACH_CATCH_DISTANCE)
                ) {
                    m.stun()
                    recordStun()
                    addFloatingText("Lost his place!", m.centerX, m.y - 40f, Color.rgb(255, 160, 40))
                    hitPatroller = true
                    break
                }
            }
            if (hitPatroller) iterator.remove()
        }
    }

    private fun updatePickups() {
        for (r in rugelach) {
            r.update()
            if (r.tryCollect(talmid.centerX, talmid.centerY, 60f)) {
                music.playSfx(Sfx.PICKUP)
                comboCount++
                comboFrames = 240
                val chazaka = comboCount >= 3
                val points =
                    if (chazaka) GameConfig.SCORE_RUGELACH * 2 else GameConfig.SCORE_RUGELACH
                addScore(points)
                if (comboCount == 3) {
                    addFloatingText(
                        "CHAZAKA! Rugelach x2!",
                        r.baseX, r.baseY - 55f, Color.rgb(255, 170, 40), 140
                    )
                }
                addFloatingText(
                    "+$points",
                    r.baseX, r.baseY - 30f, Color.rgb(60, 140, 40)
                )
            }
        }
        for (p in powerUps) {
            p.update()
            if (p.tryCollect(talmid.centerX, talmid.centerY, 65f)) {
                music.playSfx(Sfx.POWERUP)
                addScore(GameConfig.SCORE_POWER_UP)
                when (p.type) {
                    PowerUpType.COFFEE -> talmid.applyCoffee()
                    PowerUpType.SELTZER -> talmid.applySeltzer()
                    PowerUpType.KUGEL -> talmid.applyKugelShield()
                }
                addFloatingText(p.type.label, talmid.centerX, talmid.y - 60f, Color.rgb(40, 90, 200))
            }
        }
    }

    private fun checkCatches() {
        if (talmid.isInvincible) return

        if (!menahel.isStunned &&
            withinDistance(menahel.centerX, menahel.centerY, GameConfig.MENAHEL_CATCH_DISTANCE)
        ) {
            onCaught()
            return
        }
        for (a in assistants) {
            if (!a.isStunned &&
                withinDistance(a.centerX, a.centerY, GameConfig.MENAHEL_CATCH_DISTANCE)
            ) {
                onCaught()
                return
            }
        }
        for (m in mashgichim) {
            if (!m.isStunned &&
                withinDistance(m.centerX, m.centerY, GameConfig.MASHGIACH_CATCH_DISTANCE)
            ) {
                onCaught()
                return
            }
        }
    }

    private fun withinDistance(cx: Float, cy: Float, distance: Float): Boolean {
        val dx = talmid.centerX - cx
        val dy = talmid.centerY - cy
        return dx * dx + dy * dy < distance * distance
    }

    private fun onCaught() {
        if (talmid.consumeShield()) {
            menahel.stun()
            music.playSfx(Sfx.STUN)
            addFloatingText(
                "The kugel took the hit!",
                talmid.centerX, talmid.y - 70f, Color.rgb(200, 150, 0)
            )
            return
        }
        loseLife("CAUGHT! Minus one hat.")
    }

    private fun onVanLeft() {
        loseLife("THE VAN LEFT! ...It's circling the block. GO!")
        vanFrames = level.timeLimitSeconds?.times(60) ?: -1
    }

    private fun loseLife(message: String) {
        music.playSfx(Sfx.CAUGHT)
        lives--
        runLivesLost++
        if (lives <= 0) {
            gameOverLine = if (endlessMode) {
                "You lasted ${level.number} day" +
                    (if (level.number == 1) "" else "s") + " of bein hazmanim."
            } else {
                Levels.gameOverLines[Random.nextInt(Levels.gameOverLines.size)]
            }
            newBest = prefs.submitScore(score)
            if (newBest) highScore = score
            phase = GamePhase.GAME_OVER
            phaseFrames = 0
            submitRunScore()
        } else {
            addFloatingText(
                message,
                talmid.centerX, talmid.y - 70f, Color.rgb(220, 40, 40)
            )
            talmid.respawn(playerStartX, floorTop)
        }
    }

    private fun checkExit() {
        val doorRight = doorLeft + GameConfig.DOOR_WIDTH
        val atDoor = talmid.x + talmid.width > doorLeft + 20f &&
            talmid.x < doorRight &&
            talmid.y + talmid.height > floorTop - GameConfig.DOOR_HEIGHT
        if (!atDoor) return

        val seconds = levelFrames / 60
        val timeBonus = (GameConfig.TIME_BONUS_MAX - seconds * GameConfig.TIME_BONUS_DECAY_PER_SECOND)
            .coerceAtLeast(0)
        addScore(GameConfig.SCORE_LEVEL_CLEAR + timeBonus)
        music.playSfx(Sfx.LEVEL_CLEAR)

        if (seconds < 15) award(Achievement.ZRIZUS)
        if (rugelach.isNotEmpty() && rugelach.all { it.collected }) {
            award(Achievement.KIBUD_RUGELACH)
        }

        if (endlessMode) {
            prefs.submitCounterMax("endless_best", level.number)
            loadLevel(levelIndex + 1)
            return
        }

        if (levelIndex + 1 >= Levels.all.size) {
            award(Achievement.SEMICHA)
            if (runLivesLost == 0) award(Achievement.SHOMER_NAFSHO)
            prefs.incrementCounter("escapes")
            newBest = prefs.submitScore(score)
            if (newBest) highScore = score
            phase = GamePhase.VICTORY
            phaseFrames = 0
            submitRunScore()
        } else {
            loadLevel(levelIndex + 1)
        }
    }

    /** All points flow through the difficulty's score multiplier. */
    private fun addScore(points: Int) {
        score += (points * difficulty.scoreFactor).toInt()
    }

    private fun recordStun() {
        music.playSfx(Sfx.STUN)
        if (prefs.incrementCounter("stuns") >= 25) {
            award(Achievement.FELAFEL_SNIPER)
        }
    }

    /** Grants a semicha (achievement) once, with a golden banner. */
    private fun award(achievement: Achievement) {
        if (prefs.isAchieved(achievement.name)) return
        prefs.setAchieved(achievement.name)
        addFloatingText(
            achievement.title,
            worldWidth / 2, worldHeight * 0.35f, Color.rgb(255, 200, 40), 260
        )
    }

    // -------------------------------------------------------- floating text

    private fun addFloatingText(text: String, x: Float, y: Float, color: Int, frames: Int = 90) {
        floatingTexts.add(FloatingText(text, x, y, color, frames))
    }

    private fun updateFloatingTexts() {
        val iterator = floatingTexts.iterator()
        while (iterator.hasNext()) {
            val t = iterator.next()
            t.y -= 1.2f
            t.framesLeft--
            if (t.framesLeft <= 0) iterator.remove()
        }
    }

    // ----------------------------------------------------------------- draw

    /** Scales the virtual world onto the actual surface and draws everything. */
    fun draw(canvas: Canvas, viewWidth: Int, viewHeight: Int) {
        canvas.save()
        // Outer transform maps the shell/portrait space to the surface; in
        // keypad mode the world IS that space, in touch mode the landscape
        // world is placed inside the shell's LCD below.
        canvas.scale(
            viewWidth / GameConfig.WORLD_WIDTH,
            viewHeight / GameConfig.WORLD_HEIGHT
        )

        if (touchControlsEnabled) {
            // Full handheld-console look: shell around the LCD, game inside
            // it, physical-style controls on the body below
            shell.draw(canvas)
            canvas.save()
            canvas.translate(TouchGamepadLayout.screenOffsetX, TouchGamepadLayout.screenOffsetY)
            canvas.scale(TouchGamepadLayout.screenScale, TouchGamepadLayout.screenScale)
            canvas.clipRect(0f, 0f, worldWidth, worldHeight)
            drawWorldAndUi(canvas)
            canvas.restore()
            gamepad.draw(canvas, music.isMuted())
        } else {
            drawWorldAndUi(canvas)
        }

        canvas.restore()
    }

    /** Draws the game world plus HUD and overlays in virtual world coordinates. */
    private fun drawWorldAndUi(canvas: Canvas) {
        background.draw(canvas, level.theme)
        background.drawPlatforms(canvas, platforms)
        background.drawExitDoor(canvas, doorLeft)

        if (phase != GamePhase.INTRO) {
            for (r in rugelach) r.draw(canvas)
            for (p in powerUps) p.draw(canvas)
            for (m in mashgichim) m.draw(canvas)
            menahel.draw(canvas, worldWidth)
            for (a in assistants) a.draw(canvas, worldWidth)
            talmid.draw(canvas)
            for (ball in felafelBalls) ball.draw(canvas)
            for (chalk in chalks) chalk.draw(canvas)
            candyMan?.draw(canvas)
            for (t in floatingTexts) {
                floatingTextPaint.color = t.color
                canvas.drawText(t.text, t.x, t.y, floatingTextPaint)
            }
            if (Modifier.DARK in level.modifiers &&
                (phase == GamePhase.PLAYING || phase == GamePhase.PAUSED)
            ) {
                // Everything outside the talmid's little circle of light
                canvas.drawCircle(
                    talmid.centerX, talmid.centerY,
                    300f + darknessPaint.strokeWidth / 2, darknessPaint
                )
            }
            val vanSeconds = if (vanFrames >= 0) (vanFrames + 59) / 60 else null
            hud.draw(canvas, score, highScore, lives, level, talmid, vanSeconds)
            paText?.let { announcement ->
                canvas.drawRect(worldWidth * 0.04f, 128f, worldWidth * 0.96f, 172f, paBannerPaint)
                canvas.drawText(announcement, worldWidth / 2, 158f, paTextPaint)
            }
            if (!touchControlsEnabled) {
                hud.drawControlsHint(canvas, worldHeight)
            }
        }

        overlay.touchMode = touchControlsEnabled
        when (phase) {
            GamePhase.INTRO -> overlay.drawIntro(
                canvas, highScore,
                Achievement.values().count { prefs.isAchieved(it.name) },
                prefs.getCounter("escapes")
            )
            GamePhase.DIFFICULTY_SELECT -> overlay.drawDifficultySelect(canvas, difficultySelection)
            GamePhase.MODE_SELECT ->
                overlay.drawModeSelect(canvas, modeSelection, prefs.getCounter("endless_best"))
            GamePhase.LEADERBOARD ->
                overlay.drawLeaderboard(canvas, leaderboard.latest(), leaderboard.hasFailed())
            GamePhase.LEVEL_INTRO -> overlay.drawLevelIntro(canvas, level, difficulty.label)
            GamePhase.PAUSED -> overlay.drawPaused(canvas)
            GamePhase.GAME_OVER -> overlay.drawGameOver(
                canvas, gameOverLine, score, highScore, newBest, leaderboardStatusLine()
            )
            GamePhase.VICTORY ->
                overlay.drawVictory(canvas, score, highScore, newBest, leaderboardStatusLine())
            GamePhase.PLAYING -> Unit
        }
    }

    private class FloatingText(
        val text: String,
        val x: Float,
        var y: Float,
        val color: Int,
        var framesLeft: Int = 90
    )
}
