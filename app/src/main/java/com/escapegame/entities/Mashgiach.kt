package com.escapegame.entities

import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import com.escapegame.core.GameConfig

/**
 * The Mashgiach: patrols a fixed beat with a clipboard, taking attendance.
 * He isn't chasing anyone — he simply happens to be everywhere you want to go.
 * A felafel to the clipboard stuns him longer than the Menahel (he has to
 * re-count the attendance sheet).
 */
class Mashgiach(
    private val patrolStartX: Float,
    private val patrolEndX: Float,
    surfaceTopY: Float,
    private val speed: Float = GameConfig.MASHGIACH_SPEED
) {
    var x = patrolStartX
        private set
    var y = surfaceTopY - GameConfig.MASHGIACH_HEIGHT
        private set

    val width = GameConfig.MASHGIACH_WIDTH
    val height = GameConfig.MASHGIACH_HEIGHT
    val centerX: Float get() = x + width / 2
    val centerY: Float get() = y + height / 2

    private var movingRight = true
    private var stunTimer = 0
    val isStunned: Boolean get() = stunTimer > 0

    private val suitPaint = Paint().apply { color = Color.rgb(50, 50, 60); style = Paint.Style.FILL }
    private val shirtPaint = Paint().apply { color = Color.WHITE; style = Paint.Style.FILL }
    private val headPaint = Paint().apply { color = Color.rgb(255, 220, 177); style = Paint.Style.FILL }
    private val hatPaint = Paint().apply { color = Color.BLACK; style = Paint.Style.FILL }
    private val beardPaint = Paint().apply { color = Color.rgb(90, 60, 30); style = Paint.Style.FILL }
    private val detailPaint = Paint().apply {
        color = Color.BLACK
        strokeWidth = 2f
        style = Paint.Style.STROKE
    }
    private val clipboardPaint = Paint().apply { color = Color.rgb(180, 140, 90); style = Paint.Style.FILL }
    private val paperPaint = Paint().apply { color = Color.WHITE; style = Paint.Style.FILL }
    private val labelPaint = Paint().apply {
        color = Color.BLACK
        textSize = 18f
        textAlign = Paint.Align.CENTER
    }
    private val stunTextPaint = Paint().apply {
        color = Color.rgb(255, 200, 0)
        textSize = 22f
        textAlign = Paint.Align.CENTER
    }

    fun stun() {
        stunTimer = GameConfig.MASHGIACH_STUN_FRAMES
    }

    fun update() {
        if (stunTimer > 0) {
            stunTimer--
            return
        }
        if (movingRight) {
            x += speed
            if (x + width >= patrolEndX) movingRight = false
        } else {
            x -= speed
            if (x <= patrolStartX) movingRight = true
        }
    }

    fun draw(canvas: Canvas) {
        val s = width

        // Suit
        canvas.drawRect(x + 6, y + 20, x + s - 6, y + height - 6, suitPaint)
        // Shirt
        canvas.drawRect(x + 13, y + 20, x + s - 13, y + 31, shirtPaint)
        // Head
        canvas.drawOval(x + 10, y - 24, x + s - 10, y + 18, headPaint)
        // Hat
        canvas.drawOval(x + 4, y - 29, x + s - 4, y - 18, hatPaint)
        canvas.drawOval(x + 10, y - 37, x + s - 10, y - 21, hatPaint)
        // Brown beard (younger than the Menahel, still judging you)
        canvas.drawOval(x + s / 4, y - 4, x + 3 * s / 4, y + 18, beardPaint)
        // Squinting eyes: he is looking VERY carefully at the attendance list
        canvas.drawLine(x + s / 2 - 12, y - 10, x + s / 2 - 4, y - 10, detailPaint)
        canvas.drawLine(x + s / 2 + 4, y - 10, x + s / 2 + 12, y - 10, detailPaint)
        // Clipboard held in front, pointing along his walk direction
        val clipX = if (movingRight) x + s - 4 else x - 20
        canvas.drawRect(clipX, y + 28, clipX + 24, y + 60, clipboardPaint)
        canvas.drawRect(clipX + 3, y + 32, clipX + 21, y + 56, paperPaint)
        canvas.drawLine(clipX + 6, y + 38, clipX + 18, y + 38, detailPaint)
        canvas.drawLine(clipX + 6, y + 44, clipX + 18, y + 44, detailPaint)
        canvas.drawLine(clipX + 6, y + 50, clipX + 18, y + 50, detailPaint)
        // Shoes
        canvas.drawOval(x + 2, y + height - 10, x + 22, y + height + 3, hatPaint)
        canvas.drawOval(x + s - 22, y + height - 10, x + s - 2, y + height + 3, hatPaint)

        canvas.drawText("MASHGIACH", x + s / 2, y + height + 22, labelPaint)

        if (isStunned) {
            canvas.drawText("*recounting*", x + s / 2, y - 48, stunTextPaint)
        }
    }
}
