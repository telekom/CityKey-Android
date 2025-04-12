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
