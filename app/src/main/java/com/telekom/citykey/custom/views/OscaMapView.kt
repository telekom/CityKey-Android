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
import android.util.AttributeSet
import android.view.MotionEvent
import android.view.View
import android.view.ViewGroup
import android.view.accessibility.AccessibilityEvent
import androidx.core.view.AccessibilityDelegateCompat
import androidx.core.view.ViewCompat
import androidx.core.view.accessibility.AccessibilityNodeInfoCompat
import com.telekom.citykey.utils.extensions.AccessibilityRole

/**
 * This MapView allows users to scroll and zoom while it is inside a scrollbar, by obstructing the touch
 * propagation to the View's parent
 */
class OscaMapView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet,
    defStyle: Int = 0
) : LifecycleAwareMapView(context, attrs, defStyle) {

    override fun dispatchTouchEvent(event: MotionEvent): Boolean {
        when (event.action) {
            MotionEvent.ACTION_UP -> {
                parent.requestDisallowInterceptTouchEvent(false)
            }

            MotionEvent.ACTION_DOWN -> {
                parent.requestDisallowInterceptTouchEvent(true)
            }
        }
        return super.dispatchTouchEvent(event)
    }

    /**
     * Sets the accessibility elements for this [OscaMapView]. Be default the a11y role is "Link". Also helps in
     * attaching an onClickListener which gets invoked only in a11y mode
     *
     * @param accessibilityRole the a11y role to assign to this [OscaMapView], defaults to [AccessibilityRole.Link]
     * @param onMapClickedInA11yMode on click action to perform only when this map is clicked in a11y mode
     */
    fun setMapsAccessibility(
        accessibilityRole: AccessibilityRole = AccessibilityRole.Link,
        onMapClickedInA11yMode: () -> Unit
    ) = ViewCompat.setAccessibilityDelegate(this, object : AccessibilityDelegateCompat() {

        override fun onInitializeAccessibilityNodeInfo(host: View, info: AccessibilityNodeInfoCompat) {
            super.onInitializeAccessibilityNodeInfo(host, info)
            info.roleDescription = accessibilityRole.roleText
        }

        override fun onRequestSendAccessibilityEvent(
            host: ViewGroup,
            child: View,
            event: AccessibilityEvent
        ): Boolean {
            if (event.eventType == AccessibilityEvent.TYPE_VIEW_CLICKED) {
                onMapClickedInA11yMode()
                return false
            }
            return super.onRequestSendAccessibilityEvent(host, child, event)
        }
    })
}
