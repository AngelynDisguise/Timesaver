package com.example.timesaver

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.RectF
import android.util.AttributeSet
import android.util.TypedValue
import android.view.MotionEvent
import android.view.View
import kotlin.math.atan2
import kotlin.math.cos
import kotlin.math.sin
import kotlin.math.sqrt

class CircularButtonView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : View(context, attrs, defStyleAttr) {

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

    private val paint = Paint(Paint.ANTI_ALIAS_FLAG)
    private val rect = RectF()
    private val borderPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        style = Paint.Style.STROKE
        strokeWidth = 2f
        color = Color.WHITE
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

    private val borderColor: Int = Color.WHITE
    private val borderWidth: Float = 7f
    var outerCircleSections: Int = 1
        set(value) {
            field = value.coerceIn(1, 8)
            invalidate()
        }

    private var lastTouchedSection: Int = -1

    private val backgroundColor: Int
    //private val outerCircleColor: Int = 0xFF4cb6ff.toInt()

    init {
        // get the background color from the theme
        val typedValue = TypedValue()
        context.theme.resolveAttribute(android.R.attr.colorBackground, typedValue, true)
        backgroundColor = typedValue.data
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

        // Draw inner circle with background color
        paint.color = backgroundColor
        canvas.drawCircle(centerX, centerY, innerRadius, paint)

        for (i in 0 until outerCircleSections) {
            val startAngle = i * (360f / outerCircleSections)
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
        } else {
            onOuterCircleClickListener?.invoke(lastTouchedSection)
        }

        return true
    }
}
