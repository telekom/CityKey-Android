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
