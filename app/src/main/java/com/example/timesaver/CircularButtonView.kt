package com.example.timesaver

import android.content.Context
import android.content.res.Configuration
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Color.WHITE
import android.graphics.ColorFilter
import android.graphics.ColorMatrix
import android.graphics.ColorMatrixColorFilter
import android.graphics.Paint
import android.graphics.RectF
import android.graphics.drawable.Drawable
import android.util.AttributeSet
import android.util.TypedValue
import android.view.MotionEvent
import android.view.View
import androidx.appcompat.content.res.AppCompatResources
import androidx.core.content.res.ResourcesCompat
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

    private val playIcon: Drawable = AppCompatResources.getDrawable(context, R.drawable.play_button)!!
    private val pauseIcon: Drawable = AppCompatResources.getDrawable(context, R.drawable.pause_button)!!
    private var icon: Drawable = playIcon
    private var isPlaying: Boolean = false

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

    private val rect = RectF()

    private val paint = Paint(Paint.ANTI_ALIAS_FLAG)
    private val borderPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        style = Paint.Style.STROKE
        strokeWidth = 7f
        color = WHITE
    }

    private val centerX: Float
        get() = width / 2f
    private val centerY: Float
        get() = height / 2f
    private val radius: Float
        get() = width.coerceAtMost(height) / 2f

    private val innerRadius: Float
        get() = radius * 0.48f

    private var onInnerCircleClickListener: (() -> Unit)? = null
    private var onOuterCircleClickListener: ((Int) -> Unit)? = null
    var outerCircleSections: Int = 1
        set(value) {
            field = value.coerceIn(1, 8)
            invalidate()
        }

    private var lastTouchedSection: Int = -1

    private val backgroundColor: Int

    init {
        // get the background color from the theme
        val typedValue = TypedValue()
        context.theme.resolveAttribute(android.R.attr.colorBackground, typedValue, true)
        backgroundColor = typedValue.data

        updatePlayIconColor()
    }

    override fun onConfigurationChanged(newConfig: Configuration) {
        super.onConfigurationChanged(newConfig)
        updatePlayIconColor()
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

        // Draw outer circle
//        paint.color = outerCircleColor
//        canvas.drawCircle(centerX, centerY, radius, paint)

        // Draw outer circle sections with colors
        rect.set(0f, 0f, width.toFloat(), height.toFloat())
        val sweepAngle = 360f / outerCircleSections

        for (i in 0 until outerCircleSections) {
            paint.color = sectionColors[i % sectionColors.size]
            val startAngle = i * sweepAngle
            canvas.drawArc(rect, startAngle, sweepAngle, true, paint)
        }

        // Draw inner circle with background color and border
        paint.color = backgroundColor
        canvas.drawCircle(centerX, centerY, innerRadius, paint)
        canvas.drawCircle(centerX, centerY, innerRadius, borderPaint)

        // Draw outer circle section borders (if there's more than one section)
        if (outerCircleSections > 1) {
            for (i in 0 until outerCircleSections) {
                val startAngle = i * sweepAngle
                val dx = Math.cos(Math.toRadians(startAngle.toDouble())).toFloat()
                val dy = Math.sin(Math.toRadians(startAngle.toDouble())).toFloat()

                canvas.drawLine(
                    centerX + innerRadius * dx,
                    centerY + innerRadius * dy,
                    centerX + radius * dx,
                    centerY + radius * dy,
                    borderPaint
                )
            }
        }

        // Draw the play icon
        val iconSize = innerRadius * 0.75f
        val left = centerX - iconSize / 2
        val top = centerY - iconSize / 2
        val right = 20 + centerX + iconSize / 2 // looked a bit off-center
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
            onOuterCircleClickListener?.invoke(lastTouchedSection)
        }

        return true
    }

    private fun isInDarkMode(): Boolean {
        return (context.resources.configuration.uiMode and Configuration.UI_MODE_NIGHT_MASK) == Configuration.UI_MODE_NIGHT_YES
    }

    private fun changeButton() {
        icon = if (isPlaying) {
            playIcon
        } else {
            pauseIcon
        }
        isPlaying = !isPlaying
        invalidate()
    }

    fun isPlaying(): Boolean {
        return isPlaying
    }
}
