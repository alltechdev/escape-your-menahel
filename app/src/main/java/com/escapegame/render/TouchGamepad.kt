package com.escapegame.render

import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Path
import android.graphics.RectF
import android.graphics.Typeface
import com.escapegame.core.GameConfig

/**
 * Handheld-console control layout for touchscreen-only devices: a cross
 * d-pad bottom-left, diagonal B/A buttons bottom-right, SELECT/START pills,
 * with the game's landscape LCD up top. Never shown on devices with a
 * physical keypad/d-pad.
 *
 * All geometry is in shell coordinates (the portrait 1080x1920 space).
 */
object TouchGamepadLayout {

    enum class Control { DPAD_LEFT, DPAD_RIGHT, DPAD_UP, BUTTON_A, BUTTON_B, START, SELECT, MUTE, NONE }

    // Shell geometry: a wide, minimal bezel around a 4:3 landscape LCD;
    // the game world (1440x1080) fits it exactly.
    val screenBezel = RectF(40f, 70f, 1040f, 920f)
    val screenLcd = RectF(90f, 120f, 990f, 795f)

    /** Uniform scale that fits the landscape world inside the LCD. */
    val screenScale: Float = kotlin.math.min(
        screenLcd.width() / GameConfig.LANDSCAPE_WORLD_WIDTH,
        screenLcd.height() / GameConfig.LANDSCAPE_WORLD_HEIGHT
    )
    val screenOffsetX: Float =
        screenLcd.left + (screenLcd.width() - GameConfig.LANDSCAPE_WORLD_WIDTH * screenScale) / 2
    val screenOffsetY: Float =
        screenLcd.top + (screenLcd.height() - GameConfig.LANDSCAPE_WORLD_HEIGHT * screenScale) / 2

    // D-pad cross
    const val DPAD_CX = 235f
    const val DPAD_CY = 1235f
    const val DPAD_ARM = 165f
    const val DPAD_THICK = 110f

    // Buttons (diagonal: A upper-right of B)
    const val A_CX = 900f
    const val A_CY = 1200f
    const val B_CX = 660f
    const val B_CY = 1310f
    const val BUTTON_RADIUS = 92f

    // SELECT / START pills
    val selectRect = RectF(350f, 1520f, 520f, 1578f)
    val startRect = RectF(560f, 1520f, 730f, 1578f)

    // Music mute button in the bezel's top strip
    val muteRect = RectF(905f, 76f, 1028f, 114f)

    fun hit(x: Float, y: Float): Control {
        var dx = x - A_CX
        var dy = y - A_CY
        if (dx * dx + dy * dy <= BUTTON_RADIUS * BUTTON_RADIUS) return Control.BUTTON_A
        dx = x - B_CX
        dy = y - B_CY
        if (dx * dx + dy * dy <= BUTTON_RADIUS * BUTTON_RADIUS) return Control.BUTTON_B
        if (startRect.contains(x, y)) return Control.START
        if (selectRect.contains(x, y)) return Control.SELECT
        if (muteRect.contains(x, y)) return Control.MUTE

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

/** Draws the touch controls on the shell body. */
class TouchGamepadRenderer {

    private val padPaint = Paint().apply { color = Color.rgb(35, 35, 45); style = Paint.Style.FILL }
    private val padEdgePaint = Paint().apply {
        color = Color.rgb(90, 75, 15)
        strokeWidth = 4f
        style = Paint.Style.STROKE
    }
    private val buttonPaint = Paint().apply {
        color = Color.rgb(150, 30, 70) // classic burgundy
        style = Paint.Style.FILL
    }
    private val buttonTextPaint = Paint().apply {
        color = Color.argb(235, 255, 255, 255)
        textSize = 54f
        textAlign = Paint.Align.CENTER
        typeface = Typeface.DEFAULT_BOLD
    }
    private val pillTextPaint = Paint().apply {
        color = Color.rgb(80, 62, 10)
        textSize = 27f
        textAlign = Paint.Align.CENTER
        typeface = Typeface.DEFAULT_BOLD
    }
    private val captionPaint = Paint().apply {
        color = Color.rgb(110, 85, 12)
        textSize = 26f
        textAlign = Paint.Align.CENTER
        typeface = Typeface.DEFAULT_BOLD
    }
    private val arrowPaint = Paint().apply {
        color = Color.rgb(190, 190, 200)
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
    private val arrowPath = Path()

    fun draw(canvas: Canvas, musicMuted: Boolean) {
        // Mute toggle up in the bezel strip, by the LED
        canvas.drawRoundRect(TouchGamepadLayout.muteRect, 19f, 19f, padPaint)
        buttonTextPaint.textSize = 24f
        canvas.drawText(
            if (musicMuted) "\u266a OFF" else "\u266a ON",
            TouchGamepadLayout.muteRect.centerX(),
            TouchGamepadLayout.muteRect.centerY() + 9f,
            buttonTextPaint
        )
        buttonTextPaint.textSize = 54f

        val cx = TouchGamepadLayout.DPAD_CX
        val cy = TouchGamepadLayout.DPAD_CY
        val arm = TouchGamepadLayout.DPAD_ARM

        // D-pad cross
        canvas.drawRoundRect(horizontalRect, 16f, 16f, padPaint)
        canvas.drawRoundRect(verticalRect, 16f, 16f, padPaint)
        drawArrow(canvas, cx - arm + 32f, cy, -1f, 0f)
        drawArrow(canvas, cx + arm - 32f, cy, 1f, 0f)
        drawArrow(canvas, cx, cy - arm + 32f, 0f, -1f)
        drawArrow(canvas, cx, cy + arm - 32f, 0f, 1f)

        // B then A (A overlaps on top, like the real thing)
        canvas.drawCircle(TouchGamepadLayout.B_CX, TouchGamepadLayout.B_CY, TouchGamepadLayout.BUTTON_RADIUS, buttonPaint)
        canvas.drawText("B", TouchGamepadLayout.B_CX, TouchGamepadLayout.B_CY + 19f, buttonTextPaint)
        canvas.drawCircle(TouchGamepadLayout.A_CX, TouchGamepadLayout.A_CY, TouchGamepadLayout.BUTTON_RADIUS, buttonPaint)
        canvas.drawText("A", TouchGamepadLayout.A_CX, TouchGamepadLayout.A_CY + 19f, buttonTextPaint)
        canvas.drawText(
            "JUMP",
            TouchGamepadLayout.B_CX,
            TouchGamepadLayout.B_CY + TouchGamepadLayout.BUTTON_RADIUS + 34f,
            captionPaint
        )
        canvas.drawText(
            "RUN + FELAFEL",
            TouchGamepadLayout.A_CX,
            TouchGamepadLayout.A_CY - TouchGamepadLayout.BUTTON_RADIUS - 16f,
            captionPaint
        )

        // SELECT / START pills
        canvas.drawRoundRect(TouchGamepadLayout.selectRect, 29f, 29f, padPaint)
        canvas.drawRoundRect(TouchGamepadLayout.startRect, 29f, 29f, padPaint)
        canvas.drawRoundRect(TouchGamepadLayout.selectRect, 29f, 29f, padEdgePaint)
        canvas.drawRoundRect(TouchGamepadLayout.startRect, 29f, 29f, padEdgePaint)
        buttonTextPaint.textSize = 27f
        canvas.drawText(
            "SELECT",
            TouchGamepadLayout.selectRect.centerX(),
            TouchGamepadLayout.selectRect.centerY() + 10f,
            buttonTextPaint
        )
        canvas.drawText(
            "START",
            TouchGamepadLayout.startRect.centerX(),
            TouchGamepadLayout.startRect.centerY() + 10f,
            buttonTextPaint
        )
        buttonTextPaint.textSize = 54f
        canvas.drawText("MINCHA BREAK (PAUSE)", 540f, 1640f, pillTextPaint)
    }

    private fun drawArrow(canvas: Canvas, tipX: Float, tipY: Float, dirX: Float, dirY: Float) {
        val size = 25f
        arrowPath.reset()
        arrowPath.moveTo(tipX + dirX * size, tipY + dirY * size)
        // Perpendicular base corners
        arrowPath.lineTo(tipX - dirX * size + dirY * size, tipY - dirY * size + dirX * size)
        arrowPath.lineTo(tipX - dirX * size - dirY * size, tipY - dirY * size - dirX * size)
        arrowPath.close()
        canvas.drawPath(arrowPath, arrowPaint)
    }
}
