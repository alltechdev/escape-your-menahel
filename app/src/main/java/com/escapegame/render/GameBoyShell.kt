package com.escapegame.render

import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Typeface
import com.escapegame.core.GameConfig

/**
 * Draws the classic handheld-console shell used in touch mode: gray body,
 * dark screen bezel with the obligatory accent stripes, a power LED, a
 * speaker grille, and period-correct labeling ("DOT MATRIX WITH MUSSAR").
 * The actual game renders inside [TouchGamepadLayout.screenLcd] on top.
 */
class GameBoyShellRenderer {

    private val w = GameConfig.WORLD_WIDTH
    private val h = GameConfig.WORLD_HEIGHT

    private val bodyPaint = Paint().apply { color = Color.rgb(196, 196, 204); style = Paint.Style.FILL }
    private val bezelPaint = Paint().apply { color = Color.rgb(45, 45, 58); style = Paint.Style.FILL }
    private val lcdOffPaint = Paint().apply { color = Color.rgb(52, 62, 52); style = Paint.Style.FILL }
    private val stripeMaroon = Paint().apply {
        color = Color.rgb(140, 30, 50)
        strokeWidth = 8f
        style = Paint.Style.STROKE
    }
    private val stripeNavy = Paint().apply {
        color = Color.rgb(40, 50, 120)
        strokeWidth = 8f
        style = Paint.Style.STROKE
    }
    private val bezelTextPaint = Paint().apply {
        color = Color.rgb(210, 210, 220)
        textSize = 24f
        textAlign = Paint.Align.CENTER
    }
    private val ledPaint = Paint().apply { color = Color.rgb(230, 40, 40); style = Paint.Style.FILL }
    private val ledLabelPaint = Paint().apply {
        color = Color.rgb(200, 200, 210)
        textSize = 17f
        textAlign = Paint.Align.CENTER
    }
    private val logoPaint = Paint().apply {
        color = Color.rgb(215, 215, 225)
        textSize = 36f
        textAlign = Paint.Align.CENTER
        typeface = Typeface.create(Typeface.DEFAULT_BOLD, Typeface.BOLD_ITALIC)
    }
    private val bodyLabelPaint = Paint().apply {
        color = Color.rgb(120, 120, 132)
        textSize = 20f
        textAlign = Paint.Align.CENTER
    }
    private val speakerPaint = Paint().apply {
        color = Color.rgb(150, 150, 162)
        strokeWidth = 13f
        strokeCap = Paint.Cap.ROUND
        style = Paint.Style.STROKE
    }

    fun draw(canvas: Canvas) {
        // Body
        canvas.drawRect(0f, 0f, w, h, bodyPaint)

        // Screen bezel with accent stripes flanking the slogan
        canvas.drawRoundRect(TouchGamepadLayout.screenBezel, 40f, 40f, bezelPaint)
        val stripeY1 = 92f
        val stripeY2 = 112f
        canvas.drawLine(110f, stripeY1, 300f, stripeY1, stripeMaroon)
        canvas.drawLine(110f, stripeY2, 300f, stripeY2, stripeNavy)
        canvas.drawLine(780f, stripeY1, 970f, stripeY1, stripeMaroon)
        canvas.drawLine(780f, stripeY2, 970f, stripeY2, stripeNavy)
        canvas.drawText("DOT MATRIX WITH MUSSAR", w / 2, 112f, bezelTextPaint)

        // LCD backing (visible as pillarbox bars around the game)
        canvas.drawRect(TouchGamepadLayout.screenLcd, lcdOffPaint)

        // Power LED: lights up when you're shteiging (always)
        canvas.drawCircle(104f, 700f, 10f, ledPaint)
        canvas.drawText("SHTEIG", 104f, 736f, ledLabelPaint)

        // Logo strip under the LCD
        canvas.drawText("ESCAPE BOY™", w / 2, 1364f, logoPaint)

        // Body label under the START pill
        canvas.drawText("KOSHER VIDEO GAME SYSTEM · EST. 5747", w / 2, 1906f, bodyLabelPaint)

        // Speaker grille, bottom-right
        var i = 0
        while (i < 6) {
            val x = 830f + i * 30f
            canvas.drawLine(x, 1878f, x + 62f, 1792f, speakerPaint)
            i++
        }
    }
}
