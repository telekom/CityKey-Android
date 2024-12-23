package com.telekom.citykey.custom.views.inputfields

import android.content.Context
import android.graphics.Canvas
import android.graphics.Paint
import android.util.AttributeSet
import com.google.android.material.textfield.TextInputEditText
import com.telekom.citykey.R
import com.telekom.citykey.utils.isDarkMode

class PinInputEditText(context: Context, attrs: AttributeSet) : TextInputEditText(context, attrs) {

    companion object {
        const val XML_NAMESPACE_ANDROID = "http://schemas.android.com/apk/res/android"
        const val UNDERLINE_ERROR_COLOR = 0xffff5f5f.toInt()
        const val UNDERLINE_DARK_COLOR = 0xff262626.toInt()
        const val UNDERLINE_BRIGHT_COLOR = 0xffd8d8d8.toInt()
    }

    private var spaceBetween = 6f
    private var pinSubLineWidth = 0f
    private var horizontalInputPadding = 36f
    private var lineSpacing = 8f
    private var dotRadius = 6f

    private val filledDotPaint = Paint().apply {
        isAntiAlias = true
        color = context.getColor(R.color.oscaColor)
        style = Paint.Style.FILL
    }

    private val strokeDotPaint = Paint(paint).apply {
        color = context.getColor(R.color.oscaColor)
        style = Paint.Style.STROKE
        strokeWidth = 2f * context.resources.displayMetrics.density
    }

    private val textPaint = Paint(paint).apply {
        color = context.getColor(R.color.onSurface)
    }

    var maxLength: Int = 0

    var isHideContent = false
        set(value) {
            field = value
            invalidate()
        }

    var hasError: Boolean = false
        set(value) {
            field = value
            invalidate()
        }

    init {
        setBackgroundResource(0)
        setTextColor(context.getColor(R.color.onSurface))

        val multi = context.resources.displayMetrics.density
        spaceBetween *= multi
        lineSpacing *= multi
        dotRadius *= multi
        horizontalInputPadding *= multi
    }

    override fun onDraw(canvas: Canvas) {
        val availableWidth = width - paddingLeft - paddingRight
        pinSubLineWidth = (availableWidth - spaceBetween * (maxLength - 1)) / maxLength

        var startX = paddingLeft.toFloat()
        val bottom = height - paddingBottom.toFloat()

        for (position in 0 until maxLength) {
            if (isHideContent) {
                val centerX =
                    (availableWidth / maxLength) * position + (availableWidth / maxLength / 2) + paddingLeft
                val centerY = height / 2f
                val paint = if (position < text.toString().length) filledDotPaint else strokeDotPaint

                paint.color = if (hasError) UNDERLINE_ERROR_COLOR else context.getColor(R.color.oscaColor)

                canvas.drawCircle(centerX.toFloat(), centerY, dotRadius, paint)
            } else {
                canvas.drawLine(startX, bottom, pinSubLineWidth + startX, bottom, getUnderlinePaint(position))

                if (text!!.length > position) {
                    val textXStart =
                        startX + pinSubLineWidth / 2 - (paint.measureText(text.toString()[position].toString()) / 2)
                    canvas.drawText(
                        text.toString(),
                        position,
                        position + 1,
                        textXStart,
                        bottom - lineSpacing,
                        textPaint
                    )
                }

                startX += pinSubLineWidth + spaceBetween
            }
        }
    }

    override fun onSelectionChanged(selStart: Int, selEnd: Int) {
        text?.let {
            if (selStart != it.length || selEnd != it.length) {
                setSelection(it.length, it.length)
                return
            }
        }
        super.onSelectionChanged(selStart, selEnd)
    }

    private fun getUnderlinePaint(position: Int) = Paint(paint).apply {
        val isDark = resources.isDarkMode

        color = when {
            hasError -> UNDERLINE_ERROR_COLOR
            position == text!!.length -> if (isDark) UNDERLINE_BRIGHT_COLOR else UNDERLINE_DARK_COLOR
            else -> if (isDark) UNDERLINE_DARK_COLOR else UNDERLINE_BRIGHT_COLOR
        }
        strokeWidth = if (position == text!!.length) 5f else 2f
    }
}
