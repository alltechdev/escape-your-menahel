package com.escapegame.entities

import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint

/**
 * A piece of chalk, lobbed by the Menahel with uncanny accuracy honed over
 * decades of waking up dozing bochurim from thirty feet. Getting hit doesn't
 * cost a life — it delivers mussar, which slows you down while you absorb it.
 */
class Chalk(
    private var x: Float,
    private var y: Float,
    private val velocityX: Float
) {
    private var velocityY = -7.5f
    var active = true
        private set

    private val chalkPaint = Paint().apply { color = Color.WHITE; style = Paint.Style.FILL }
    private val edgePaint = Paint().apply {
        color = Color.rgb(200, 200, 205)
        strokeWidth = 2f
        style = Paint.Style.STROKE
    }

    fun update(worldWidth: Float, floorTopY: Float) {
        if (!active) return
        x += velocityX
        velocityY += 0.55f
        y += velocityY
        if (y > floorTopY || x < -20f || x > worldWidth + 20f) {
            active = false
        }
    }

    fun draw(canvas: Canvas) {
        if (!active) return
        canvas.drawRect(x - 9f, y - 4f, x + 9f, y + 4f, chalkPaint)
        canvas.drawRect(x - 9f, y - 4f, x + 9f, y + 4f, edgePaint)
    }

    /** True if this (active) chalk is within [hitDistance] of the given center. */
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
