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

package com.telekom.citykey.utils

import android.graphics.Rect
import android.os.Bundle
import android.text.Spanned
import android.text.style.ClickableSpan
import android.view.accessibility.AccessibilityEvent
import android.widget.TextView
import androidx.core.view.accessibility.AccessibilityNodeInfoCompat
import androidx.customview.widget.ExploreByTouchHelper

/**
 * An accessibility delegate that allows [ClickableSpan] to be focused and clicked by accessibility services.
 */
class LinkAccessibilityHelper(
    private val linkTextView: TextView,
    private val linkAccessibilityId: String? = null
) : ExploreByTouchHelper(linkTextView) {

    private val viewRect = Rect()

    override fun getVirtualViewAt(x: Float, y: Float): Int {
        val text = linkTextView.text
        if (text is Spanned) {
            val offset = getOffsetForPosition(linkTextView, x, y)
            val linkSpans = text.getSpans(offset, offset, ClickableSpan::class.java)
            if (linkSpans.size == 1) {
                val linkSpan = linkSpans[0]
                return text.getSpanStart(linkSpan)
            }
        }
        return INVALID_ID
    }

    override fun getVisibleVirtualViews(virtualViewIds: MutableList<Int>) {
        val text = linkTextView.text
        if (text is Spanned) {
            val linkSpans = text.getSpans(0, text.length, ClickableSpan::class.java)
            for (span in linkSpans) {
                virtualViewIds.add(text.getSpanStart(span))
            }
        }
    }

    override fun onPopulateEventForVirtualView(virtualViewId: Int, event: AccessibilityEvent) {
        val span = getSpanForOffset(virtualViewId)
        if (span != null) {
            val spannedTextAccessibility = getTextForSpan(span).toString() + "link."
            event.contentDescription = spannedTextAccessibility
        } else {
            event.contentDescription = linkTextView.text
        }
    }

    override fun onPopulateNodeForVirtualView(
        virtualViewId: Int,
        info: AccessibilityNodeInfoCompat
    ) {
        linkAccessibilityId?.let { info.viewIdResourceName = it }
        val span = getSpanForOffset(virtualViewId)
        if (span != null) {
            info.contentDescription = getTextForSpan(span).toString()
        } else {
            info.contentDescription = linkTextView.text
        }
        info.isFocusable = true
        info.isClickable = true
        getBoundsForSpan(span, viewRect)
        if (viewRect.isEmpty) {
            viewRect[0, 0, 1] = 1
        }
        info.setBoundsInParent(viewRect)
        info.addAction(AccessibilityNodeInfoCompat.ACTION_CLICK)
    }

    override fun onPerformActionForVirtualView(
        virtualViewId: Int,
        action: Int,
        arguments: Bundle?
    ): Boolean {
        if (action == AccessibilityNodeInfoCompat.ACTION_CLICK) {
            val span = getSpanForOffset(virtualViewId)
            if (span != null) {
                span.onClick(linkTextView)
                return true
            }
        }
        return false
    }

    private fun getSpanForOffset(offset: Int): ClickableSpan? {
        val text = linkTextView.text
        if (text is Spanned) {
            val spans = text.getSpans(offset, offset, ClickableSpan::class.java)
            if (spans.size == 1) {
                return spans[0]
            }
        }
        return null
    }

    private fun getTextForSpan(span: ClickableSpan): CharSequence {
        val text = linkTextView.text
        if (text is Spanned) {
            return text.subSequence(
                text.getSpanStart(span),
                text.getSpanEnd(span)
            )
        }
        return text
    }

    // Find the bounds of a span. If it spans multiple lines, it will only return the bounds for the
    // section on the first line.
    private fun getBoundsForSpan(span: ClickableSpan?, outRect: Rect): Rect {
        val text = linkTextView.text
        outRect.setEmpty()
        if (text is Spanned) {
            val layout = linkTextView.layout
            if (layout != null) {
                val spanStart = text.getSpanStart(span)
                val spanEnd = text.getSpanEnd(span)
                val xStart = layout.getPrimaryHorizontal(spanStart)
                val xEnd = layout.getPrimaryHorizontal(spanEnd)
                val lineStart = layout.getLineForOffset(spanStart)
                val lineEnd = layout.getLineForOffset(spanEnd)
                layout.getLineBounds(lineStart, outRect)
                if (lineEnd == lineStart) {
                    // If the span is on a single line, adjust both the left and right bounds
                    // so outrect is exactly bounding the span.
                    outRect.left = xStart.coerceAtMost(xEnd).toInt()
                    outRect.right = xStart.coerceAtLeast(xEnd).toInt()
                } else {
                    // If the span wraps across multiple lines, only use the first line (as returned
                    // by layout.getLineBounds above), and adjust the "start" of outrect to where
                    // the span starts, leaving the "end" of outrect at the end of the line.
                    // ("start" being left for LTR, and right for RTL)
                    if (layout.getParagraphDirection(lineStart) == android.text.Layout.DIR_RIGHT_TO_LEFT) {
                        outRect.right = xStart.toInt()
                    } else {
                        outRect.left = xStart.toInt()
                    }
                }

                // Offset for padding
                outRect.offset(linkTextView.totalPaddingLeft, linkTextView.totalPaddingTop)
            }
        }
        return outRect
    }

    // Compat implementation of TextView#getOffsetForPosition()
    private fun getOffsetForPosition(view: TextView, x: Float, y: Float): Int {
        if (view.layout == null) {
            return -1
        }
        val line = getLineAtCoordinate(view, y)
        return getOffsetAtCoordinate(view, line, x)
    }

    private fun convertToLocalHorizontalCoordinate(view: TextView, x: Float): Float {
        var coordinateX = x
        coordinateX -= view.totalPaddingLeft.toFloat()
        // Clamp the position to inside of the view.
        coordinateX = 0.0f.coerceAtLeast(coordinateX)
        coordinateX = (view.width - view.totalPaddingRight - 1).toFloat().coerceAtMost(coordinateX)
        coordinateX += view.scrollX.toFloat()
        return coordinateX
    }

    private fun getLineAtCoordinate(view: TextView, y: Float): Int {
        var coordinateY = y
        coordinateY -= view.totalPaddingTop.toFloat()
        // Clamp the position to inside of the view.
        coordinateY = 0.0f.coerceAtLeast(coordinateY)
        coordinateY = (view.height - view.totalPaddingBottom - 1).toFloat().coerceAtMost(coordinateY)
        coordinateY += view.scrollY.toFloat()
        return view.layout.getLineForVertical(coordinateY.toInt())
    }

    private fun getOffsetAtCoordinate(view: TextView, line: Int, x: Float): Int {
        var coordinateX = x
        coordinateX = convertToLocalHorizontalCoordinate(view, coordinateX)
        return view.layout.getOffsetForHorizontal(line, coordinateX)
    }
}
