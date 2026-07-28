package com.escapegame.entities

import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import com.escapegame.core.GameConfig
import com.escapegame.model.Platform

/**
 * The player: a young talmid making a break for it.
 *
 * Owns its own physics, active power-up effects, and rendering. All positions
 * are in virtual world units.
 */
class Talmid {
    var x = 100f
        private set
    var y = 100f
        private set

    val width = GameConfig.PLAYER_WIDTH
    val height = GameConfig.PLAYER_HEIGHT
    val centerX: Float get() = x + width / 2
    val centerY: Float get() = y + height / 2

    private var velocityX = 0f
    private var velocityY = 0f
    private var running = false
    private var facingRight = true
    private var lastShootTime = 0L
    private var jumpCount = 0
    private var animFrame = 0

    // Active effects (frames remaining; shield is a charge count)
    var coffeeFrames = 0
        private set
    var seltzerFrames = 0
        private set
    var shieldCharges = 0
        private set
    var invincibleFrames = 0
        private set

    val isInvincible: Boolean get() = invincibleFrames > 0

    private val suitPaint = Paint().apply { color = Color.rgb(40, 40, 40); style = Paint.Style.FILL }
    private val shirtPaint = Paint().apply { color = Color.WHITE; style = Paint.Style.FILL }
    private val tiePaint = Paint().apply { color = Color.rgb(139, 0, 0); style = Paint.Style.FILL }
    private val headPaint = Paint().apply { color = Color.rgb(255, 220, 177); style = Paint.Style.FILL }
    private val hatPaint = Paint().apply { color = Color.BLACK; style = Paint.Style.FILL }
    private val glassesPaint = Paint().apply {
        color = Color.BLACK
        strokeWidth = 2f
        style = Paint.Style.STROKE
    }
    private val eyePaint = Paint().apply { color = Color.WHITE; style = Paint.Style.FILL }
    private val pupilPaint = Paint().apply { color = Color.BLACK; style = Paint.Style.FILL }
    private val shoePaint = Paint().apply { color = Color.BLACK; style = Paint.Style.FILL }
    private val shieldPaint = Paint().apply {
        color = Color.rgb(255, 200, 40)
        strokeWidth = 5f
        style = Paint.Style.STROKE
    }
    private val bubblePaint = Paint().apply {
        color = Color.argb(160, 200, 255, 200)
        style = Paint.Style.FILL
    }
    private val tzitzisPaint = Paint().apply {
        color = Color.WHITE
        strokeWidth = 3f
        style = Paint.Style.STROKE
    }

    /** Full reset at level start: position, physics, but effects persist across levels. */
    fun resetForLevel(startX: Float, floorTopY: Float) {
        x = startX
        y = floorTopY - height
        velocityX = 0f
        velocityY = 0f
        jumpCount = 0
        running = false
        facingRight = true
    }

    /** Reset after being caught: back to start with mercy invincibility. */
    fun respawn(startX: Float, floorTopY: Float) {
        resetForLevel(startX, floorTopY)
        invincibleFrames = GameConfig.RESPAWN_INVINCIBILITY
    }

    fun clearEffects() {
        coffeeFrames = 0
        seltzerFrames = 0
        shieldCharges = 0
        invincibleFrames = 0
    }

    fun jump() {
        val maxJumps =
            if (seltzerFrames > 0) GameConfig.PLAYER_SELTZER_JUMPS else GameConfig.PLAYER_BASE_JUMPS
        if (jumpCount < maxJumps) {
            velocityY = GameConfig.PLAYER_JUMP_POWER
            jumpCount++
        }
    }

    fun moveLeft(isRunning: Boolean) {
        running = isRunning
        facingRight = false
        velocityX = kotlin.math.max(velocityX - 1.5f, -currentSpeed(isRunning))
    }

    fun moveRight(isRunning: Boolean) {
        running = isRunning
        facingRight = true
        velocityX = kotlin.math.min(velocityX + 1.5f, currentSpeed(isRunning))
    }

    fun stopMoving() {
        running = false
    }

    private fun currentSpeed(isRunning: Boolean): Float {
        val base = if (isRunning) GameConfig.PLAYER_RUN_SPEED else GameConfig.PLAYER_WALK_SPEED
        return if (coffeeFrames > 0) base * GameConfig.COFFEE_SPEED_MULTIPLIER else base
    }

    /** Returns a felafel ball if the cooldown allows, else null. */
    fun shootFelafel(): FelafelBall? {
        val now = System.currentTimeMillis()
        if (now - lastShootTime > GameConfig.FELAFEL_COOLDOWN_MILLIS) {
            lastShootTime = now
            val direction = if (facingRight) 1f else -1f
            return FelafelBall(centerX, centerY, direction)
        }
        return null
    }

    fun applyCoffee() {
        coffeeFrames = GameConfig.COFFEE_DURATION
    }

    fun applySeltzer() {
        seltzerFrames = GameConfig.SELTZER_DURATION
    }

    fun applyKugelShield() {
        shieldCharges = 1
    }

    /** Consumes a shield charge if one is available. Returns true if consumed. */
    fun consumeShield(): Boolean {
        if (shieldCharges > 0) {
            shieldCharges--
            invincibleFrames = GameConfig.SHIELD_HIT_INVINCIBILITY
            return true
        }
        return false
    }

    fun update(worldWidth: Float, floorTopY: Float, platforms: List<Platform>) {
        animFrame++
        if (coffeeFrames > 0) coffeeFrames--
        if (seltzerFrames > 0) seltzerFrames--
        if (invincibleFrames > 0) invincibleFrames--

        velocityY += GameConfig.GRAVITY
        if (!running) {
            velocityX *= GameConfig.PLAYER_FRICTION
        }

        x += velocityX
        if (x < 0f) {
            x = 0f
            velocityX = 0f
        }
        if (x + width > worldWidth) {
            x = worldWidth - width
            velocityX = 0f
        }

        y += velocityY

        for (platform in platforms) {
            if (x + width > platform.x && x < platform.x + platform.width) {
                if (velocityY > 0 && y < platform.y && y + height > platform.y) {
                    y = platform.y - height
                    velocityY = 0f
                    jumpCount = 0
                }
            }
        }

        if (y + height > floorTopY) {
            y = floorTopY - height
            velocityY = 0f
            jumpCount = 0
        }

        if (y < 0f) {
            y = 0f
            velocityY = 0f
        }
    }

    fun draw(canvas: Canvas) {
        // Blink while invincible so the mercy window is obvious
        if (isInvincible && (invincibleFrames / 6) % 2 == 0) return

        val s = width

        // Seltzer bubbles rising around the talmid
        if (seltzerFrames > 0) {
            val bob = (animFrame % 30) * 2f
            canvas.drawCircle(x - 8f, y + height - bob, 6f, bubblePaint)
            canvas.drawCircle(x + s + 8f, y + height - bob * 1.4f + 10f, 5f, bubblePaint)
        }

        // Suit jacket
        canvas.drawRect(x + 7, y + 22, x + s - 7, y + height - 7, suitPaint)
        // Shirt collar and front
        canvas.drawRect(x + 14, y + 22, x + s - 14, y + 33, shirtPaint)
        canvas.drawRect(x + s / 2 - 9, y + 22, x + s / 2 + 9, y + 44, shirtPaint)
        // Tie
        canvas.drawRect(x + s / 2 - 4, y + 26, x + s / 2 + 4, y + 47, tiePaint)
        // Tzitzis strings peeking out
        canvas.drawLine(x + 9, y + height - 12, x + 4, y + height + 2, tzitzisPaint)
        canvas.drawLine(x + s - 9, y + height - 12, x + s - 4, y + height + 2, tzitzisPaint)
        // Head
        canvas.drawOval(x + 12, y - 26, x + s - 12, y + 18, headPaint)
        // Black hat: brim + crown
        canvas.drawOval(x + 5, y - 32, x + s - 5, y - 20, hatPaint)
        canvas.drawOval(x + 12, y - 41, x + s - 12, y - 23, hatPaint)
        // Glasses
        canvas.drawCircle(x + s / 2 - 9, y - 11, 6f, glassesPaint)
        canvas.drawCircle(x + s / 2 + 9, y - 11, 6f, glassesPaint)
        canvas.drawLine(x + s / 2 - 3, y - 11, x + s / 2 + 3, y - 11, glassesPaint)
        // Eyes + pupils looking in movement direction
        canvas.drawCircle(x + s / 2 - 9, y - 11, 4f, eyePaint)
        canvas.drawCircle(x + s / 2 + 9, y - 11, 4f, eyePaint)
        val pupilOffset = if (facingRight) 1.5f else -1.5f
        canvas.drawCircle(x + s / 2 - 9 + pupilOffset, y - 11, 2f, pupilPaint)
        canvas.drawCircle(x + s / 2 + 9 + pupilOffset, y - 11, 2f, pupilPaint)
        // Mouth
        canvas.drawLine(x + s / 2 - 6, y + 2, x + s / 2 + 6, y + 2, glassesPaint)
        // Shoes
        canvas.drawOval(x + 3, y + height - 11, x + 24, y + height + 4, shoePaint)
        canvas.drawOval(x + s - 24, y + height - 11, x + s - 3, y + height + 4, shoePaint)

        // Kugel shield: golden aura
        if (shieldCharges > 0) {
            canvas.drawOval(x - 12, y - 48, x + s + 12, y + height + 12, shieldPaint)
        }
    }
}
