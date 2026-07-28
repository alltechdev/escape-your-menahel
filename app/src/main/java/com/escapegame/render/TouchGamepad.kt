package com.escapegame.render

import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Path
import android.graphics.RectF
import android.graphics.Typeface

/**
 * Game Boy-style on-screen gamepad for touchscreen-only devices: a cross
 * d-pad bottom-left, diagonal B/A buttons bottom-right, and a START pill for
 * pause. Never shown on devices with a physical keypad/d-pad — [hit] and the
 * renderer are only used once the view enables touch mode.
 *
 * All geometry is in virtual world coordinates so the pad scales with the
 * rest of the game.
 */
object TouchGamepadLayout {

    enum class Control { DPAD_LEFT, DPAD_RIGHT, DPAD_UP, BUTTON_A, BUTTON_B, START, NONE }

    // Handheld-shell geometry: the game renders inside the "LCD" up top,
    // the controls live on the body below it.
    val screenBezel = RectF(70f, 50f, 1010f, 1380f)
    val screenLcd = RectF(140f, 150f, 940f, 1330f)

    /** Uniform scale that fits the virtual world inside the LCD. */
    val screenScale: Float = kotlin.math.min(
        screenLcd.width() / com.escapegame.core.GameConfig.WORLD_WIDTH,
        screenLcd.height() / com.escapegame.core.GameConfig.WORLD_HEIGHT
    )
    val screenOffsetX: Float =
        screenLcd.left + (screenLcd.width() - com.escapegame.core.GameConfig.WORLD_WIDTH * screenScale) / 2
    val screenOffsetY: Float =
        screenLcd.top + (screenLcd.height() - com.escapegame.core.GameConfig.WORLD_HEIGHT * screenScale) / 2

    // D-pad cross
    const val DPAD_CX = 215f
    const val DPAD_CY = 1600f
    const val DPAD_ARM = 175f
    const val DPAD_THICK = 115f

    // Buttons (Game Boy diagonal: A upper-right of B)
    const val A_CX = 935f
    const val A_CY = 1520f
    const val B_CX = 715f
    const val B_CY = 1660f
    const val BUTTON_RADIUS = 95f

    // START pill
    const val START_LEFT = 440f
    const val START_TOP = 1810f
    const val START_RIGHT = 640f
    const val START_BOTTOM = 1870f

    fun hit(x: Float, y: Float): Control {
        var dx = x - A_CX
        var dy = y - A_CY
        if (dx * dx + dy * dy <= BUTTON_RADIUS * BUTTON_RADIUS) return Control.BUTTON_A
        dx = x - B_CX
        dy = y - B_CY
        if (dx * dx + dy * dy <= BUTTON_RADIUS * BUTTON_RADIUS) return Control.BUTTON_B
        if (x in START_LEFT..START_RIGHT && y in START_TOP..START_BOTTOM) return Control.START

        // D-pad: inside the cross's bounding box, pick the dominant axis
        dx = x - DPAD_CX
        dy = y - DPAD_CY
        val within = kotlin.math.abs(dx) <= DPAD_ARM && kotlin.math.abs(dy) <= DPAD_ARM &&
            (kotlin.math.abs(dx) <= DPAD_THICK / 2 || kotlin.math.abs(dy) <= DPAD_THICK / 2)
        if (within) {
            return if (kotlin.math.abs(dx) > kotlin.math.abs(dy)) {
                if (dx < 0) Control.DPAD_LEFT else Control.DPAD_RIGHT
            } else {
                if (dy < 0) Control.DPAD_UP else Control.NONE // down is unused
            }
        }
        return Control.NONE
    }
}

/** Draws the touch gamepad. Only invoked when touch mode is active. */
class TouchGamepadRenderer {

    private val padPaint = Paint().apply {
        color = Color.argb(150, 35, 35, 40)
        style = Paint.Style.FILL
    }
    private val padEdgePaint = Paint().apply {
        color = Color.argb(180, 210, 210, 215)
        strokeWidth = 4f
        style = Paint.Style.STROKE
    }
    private val buttonPaint = Paint().apply {
        color = Color.argb(170, 150, 30, 70) // classic burgundy
        style = Paint.Style.FILL
    }
    private val buttonTextPaint = Paint().apply {
        color = Color.argb(230, 255, 255, 255)
        textSize = 56f
        textAlign = Paint.Align.CENTER
        typeface = Typeface.DEFAULT_BOLD
    }
    private val captionPaint = Paint().apply {
        color = Color.argb(200, 60, 60, 65)
        textSize = 26f
        textAlign = Paint.Align.CENTER
        typeface = Typeface.DEFAULT_BOLD
    }
    private val arrowPaint = Paint().apply {
        color = Color.argb(180, 220, 220, 225)
        style = Paint.Style.FILL
    }
    private val horizontalRect = RectF(
        TouchGamepadLayout.DPAD_CX - TouchGamepadLayout.DPAD_ARM,
        TouchGamepadLayout.DPAD_CY - TouchGamepadLayout.DPAD_THICK / 2,
        TouchGamepadLayout.DPAD_CX + TouchGamepadLayout.DPAD_ARM,
        TouchGamepadLayout.DPAD_CY + TouchGamepadLayout.DPAD_THICK / 2
    )
    private val verticalRect = RectF(
        TouchGamepadLayout.DPAD_CX - TouchGamepadLayout.DPAD_THICK / 2,
        TouchGamepadLayout.DPAD_CY - TouchGamepadLayout.DPAD_ARM,
        TouchGamepadLayout.DPAD_CX + TouchGamepadLayout.DPAD_THICK / 2,
        TouchGamepadLayout.DPAD_CY + TouchGamepadLayout.DPAD_ARM
    )
    private val startRect = RectF(
        TouchGamepadLayout.START_LEFT,
        TouchGamepadLayout.START_TOP,
        TouchGamepadLayout.START_RIGHT,
        TouchGamepadLayout.START_BOTTOM
    )
    private val arrowPath = Path()

    fun draw(canvas: Canvas) {
        val cx = TouchGamepadLayout.DPAD_CX
        val cy = TouchGamepadLayout.DPAD_CY
        val arm = TouchGamepadLayout.DPAD_ARM

        // D-pad cross
        canvas.drawRoundRect(horizontalRect, 18f, 18f, padPaint)
        canvas.drawRoundRect(verticalRect, 18f, 18f, padPaint)
        canvas.drawRoundRect(horizontalRect, 18f, 18f, padEdgePaint)
        canvas.drawRoundRect(verticalRect, 18f, 18f, padEdgePaint)
        drawArrow(canvas, cx - arm + 34f, cy, -1f, 0f)
        drawArrow(canvas, cx + arm - 34f, cy, 1f, 0f)
        drawArrow(canvas, cx, cy - arm + 34f, 0f, -1f)

        // B then A (A overlaps on top, like the real thing)
        canvas.drawCircle(TouchGamepadLayout.B_CX, TouchGamepadLayout.B_CY, TouchGamepadLayout.BUTTON_RADIUS, buttonPaint)
        canvas.drawCircle(TouchGamepadLayout.B_CX, TouchGamepadLayout.B_CY, TouchGamepadLayout.BUTTON_RADIUS, padEdgePaint)
        canvas.drawText("B", TouchGamepadLayout.B_CX, TouchGamepadLayout.B_CY + 20f, buttonTextPaint)
        canvas.drawCircle(TouchGamepadLayout.A_CX, TouchGamepadLayout.A_CY, TouchGamepadLayout.BUTTON_RADIUS, buttonPaint)
        canvas.drawCircle(TouchGamepadLayout.A_CX, TouchGamepadLayout.A_CY, TouchGamepadLayout.BUTTON_RADIUS, padEdgePaint)
        canvas.drawText("A", TouchGamepadLayout.A_CX, TouchGamepadLayout.A_CY + 20f, buttonTextPaint)
        canvas.drawText(
            "JUMP",
            TouchGamepadLayout.B_CX,
            TouchGamepadLayout.B_CY + TouchGamepadLayout.BUTTON_RADIUS + 32f,
            captionPaint
        )
        canvas.drawText(
            "RUN + FELAFEL",
            TouchGamepadLayout.A_CX,
            TouchGamepadLayout.A_CY - TouchGamepadLayout.BUTTON_RADIUS - 14f,
            captionPaint
        )

        // START pill
        canvas.drawRoundRect(startRect, 30f, 30f, padPaint)
        canvas.drawRoundRect(startRect, 30f, 30f, padEdgePaint)
        buttonTextPaint.textSize = 30f
        canvas.drawText("START", startRect.centerX(), startRect.centerY() + 11f, buttonTextPaint)
        buttonTextPaint.textSize = 56f
    }

    private fun drawArrow(canvas: Canvas, tipX: Float, tipY: Float, dirX: Float, dirY: Float) {
        val size = 26f
        arrowPath.reset()
        arrowPath.moveTo(tipX + dirX * size, tipY + dirY * size)
        // Perpendicular base corners
        arrowPath.lineTo(tipX - dirX * size + dirY * size, tipY - dirY * size + dirX * size)
        arrowPath.lineTo(tipX - dirX * size - dirY * size, tipY - dirY * size - dirX * size)
        arrowPath.close()
        canvas.drawPath(arrowPath, arrowPaint)
    }
}
