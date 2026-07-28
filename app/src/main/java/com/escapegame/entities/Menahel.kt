package com.escapegame.entities

import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.RectF
import com.escapegame.core.GameConfig
import com.escapegame.levels.Levels
import com.escapegame.model.Platform
import kotlin.random.Random

/**
 * The Menahel: relentless, disappointed, and surprisingly quick for a man
 * who has been standing by the door of the beis medrash since 1987.
 *
 * Chases the talmid horizontally, is affected by gravity and platforms, can be
 * stunned by a well-aimed felafel, and periodically yells mussar in a speech
 * bubble.
 */
class Menahel(private val title: String = "MENAHEL") {
    var x = 500f
        private set
    var y = 500f
        private set

    val width = GameConfig.MENAHEL_WIDTH
    val height = GameConfig.MENAHEL_HEIGHT
    val centerX: Float get() = x + width / 2
    val centerY: Float get() = y + height / 2

    private var chaseSpeed = 2.6f
    private var velocityY = 0f
    private var stunTimer = 0
    val isStunned: Boolean get() = stunTimer > 0

    // Speech bubble state
    private var quote: String? = null
    private var quoteTimer = 0
    private var quoteCooldown = randomQuoteCooldown()

    private val suitPaint = Paint().apply { color = Color.rgb(25, 25, 25); style = Paint.Style.FILL }
    private val shirtPaint = Paint().apply { color = Color.WHITE; style = Paint.Style.FILL }
    private val tiePaint = Paint().apply { color = Color.rgb(139, 0, 0); style = Paint.Style.FILL }
    private val headPaint = Paint().apply { color = Color.rgb(255, 220, 177); style = Paint.Style.FILL }
    private val hatPaint = Paint().apply { color = Color.BLACK; style = Paint.Style.FILL }
    private val beardPaint = Paint().apply { color = Color.WHITE; style = Paint.Style.FILL }
    private val glassesPaint = Paint().apply {
        color = Color.BLACK
        strokeWidth = 3f
        style = Paint.Style.STROKE
    }
    private val eyePaint = Paint().apply { color = Color.WHITE; style = Paint.Style.FILL }
    private val pupilPaint = Paint().apply { color = Color.BLACK; style = Paint.Style.FILL }
    private val shoePaint = Paint().apply { color = Color.BLACK; style = Paint.Style.FILL }
    private val labelPaint = Paint().apply {
        color = Color.BLACK
        textSize = 20f
        textAlign = Paint.Align.CENTER
    }
    private val stunTextPaint = Paint().apply {
        color = Color.rgb(255, 200, 0)
        textSize = 26f
        textAlign = Paint.Align.CENTER
    }
    private val bubbleFillPaint = Paint().apply { color = Color.WHITE; style = Paint.Style.FILL }
    private val bubbleStrokePaint = Paint().apply {
        color = Color.BLACK
        strokeWidth = 3f
        style = Paint.Style.STROKE
    }
    private val bubbleTextPaint = Paint().apply {
        color = Color.BLACK
        textSize = 26f
        textAlign = Paint.Align.CENTER
    }
    private val bubbleRect = RectF()

    fun resetForLevel(startX: Float, floorTopY: Float, speed: Float) {
        x = startX
        y = floorTopY - height
        velocityY = 0f
        chaseSpeed = speed
        stunTimer = 0
        quote = null
        quoteTimer = 0
        quoteCooldown = randomQuoteCooldown()
    }

    fun stun() {
        stunTimer = GameConfig.MENAHEL_STUN_FRAMES
    }

    fun update(targetX: Float, worldWidth: Float, floorTopY: Float, platforms: List<Platform>) {
        if (stunTimer > 0) {
            stunTimer--
        }

        updateQuote()

        velocityY += GameConfig.GRAVITY

        if (!isStunned) {
            val dx = targetX - centerX
            if (kotlin.math.abs(dx) > 5f) {
                x += if (dx > 0) chaseSpeed else -chaseSpeed
            }
        }

        y += velocityY

        for (platform in platforms) {
            if (x + width > platform.x && x < platform.x + platform.width) {
                if (velocityY > 0 && y < platform.y && y + height > platform.y) {
                    y = platform.y - height
                    velocityY = 0f
                }
            }
        }

        if (y + height > floorTopY) {
            y = floorTopY - height
            velocityY = 0f
        }

        if (x < 0f) x = 0f
        if (x + width > worldWidth) x = worldWidth - width
        if (y < 0f) {
            y = 0f
            velocityY = 0f
        }
    }

    private fun updateQuote() {
        if (quoteTimer > 0) {
            quoteTimer--
            if (quoteTimer == 0) {
                quote = null
                quoteCooldown = randomQuoteCooldown()
            }
        } else {
            quoteCooldown--
            if (quoteCooldown <= 0) {
                quote = Levels.menahelQuotes[Random.nextInt(Levels.menahelQuotes.size)]
                quoteTimer = GameConfig.QUOTE_DURATION
            }
        }
    }

    private fun randomQuoteCooldown(): Int =
        Random.nextInt(GameConfig.QUOTE_MIN_COOLDOWN, GameConfig.QUOTE_MAX_COOLDOWN)

    fun draw(canvas: Canvas, worldWidth: Float) {
        val s = width

        // Suit jacket
        canvas.drawRect(x + 7, y + 22, x + s - 7, y + height - 7, suitPaint)
        // Shirt collar and front
        canvas.drawRect(x + 16, y + 22, x + s - 16, y + 35, shirtPaint)
        canvas.drawRect(x + s / 2 - 11, y + 22, x + s / 2 + 11, y + 49, shirtPaint)
        // Tie
        canvas.drawRect(x + s / 2 - 5, y + 28, x + s / 2 + 5, y + 56, tiePaint)
        // Head
        canvas.drawOval(x + 11, y - 28, x + s - 11, y + 21, headPaint)
        // Hat: brim + tall crown
        canvas.drawOval(x + 3, y - 35, x + s - 3, y - 21, hatPaint)
        canvas.drawOval(x + 11, y - 45, x + s - 11, y - 25, hatPaint)
        // White beard
        canvas.drawOval(x + s / 4, y - 7, x + 3 * s / 4, y + 21, beardPaint)
        // Glasses
        canvas.drawCircle(x + s / 2 - 11, y - 11, 8f, glassesPaint)
        canvas.drawCircle(x + s / 2 + 11, y - 11, 8f, glassesPaint)
        canvas.drawLine(x + s / 2 - 3, y - 11, x + s / 2 + 3, y - 11, glassesPaint)
        // Eyes + stern pupils
        canvas.drawCircle(x + s / 2 - 11, y - 11, 5f, eyePaint)
        canvas.drawCircle(x + s / 2 + 11, y - 11, 5f, eyePaint)
        canvas.drawCircle(x + s / 2 - 11, y - 11, 2.5f, pupilPaint)
        canvas.drawCircle(x + s / 2 + 11, y - 11, 2.5f, pupilPaint)
        // Disapproving eyebrows
        canvas.drawLine(x + s / 2 - 17, y - 21, x + s / 2 - 5, y - 17, glassesPaint)
        canvas.drawLine(x + s / 2 + 17, y - 21, x + s / 2 + 5, y - 17, glassesPaint)
        // Shoes
        canvas.drawOval(x + 3, y + height - 11, x + 26, y + height + 4, shoePaint)
        canvas.drawOval(x + s - 26, y + height - 11, x + s - 3, y + height + 4, shoePaint)

        canvas.drawText(title, x + s / 2, y + height + 26, labelPaint)

        if (isStunned) {
            canvas.drawText("*sees stars*", x + s / 2, y - 60, stunTextPaint)
        } else {
            drawSpeechBubble(canvas, worldWidth)
        }
    }

    private fun drawSpeechBubble(canvas: Canvas, worldWidth: Float) {
        val text = quote ?: return
        val textWidth = bubbleTextPaint.measureText(text)
        val padding = 16f
        val bubbleWidth = textWidth + padding * 2
        val bubbleHeight = 48f
        var left = centerX - bubbleWidth / 2
        // Keep the bubble on screen
        if (left < 6f) left = 6f
        if (left + bubbleWidth > worldWidth - 6f) left = worldWidth - 6f - bubbleWidth
        val top = y - 118f
        bubbleRect.set(left, top, left + bubbleWidth, top + bubbleHeight)
        canvas.drawRoundRect(bubbleRect, 14f, 14f, bubbleFillPaint)
        canvas.drawRoundRect(bubbleRect, 14f, 14f, bubbleStrokePaint)
        // Bubble tail
        canvas.drawLine(centerX, top + bubbleHeight, centerX - 8f, y - 50f, bubbleStrokePaint)
        canvas.drawText(text, left + bubbleWidth / 2, top + bubbleHeight - 15f, bubbleTextPaint)
    }
}
