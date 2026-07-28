package com.escapegame.engine

import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Typeface
import com.escapegame.core.GameConfig
import com.escapegame.entities.FelafelBall
import com.escapegame.entities.Mashgiach
import com.escapegame.entities.Menahel
import com.escapegame.entities.PowerUp
import com.escapegame.entities.Rugelach
import com.escapegame.entities.Talmid
import com.escapegame.levels.Levels
import com.escapegame.model.GamePhase
import com.escapegame.model.LevelDefinition
import com.escapegame.model.Platform
import com.escapegame.model.PowerUpType
import com.escapegame.persistence.HighScoreStore
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
class GameEngine(private val highScores: HighScoreStore) {

    private val worldWidth = GameConfig.WORLD_WIDTH
    private val worldHeight = GameConfig.WORLD_HEIGHT
    private val floorTop = worldHeight - GameConfig.FLOOR_HEIGHT
    private val playerStartX = 40f
    private val doorLeft = worldWidth - GameConfig.DOOR_WIDTH - 30f

    var phase = GamePhase.INTRO
        private set

    private val talmid = Talmid()
    private val menahel = Menahel()
    private val mashgichim = mutableListOf<Mashgiach>()
    private val platforms = mutableListOf<Platform>()
    private val felafelBalls = mutableListOf<FelafelBall>()
    private val rugelach = mutableListOf<Rugelach>()
    private val powerUps = mutableListOf<PowerUp>()
    private val floatingTexts = mutableListOf<FloatingText>()

    private var levelIndex = 0
    private var level: LevelDefinition = Levels.all.first()
    private var score = 0
    private var lives = GameConfig.STARTING_LIVES
    private var levelFrames = 0
    private var phaseFrames = 0
    private var highScore = highScores.getHighScore()
    private var newBest = false
    private var gameOverLine = Levels.gameOverLines.first()

    private var leftPressed = false
    private var rightPressed = false
    private var runPressed = false

    /**
     * True when the on-screen Game Boy-style gamepad should be shown: set by
     * the view when the device has no physical keypad/d-pad or the player
     * touches the screen; cleared again the moment a physical key is used.
     */
    var touchControlsEnabled = false

    private val background = BackgroundRenderer()
    private val hud = HudRenderer()
    private val overlay = OverlayRenderer()
    private val gamepad = TouchGamepadRenderer()
    private val shell = GameBoyShellRenderer()
    private val floatingTextPaint = Paint().apply {
        textSize = 30f
        textAlign = Paint.Align.CENTER
        typeface = Typeface.DEFAULT_BOLD
        style = Paint.Style.FILL
    }

    // ---------------------------------------------------------------- input

    fun onLeft(pressed: Boolean) {
        leftPressed = pressed
    }

    fun onRight(pressed: Boolean) {
        rightPressed = pressed
    }

    fun onJump() {
        if (phase == GamePhase.PLAYING) talmid.jump()
    }

    /** CENTER / 5 / OK pressed: the universal confirm-and-action button. */
    fun onActionDown() {
        when (phase) {
            GamePhase.INTRO -> startNewGame()
            GamePhase.LEVEL_INTRO -> beginPlay()
            GamePhase.PLAYING -> {
                runPressed = true
                talmid.shootFelafel()?.let { felafelBalls.add(it) }
            }
            GamePhase.PAUSED -> resumePlay()
            GamePhase.GAME_OVER -> startNewGame()
            GamePhase.VICTORY -> resetToIntro()
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

    private fun startNewGame() {
        score = 0
        lives = GameConfig.STARTING_LIVES
        newBest = false
        talmid.clearEffects()
        loadLevel(0)
    }

    private fun resetToIntro() {
        phase = GamePhase.INTRO
        phaseFrames = 0
    }

    private fun loadLevel(index: Int) {
        levelIndex = index
        level = Levels.all[index]

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
                    spec.fy * worldHeight
                )
            )
        }

        felafelBalls.clear()
        floatingTexts.clear()
        talmid.resetForLevel(playerStartX, floorTop)
        menahel.resetForLevel(worldWidth * 0.72f, floorTop, level.menahelSpeed)

        levelFrames = 0
        phaseFrames = 0
        phase = GamePhase.LEVEL_INTRO
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
        for (m in mashgichim) m.update()

        updateFelafel()
        updatePickups()
        updateFloatingTexts()
        checkCatches()
        checkExit()
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
                addFloatingText("BULLSEYE!", menahel.centerX, menahel.y - 40f, Color.rgb(255, 160, 40))
                iterator.remove()
                continue
            }
            var hitPatroller = false
            for (m in mashgichim) {
                if (!m.isStunned &&
                    ball.hits(m.centerX, m.centerY, GameConfig.MASHGIACH_CATCH_DISTANCE)
                ) {
                    m.stun()
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
                score += GameConfig.SCORE_RUGELACH
                addFloatingText(
                    "+${GameConfig.SCORE_RUGELACH}",
                    r.baseX, r.baseY - 30f, Color.rgb(60, 140, 40)
                )
            }
        }
        for (p in powerUps) {
            p.update()
            if (p.tryCollect(talmid.centerX, talmid.centerY, 65f)) {
                score += GameConfig.SCORE_POWER_UP
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
            addFloatingText(
                "The kugel took the hit!",
                talmid.centerX, talmid.y - 70f, Color.rgb(200, 150, 0)
            )
            return
        }

        lives--
        if (lives <= 0) {
            gameOverLine = Levels.gameOverLines[Random.nextInt(Levels.gameOverLines.size)]
            newBest = highScores.submitScore(score)
            if (newBest) highScore = score
            phase = GamePhase.GAME_OVER
            phaseFrames = 0
        } else {
            addFloatingText(
                "CAUGHT! Minus one hat.",
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
        score += GameConfig.SCORE_LEVEL_CLEAR + timeBonus

        if (levelIndex + 1 >= Levels.all.size) {
            newBest = highScores.submitScore(score)
            if (newBest) highScore = score
            phase = GamePhase.VICTORY
            phaseFrames = 0
        } else {
            loadLevel(levelIndex + 1)
        }
    }

    // -------------------------------------------------------- floating text

    private fun addFloatingText(text: String, x: Float, y: Float, color: Int) {
        floatingTexts.add(FloatingText(text, x, y, color))
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
        canvas.scale(viewWidth / worldWidth, viewHeight / worldHeight)

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
            gamepad.draw(canvas)
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
            talmid.draw(canvas)
            for (ball in felafelBalls) ball.draw(canvas)
            for (t in floatingTexts) {
                floatingTextPaint.color = t.color
                canvas.drawText(t.text, t.x, t.y, floatingTextPaint)
            }
            hud.draw(canvas, score, highScore, lives, level, talmid)
            if (!touchControlsEnabled) {
                hud.drawControlsHint(canvas, worldHeight)
            }
        }

        overlay.touchMode = touchControlsEnabled
        when (phase) {
            GamePhase.INTRO -> overlay.drawIntro(canvas, highScore)
            GamePhase.LEVEL_INTRO -> overlay.drawLevelIntro(canvas, level)
            GamePhase.PAUSED -> overlay.drawPaused(canvas)
            GamePhase.GAME_OVER -> overlay.drawGameOver(canvas, gameOverLine, score, highScore, newBest)
            GamePhase.VICTORY -> overlay.drawVictory(canvas, score, highScore, newBest)
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
