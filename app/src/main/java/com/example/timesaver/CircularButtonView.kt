package com.example.timesaver

import android.content.Context
import android.content.res.Configuration
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Color.WHITE
import android.graphics.Paint
import android.graphics.RectF
import android.graphics.drawable.Drawable
import android.util.AttributeSet
import android.util.TypedValue
import android.view.MotionEvent
import android.view.View
import androidx.appcompat.content.res.AppCompatResources
import androidx.core.graphics.drawable.DrawableCompat
import kotlin.math.atan2
import kotlin.math.cos
import kotlin.math.sin
import kotlin.math.sqrt

class CircularButtonView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : View(context, attrs, defStyleAttr) {

    // Play icon things
    private val playIcon: Drawable = AppCompatResources.getDrawable(context, R.drawable.play_button)!!
    private val pauseIcon: Drawable = AppCompatResources.getDrawable(context, R.drawable.pause_button)!!
    var icon: Drawable = playIcon
    private var isPlaying: Boolean = false

    // Some random activity button colors (make pretty later)
    private val sectionColors = listOf(
        0xFF4cb6ff.toInt(), // Light blue (#4cb6ff)
        0xFFFF5733.toInt(), // Coral (#FF5733)
        0xFFFFC300.toInt(), // Vivid Yellow (#FFC300)
        0xFF900C3F.toInt(), // Burgundy (#900C3F)
        0xFF4CAF50.toInt(), // Green (#4CAF50)
        0xFF9C27B0.toInt(), // Purple (#9C27B0)
        0xFFFF9800.toInt(), // Orange (#FF9800)
        0xFF2196F3.toInt()  // Blue (#2196F3)
    )

    // Space to draw oval
    private val rect = RectF()

    // Paints
    private val paint = Paint(Paint.ANTI_ALIAS_FLAG)
    private val borderPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        style = Paint.Style.STROKE
        strokeWidth = 7f
        color = WHITE
    }
    private val textPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = WHITE
        textSize = 30f
        textAlign = Paint.Align.CENTER
    }

    // Circle dimensions
    private val centerX: Float
        get() = width / 2f
    private val centerY: Float
        get() = height / 2f
    private val radius: Float
        get() = width.coerceAtMost(height) / 2f

    // Inner circle things
    private val innerRadius: Float
        get() = radius * 0.48f
    private val backgroundColor: Int

    /* Outer circle things (USER configures this) */
    var outerCircleSections: Int = 1
        set(value) {
            if(outerCircleSections < 1 || outerCircleSections > 8) {
                throw IllegalArgumentException("The number of sections must be from 1-8.")
            }
            field = value.coerceIn(1, 8)
            sectionLabels = List(field) { "Section ${it + 1}" } // Force section labels
            invalidate()
        }

    // Number of labels doesn't match the number of buttons
    var sectionLabels: List<String> = List(1) { "Section 1" }
        set(value) {
            if (value.size == outerCircleSections) {
                field = value
                invalidate()
            } else {
                throw IllegalArgumentException("The number of section labels must match outerCircleSections (${outerCircleSections})")
            }
        }

    private var onInnerCircleClickListener: (() -> Unit)? = null
    private var onOuterCircleClickListener: ((Int) -> Unit)? = null

    /* End of outer circle things */

    private var lastTouchedSection: Int = -1

    init {
        // get the background color from the theme
        val typedValue = TypedValue()
        context.theme.resolveAttribute(android.R.attr.colorBackground, typedValue, true)
        backgroundColor = typedValue.data
    }

    override fun onConfigurationChanged(newConfig: Configuration) {
        super.onConfigurationChanged(newConfig)
        //updatePlayIconColor()
        invalidate()
    }

    private fun updatePlayIconColor() {
        val color = if (isInDarkMode()) WHITE else Color.BLACK
        DrawableCompat.setTint(icon, color)
    }

    fun setOnInnerCircleClickListener(listener: () -> Unit) {
        onInnerCircleClickListener = listener
    }

    fun setOnOuterCircleClickListener(listener: (Int) -> Unit) {
        onOuterCircleClickListener = listener
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        drawOuterCircle(canvas)
        drawInnerCircle(canvas)
        drawIcon(canvas)
    }

    private fun drawOuterCircle(canvas: Canvas) {
        rect.set(0f, 0f, width.toFloat(), height.toFloat())
        val sweepAngle = 360f / outerCircleSections

        // Draw sections with color
        for (i in 0 until outerCircleSections) {
            paint.color = sectionColors[i % sectionColors.size]
            val startAngle = i * sweepAngle
            canvas.drawArc(rect, startAngle, sweepAngle, true, paint)

            // Draw activity label for section
            drawTextOnArc(canvas, sectionLabels[i], startAngle, sweepAngle)
        }

        // Draw borders (if there's more than one section)
        if (outerCircleSections > 1) {
            for (i in 0 until outerCircleSections) {
                val startAngle = i * sweepAngle
                val dx = cos(Math.toRadians(startAngle.toDouble())).toFloat()
                val dy = sin(Math.toRadians(startAngle.toDouble())).toFloat()

                canvas.drawLine(
                    centerX + innerRadius * dx,
                    centerY + innerRadius * dy,
                    centerX + radius * dx,
                    centerY + radius * dy,
                    borderPaint
                )
            }
        }
    }

    private fun drawInnerCircle(canvas: Canvas) {
        paint.color = backgroundColor
        canvas.drawCircle(centerX, centerY, innerRadius, paint)
        canvas.drawCircle(centerX, centerY, innerRadius, borderPaint)
    }

    private fun drawTextOnArc(canvas: Canvas, text: String, startAngle: Float, sweepAngle: Float) {
        val textRadius = (radius + innerRadius) / 2

        // Find mid-angle for each section
        val adjustedMidAngle = if (outerCircleSections == 1) {
            Math.toRadians(90.0) // 90 degrees (top of the circle)
        } else {
            Math.toRadians((startAngle + sweepAngle / 2).toDouble())
        }

        val x = (centerX + textRadius * cos(adjustedMidAngle)).toFloat()
        val y = (centerY + textRadius * sin(adjustedMidAngle)).toFloat()

        canvas.save()
        canvas.translate(x, y)

        var rotationAngle = Math.toDegrees(adjustedMidAngle).toFloat() + 90f
        if (rotationAngle > 90 && rotationAngle < 270) {
            rotationAngle += 180f
        }

        // If one section, make sure text is horizontal
        if (outerCircleSections == 1) {
            rotationAngle = 0f
        }

        // Rotate position at section angle
        canvas.rotate(rotationAngle)

        // Center the vertical position of text
        val textHeight = textPaint.descent() - textPaint.ascent()
        val textOffset = textHeight / 2 - textPaint.descent()

        canvas.drawText(text, 0f, textOffset, textPaint)
        canvas.restore()
    }

    private fun drawIcon(canvas: Canvas) {
        updatePlayIconColor() // set color according to light/dark theme

        // Draw the play icon in inner circle
        val iconSize = innerRadius * 0.75f
        val left = centerX - iconSize / 2
        val top = centerY - iconSize / 2
        val right = centerX + iconSize / 2 // looked a bit off-center
        val bottom = centerY + iconSize / 2

        icon.setBounds(left.toInt(), top.toInt(), right.toInt(), bottom.toInt())
        icon.draw(canvas)
    }


    override fun onTouchEvent(event: MotionEvent): Boolean {
        when (event.action) {
            MotionEvent.ACTION_DOWN -> {
                val dx = event.x - centerX
                val dy = event.y - centerY
                val distance = sqrt((dx * dx + dy * dy).toDouble()).toFloat()

                if (distance <= innerRadius) {
                    lastTouchedSection = -1
                    performClick()
                } else if (distance <= radius) {
                    val angle = Math.toDegrees(atan2(dy.toDouble(), dx.toDouble())).toFloat()
                    lastTouchedSection = ((angle + 360) % 360 / (360f / outerCircleSections)).toInt()
                    performClick()
                }
                return true
            }
        }
        return super.onTouchEvent(event)
    }

    override fun performClick(): Boolean {
        if (super.performClick()) return true

        if (lastTouchedSection == -1) {
            onInnerCircleClickListener?.invoke()
            changeButton()
        } else {
            onOuterCircleClickListener?.invoke(lastTouchedSection) // starts at 0
        }

        return true
    }

    private fun isInDarkMode(): Boolean {
        return (context.resources.configuration.uiMode and Configuration.UI_MODE_NIGHT_MASK) == Configuration.UI_MODE_NIGHT_YES
    }

    fun changeButton() {
        icon = if (isPlaying) {
            playIcon
        } else {
            pauseIcon
        }
        isPlaying = !isPlaying
        updatePlayIconColor()
        invalidate()
    }

    fun isPlaying(): Boolean {
        return isPlaying
    }
}
