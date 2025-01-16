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

package com.telekom.citykey.custom

import android.graphics.Canvas
import android.graphics.Paint
import androidx.annotation.ColorInt
import androidx.recyclerview.widget.RecyclerView

class AnimateableDecoration(@ColorInt color: Int = 0, width: Float = 0f) :
    RecyclerView.ItemDecoration() {

    private val paint = Paint()
    private val alpha: Int

    init {
        if (color != 0) {
            paint.color = color
            paint.strokeWidth = width
        }
        alpha = paint.alpha
    }

    override fun onDraw(c: Canvas, parent: RecyclerView, state: RecyclerView.State) {

        for (i in 0 until parent.childCount) {
            val child = parent.getChildAt(i)
            val params = child.layoutParams as RecyclerView.LayoutParams
            val position = params.absoluteAdapterPosition
            val offset = paint.strokeWidth / 2

            // and finally draw the separator
            if (position < state.itemCount) {
                // apply alpha to support animations
                paint.alpha = (child.alpha * alpha).toInt()
                val positionY = child.bottom + offset + child.translationY

                c.drawLine(
                    parent.left.toFloat(),
                    positionY,
                    parent.right.toFloat(),
                    positionY,
                    paint
                )
            }
        }
    }
}
