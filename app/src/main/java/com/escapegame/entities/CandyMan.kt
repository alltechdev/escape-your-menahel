package com.escapegame.entities

import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint

/**
 * The Candy Man, out of retirement for one day only. Shuffles across the
 * floor with his little bag; reach him before he leaves and he'll slip you
 * a sucking candy (an extra hat, or points if you're already at five).
 * He does not run. He has never run. He doesn't need to.
 */
class CandyMan(
    startLeft: Boolean,
    private val worldWidth: Float,
    floorTopY: Float
) {
    var x = if (startLeft) -60f else worldWidth + 10f
        private set
    val y = floorTopY - HEIGHT

    private val direction = if (startLeft) 1f else -1f
    var active = true
        private set
    var collected = false
        private set

    val centerX: Float get() = x + WIDTH / 2
    val centerY: Float get() = y + HEIGHT / 2

    private var animFrame = 0

    private val coatPaint = Paint().apply { color = Color.rgb(90, 65, 45); style = Paint.Style.FILL }
    private val shirtPaint = Paint().apply { color = Color.WHITE; style = Paint.Style.FILL }
    private val headPaint = Paint().apply { color = Color.rgb(255, 220, 177); style = Paint.Style.FILL }
    private val hatPaint = Paint().apply { color = Color.BLACK; style = Paint.Style.FILL }
    private val beardPaint = Paint().apply { color = Color.WHITE; style = Paint.Style.FILL }
    private val bagPaint = Paint().apply { color = Color.rgb(180, 40, 40); style = Paint.Style.FILL }
    private val candyPaint = Paint().apply { color = Color.rgb(240, 200, 60); style = Paint.Style.FILL }
    private val detailPaint = Paint().apply {
        color = Color.BLACK
        strokeWidth = 2f
        style = Paint.Style.STROKE
    }
    private val labelPaint = Paint().apply {
        color = Color.rgb(120, 60, 20)
        textSize = 18f
        textAlign = Paint.Align.CENTER
    }

    /** Marks the candy as taken; he keeps shuffling on, mission accomplished. */
    fun collect() {
        collected = true
    }

    fun update() {
        if (!active) return
        animFrame++
        x += direction * SPEED
        if (x < -80f || x > worldWidth + 80f) active = false
    }

    fun draw(canvas: Canvas) {
        if (!active) return
        val s = WIDTH
        val bob = if ((animFrame / 20) % 2 == 0) 0f else 2f

        // Long coat
        canvas.drawRect(x + 5, y + 18 + bob, x + s - 5, y + HEIGHT + bob, coatPaint)
        canvas.drawRect(x + 16, y + 18 + bob, x + s - 16, y + 30 + bob, shirtPaint)
        // Head, hat, big friendly beard
        canvas.drawOval(x + 9, y - 24 + bob, x + s - 9, y + 16 + bob, headPaint)
        canvas.drawOval(x + 3, y - 28 + bob, x + s - 3, y - 18 + bob, hatPaint)
        canvas.drawOval(x + 9, y - 36 + bob, x + s - 9, y - 20 + bob, hatPaint)
        canvas.drawOval(x + s / 4, y - 2 + bob, x + 3 * s / 4, y + 22 + bob, beardPaint)
        // Smiling eyes (the only non-stern eyes in this yeshiva)
        canvas.drawLine(x + s / 2 - 12, y - 10 + bob, x + s / 2 - 4, y - 13 + bob, detailPaint)
        canvas.drawLine(x + s / 2 + 4, y - 13 + bob, x + s / 2 + 12, y - 10 + bob, detailPaint)
        // The famous candy bag
        val bagX = if (direction > 0) x + s - 2 else x - 24
        canvas.drawOval(bagX, y + 34 + bob, bagX + 26, y + 64 + bob, bagPaint)
        if (!collected) {
            canvas.drawCircle(bagX + 13, y + 30 + bob, 7f, candyPaint)
        }

        canvas.drawText("CANDY MAN", x + s / 2, y + HEIGHT + 20, labelPaint)
    }

    private companion object {
        const val WIDTH = 52f
        const val HEIGHT = 70f
        const val SPEED = 2f
    }
}
