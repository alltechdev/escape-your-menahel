package com.escapegame.render

import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import com.escapegame.entities.Talmid
import com.escapegame.model.LevelDefinition

/**
 * In-game heads-up display: score, high score, level name, remaining lives
 * (drawn as little black hats, naturally), and active power-up timers.
 */
class HudRenderer(private val w: Float) {

    private val textPaint = Paint().apply {
        color = Color.BLACK
        textSize = 34f
        style = Paint.Style.FILL
    }
    private val smallTextPaint = Paint().apply {
        color = Color.BLACK
        textSize = 26f
        style = Paint.Style.FILL
    }
    private val rightTextPaint = Paint().apply {
        color = Color.BLACK
        textSize = 26f
        textAlign = Paint.Align.RIGHT
        style = Paint.Style.FILL
    }
    private val hatPaint = Paint().apply { color = Color.BLACK; style = Paint.Style.FILL }
    private val effectPaint = Paint().apply {
        textSize = 26f
        style = Paint.Style.FILL
    }
    private val vanPaint = Paint().apply {
        textSize = 34f
        textAlign = Paint.Align.CENTER
        typeface = android.graphics.Typeface.DEFAULT_BOLD
        style = Paint.Style.FILL
    }
    private val barPaint = Paint().apply {
        color = Color.argb(140, 255, 255, 255)
        style = Paint.Style.FILL
    }

    fun draw(
        canvas: Canvas,
        score: Int,
        highScore: Int,
        lives: Int,
        level: LevelDefinition,
        talmid: Talmid,
        vanSecondsLeft: Int?
    ) {
        // Translucent strip so the HUD stays readable over any theme
        canvas.drawRect(0f, 0f, w, 116f, barPaint)

        textPaint.textSize = 34f
        canvas.drawText("Level ${level.number}: ${level.name}", 20f, 44f, textPaint)
        canvas.drawText("Score: $score", 20f, 88f, textPaint)
        rightTextPaint.color = Color.rgb(90, 60, 20)
        canvas.drawText("Best: $highScore", w - 20f, 44f, rightTextPaint)

        if (vanSecondsLeft != null) {
            vanPaint.color =
                if (vanSecondsLeft <= 10) Color.rgb(220, 30, 30) else Color.rgb(150, 60, 10)
            canvas.drawText("VAN LEAVES: ${vanSecondsLeft}s", w / 2, 88f, vanPaint)
        }

        // Lives as black hats, top right
        for (i in 0 until lives) {
            val hatX = w - 40f - i * 52f
            canvas.drawOval(hatX - 22f, 78f, hatX + 22f, 92f, hatPaint)
            canvas.drawOval(hatX - 13f, 58f, hatX + 13f, 84f, hatPaint)
        }

        // Active effects, listed under the strip
        var effectY = 150f
        if (talmid.coffeeFrames > 0) {
            effectPaint.color = Color.rgb(110, 60, 20)
            canvas.drawText("Kavana Coffee: ${talmid.coffeeFrames / 60 + 1}s", 20f, effectY, effectPaint)
            effectY += 34f
        }
        if (talmid.seltzerFrames > 0) {
            effectPaint.color = Color.rgb(20, 120, 60)
            canvas.drawText("Seltzer Jump: ${talmid.seltzerFrames / 60 + 1}s", 20f, effectY, effectPaint)
            effectY += 34f
        }
        if (talmid.shieldCharges > 0) {
            effectPaint.color = Color.rgb(180, 130, 0)
            canvas.drawText("Kugel Shield: ready", 20f, effectY, effectPaint)
        }
    }

    fun drawControlsHint(canvas: Canvas, worldHeight: Float) {
        smallTextPaint.color = Color.BLACK
        canvas.drawText(
            "4/6 move · 2 jump · 5 run+felafel · #/0 ♪",
            20f,
            worldHeight - 24f,
            smallTextPaint
        )
    }
}
