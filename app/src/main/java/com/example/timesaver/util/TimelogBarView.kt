package com.example.timesaver.util

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.util.AttributeSet
import android.view.View
import androidx.annotation.ColorInt

class TimelogBarView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : View(context, attrs, defStyleAttr) {
    private var ratio: Float = 0f
    private var barColor: Int = Color.BLUE
    private val paint = Paint(Paint.ANTI_ALIAS_FLAG)

    fun setRatio(ratio: Float) {
        this.ratio = ratio.coerceIn(0f, 1f)
        invalidate()
    }

    fun setBarColor(@ColorInt color: Int) {
        barColor = color
        invalidate()
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        paint.color = barColor
        val barWidth = width * ratio
        canvas.drawRect(0f, 0f, barWidth, height.toFloat(), paint)
    }
}