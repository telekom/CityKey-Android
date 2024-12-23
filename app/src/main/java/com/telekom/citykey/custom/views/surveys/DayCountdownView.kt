package com.telekom.citykey.custom.views.surveys

import android.animation.ValueAnimator
import android.content.Context
import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.Path
import android.graphics.RectF
import android.util.AttributeSet
import android.view.View
import androidx.annotation.ColorInt
import com.telekom.citykey.R
import kotlin.math.min

class DayCountdownView(context: Context, attrs: AttributeSet) : View(context, attrs) {

    companion object {
        private const val MAX_SWEEP = 359.9999f
        private const val DEFAULT_RADIUS = 20f
    }

    private var radius = 0f

    private val progressBGPaint = Paint().apply {
        isDither = true
        style = Paint.Style.FILL
        strokeJoin = Paint.Join.ROUND
        strokeCap = Paint.Cap.ROUND
        isAntiAlias = true
        shader = null
    }

    private val progressPaint = Paint().apply {
        isDither = true
        style = Paint.Style.FILL
        strokeJoin = Paint.Join.ROUND
        strokeCap = Paint.Cap.ROUND
        isAntiAlias = true
        shader = null
    }

    private val path = Path()

    private val outerCircle = RectF()
    private val innerCircle = RectF()

    private var progressSweep: Float = 0f
    private var creditAnimation: ValueAnimator? = null

    init {
        val a = context.theme.obtainStyledAttributes(
            attrs, R.styleable.DayCountdownView, 0, 0
        )

        try {
            radius = a.getDimension(R.styleable.DayCountdownView_progressRadius, DEFAULT_RADIUS)
        } finally {
            a.recycle()
        }

        progressBGPaint.color = context.getColor(R.color.separatorLight)
        progressPaint.strokeWidth = radius / 14.0f

        var adjust = .038f * radius
        outerCircle.set(adjust, adjust, radius * 2 - adjust, radius * 2 - adjust)

        adjust = .18f * radius
        innerCircle.set(adjust, adjust, radius * 2 - adjust, radius * 2 - adjust)
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)

        drawDonut(canvas, progressBGPaint, MAX_SWEEP)
        drawDonut(canvas, progressPaint, progressSweep)
    }

    private fun drawDonut(canvas: Canvas, paint: Paint, sweep: Float) {
        path.reset()
        path.arcTo(outerCircle, -90f, sweep, false)
        path.arcTo(innerCircle, sweep - 90, -sweep, false)
        path.close()
        canvas.drawPath(path, paint)
    }

    fun setColor(@ColorInt color: Int) {
        progressPaint.color = color
    }

    fun setValues(max: Int, daysLeft: Int) {
        creditAnimation?.cancel()
        creditAnimation = ValueAnimator.ofFloat(progressSweep, calculateSweep(max - daysLeft, max))
            .apply {
                addUpdateListener {
                    progressSweep = it.animatedValue as Float
                    invalidate()
                }
                start()
            }
    }

    private fun calculateSweep(progress: Int, max: Int) =
        min(if (progress < 0) 0f else MAX_SWEEP * progress / max.toFloat(), MAX_SWEEP)

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec)

        val desiredWidth = radius.toInt() * 2
        val desiredHeight = radius.toInt() * 2

        val widthMode = MeasureSpec.getMode(widthMeasureSpec)
        val widthSize = MeasureSpec.getSize(widthMeasureSpec)
        val heightMode = MeasureSpec.getMode(heightMeasureSpec)
        val heightSize = MeasureSpec.getSize(heightMeasureSpec)

        val width = when (widthMode) {
            MeasureSpec.EXACTLY -> widthSize
            MeasureSpec.AT_MOST -> // wrap content
                min(desiredWidth, widthSize)
            else -> desiredWidth
        }

        val height = when (heightMode) {
            MeasureSpec.EXACTLY -> heightSize
            MeasureSpec.AT_MOST -> min(desiredHeight, heightSize)
            else -> desiredHeight
        }

        setMeasuredDimension(width, height)
    }
}
