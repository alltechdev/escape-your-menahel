package com.escapegame

import android.content.Context
import android.graphics.*
import android.view.KeyEvent
import android.view.MotionEvent
import android.view.SurfaceHolder
import android.view.SurfaceView

class GameView(context: Context) : SurfaceView(context), SurfaceHolder.Callback {
    private var gameThread: GameThread? = null
    private val talmid = Talmid()
    private val menahel = Rabbi()
    private var screenWidth = 0
    private var screenHeight = 0
    private var currentLevel = 1
    private var leftPressed = false
    private var rightPressed = false
    private var runPressed = false
    private val platforms = mutableListOf<Platform>()
    private val felafelBalls = mutableListOf<FelafelBall>()
    private var gameOver = false
    private var gameOverMessage = ""
    private var showIntro = true
    // Guards platforms/felafelBalls and game state shared between the UI thread
    // (key events, surface callbacks) and the game thread (update/draw)
    private val stateLock = Any()

    init {
        holder.addCallback(this)
        isFocusable = true
    }

    override fun surfaceCreated(holder: SurfaceHolder) {
        startGameThread()
    }

    override fun surfaceChanged(holder: SurfaceHolder, format: Int, width: Int, height: Int) {
        synchronized(stateLock) {
            screenWidth = width
            screenHeight = height
            talmid.init(width, height)
            menahel.init(width, height)
            setupLevel(currentLevel)
        }
    }

    override fun surfaceDestroyed(holder: SurfaceHolder) {
        stopGameThread()
    }

    override fun onKeyDown(keyCode: Int, event: KeyEvent): Boolean {
        when (keyCode) {
            KeyEvent.KEYCODE_DPAD_UP, KeyEvent.KEYCODE_W -> {
                if (!showIntro) talmid.jump()
            }
            KeyEvent.KEYCODE_DPAD_LEFT, KeyEvent.KEYCODE_A -> leftPressed = true
            KeyEvent.KEYCODE_DPAD_RIGHT, KeyEvent.KEYCODE_D -> rightPressed = true
            KeyEvent.KEYCODE_DPAD_CENTER, KeyEvent.KEYCODE_SPACE -> {
                if (showIntro) {
                    showIntro = false // Dismiss intro screen
                } else {
                    runPressed = true
                    // Shoot felafel ball on center press
                    val felafelBall = talmid.shootFelafelBall()
                    felafelBall?.let {
                        synchronized(stateLock) { felafelBalls.add(it) }
                    }
                }
            }
            else -> return super.onKeyDown(keyCode, event)
        }
        return true
    }

    override fun onKeyUp(keyCode: Int, event: KeyEvent): Boolean {
        when (keyCode) {
            KeyEvent.KEYCODE_DPAD_LEFT, KeyEvent.KEYCODE_A -> leftPressed = false
            KeyEvent.KEYCODE_DPAD_RIGHT, KeyEvent.KEYCODE_D -> rightPressed = false
            KeyEvent.KEYCODE_DPAD_CENTER, KeyEvent.KEYCODE_SPACE -> {
                runPressed = false
                if (gameOver) {
                    synchronized(stateLock) { restartGame() }
                }
            }
            else -> return super.onKeyUp(keyCode, event)
        }
        return true
    }

    override fun onTouchEvent(event: MotionEvent): Boolean {
        // Touch events disabled for d-pad control
        return true
    }

    fun resume() {
        startGameThread()
    }

    fun pause() {
        stopGameThread()
    }

    // A stopped Thread cannot be restarted, so each start creates a fresh one
    private fun startGameThread() {
        if (gameThread?.isAlive == true) return
        if (holder.surface?.isValid != true) return
        gameThread = GameThread(holder).also {
            it.running = true
            it.start()
        }
    }

    private fun stopGameThread() {
        val thread = gameThread ?: return
        thread.running = false
        while (true) {
            try {
                thread.join()
                break
            } catch (e: InterruptedException) {
                // Will try again
            }
        }
        gameThread = null
    }

    inner class GameThread(private val surfaceHolder: SurfaceHolder) : Thread() {
        @Volatile
        var running = false

        private val wallPaint = Paint().apply {
            color = Color.rgb(240, 240, 220) // Off-white walls
            style = Paint.Style.FILL
        }
        private val floorPaint = Paint().apply {
            color = Color.rgb(139, 69, 19) // Brown floor
            style = Paint.Style.FILL
        }
        private val tilePaint = Paint().apply {
            color = Color.rgb(101, 50, 15) // Darker brown
            strokeWidth = 2f
            style = Paint.Style.STROKE
        }
        private val tablePaint = Paint().apply {
            color = Color.rgb(139, 69, 19) // Brown table
            style = Paint.Style.FILL
        }
        private val edgePaint = Paint().apply {
            color = Color.rgb(101, 50, 15) // Darker brown edge
            strokeWidth = 3f
            style = Paint.Style.STROKE
        }
        private val legPaint = Paint().apply {
            color = Color.GRAY
            style = Paint.Style.FILL
        }
        private val trayPaint = Paint().apply {
            color = Color.rgb(255, 165, 0) // Orange tray
            style = Paint.Style.FILL
        }
        private val foodPaint = Paint().apply {
            color = Color.rgb(255, 100, 100) // Reddish food
            style = Paint.Style.FILL
        }
        private val windowPaint = Paint().apply {
            color = Color.rgb(173, 216, 230) // Light blue window
            style = Paint.Style.FILL
        }
        private val framePaint = Paint().apply {
            color = Color.rgb(139, 69, 19) // Brown frame
            strokeWidth = 4f
            style = Paint.Style.STROKE
        }
        private val crossPaint = Paint().apply {
            color = Color.rgb(139, 69, 19)
            strokeWidth = 2f
            style = Paint.Style.STROKE
        }
        private val uiPaint = Paint().apply {
            color = Color.BLACK
            textSize = 24f
            style = Paint.Style.FILL
        }
        private val controlPaint = Paint().apply {
            color = Color.BLACK
            textSize = 16f
            style = Paint.Style.FILL
        }
        private val gameOverPaint = Paint().apply {
            color = Color.RED
            textSize = 48f
            textAlign = Paint.Align.CENTER
        }
        private val restartPaint = Paint().apply {
            color = Color.WHITE
            textSize = 24f
            textAlign = Paint.Align.CENTER
        }
        private val distancePaint = Paint().apply {
            color = Color.BLACK
            textSize = 20f
            style = Paint.Style.FILL
        }
        private val overlayPaint = Paint().apply {
            color = Color.argb(200, 0, 0, 0) // Semi-transparent black
            style = Paint.Style.FILL
        }
        private val titlePaint = Paint().apply {
            color = Color.YELLOW
            textAlign = Paint.Align.CENTER
            typeface = android.graphics.Typeface.DEFAULT_BOLD
        }
        private val controlsPaint = Paint().apply {
            color = Color.WHITE
            textAlign = Paint.Align.CENTER
        }
        private val okPaint = Paint().apply {
            color = Color.GREEN
            textAlign = Paint.Align.CENTER
            typeface = android.graphics.Typeface.DEFAULT_BOLD
        }

        override fun run() {
            while (running) {
                val canvas = surfaceHolder.lockCanvas()
                canvas?.let {
                    synchronized(stateLock) {
                        update()
                        draw(it)
                    }
                    surfaceHolder.unlockCanvasAndPost(it)
                }
                try {
                    sleep(16) // ~60 FPS
                } catch (e: InterruptedException) {
                    break
                }
            }
        }

        private fun update() {
            // Handle continuous left/right movement only
            if (leftPressed) {
                talmid.moveLeft(runPressed)
            } else if (rightPressed) {
                talmid.moveRight(runPressed)
            } else {
                talmid.stopMoving()
            }

            if (!gameOver && !showIntro) {
                talmid.update(screenWidth, screenHeight, platforms)
                menahel.update(talmid.x, talmid.y, screenWidth, screenHeight, platforms)

                // Update felafel balls
                updateFelafelBalls()

                // Check collision with menahel
                checkMenahelCollision()

                // Check level progression
                checkLevelCompletion()
            }
        }

        private fun draw(canvas: Canvas) {
            // Draw lunchroom background
            canvas.drawRect(0f, 0f, screenWidth.toFloat(), screenHeight.toFloat(), wallPaint)

            // Draw lunchroom floor
            canvas.drawRect(0f, screenHeight - 100f, screenWidth.toFloat(), screenHeight.toFloat(), floorPaint)

            // Add floor tiles pattern
            val tileSize = 50f
            for (i in 0 until screenWidth.toInt() step tileSize.toInt()) {
                for (j in (screenHeight - 100).toInt() until screenHeight step tileSize.toInt()) {
                    canvas.drawRect(i.toFloat(), j.toFloat(), (i + tileSize), (j + tileSize), tilePaint)
                }
            }

            // Draw lunchroom elements
            drawLunchroomElements(canvas)

            // Draw lunch tables (platforms)
            drawTables(canvas)

            // Draw cafeteria windows
            drawWindows(canvas)

            if (showIntro) {
                drawIntroScreen(canvas)
            } else {
                talmid.draw(canvas)
                menahel.draw(canvas)

                // Draw felafel balls
                for (felafelBall in felafelBalls) {
                    felafelBall.draw(canvas)
                }
            }

            // Draw UI with black text for visibility
            canvas.drawText("ESCAPE YOUR MENAHEL!", 20f, 40f, uiPaint)
            canvas.drawText("Level: $currentLevel", 20f, 70f, uiPaint)
            val controlText = if (gameOver) "CENTER button to restart" else "Controls: LEFT/RIGHT=move, UP=jump, CENTER=run+shoot"
            canvas.drawText(controlText, 20f, screenHeight - 20f, controlPaint)

            if (gameOver) {
                // Draw game over screen
                canvas.drawText(gameOverMessage, screenWidth / 2f, screenHeight / 2f, gameOverPaint)
                canvas.drawText("Press CENTER to restart", screenWidth / 2f, screenHeight / 2f + 60f, restartPaint)
            } else {
                val distance = kotlin.math.sqrt(
                    ((talmid.x - menahel.x) * (talmid.x - menahel.x) +
                     (talmid.y - menahel.y) * (talmid.y - menahel.y)).toDouble()
                ).toInt()
                canvas.drawText("Distance: $distance", screenWidth - 200f, 40f, distancePaint)
            }
        }

        private fun drawTables(canvas: Canvas) {
            for (platform in platforms) {
                // Draw lunch table top
                canvas.drawRect(platform.x, platform.y, platform.x + platform.width, platform.y + platform.height, tablePaint)

                // Add table edge
                canvas.drawRect(platform.x, platform.y, platform.x + platform.width, platform.y + platform.height, edgePaint)

                // Draw table legs
                val legWidth = 8f
                canvas.drawRect(platform.x + 10, platform.y + platform.height, platform.x + 10 + legWidth, screenHeight - 100f, legPaint)
                canvas.drawRect(platform.x + platform.width - 18, platform.y + platform.height, platform.x + platform.width - 10, screenHeight - 100f, legPaint)
            }
        }

        private fun drawLunchroomElements(canvas: Canvas) {
            // Draw lunch trays on floor
            canvas.drawRect(80f, screenHeight - 130f, 120f, screenHeight - 110f, trayPaint)
            canvas.drawRect(200f, screenHeight - 125f, 240f, screenHeight - 105f, trayPaint)

            // Draw spilled food
            canvas.drawCircle(150f, screenHeight - 120f, 8f, foodPaint)
            canvas.drawCircle(300f, screenHeight - 115f, 6f, foodPaint)
        }

        private fun drawWindows(canvas: Canvas) {
            // Draw windows on back wall
            canvas.drawRect(50f, 50f, 150f, 150f, windowPaint)
            canvas.drawRect(50f, 50f, 150f, 150f, framePaint)

            canvas.drawRect(screenWidth - 150f, 40f, screenWidth - 50f, 140f, windowPaint)
            canvas.drawRect(screenWidth - 150f, 40f, screenWidth - 50f, 140f, framePaint)

            // Window cross patterns
            canvas.drawLine(100f, 50f, 100f, 150f, crossPaint)
            canvas.drawLine(50f, 100f, 150f, 100f, crossPaint)

            canvas.drawLine(screenWidth - 100f, 40f, screenWidth - 100f, 140f, crossPaint)
            canvas.drawLine(screenWidth - 150f, 90f, screenWidth - 50f, 90f, crossPaint)
        }

        private fun checkMenahelCollision() {
            val distance = kotlin.math.sqrt(
                ((talmid.x - menahel.x) * (talmid.x - menahel.x) +
                 (talmid.y - menahel.y) * (talmid.y - menahel.y)).toDouble()
            )
            if (distance < 50) { // Collision threshold for taller characters
                gameOver = true
                gameOverMessage = "THE MENAHEL CAUGHT YOU!"
            }
        }

        private fun updateFelafelBalls() {
            val iterator = felafelBalls.iterator()
            while (iterator.hasNext()) {
                val felafelBall = iterator.next()
                felafelBall.update()

                // Check bounds
                if (!felafelBall.checkBounds(screenWidth)) {
                    iterator.remove()
                    continue
                }

                // Check collision with menahel
                if (felafelBall.checkCollision(menahel.x, menahel.y, 45f)) {
                    // Stun menahel temporarily
                    menahel.stun()
                    iterator.remove()
                }
            }
        }

        private fun checkLevelCompletion() {
            // Simple level progression - reach right side
            if (talmid.x > screenWidth - 100) {
                nextLevel()
            }
        }

        private fun drawIntroScreen(canvas: Canvas) {
            // Draw semi-transparent overlay
            canvas.drawRect(0f, 0f, screenWidth.toFloat(), screenHeight.toFloat(), overlayPaint)

            // More conservative text sizes for better fit
            val titleSize = (screenHeight * 0.035f).coerceIn(20f, 36f)
            val controlSize = (screenHeight * 0.02f).coerceIn(12f, 18f)
            val okSize = (screenHeight * 0.03f).coerceIn(16f, 24f)
            val lineHeight = controlSize + 6f

            // Draw title
            titlePaint.textSize = titleSize
            canvas.drawText("ESCAPE YOUR MENAHEL!", screenWidth / 2f, screenHeight * 0.12f, titlePaint)

            // Draw controls - more compact
            controlsPaint.textSize = controlSize
            val yStart = screenHeight * 0.25f

            canvas.drawText("CONTROLS:", screenWidth / 2f, yStart, controlsPaint)
            canvas.drawText("LEFT/RIGHT: Move", screenWidth / 2f, yStart + lineHeight, controlsPaint)
            canvas.drawText("UP: Jump (double jump)", screenWidth / 2f, yStart + lineHeight * 2, controlsPaint)
            canvas.drawText("CENTER: Run + shoot felafel", screenWidth / 2f, yStart + lineHeight * 3, controlsPaint)

            canvas.drawText("GOAL: Avoid the menahel!", screenWidth / 2f, yStart + lineHeight * 5, controlsPaint)
            canvas.drawText("Stun him with felafel!", screenWidth / 2f, yStart + lineHeight * 6, controlsPaint)

            // Draw OK button prompt
            okPaint.textSize = okSize
            canvas.drawText("Press CENTER to start!", screenWidth / 2f, screenHeight * 0.88f, okPaint)
        }
    }

    private fun nextLevel() {
        currentLevel++
        talmid.init(screenWidth, screenHeight)
        setupLevel(currentLevel)
    }

    private fun restartGame() {
        gameOver = false
        gameOverMessage = ""
        showIntro = false // Don't show intro again after restart
        currentLevel = 1
        felafelBalls.clear()
        talmid.init(screenWidth, screenHeight)
        menahel.init(screenWidth, screenHeight)
        setupLevel(currentLevel)
    }

    private fun setupLevel(level: Int) {
        platforms.clear()
        when (level) {
            1 -> {
                // Level 1: Jump-friendly platforms with good spacing
                platforms.add(Platform(150f, screenHeight - 180f, 120f, 25f))
                platforms.add(Platform(350f, screenHeight - 250f, 120f, 25f))
                platforms.add(Platform(550f, screenHeight - 200f, 120f, 25f))
            }
            2 -> {
                // Level 2: More challenging platforming
                platforms.add(Platform(80f, screenHeight - 160f, 100f, 25f))
                platforms.add(Platform(220f, screenHeight - 220f, 100f, 25f))
                platforms.add(Platform(380f, screenHeight - 180f, 100f, 25f))
                platforms.add(Platform(520f, screenHeight - 280f, 100f, 25f))
                platforms.add(Platform(650f, screenHeight - 160f, 100f, 25f))
            }
            else -> {
                // Progressive difficulty with good jump spacing
                val baseHeight = screenHeight - 160f
                val spacing = 140f
                for (i in 0 until 5) {
                    val x = 50f + i * spacing
                    val heightVariation = (kotlin.math.sin(i * 0.8) * 60f).toFloat()
                    platforms.add(Platform(x, baseHeight + heightVariation, 100f, 25f))
                }
            }
        }
    }

    data class Platform(val x: Float, val y: Float, val width: Float, val height: Float)
}
