package com.escapegame

import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint

class Talmid {
    var x = 100f
    var y = 100f
    private var velocityX = 0f
    private var velocityY = 0f
    private val size = 40f
    private val height = 50f
    private val runSpeed = 6f
    private val maxRunSpeed = 12f
    private val jumpPower = -15f
    private val gravity = 0.5f
    private val friction = 0.85f
    private var screenWidth = 0
    private var screenHeight = 0
    private var onGround = false
    private var running = false
    private var facingRight = true
    private var lastShootTime = 0L
    private var jumpCount = 0
    private val maxJumps = 2
    
    private val paint = Paint().apply {
        color = Color.BLUE
        style = Paint.Style.FILL
    }

    fun init(width: Int, screenHeight: Int) {
        screenWidth = width
        this.screenHeight = screenHeight
        x = 50f
        y = screenHeight - 150f // Simple ground position
        velocityY = 0f
        velocityX = 0f
        onGround = true
        jumpCount = 0
    }

    fun jump() {
        if (jumpCount < maxJumps) {
            velocityY = jumpPower
            jumpCount++
            if (jumpCount == 1) {
                onGround = false
            }
        }
    }

    fun moveLeft(isRunning: Boolean = false) {
        running = isRunning
        facingRight = false
        val speed = if (isRunning) maxRunSpeed else runSpeed
        velocityX = kotlin.math.max(velocityX - 1f, -speed)
    }

    fun moveRight(isRunning: Boolean = false) {
        running = isRunning
        facingRight = true
        val speed = if (isRunning) maxRunSpeed else runSpeed
        velocityX = kotlin.math.min(velocityX + 1f, speed)
    }
    
    
    fun shootFelafelBall(): FelafelBall? {
        val currentTime = System.currentTimeMillis()
        if (currentTime - lastShootTime > 300) { // 300ms cooldown
            lastShootTime = currentTime
            val direction = if (facingRight) 1f else -1f
            return FelafelBall(x + size/2, y + height/2, direction)
        }
        return null
    }

    fun stopMoving() {
        running = false
    }

    fun update(width: Int, screenHeight: Int, platforms: List<GameView.Platform>) {
        // Apply gravity
        velocityY += gravity
        
        // Apply friction when not actively moving
        if (!running) {
            velocityX *= friction
        }
        
        // Update horizontal position
        x += velocityX
        
        // Side boundaries
        if (x < 0f) {
            x = 0f
            velocityX = 0f
        }
        if (x + size > width) {
            x = width - size
            velocityX = 0f
        }
        
        // Update vertical position
        y += velocityY
        
        // Platform collisions
        onGround = false
        for (platform in platforms) {
            if (x + size > platform.x && x < platform.x + platform.width) {
                // Landing on top of platform
                if (velocityY > 0 && y < platform.y && y + this.height > platform.y) {
                    y = platform.y - this.height
                    velocityY = 0f
                    onGround = true
                    jumpCount = 0 // Reset jump count when landing on platform
                }
            }
        }
        
        // Ground collision (simple floor at bottom)
        val groundY = screenHeight - 100f
        if (y + this.height > groundY) {
            y = groundY - this.height
            velocityY = 0f
            onGround = true
            jumpCount = 0 // Reset jump count when touching ground
        }
        
        // Top boundary
        if (y < 0f) {
            y = 0f
            velocityY = 0f
        }
    }
    

    fun draw(canvas: Canvas) {
        // Draw suit jacket (same as menahel but smaller/younger)
        val suitPaint = Paint().apply {
            color = Color.rgb(40, 40, 40) // Dark gray suit
            style = Paint.Style.FILL
        }
        canvas.drawRect(x + 5, y + 15, x + size - 5, y + height - 5, suitPaint)
        
        // Draw white shirt collar and front
        val shirtPaint = Paint().apply {
            color = Color.WHITE
            style = Paint.Style.FILL
        }
        // Shirt collar
        canvas.drawRect(x + 10, y + 15, x + size - 10, y + 23, shirtPaint)
        // Shirt front (visible part)
        canvas.drawRect(x + size/2 - 6, y + 15, x + size/2 + 6, y + 30, shirtPaint)
        
        // Draw tie (smaller than menahel's)
        val tiePaint = Paint().apply {
            color = Color.rgb(139, 0, 0) // Dark red tie
            style = Paint.Style.FILL
        }
        canvas.drawRect(x + size/2 - 3, y + 18, x + size/2 + 3, y + 32, tiePaint)
        
        // Draw younger/smaller head
        val headPaint = Paint().apply {
            color = Color.rgb(255, 220, 177) // skin tone
            style = Paint.Style.FILL
        }
        canvas.drawOval(x + 8, y - 18, x + size - 8, y + 12, headPaint)
        
        // Draw smaller black hat (same style as menahel)
        val hatPaint = Paint().apply {
            color = Color.BLACK
            style = Paint.Style.FILL
        }
        // Hat brim
        canvas.drawOval(x + 4, y - 22, x + size - 4, y - 14, hatPaint)
        // Hat crown  
        canvas.drawOval(x + 8, y - 28, x + size - 8, y - 16, hatPaint)
        
        // No beard (younger version)
        
        // Draw smaller glasses (younger/student look)
        val glassesPaint = Paint().apply {
            color = Color.BLACK
            strokeWidth = 1.5f
            style = Paint.Style.STROKE
        }
        canvas.drawCircle(x + size/2 - 6, y - 8, 4f, glassesPaint)
        canvas.drawCircle(x + size/2 + 6, y - 8, 4f, glassesPaint)
        // Bridge of glasses
        canvas.drawLine(x + size/2 - 2, y - 8, x + size/2 + 2, y - 8, glassesPaint)
        
        // Draw eyes behind glasses
        val eyePaint = Paint().apply {
            color = Color.WHITE
            style = Paint.Style.FILL
        }
        canvas.drawCircle(x + size/2 - 6, y - 8, 3f, eyePaint)
        canvas.drawCircle(x + size/2 + 6, y - 8, 3f, eyePaint)
        
        // Draw pupils looking in movement direction
        val pupilPaint = Paint().apply {
            color = Color.BLACK
            style = Paint.Style.FILL
        }
        val pupilOffset = if (facingRight) 1f else -1f
        canvas.drawCircle(x + size/2 - 6 + pupilOffset, y - 8, 1.5f, pupilPaint)
        canvas.drawCircle(x + size/2 + 6 + pupilOffset, y - 8, 1.5f, pupilPaint)
        
        // Draw smaller mouth (young/innocent look)
        val mouthPaint = Paint().apply {
            color = Color.BLACK
            style = Paint.Style.STROKE
            strokeWidth = 1.5f
        }
        canvas.drawLine(x + size/2 - 4, y + 0, x + size/2 + 4, y + 0, mouthPaint)
        
        // Draw dress shoes (same as menahel)
        val shoePaint = Paint().apply {
            color = Color.BLACK
            style = Paint.Style.FILL
        }
        canvas.drawOval(x + 2, y + height - 8, x + 16, y + height + 3, shoePaint)
        canvas.drawOval(x + size - 16, y + height - 8, x + size - 2, y + height + 3, shoePaint)
        
        // Add "TALMID" text below
        val textPaint = Paint().apply {
            color = Color.WHITE
            textSize = 12f
            textAlign = Paint.Align.CENTER
        }
        canvas.drawText("TALMID", x + size/2, y + height + 20, textPaint)
    }
}