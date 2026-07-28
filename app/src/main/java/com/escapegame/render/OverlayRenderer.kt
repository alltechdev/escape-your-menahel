package com.escapegame.render

import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.RectF
import android.graphics.Typeface
import com.escapegame.levels.Levels
import com.escapegame.model.LevelDefinition

/**
 * Full-screen overlay states: title screen, level intro cards, pause
 * ("mincha break"), game over, and victory. Construct with the active
 * world's dimensions; landscape (LCD) worlds get a tighter layout.
 * CENTER/5/OK — or any tap in touch mode — is always the confirm button.
 */
class OverlayRenderer(private val w: Float, private val h: Float) {

    /** When true, prompts say "tap" instead of naming keypad keys. */
    var touchMode = false

    /** Landscape LCD world: less vertical room, so trim the layout. */
    private val compact = w > h

    private val dimPaint = Paint().apply { color = Color.argb(205, 0, 0, 0); style = Paint.Style.FILL }
    private val cardPaint = Paint().apply { color = Color.argb(235, 25, 25, 35); style = Paint.Style.FILL }
    private val cardStrokePaint = Paint().apply {
        color = Color.rgb(240, 190, 60)
        strokeWidth = 5f
        style = Paint.Style.STROKE
    }
    private val titlePaint = Paint().apply {
        color = Color.YELLOW
        textSize = 74f
        textAlign = Paint.Align.CENTER
        typeface = Typeface.DEFAULT_BOLD
    }
    private val headerPaint = Paint().apply {
        color = Color.WHITE
        textSize = 56f
        textAlign = Paint.Align.CENTER
        typeface = Typeface.DEFAULT_BOLD
    }
    private val bodyPaint = Paint().apply {
        color = Color.WHITE
        textSize = 34f
        textAlign = Paint.Align.CENTER
    }
    private val accentPaint = Paint().apply {
        color = Color.rgb(120, 255, 140)
        textSize = 40f
        textAlign = Paint.Align.CENTER
        typeface = Typeface.DEFAULT_BOLD
    }
    private val redPaint = Paint().apply {
        color = Color.rgb(255, 90, 90)
        textSize = 84f
        textAlign = Paint.Align.CENTER
        typeface = Typeface.DEFAULT_BOLD
    }
    private val finePrintPaint = Paint().apply {
        color = Color.rgb(170, 170, 170)
        textSize = 26f
        textAlign = Paint.Align.CENTER
    }
    private val modifierPaint = Paint().apply {
        color = Color.rgb(255, 170, 60)
        textSize = 28f
        textAlign = Paint.Align.CENTER
        typeface = Typeface.DEFAULT_BOLD
    }
    private val cardRect = RectF()

    fun drawIntro(canvas: Canvas, highScore: Int, semichos: Int, escapes: Int) {
        canvas.drawRect(0f, 0f, w, h, dimPaint)

        if (compact) {
            canvas.drawText("ESCAPE YOUR MENAHEL!", w / 2, h * 0.16f, titlePaint)
            canvas.drawText("You left the beis medrash early. He saw.", w / 2, h * 0.27f, bodyPaint)

            var y = h * 0.40f
            val lh = 46f
            canvas.drawText("D-pad — move  ·  B — jump (x2 = double)", w / 2, y, bodyPaint); y += lh
            canvas.drawText("A — run + shoot felafel", w / 2, y, bodyPaint); y += lh
            canvas.drawText("START — mincha break (pause)", w / 2, y, bodyPaint); y += lh * 1.4f
            canvas.drawText("Grab rugelach. ${Levels.all.size} levels to freedom.", w / 2, y, bodyPaint)

            if (highScore > 0) {
                canvas.drawText(
                    "Best: $highScore · Semichos: $semichos/5 · Escapes: $escapes",
                    w / 2, h * 0.76f, bodyPaint
                )
            }
            canvas.drawText(confirmPrompt("start"), w / 2, h * 0.87f, accentPaint)
            return
        }

        canvas.drawText("ESCAPE YOUR", w / 2, h * 0.13f, titlePaint)
        canvas.drawText("MENAHEL!", w / 2, h * 0.18f, titlePaint)
        canvas.drawText("The Yeshiva Breakout", w / 2, h * 0.225f, bodyPaint)

        var y = h * 0.32f
        val lh = 48f
        canvas.drawText("You left the beis medrash early.", w / 2, y, bodyPaint); y += lh
        canvas.drawText("He saw. He ALWAYS sees.", w / 2, y, bodyPaint); y += lh * 1.8f

        canvas.drawText("KEYPAD / D-PAD CONTROLS", w / 2, y, accentPaint); y += lh * 1.2f
        canvas.drawText("4 / 6  or  LEFT / RIGHT — move", w / 2, y, bodyPaint); y += lh
        canvas.drawText("2  or  UP — jump (twice = double jump)", w / 2, y, bodyPaint); y += lh
        canvas.drawText("5  or  OK — run + shoot felafel", w / 2, y, bodyPaint); y += lh
        canvas.drawText("P / MENU — mincha break (pause)", w / 2, y, bodyPaint); y += lh
        canvas.drawText("0 / # — klezmer on/off", w / 2, y, bodyPaint); y += lh * 1.8f

        canvas.drawText("Grab rugelach. Chap the power-ups.", w / 2, y, bodyPaint); y += lh
        canvas.drawText("${Levels.all.size} levels between you and freedom.", w / 2, y, bodyPaint); y += lh * 1.6f

        if (highScore > 0) {
            canvas.drawText("Best escape so far: $highScore", w / 2, y, bodyPaint); y += lh
        }
        canvas.drawText("Semichos earned: $semichos/5 · Escapes: $escapes", w / 2, y, bodyPaint)

        canvas.drawText(confirmPrompt("start"), w / 2, h * 0.88f, accentPaint)
        canvas.drawText("Est. 5747 · Accredited by absolutely nobody", w / 2, h * 0.93f, finePrintPaint)
    }

    fun drawLevelIntro(canvas: Canvas, level: LevelDefinition) {
        canvas.drawRect(0f, 0f, w, h, dimPaint)
        if (compact) {
            cardRect.set(w * 0.12f, h * 0.24f, w * 0.88f, h * 0.76f)
        } else {
            cardRect.set(w * 0.08f, h * 0.32f, w * 0.92f, h * 0.62f)
        }
        canvas.drawRoundRect(cardRect, 24f, 24f, cardPaint)
        canvas.drawRoundRect(cardRect, 24f, 24f, cardStrokePaint)

        val top = cardRect.top
        val height = cardRect.height()
        canvas.drawText("Level ${level.number} of ${Levels.all.size}", w / 2, top + height * 0.16f, bodyPaint)
        canvas.drawText(level.name, w / 2, top + height * 0.32f, headerPaint)
        drawWrapped(canvas, level.quip, w / 2, top + height * 0.47f, cardRect.width() * 0.86f)

        var modifierY = top + height * 0.68f
        for (modifier in level.modifiers) {
            canvas.drawText(modifier.announcement, w / 2, modifierY, modifierPaint)
            modifierY += 34f
        }
        level.timeLimitSeconds?.let {
            canvas.drawText("THE VAN LEAVES IN $it SECONDS!", w / 2, modifierY, modifierPaint)
        }

        canvas.drawText(confirmPrompt("go"), w / 2, cardRect.bottom - height * 0.08f, accentPaint)
    }

    fun drawPaused(canvas: Canvas) {
        canvas.drawRect(0f, 0f, w, h, dimPaint)
        canvas.drawText("MINCHA BREAK", w / 2, h * 0.42f, headerPaint)
        canvas.drawText("(Paused. The Menahel waits. Patiently.)", w / 2, h * 0.47f, bodyPaint)
        canvas.drawText(confirmPrompt("resume"), w / 2, h * 0.55f, accentPaint)
    }

    fun drawGameOver(canvas: Canvas, line: String, score: Int, highScore: Int, isNewBest: Boolean) {
        canvas.drawRect(0f, 0f, w, h, dimPaint)
        canvas.drawText("CAUGHT!", w / 2, h * 0.32f, redPaint)
        drawWrapped(canvas, line, w / 2, h * 0.42f, w * 0.8f)
        canvas.drawText("Score: $score", w / 2, h * 0.55f, bodyPaint)
        if (isNewBest) {
            canvas.drawText("NEW RECORD! Mamash a kiddush!", w / 2, h * 0.61f, accentPaint)
        } else {
            canvas.drawText("Best: $highScore", w / 2, h * 0.61f, bodyPaint)
        }
        canvas.drawText(confirmPrompt("try again"), w / 2, h * 0.72f, accentPaint)
    }

    fun drawVictory(canvas: Canvas, score: Int, highScore: Int, isNewBest: Boolean) {
        canvas.drawRect(0f, 0f, w, h, dimPaint)
        if (compact) {
            canvas.drawText("BARUCH SHEPTARANI!", w / 2, h * 0.2f, titlePaint)
        } else {
            canvas.drawText("BARUCH", w / 2, h * 0.16f, titlePaint)
            canvas.drawText("SHEPTARANI!", w / 2, h * 0.21f, titlePaint)
        }

        var y = h * 0.32f
        val lh = if (compact) 46f else 52f
        for (line in Levels.victoryLines) {
            canvas.drawText(line, w / 2, y, bodyPaint)
            y += lh
        }

        canvas.drawText("Final score: $score", w / 2, h * 0.62f, headerPaint)
        if (isNewBest) {
            canvas.drawText("NEW RECORD! Tell your chavrusa!", w / 2, h * 0.69f, accentPaint)
        } else {
            canvas.drawText("Best: $highScore", w / 2, h * 0.69f, bodyPaint)
        }

        canvas.drawText(confirmPrompt("start another zman"), w / 2, h * 0.8f, accentPaint)
    }

    /** "Press 5 / OK to X" on keypads, "Tap to X" on touchscreens. */
    private fun confirmPrompt(action: String): String =
        if (touchMode) "Tap to $action" else "Press 5 / OK to $action"

    /** Rudimentary center-aligned word wrap for quips of unknown length. */
    private fun drawWrapped(canvas: Canvas, text: String, centerX: Float, startY: Float, maxWidth: Float) {
        var y = startY
        var line = StringBuilder()
        for (word in text.split(" ")) {
            val candidate = if (line.isEmpty()) word else "$line $word"
            if (bodyPaint.measureText(candidate) > maxWidth && line.isNotEmpty()) {
                canvas.drawText(line.toString(), centerX, y, bodyPaint)
                y += 44f
                line = StringBuilder(word)
            } else {
                line = StringBuilder(candidate)
            }
        }
        if (line.isNotEmpty()) {
            canvas.drawText(line.toString(), centerX, y, bodyPaint)
        }
    }
}
