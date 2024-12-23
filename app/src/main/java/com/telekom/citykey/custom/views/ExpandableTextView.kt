package com.telekom.citykey.custom.views

import android.content.Context
import android.graphics.Canvas
import android.text.Editable
import android.util.AttributeSet
import com.google.android.material.textview.MaterialTextView
import com.telekom.citykey.R
import com.telekom.citykey.utils.EmptyTextWatcher
import kotlin.math.ceil

class ExpandableTextView(context: Context, attrs: AttributeSet) : MaterialTextView(context, attrs) {

    private var collapsedLines: Int = 0
    private var expandedLines: Int = 0
    private var collapsedHeight: Int = 0
    private var expandedHeight: Int = 0
    private var collapsed = true
    private var initialized = false

    init {
        val typeArray = context.obtainStyledAttributes(attrs, R.styleable.ExpandableTextView)
        collapsedLines = typeArray.getInt(R.styleable.ExpandableTextView_collapsedLines, 5)
        typeArray.recycle()
    }

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec)
        if (!initialized) {
            collapsedHeight = getHeight(collapsedLines)
            setMeasuredDimension(widthMeasureSpec, collapsedHeight)
        }
    }

    override fun onLayout(changed: Boolean, left: Int, top: Int, right: Int, bottom: Int) {
        super.onLayout(changed, left, top, right, bottom)
        setExpandedLines(lineCount)
        init()
    }

    fun init() {
        if (!initialized) {
            initialized = true

            this.addTextChangedListener(object : EmptyTextWatcher() {
                override fun afterTextChanged(s: Editable?) {
                    setExpandedLines(lineCount)
                }
            })
        }
    }

    fun updateState() {
        collapsed = !collapsed
        invalidate()
    }

    val isCollapsed: Boolean get() = collapsed

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        if (!collapsed && height < expandedHeight) {
            height = expandedHeight
            invalidate()
        }
        if (collapsed && height > collapsedHeight) {
            height = collapsedHeight
            invalidate()
        }
    }

    private fun setExpandedLines(expandedLines: Int) {
        this.expandedLines = expandedLines
        expandedHeight = getHeight(expandedLines)
    }

    private fun getHeight(linesCount: Int) =
        ceil(linesCount * (lineHeight + lineSpacingExtra) + paddingBottom + paddingTop + lastBaselineToBottomHeight)
            .toInt()
}
