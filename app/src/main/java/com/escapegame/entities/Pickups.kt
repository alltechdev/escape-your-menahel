package com.escapegame.entities

import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.RectF
import com.escapegame.model.PowerUpType
import kotlin.math.sin

/** Shared pickup behavior: a bobbing collectible with circle collision. */
abstract class Pickup(val baseX: Float, val baseY: Float) {
    var collected = false
        protected set

    protected var animFrame = 0
    protected val bobOffset: Float get() = sin(animFrame * 0.08).toFloat() * 8f

    fun update() {
        animFrame++
    }

    /** True (once) if the given center point comes within [pickupDistance]. */
    fun tryCollect(centerX: Float, centerY: Float, pickupDistance: Float): Boolean {
        if (collected) return false
        val dx = baseX - centerX
        val dy = (baseY + bobOffset) - centerY
        if (dx * dx + dy * dy < pickupDistance * pickupDistance) {
            collected = true
            return true
        }
        return false
    }

    abstract fun draw(canvas: Canvas)
}

/** A rugelach: 100 points of pure nachas. */
class Rugelach(x: Float, y: Float) : Pickup(x, y) {
    private val doughPaint = Paint().apply { color = Color.rgb(200, 150, 90); style = Paint.Style.FILL }
    private val swirlPaint = Paint().apply {
        color = Color.rgb(120, 70, 30)
        strokeWidth = 4f
        style = Paint.Style.STROKE
    }
    private val glowPaint = Paint().apply {
        color = Color.argb(70, 255, 220, 120)
        style = Paint.Style.FILL
    }

    override fun draw(canvas: Canvas) {
        if (collected) return
        val y = baseY + bobOffset
        canvas.drawCircle(baseX, y, 22f, glowPaint)
        canvas.drawCircle(baseX, y, 15f, doughPaint)
        canvas.drawCircle(baseX, y, 10f, swirlPaint)
        canvas.drawCircle(baseX, y, 4f, swirlPaint)
    }
}

/** A power-up crate of one of the [PowerUpType]s. */
class PowerUp(x: Float, y: Float, val type: PowerUpType) : Pickup(x, y) {
    private val boxRect = RectF()
    private val strokePaint = Paint().apply {
        color = Color.BLACK
        strokeWidth = 3f
        style = Paint.Style.STROKE
    }
    private val coffeePaint = Paint().apply { color = Color.rgb(110, 70, 40); style = Paint.Style.FILL }
    private val seltzerPaint = Paint().apply { color = Color.rgb(80, 200, 120); style = Paint.Style.FILL }
    private val kugelPaint = Paint().apply { color = Color.rgb(240, 190, 60); style = Paint.Style.FILL }
    private val whitePaint = Paint().apply { color = Color.WHITE; style = Paint.Style.FILL }
    private val glowPaint = Paint().apply {
        color = Color.argb(70, 160, 220, 255)
        style = Paint.Style.FILL
    }

    override fun draw(canvas: Canvas) {
        if (collected) return
        val y = baseY + bobOffset
        canvas.drawCircle(baseX, y, 30f, glowPaint)
        when (type) {
            PowerUpType.COFFEE -> {
                // A serious cup of yeshivishe coffee
                boxRect.set(baseX - 14, y - 12, baseX + 10, y + 14)
                canvas.drawRect(boxRect, coffeePaint)
                canvas.drawRect(boxRect, strokePaint)
                // Handle
                canvas.drawCircle(baseX + 15, y + 1, 7f, strokePaint)
                // Steam
                canvas.drawLine(baseX - 8, y - 16, baseX - 5, y - 26, strokePaint)
                canvas.drawLine(baseX + 2, y - 16, baseX + 5, y - 26, strokePaint)
            }
            PowerUpType.SELTZER -> {
                // The classic green seltzer bottle
                boxRect.set(baseX - 9, y - 16, baseX + 9, y + 16)
                canvas.drawRect(boxRect, seltzerPaint)
                canvas.drawRect(boxRect, strokePaint)
                // Cap
                canvas.drawRect(baseX - 5, y - 24, baseX + 5, y - 16, strokePaint)
                // Bubbles
                canvas.drawCircle(baseX - 3, y - 4, 2f, whitePaint)
                canvas.drawCircle(baseX + 4, y + 3, 2f, whitePaint)
                canvas.drawCircle(baseX, y + 9, 2f, whitePaint)
            }
            PowerUpType.KUGEL -> {
                // A square of Bubby's potato kugel, cut from the corner piece
                boxRect.set(baseX - 15, y - 12, baseX + 15, y + 12)
                canvas.drawRect(boxRect, kugelPaint)
                canvas.drawRect(boxRect, strokePaint)
                // Crispy grid on top
                canvas.drawLine(baseX - 15, y, baseX + 15, y, strokePaint)
                canvas.drawLine(baseX - 5, y - 12, baseX - 5, y + 12, strokePaint)
                canvas.drawLine(baseX + 5, y - 12, baseX + 5, y + 12, strokePaint)
            }
        }
    }
}
