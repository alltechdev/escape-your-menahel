package com.escapegame.render

import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.RectF
import com.escapegame.core.GameConfig
import com.escapegame.model.LevelTheme
import com.escapegame.model.Platform

/**
 * Draws the per-theme scenery, the platforms, and the exit door.
 * Everything is drawn in virtual world coordinates.
 */
class BackgroundRenderer {

    private val w = GameConfig.WORLD_WIDTH
    private val h = GameConfig.WORLD_HEIGHT
    private val floorTop = h - GameConfig.FLOOR_HEIGHT

    private val fillPaint = Paint().apply { style = Paint.Style.FILL }
    private val strokePaint = Paint().apply { style = Paint.Style.STROKE; strokeWidth = 3f }
    private val textPaint = Paint().apply {
        textAlign = Paint.Align.CENTER
        style = Paint.Style.FILL
    }
    private val tablePaint = Paint().apply { color = Color.rgb(139, 69, 19); style = Paint.Style.FILL }
    private val tableEdgePaint = Paint().apply {
        color = Color.rgb(101, 50, 15)
        strokeWidth = 4f
        style = Paint.Style.STROKE
    }
    private val legPaint = Paint().apply { color = Color.GRAY; style = Paint.Style.FILL }
    private val doorPaint = Paint().apply { color = Color.rgb(90, 60, 30); style = Paint.Style.FILL }
    private val doorFramePaint = Paint().apply {
        color = Color.rgb(60, 40, 20)
        strokeWidth = 6f
        style = Paint.Style.STROKE
    }
    private val exitSignPaint = Paint().apply { color = Color.rgb(0, 150, 60); style = Paint.Style.FILL }
    private val exitTextPaint = Paint().apply {
        color = Color.WHITE
        textSize = 24f
        textAlign = Paint.Align.CENTER
    }
    private val knobPaint = Paint().apply { color = Color.rgb(230, 200, 90); style = Paint.Style.FILL }
    private val signRect = RectF()

    fun draw(canvas: Canvas, theme: LevelTheme) {
        drawWallsAndFloor(canvas, theme)
        drawDecorations(canvas, theme)
    }

    private fun drawWallsAndFloor(canvas: Canvas, theme: LevelTheme) {
        val outdoors = theme == LevelTheme.ROOFTOP ||
            theme == LevelTheme.PARKING_LOT ||
            theme == LevelTheme.BUS_STOP

        fillPaint.color = when (theme) {
            LevelTheme.LUNCHROOM -> Color.rgb(240, 240, 220)
            LevelTheme.HALLWAY -> Color.rgb(220, 228, 235)
            LevelTheme.CLASSROOM -> Color.rgb(235, 232, 210)
            LevelTheme.BEIS_MEDRASH -> Color.rgb(232, 222, 200)
            LevelTheme.GYM -> Color.rgb(215, 225, 215)
            LevelTheme.LIBRARY -> Color.rgb(226, 218, 205)
            LevelTheme.KITCHEN -> Color.rgb(238, 238, 238)
            LevelTheme.DETENTION -> Color.rgb(200, 200, 205)
            LevelTheme.ROOFTOP -> Color.rgb(160, 210, 245)
            LevelTheme.PARKING_LOT -> Color.rgb(175, 215, 240)
            LevelTheme.BUS_STOP -> Color.rgb(255, 205, 150) // late-afternoon sky
        }
        canvas.drawRect(0f, 0f, w, h, fillPaint)

        fillPaint.color = if (outdoors) Color.rgb(110, 110, 115) else Color.rgb(139, 69, 19)
        canvas.drawRect(0f, floorTop, w, h, fillPaint)

        // Floor detail lines
        strokePaint.color = if (outdoors) Color.rgb(85, 85, 90) else Color.rgb(101, 50, 15)
        var tx = 0f
        while (tx < w) {
            canvas.drawLine(tx, floorTop, tx + 30f, h, strokePaint)
            tx += 90f
        }
    }

    private fun drawDecorations(canvas: Canvas, theme: LevelTheme) {
        when (theme) {
            LevelTheme.LUNCHROOM -> {
                drawWindow(canvas, 90f, 120f)
                drawWindow(canvas, w - 290f, 120f)
                // Abandoned lunch trays
                fillPaint.color = Color.rgb(255, 165, 0)
                canvas.drawRect(150f, floorTop - 22f, 240f, floorTop - 6f, fillPaint)
                canvas.drawRect(420f, floorTop - 20f, 505f, floorTop - 4f, fillPaint)
                drawWallSign(canvas, w / 2, 250f, "BENTCH BEFORE YOU LEAVE")
            }
            LevelTheme.HALLWAY -> {
                // A row of lockers
                fillPaint.color = Color.rgb(120, 140, 170)
                strokePaint.color = Color.rgb(80, 95, 120)
                var lx = 60f
                while (lx < w - 160f) {
                    canvas.drawRect(lx, 150f, lx + 90f, 400f, fillPaint)
                    canvas.drawRect(lx, 150f, lx + 90f, 400f, strokePaint)
                    canvas.drawLine(lx + 70f, 250f, lx + 70f, 290f, strokePaint)
                    lx += 100f
                }
                drawWallSign(canvas, w / 2, 500f, "NO RUNNING IN THE HALLWAY")
            }
            LevelTheme.CLASSROOM -> {
                // Blackboard with unfinished homework assignment
                fillPaint.color = Color.rgb(35, 70, 45)
                canvas.drawRect(140f, 140f, w - 140f, 420f, fillPaint)
                strokePaint.color = Color.rgb(150, 110, 60)
                canvas.drawRect(140f, 140f, w - 140f, 420f, strokePaint)
                textPaint.color = Color.WHITE
                textPaint.textSize = 40f
                canvas.drawText("Chazara: Perek 2", w / 2, 240f, textPaint)
                canvas.drawText("TEST SUNDAY (yes, Sunday)", w / 2, 320f, textPaint)
            }
            LevelTheme.BEIS_MEDRASH -> {
                drawBookshelf(canvas, 80f, 130f, 300f)
                drawBookshelf(canvas, w - 380f, 130f, 300f)
                // A shtender, mid-room
                fillPaint.color = Color.rgb(120, 80, 40)
                canvas.drawRect(w / 2 - 40f, 380f, w / 2 + 40f, 400f, fillPaint)
                canvas.drawRect(w / 2 - 8f, 400f, w / 2 + 8f, 500f, fillPaint)
                drawWallSign(canvas, w / 2, 560f, "SHEKET B'VAKASHA")
            }
            LevelTheme.GYM -> {
                // Basketball hoop
                fillPaint.color = Color.WHITE
                canvas.drawRect(w - 260f, 160f, w - 100f, 280f, fillPaint)
                strokePaint.color = Color.rgb(200, 80, 40)
                canvas.drawCircle(w - 180f, 300f, 34f, strokePaint)
                drawWallSign(canvas, 350f, 220f, "GO FIGHTING SCHOLARS!")
                // Folding chairs from last night's vort
                fillPaint.color = Color.rgb(150, 150, 155)
                canvas.drawRect(120f, floorTop - 60f, 180f, floorTop, fillPaint)
                canvas.drawRect(230f, floorTop - 60f, 290f, floorTop, fillPaint)
            }
            LevelTheme.LIBRARY -> {
                drawBookshelf(canvas, 70f, 120f, 380f)
                drawBookshelf(canvas, w / 2 - 150f, 120f, 380f)
                drawBookshelf(canvas, w - 370f, 120f, 380f)
                drawWallSign(canvas, w / 2, 600f, "SHHHH!")
            }
            LevelTheme.KITCHEN -> {
                // Stove with a very large cholent pot
                fillPaint.color = Color.rgb(160, 160, 170)
                canvas.drawRect(100f, 300f, 420f, 430f, fillPaint)
                fillPaint.color = Color.rgb(60, 60, 70)
                canvas.drawOval(150f, 220f, 370f, 320f, fillPaint)
                // Steam
                strokePaint.color = Color.rgb(190, 190, 200)
                canvas.drawLine(200f, 210f, 215f, 150f, strokePaint)
                canvas.drawLine(260f, 210f, 275f, 140f, strokePaint)
                canvas.drawLine(320f, 210f, 335f, 150f, strokePaint)
                drawWallSign(canvas, w - 300f, 250f, "FLEISHIGS ONLY")
            }
            LevelTheme.DETENTION -> {
                // Barred window and an extremely slow clock
                drawWindow(canvas, w / 2 - 100f, 140f)
                strokePaint.color = Color.rgb(60, 60, 60)
                canvas.drawLine(w / 2 - 66f, 140f, w / 2 - 66f, 290f, strokePaint)
                canvas.drawLine(w / 2 - 33f, 140f, w / 2 - 33f, 290f, strokePaint)
                canvas.drawLine(w / 2, 140f, w / 2, 290f, strokePaint)
                fillPaint.color = Color.WHITE
                canvas.drawCircle(180f, 220f, 60f, fillPaint)
                strokePaint.color = Color.BLACK
                canvas.drawCircle(180f, 220f, 60f, strokePaint)
                canvas.drawLine(180f, 220f, 180f, 175f, strokePaint)
                canvas.drawLine(180f, 220f, 215f, 220f, strokePaint)
                drawWallSign(canvas, w / 2, 420f, "THINK ABOUT WHAT YOU DID")
            }
            LevelTheme.ROOFTOP -> {
                // Sun and pigeons
                fillPaint.color = Color.rgb(255, 235, 120)
                canvas.drawCircle(w - 160f, 170f, 70f, fillPaint)
                strokePaint.color = Color.rgb(70, 70, 70)
                drawPigeon(canvas, 200f, 240f)
                drawPigeon(canvas, 300f, 190f)
                drawPigeon(canvas, 420f, 260f)
                // Antenna
                canvas.drawLine(120f, floorTop, 120f, 420f, strokePaint)
                canvas.drawLine(80f, 470f, 160f, 470f, strokePaint)
            }
            LevelTheme.PARKING_LOT -> {
                // The infamous minivan
                fillPaint.color = Color.rgb(140, 120, 160)
                canvas.drawRect(90f, 300f, 470f, 430f, fillPaint)
                canvas.drawRect(150f, 230f, 410f, 310f, fillPaint)
                fillPaint.color = Color.rgb(200, 225, 245)
                canvas.drawRect(170f, 245f, 280f, 300f, fillPaint)
                canvas.drawRect(300f, 245f, 395f, 300f, fillPaint)
                fillPaint.color = Color.BLACK
                canvas.drawCircle(170f, 435f, 38f, fillPaint)
                canvas.drawCircle(390f, 435f, 38f, fillPaint)
                // License plate
                fillPaint.color = Color.WHITE
                canvas.drawRect(230f, 390f, 340f, 420f, fillPaint)
                textPaint.color = Color.BLACK
                textPaint.textSize = 24f
                canvas.drawText("MNHL-1", 285f, 412f, textPaint)
            }
            LevelTheme.BUS_STOP -> {
                // Setting sun and the bus, tantalizingly close
                fillPaint.color = Color.rgb(255, 150, 90)
                canvas.drawCircle(170f, 200f, 80f, fillPaint)
                // Bus stop sign
                strokePaint.color = Color.rgb(70, 70, 70)
                canvas.drawLine(w - 150f, floorTop, w - 150f, 420f, strokePaint)
                fillPaint.color = Color.rgb(30, 90, 180)
                canvas.drawRect(w - 210f, 350f, w - 90f, 420f, fillPaint)
                textPaint.color = Color.WHITE
                textPaint.textSize = 28f
                canvas.drawText("BUS", w - 150f, 397f, textPaint)
                // The 4:15 itself, on the horizon
                fillPaint.color = Color.rgb(230, 190, 40)
                canvas.drawRect(560f, 300f, 860f, 420f, fillPaint)
                fillPaint.color = Color.rgb(180, 220, 240)
                var wx = 580f
                while (wx < 830f) {
                    canvas.drawRect(wx, 320f, wx + 45f, 360f, fillPaint)
                    wx += 60f
                }
                fillPaint.color = Color.BLACK
                canvas.drawCircle(620f, 425f, 28f, fillPaint)
                canvas.drawCircle(800f, 425f, 28f, fillPaint)
                textPaint.color = Color.BLACK
                textPaint.textSize = 30f
                canvas.drawText("4:15 EXPRESS", 710f, 275f, textPaint)
            }
        }
    }

    fun drawPlatforms(canvas: Canvas, platforms: List<Platform>) {
        for (p in platforms) {
            canvas.drawRect(p.x, p.y, p.x + p.width, p.y + p.height, tablePaint)
            canvas.drawRect(p.x, p.y, p.x + p.width, p.y + p.height, tableEdgePaint)
            val legWidth = 10f
            canvas.drawRect(p.x + 14f, p.y + p.height, p.x + 14f + legWidth, floorTop, legPaint)
            canvas.drawRect(
                p.x + p.width - 14f - legWidth, p.y + p.height,
                p.x + p.width - 14f, floorTop, legPaint
            )
        }
    }

    fun drawExitDoor(canvas: Canvas, doorLeft: Float) {
        val top = floorTop - GameConfig.DOOR_HEIGHT
        val right = doorLeft + GameConfig.DOOR_WIDTH
        canvas.drawRect(doorLeft, top, right, floorTop, doorPaint)
        canvas.drawRect(doorLeft, top, right, floorTop, doorFramePaint)
        canvas.drawCircle(right - 18f, top + GameConfig.DOOR_HEIGHT / 2, 7f, knobPaint)
        // Green EXIT sign above, bilingual for full authenticity
        signRect.set(doorLeft - 10f, top - 56f, right + 10f, top - 12f)
        canvas.drawRoundRect(signRect, 8f, 8f, exitSignPaint)
        canvas.drawText("EXIT / יציאה", (doorLeft + right) / 2, top - 24f, exitTextPaint)
    }

    private fun drawWindow(canvas: Canvas, left: Float, top: Float) {
        fillPaint.color = Color.rgb(173, 216, 230)
        canvas.drawRect(left, top, left + 200f, top + 150f, fillPaint)
        strokePaint.color = Color.rgb(139, 69, 19)
        canvas.drawRect(left, top, left + 200f, top + 150f, strokePaint)
        canvas.drawLine(left + 100f, top, left + 100f, top + 150f, strokePaint)
        canvas.drawLine(left, top + 75f, left + 200f, top + 75f, strokePaint)
    }

    private fun drawBookshelf(canvas: Canvas, left: Float, top: Float, height: Float) {
        fillPaint.color = Color.rgb(110, 75, 40)
        canvas.drawRect(left, top, left + 300f, top + height, fillPaint)
        // Rows of seforim in classic bindings
        val rowHeight = height / 4f
        val bookColors = intArrayOf(
            Color.rgb(120, 40, 40),
            Color.rgb(40, 60, 120),
            Color.rgb(150, 120, 50),
            Color.rgb(50, 90, 50)
        )
        for (row in 0 until 4) {
            var bx = left + 10f
            var i = row
            while (bx < left + 270f) {
                fillPaint.color = bookColors[i % bookColors.size]
                canvas.drawRect(bx, top + row * rowHeight + 8f, bx + 26f, top + (row + 1) * rowHeight - 4f, fillPaint)
                bx += 32f
                i++
            }
        }
    }

    private fun drawPigeon(canvas: Canvas, x: Float, y: Float) {
        canvas.drawLine(x - 18f, y, x, y - 12f, strokePaint)
        canvas.drawLine(x, y - 12f, x + 18f, y, strokePaint)
    }

    private fun drawWallSign(canvas: Canvas, centerX: Float, centerY: Float, text: String) {
        textPaint.textSize = 30f
        val halfWidth = textPaint.measureText(text) / 2 + 20f
        signRect.set(centerX - halfWidth, centerY - 34f, centerX + halfWidth, centerY + 14f)
        fillPaint.color = Color.rgb(250, 245, 220)
        canvas.drawRoundRect(signRect, 8f, 8f, fillPaint)
        strokePaint.color = Color.rgb(120, 100, 60)
        canvas.drawRoundRect(signRect, 8f, 8f, strokePaint)
        textPaint.color = Color.rgb(80, 50, 30)
        canvas.drawText(text, centerX, centerY + 2f, textPaint)
    }
}
