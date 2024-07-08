package com.example.timesaver

import android.content.Context
import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.RectF
import android.util.AttributeSet
import android.view.MotionEvent
import android.view.View
import kotlin.math.atan2

class CircularButtonView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : View(context, attrs, defStyleAttr) {

    private val paint = Paint(Paint.ANTI_ALIAS_FLAG)
    private val rect = RectF()
    private val centerX: Float
        get() = width / 2f
    private val centerY: Float
        get() = height / 2f
    private val radius: Float
        get() = width.coerceAtMost(height) / 2f

    private val innerRadius: Float
        get() = radius * 0.7f

    private var onInnerCircleClickListener: (() -> Unit)? = null
    private var onOuterCircleClickListener: ((Int) -> Unit)? = null

    private val outerCircleSections = 6 // Number of clickable sections in the outer circle

    fun setOnInnerCircleClickListener(listener: () -> Unit) {
        onInnerCircleClickListener = listener
    }

    fun setOnOuterCircleClickListener(listener: (Int) -> Unit) {
        onOuterCircleClickListener = listener
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        // The background drawable will handle the rendering of the circles
    }

    private var lastTouchedSection: Int = -1

    override fun onTouchEvent(event: MotionEvent): Boolean {
        when (event.action) {
            MotionEvent.ACTION_DOWN -> {
                val dx = event.x - centerX
                val dy = event.y - centerY
                val distance = Math.sqrt((dx * dx + dy * dy).toDouble()).toFloat()

                if (distance <= innerRadius) {
                    lastTouchedSection = -1
                    performClick()
                } else if (distance <= radius) {
                    val angle = Math.toDegrees(atan2(dy.toDouble(), dx.toDouble()).toDouble()).toFloat()
                    lastTouchedSection = ((angle + 360) % 360 / (360 / outerCircleSections)).toInt()
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
