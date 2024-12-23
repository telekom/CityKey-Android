package com.telekom.citykey.utils.extensions

import android.os.Build
import android.view.View
import android.view.accessibility.AccessibilityEvent
import android.widget.TextView
import androidx.core.view.AccessibilityDelegateCompat
import androidx.core.view.ViewCompat
import androidx.core.view.accessibility.AccessibilityNodeInfoCompat
import androidx.core.view.children
import com.google.android.material.appbar.MaterialToolbar
import com.telekom.citykey.R

sealed class AccessibilityRole(val roleText: String) {
    object Button : AccessibilityRole("Button")
    object Link : AccessibilityRole("Link")
    object Heading : AccessibilityRole("Heading")
}

fun View.setAccessibilityRole(accessibilityRole: AccessibilityRole, headingLevelDescription: String = "") {
    ViewCompat.setAccessibilityDelegate(this, object : AccessibilityDelegateCompat() {
        override fun onInitializeAccessibilityNodeInfo(v: View, info: AccessibilityNodeInfoCompat) {
            super.onInitializeAccessibilityNodeInfo(v, info)

            //for heading
            if (accessibilityRole.roleText == AccessibilityRole.Heading.roleText) {
                if (headingLevelDescription.isEmpty().not()) {
                    info.stateDescription = headingLevelDescription
                }
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
                    isAccessibilityHeading = true
                } else {
                    ViewCompat.setAccessibilityDelegate(this@setAccessibilityRole,
                        object : AccessibilityDelegateCompat() {
                            override fun onInitializeAccessibilityNodeInfo(
                                host: View, info: AccessibilityNodeInfoCompat
                            ) {
                                info.isHeading = true
                                super.onInitializeAccessibilityNodeInfo(host, info)
                            }
                        })
                }
            } else {
                info.roleDescription = accessibilityRole.roleText
            }
        }
    })
}

fun View.setAccessibilityBasedOnViewStateSelection(isSelected: Boolean) {
    ViewCompat.setAccessibilityDelegate(this, object : AccessibilityDelegateCompat() {
        override fun onInitializeAccessibilityNodeInfo(v: View, info: AccessibilityNodeInfoCompat) {
            super.onInitializeAccessibilityNodeInfo(v, info)
            if (isSelected) {
                info.stateDescription =
                    context.getString(R.string.accessibility_label_pickup_read_state_selected, info.text)
            } else {
                info.stateDescription =
                    context.getString(R.string.accessibility_label_pickup_read_state_not_selected, info.text)
            }
            info.text = ""
        }
    })
}

fun setAccessibilityRoleForToolbarTitle(toolbar: MaterialToolbar) {
    val titleTextView = toolbar.children.find {
        it is TextView
    }
    titleTextView?.setAccessibilityRole(AccessibilityRole.Heading)
}

//To set the focus of the view forcefully
fun View.setAndPerformAccessibilityFocusAction() {
    sendAccessibilityEvent(AccessibilityEvent.TYPE_VIEW_FOCUSED)
    performAccessibilityAction(64, null)
}