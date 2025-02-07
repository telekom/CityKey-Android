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
 * SPDX-License-Identifier: Apache-2.0
 * License-Filename: LICENSES/Apache-2.0.txt
 */

package com.telekom.citykey.custom.views

import android.content.Context
import android.graphics.Matrix
import android.util.AttributeSet
import android.view.*
import androidx.appcompat.widget.AppCompatImageView
import com.google.android.material.appbar.AppBarLayout
import com.google.android.material.appbar.AppBarLayout.OnOffsetChangedListener
import com.google.android.material.appbar.CollapsingToolbarLayout
import com.telekom.citykey.R
import kotlin.math.roundToLong

/*
    Image has to have id zoomableImage.
    Tag not working because Glide uses it.(Maybe other things uses it as well)
*/

class OscaAppBarLayout(context: Context, attrs: AttributeSet) : AppBarLayout(context, attrs) {
    val isToolbarCollapsed: Boolean get() = _isToolbarCollapsed

    private var collapsingToolbar: CollapsingToolbarLayout? = null
    private var zoomableImageView: AppCompatImageView? = null
    private var _isToolbarCollapsed = false
    private var listener: ((collapsed: Boolean) -> Unit)? = null

    init {
        addOnOffsetChangedListener(
            OnOffsetChangedListener { appBarLayout, verticalOffset ->

                if (appBarLayout.height + verticalOffset < collapsingToolbar?.scrimVisibleHeightTrigger!!) {
                    if (_isToolbarCollapsed) {
                        listener?.invoke(true)
                        _isToolbarCollapsed = false
                    }
                } else if (!_isToolbarCollapsed) {
                    listener?.invoke(false)
                    _isToolbarCollapsed = true
                }

                zoomableImageView?.let { imageView ->
                    if (imageView.drawable != null) {
                        val matrix = Matrix(imageView.imageMatrix)

                        // get zoomableImageView's width and height
                        val dWidth = imageView.drawable.intrinsicWidth
                        val dHeight = imageView.drawable.intrinsicHeight

                        // get view's width and height
                        val vWidth = imageView.width - imageView.paddingLeft - imageView.paddingRight
                        var vHeight = imageView.height - imageView.paddingTop - imageView.paddingBottom

                        val scale: Float
                        var dx = 0f
                        val dy: Float
                        val parallaxMultiplier =
                            (imageView.layoutParams as CollapsingToolbarLayout.LayoutParams).parallaxMultiplier

                        // maintain the zoomableImageView's aspect ratio depending on offset
                        if (dWidth * vHeight > vWidth * dHeight) {
                            vHeight += verticalOffset // calculate view height depending on offset
                            scale = vHeight.toFloat() / dHeight.toFloat() // calculate scale
                            // calculate x value of the center point of scaled drawable:
                            dx = (vWidth - dWidth * scale) * 0.5f
                            // calculate y value by compensating parallaxMultiplier:
                            dy = -verticalOffset * (1 - parallaxMultiplier)
                        } else {
                            scale = vWidth.toFloat() / dWidth.toFloat()
                            dy = (vHeight - dHeight * scale) * 0.5f
                        }

                        // calculate current intrinsic width of the drawable:
                        val currentWidth = (scale * dWidth).roundToLong()

                        // compare view width and drawable width to decide, should we scale more or not
                        if (vWidth <= currentWidth) {
                            matrix.setScale(scale, scale)
                            matrix.postTranslate(dx.roundToLong().toFloat(), dy.roundToLong().toFloat())
                            imageView.imageMatrix = matrix
                        }
                    }
                }
            }
        )
    }

    fun onCollapse(listener: (collapsed: Boolean) -> Unit) {
        this.listener = listener
    }

    override fun onViewAdded(child: View) {
        super.onViewAdded(child)
        if (child is CollapsingToolbarLayout) {
            zoomableImageView = child.findViewById(R.id.zoomableImage)
            collapsingToolbar = child
        }
    }
}
