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
import android.util.AttributeSet
import androidx.appcompat.widget.AppCompatImageView
import kotlin.math.ceil
import kotlin.math.min

/**
 * Image view in e.g. article view: the drawable should be scaled (according to design documents) as follows:
 * - Full width
 * - Height according to maximum as specified and cropped centered vertically
 */
class VerticallyCroppedImageView : AppCompatImageView {

    constructor(context: Context, attrs: AttributeSet, defStyle: Int) : super(context, attrs, defStyle)
    constructor(context: Context) : super(context)
    constructor(context: Context, attrs: AttributeSet?) : super(context, attrs)

    /**
     * Scale image according to design docs
     */
    override fun setFrame(frameLeft: Int, frameTop: Int, frameRight: Int, frameBottom: Int): Boolean {
        val drawable = drawable ?: return super.setFrame(frameLeft, frameTop, frameRight, frameBottom)

        val frameWidth = frameRight - frameLeft
        val scaleFactor = frameWidth.toFloat() / drawable.intrinsicWidth.toFloat()
        val tmpScaledImgHeight = drawable.intrinsicHeight * scaleFactor
        val verticalCropAmount = if (tmpScaledImgHeight > maxHeight) (tmpScaledImgHeight - maxHeight) / 2.0f else 0f

        val tmpMatrix = matrix
        tmpMatrix.setScale(scaleFactor, scaleFactor, .0f, .0f)
        if (verticalCropAmount > 0) {
            tmpMatrix.postTranslate(0f, -verticalCropAmount)
        }
        imageMatrix = tmpMatrix

        return super.setFrame(frameLeft, frameTop, frameRight, frameBottom)
    }

    /**
     * Set height of image view, since it doesn't seem to happen automatically in {@see setFrame()}
     */
    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        val drawable = drawable ?: return super.onMeasure(widthMeasureSpec, heightMeasureSpec)

        val specWidth = MeasureSpec.getSize(widthMeasureSpec)
        val scaleFactor: Float = specWidth.toFloat() / drawable.intrinsicWidth.toFloat()
        val tmpScaledImgHeight = drawable.intrinsicHeight * scaleFactor

        setMeasuredDimension(specWidth, min(maxHeight, ceil(tmpScaledImgHeight).toInt()))
    }
}
