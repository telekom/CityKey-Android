/**
 * Copyright (C) 2025 Deutsche Telekom AG
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 * SPDX-FileCopyrightText: 2025 Deutsche Telekom AG
 * SPDX-License-Identifier: Apache-2.0
 * License-Filename: LICENSES/Apache-2.0.txt
 */

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
