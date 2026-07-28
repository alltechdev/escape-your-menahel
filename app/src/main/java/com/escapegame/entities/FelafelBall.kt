package com.escapegame.entities

import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import com.escapegame.core.GameConfig

/**
 * A felafel ball in flight. The talmid's only weapon, and honestly a waste
 * of good felafel.
 */
class FelafelBall(
    private var x: Float,
    private val y: Float,
    private val directionX: Float
) {
    private val radius = GameConfig.FELAFEL_RADIUS
    var active = true
        private set

    private val paint = Paint().apply { color = Color.rgb(139, 69, 19); style = Paint.Style.FILL }
    private val innerPaint = Paint().apply { color = Color.rgb(160, 82, 45); style = Paint.Style.FILL }
    private val dotPaint = Paint().apply { color = Color.rgb(101, 50, 15); style = Paint.Style.FILL }

    fun update(worldWidth: Float) {
        if (!active) return
        x += directionX * GameConfig.FELAFEL_SPEED
        if (x < -radius || x > worldWidth + radius) {
            active = false
        }
    }

    fun draw(canvas: Canvas) {
        if (!active) return
        canvas.drawCircle(x, y, radius, paint)
        canvas.drawCircle(x, y, radius * 0.6f, innerPaint)
        canvas.drawCircle(x - 3, y - 3, 2f, dotPaint)
        canvas.drawCircle(x + 2, y - 4, 1.5f, dotPaint)
        canvas.drawCircle(x - 2, y + 3, 1.5f, dotPaint)
        canvas.drawCircle(x + 4, y + 2, 2f, dotPaint)
    }

    /** True if this (active) ball is within [hitDistance] of the given center. */
    fun hits(targetCenterX: Float, targetCenterY: Float, hitDistance: Float): Boolean {
        if (!active) return false
        val dx = x - targetCenterX
        val dy = y - targetCenterY
        if (dx * dx + dy * dy < hitDistance * hitDistance) {
            active = false
            return true
        }
        return false
    }
}
