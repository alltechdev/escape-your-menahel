package com.escapegame

import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import kotlin.random.Random

class Rabbi {
    var x = 500f
    var y = 500f
    private val size = 45f
    private val height = 60f
    private val speed = 2f
    private var screenWidth = 0
    private var screenHeight = 0
    private var changeDirectionTimer = 0
    private var randomDirectionX = 0f
    private var randomDirectionY = 0f
    private var stunTimer = 0
    private var isStunned = false
    private var velocityY = 0f
    private val gravity = 0.5f
    private var onGround = false
    
    private val paint = Paint().apply {
        color = Color.RED
        style = Paint.Style.FILL
    }

    fun init(width: Int, screenHeight: Int) {
        screenWidth = width
        this.screenHeight = screenHeight
        x = (width * 0.8).toFloat()
        y = screenHeight - 150f // Simple ground position
        velocityY = 0f
        onGround = true
    }

    fun update(studentX: Float, studentY: Float, width: Int, screenHeight: Int, platforms: List<GameView.Platform>) {
        // Handle stun
        if (isStunned) {
            stunTimer--
            if (stunTimer <= 0) {
                isStunned = false
            }
        }
        
        // Apply gravity
        velocityY += gravity
        
        if (!isStunned) {
            changeDirectionTimer++
            
            // Mix of following student and random movement
            if (changeDirectionTimer % 60 == 0) { // Change direction every second
                randomDirectionX = (Random.nextFloat() - 0.5f) * 2f
                randomDirectionY = 0f // No vertical random movement, let gravity handle it
            }
            
            // Calculate direction to student (horizontal only)
            val dx = studentX - x
            val distance = kotlin.math.abs(dx)
            
            if (distance > 5f) { // Only move if not too close
                // Follow student horizontally
                val moveX = if (dx > 0) speed else -speed
                x += moveX
            }
        }
        
        // Update vertical position with gravity
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
                }
            }
        }
        
        // Ground collision
        val groundY = screenHeight - 100f
        if (y + this.height > groundY) {
            y = groundY - this.height
            velocityY = 0f
            onGround = true
        }
        
        // Keep menahel within horizontal bounds
        if (x < 0f) x = 0f
        if (x + size > width) x = width - size
        
        // Top boundary
        if (y < 0f) {
            y = 0f
            velocityY = 0f
        }
    }

    fun draw(canvas: Canvas) {
        // Draw suit jacket (dark gray/black)
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
        canvas.drawRect(x + 12, y + 15, x + size - 12, y + 25, shirtPaint)
        // Shirt front (visible part)
        canvas.drawRect(x + size/2 - 8, y + 15, x + size/2 + 8, y + 35, shirtPaint)
        
        // Draw tie
        val tiePaint = Paint().apply {
            color = Color.rgb(139, 0, 0) // Dark red tie
            style = Paint.Style.FILL
        }
        canvas.drawRect(x + size/2 - 4, y + 20, x + size/2 + 4, y + 40, tiePaint)
        
        // Draw head as larger oval
        val headPaint = Paint().apply {
            color = Color.rgb(255, 220, 177) // skin tone
            style = Paint.Style.FILL
        }
        canvas.drawOval(x + 8, y - 20, x + size - 8, y + 15, headPaint)
        
        // Draw formal black hat (fedora style)
        val hatPaint = Paint().apply {
            color = Color.BLACK
            style = Paint.Style.FILL
        }
        // Hat brim
        canvas.drawOval(x + 2, y - 25, x + size - 2, y - 15, hatPaint)
        // Hat crown
        canvas.drawOval(x + 8, y - 32, x + size - 8, y - 18, hatPaint)
        
        // Draw white beard (more professional)
        val beardPaint = Paint().apply {
            color = Color.WHITE
            style = Paint.Style.FILL
        }
        canvas.drawOval(x + size/4, y - 5, x + 3*size/4, y + 15, beardPaint)
        
        // Draw professional glasses
        val glassesPaint = Paint().apply {
            color = Color.BLACK
            strokeWidth = 2f
            style = Paint.Style.STROKE
        }
        canvas.drawCircle(x + size/2 - 8, y - 8, 6f, glassesPaint)
        canvas.drawCircle(x + size/2 + 8, y - 8, 6f, glassesPaint)
        // Bridge of glasses
        canvas.drawLine(x + size/2 - 2, y - 8, x + size/2 + 2, y - 8, glassesPaint)
        
        // Draw eyes behind glasses
        val eyePaint = Paint().apply {
            color = Color.WHITE
            style = Paint.Style.FILL
        }
        canvas.drawCircle(x + size/2 - 8, y - 8, 4f, eyePaint)
        canvas.drawCircle(x + size/2 + 8, y - 8, 4f, eyePaint)
        
        // Draw stern pupils
        val pupilPaint = Paint().apply {
            color = Color.BLACK
            style = Paint.Style.FILL
        }
        canvas.drawCircle(x + size/2 - 8, y - 8, 2f, pupilPaint)
        canvas.drawCircle(x + size/2 + 8, y - 8, 2f, pupilPaint)
        
        // Draw serious mouth
        val mouthPaint = Paint().apply {
            color = Color.BLACK
            style = Paint.Style.STROKE
            strokeWidth = 2f
        }
        canvas.drawLine(x + size/2 - 6, y + 2, x + size/2 + 6, y + 2, mouthPaint)
        
        // Draw dress shoes
        val shoePaint = Paint().apply {
            color = Color.BLACK
            style = Paint.Style.FILL
        }
        canvas.drawOval(x + 2, y + height - 8, x + 18, y + height + 3, shoePaint)
        canvas.drawOval(x + size - 18, y + height - 8, x + size - 2, y + height + 3, shoePaint)
        
        // Add "MENAHEL" text below
        val textPaint = Paint().apply {
            color = Color.WHITE
            textSize = 14f
            textAlign = Paint.Align.CENTER
        }
        canvas.drawText("MENAHEL", x + size/2, y + height + 20, textPaint)
        
        // Draw stun effect
        if (isStunned) {
            val stunPaint = Paint().apply {
                color = Color.YELLOW
                textSize = 18f
                textAlign = Paint.Align.CENTER
            }
            canvas.drawText("STUNNED!", x + size/2, y - 45, stunPaint)
        }
    }
    
    fun stun() {
        isStunned = true
        stunTimer = 120 // 2 seconds at 60 FPS
    }
}