package com.escapegame

import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint

data class FelafelBall(var x: Float, var y: Float, val velocityX: Float) {
    private val size = 8f
    private val speed = 10f
    var active = true
    
    private val paint = Paint().apply {
        color = Color.rgb(139, 69, 19) // Brown felafel
        style = Paint.Style.FILL
    }
    
    private val innerPaint = Paint().apply {
        color = Color.rgb(160, 82, 45) // Lighter brown
        style = Paint.Style.FILL
    }
    
    fun update() {
        if (active) {
            x += velocityX * speed
        }
    }
    
    fun draw(canvas: Canvas) {
        if (active) {
            // Draw felafel ball - main brown circle
            canvas.drawCircle(x, y, size, paint)
            // Draw inner lighter brown
            canvas.drawCircle(x, y, size * 0.6f, innerPaint)
            // Add felafel texture dots
            val dotPaint = Paint().apply {
                color = Color.rgb(101, 50, 15) // Dark brown dots
                style = Paint.Style.FILL
            }
            canvas.drawCircle(x - 2, y - 2, 1.5f, dotPaint)
            canvas.drawCircle(x + 1, y - 3, 1f, dotPaint)
            canvas.drawCircle(x - 1, y + 2, 1f, dotPaint)
            canvas.drawCircle(x + 3, y + 1, 1.5f, dotPaint)
        }
    }
    
    fun checkBounds(screenWidth: Int): Boolean {
        if (x < 0 || x > screenWidth) {
            active = false
            return false
        }
        return true
    }
    
    fun checkCollision(targetX: Float, targetY: Float, targetSize: Float): Boolean {
        if (!active) return false
        
        val distance = kotlin.math.sqrt(
            ((x - (targetX + targetSize/2)) * (x - (targetX + targetSize/2)) + 
             (y - (targetY + targetSize/2)) * (y - (targetY + targetSize/2))).toDouble()
        )
        
        if (distance < size + targetSize/2) {
            active = false
            return true
        }
        return false
    }
}