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
 * In accordance with Sections 4 and 6 of the License, the following exclusions apply:
 *
 *  1. Trademarks & Logos – The names, logos, and trademarks of the Licensor are not covered by this License and may not be used without separate permission.
 *  2. Design Rights – Visual identities, UI/UX designs, and other graphical elements remain the property of their respective owners and are not licensed under the Apache License 2.0.
 *  3: Non-Coded Copyrights – Documentation, images, videos, and other non-software materials require separate authorization for use, modification, or distribution.
 *
 * These elements are not considered part of the licensed Work or Derivative Works unless explicitly agreed otherwise. All elements must be altered, removed, or replaced before use or distribution. All rights to these materials are reserved, and Contributor accepts no liability for any infringing use. By using this repository, you agree to indemnify and hold harmless Contributor against any claims, costs, or damages arising from your use of the excluded elements.
 *
 * SPDX-FileCopyrightText: 2025 Deutsche Telekom AG
 * SPDX-License-Identifier: Apache-2.0 AND LicenseRef-Deutsche-Telekom-Brand
 * License-Filename: LICENSES/Apache-2.0.txt LICENSES/LicenseRef-Deutsche-Telekom-Brand.txt
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
