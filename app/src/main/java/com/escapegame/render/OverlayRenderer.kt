package com.escapegame.render

import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.RectF
import android.graphics.Typeface
import com.escapegame.levels.Levels
import com.escapegame.model.Difficulty
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
    private val difficultyLabelPaint = Paint().apply {
        color = Color.WHITE
        textSize = 40f
        textAlign = Paint.Align.CENTER
        typeface = Typeface.DEFAULT_BOLD
    }
    private val difficultyDescPaint = Paint().apply {
        color = Color.rgb(200, 200, 210)
        textSize = 27f
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

    fun drawDifficultySelect(canvas: Canvas, selected: Int) {
        canvas.drawRect(0f, 0f, w, h, dimPaint)
        canvas.drawText("CHOOSE YOUR MADREIGA", w / 2, h * 0.13f, headerPaint)

        val values = Difficulty.values()
        for (i in values.indices) {
            val top = difficultyRowTop(i)
            cardRect.set(w * 0.08f, top, w * 0.92f, top + difficultyRowHeight())
            canvas.drawRoundRect(cardRect, 18f, 18f, cardPaint)
            if (i == selected) {
                canvas.drawRoundRect(cardRect, 18f, 18f, cardStrokePaint)
            }
            val option = values[i]
            difficultyLabelPaint.color = if (i == selected) Color.YELLOW else Color.WHITE
            canvas.drawText(option.label, w / 2, top + difficultyRowHeight() * 0.42f, difficultyLabelPaint)
            canvas.drawText(option.description, w / 2, top + difficultyRowHeight() * 0.78f, difficultyDescPaint)
        }

        val prompt = if (touchMode) "Tap a madreiga to begin" else "2/8 or UP/DOWN choose · 5/OK begin"
        canvas.drawText(prompt, w / 2, h * 0.93f, accentPaint)
    }

    fun drawModeSelect(canvas: Canvas, selected: Int, endlessBestDay: Int) {
        canvas.drawRect(0f, 0f, w, h, dimPaint)
        canvas.drawText("CHOOSE YOUR ZMAN", w / 2, h * 0.16f, headerPaint)

        val labels = arrayOf("THE ESCAPE", "BEIN HAZMANIM")
        val descriptions = arrayOf(
            "The story: 18 levels to freedom.",
            if (endlessBestDay > 0) "Endless days. Best so far: day $endlessBestDay."
            else "Endless days, rising danger, no exit exam."
        )
        for (i in 0..1) {
            val top = modeRowTop(i)
            cardRect.set(w * 0.08f, top, w * 0.92f, top + modeRowHeight())
            canvas.drawRoundRect(cardRect, 18f, 18f, cardPaint)
            if (i == selected) canvas.drawRoundRect(cardRect, 18f, 18f, cardStrokePaint)
            difficultyLabelPaint.color = if (i == selected) Color.YELLOW else Color.WHITE
            canvas.drawText(labels[i], w / 2, top + modeRowHeight() * 0.42f, difficultyLabelPaint)
            canvas.drawText(descriptions[i], w / 2, top + modeRowHeight() * 0.78f, difficultyDescPaint)
        }

        val prompt = if (touchMode) "Tap a zman to begin" else "2/8 or UP/DOWN choose · 5/OK begin"
        canvas.drawText(prompt, w / 2, h * 0.85f, accentPaint)
    }

    /** Index of the mode row containing the point, or -1. */
    fun modeRowAt(x: Float, y: Float): Int {
        if (x < w * 0.08f || x > w * 0.92f) return -1
        for (i in 0..1) {
            val top = modeRowTop(i)
            if (y >= top && y <= top + modeRowHeight()) return i
        }
        return -1
    }

    private fun modeRowTop(index: Int): Float = h * (0.28f + index * 0.22f)

    private fun modeRowHeight(): Float = h * 0.16f

    /** Index of the difficulty row containing the point, or -1. */
    fun difficultyRowAt(x: Float, y: Float): Int {
        if (x < w * 0.08f || x > w * 0.92f) return -1
        for (i in Difficulty.values().indices) {
            val top = difficultyRowTop(i)
            if (y >= top && y <= top + difficultyRowHeight()) return i
        }
        return -1
    }

    private fun difficultyRowTop(index: Int): Float = h * (0.20f + index * 0.17f)

    private fun difficultyRowHeight(): Float = h * 0.13f

    fun drawLevelIntro(canvas: Canvas, level: LevelDefinition, difficultyLabel: String) {
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
        canvas.drawText(
            "Level ${level.number} of ${Levels.all.size} · $difficultyLabel",
            w / 2, top + height * 0.16f, bodyPaint
        )
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
