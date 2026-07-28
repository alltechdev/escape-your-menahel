package com.escapegame.render

import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.RectF
import android.graphics.Typeface
import com.escapegame.core.GameConfig

/**
 * Draws the handheld-console shell used in touch mode, styled after the
 * classic yellow color handheld: yellow body, minimal dark bezel around a
 * wide landscape LCD, multicolor logo, power LED, dotted speaker grille.
 * The actual game renders inside [TouchGamepadLayout.screenLcd] on top.
 */
class GameBoyShellRenderer {

    private val w = GameConfig.WORLD_WIDTH
    private val h = GameConfig.WORLD_HEIGHT

    private val bodyPaint = Paint().apply { color = Color.rgb(248, 200, 40); style = Paint.Style.FILL }
    private val bodyShadePaint = Paint().apply { color = Color.rgb(226, 176, 24); style = Paint.Style.FILL }
    private val bezelPaint = Paint().apply { color = Color.rgb(24, 24, 34); style = Paint.Style.FILL }
    private val lcdOffPaint = Paint().apply { color = Color.rgb(52, 62, 52); style = Paint.Style.FILL }
    private val ledPaint = Paint().apply { color = Color.rgb(235, 45, 45); style = Paint.Style.FILL }
    private val ledLabelPaint = Paint().apply {
        color = Color.rgb(200, 200, 210)
        textSize = 19f
        textAlign = Paint.Align.LEFT
    }
    private val logoPaint = Paint().apply {
        textSize = 44f
        textAlign = Paint.Align.LEFT
        typeface = Typeface.create(Typeface.DEFAULT_BOLD, Typeface.BOLD_ITALIC)
    }
    private val pillOutlinePaint = Paint().apply {
        color = Color.rgb(170, 130, 15)
        strokeWidth = 4f
        style = Paint.Style.STROKE
    }
    private val pillTextPaint = Paint().apply {
        color = Color.rgb(140, 105, 12)
        textSize = 24f
        textAlign = Paint.Align.CENTER
    }
    private val bodyLabelPaint = Paint().apply {
        color = Color.rgb(160, 122, 14)
        textSize = 20f
        textAlign = Paint.Align.CENTER
    }
    private val speakerPaint = Paint().apply { color = Color.rgb(120, 92, 12); style = Paint.Style.FILL }
    private val hanhalaRect = RectF(450f, 948f, 630f, 1000f)

    // "COLOR" gets one color per letter, like the original logo
    private val colorLetterColors = intArrayOf(
        Color.rgb(235, 60, 60),
        Color.rgb(250, 170, 40),
        Color.rgb(90, 190, 80),
        Color.rgb(70, 130, 230),
        Color.rgb(170, 90, 200)
    )

    fun draw(canvas: Canvas) {
        // Body with a subtly shaded lower edge
        canvas.drawRect(0f, 0f, w, h, bodyPaint)
        canvas.drawRect(0f, h - 40f, w, h, bodyShadePaint)

        // Minimal bezel around the wide landscape LCD
        canvas.drawRoundRect(TouchGamepadLayout.screenBezel, 36f, 36f, bezelPaint)

        // Power LED in the bezel's top strip
        canvas.drawCircle(80f, 96f, 9f, ledPaint)
        canvas.drawText("SHTEIG", 100f, 103f, ledLabelPaint)

        // LCD backing behind the game
        canvas.drawRect(TouchGamepadLayout.screenLcd, lcdOffPaint)

        // Logo strip under the LCD: "ESCAPE BOY" white, "COLOR" multicolored
        val logoY = 872f
        val part1 = "ESCAPE BOY "
        val colorWord = "COLOR"
        val part1Width = logoPaint.measureText(part1)
        var letterX = 0f
        val letterWidths = FloatArray(colorWord.length)
        var colorWidth = 0f
        for (i in colorWord.indices) {
            letterWidths[i] = logoPaint.measureText(colorWord, i, i + 1)
            colorWidth += letterWidths[i]
        }
        var x = (w - part1Width - colorWidth) / 2
        logoPaint.color = Color.rgb(230, 230, 240)
        canvas.drawText(part1, x, logoY, logoPaint)
        x += part1Width
        for (i in colorWord.indices) {
            logoPaint.color = colorLetterColors[i % colorLetterColors.size]
            canvas.drawText(colorWord, i, i + 1, x + letterX, logoY, logoPaint)
            letterX += letterWidths[i]
        }

        // The oval maker's mark on the body
        canvas.drawRoundRect(hanhalaRect, 26f, 26f, pillOutlinePaint)
        canvas.drawText("HANHALA®", hanhalaRect.centerX(), hanhalaRect.centerY() + 9f, pillTextPaint)

        // Dotted speaker grille, bottom-right diamond
        var row = 0
        while (row < 6) {
            var col = 0
            while (col < 5) {
                // Trim corners for the classic diamond-ish grille shape
                val corner = (row == 0 || row == 5) && (col == 0 || col == 4)
                if (!corner) {
                    canvas.drawCircle(818f + col * 40f, 1660f + row * 40f, 9f, speakerPaint)
                }
                col++
            }
            row++
        }

        canvas.drawText("KOSHER VIDEO GAME SYSTEM · EST. 5747", w / 2, 1878f, bodyLabelPaint)
    }
}
