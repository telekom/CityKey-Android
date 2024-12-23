package com.telekom.citykey.utils.extensions

import android.view.View
import android.view.ViewGroup
import androidx.core.view.children

val View.isNotVisible: Boolean get() = visibility == View.GONE || visibility == View.INVISIBLE

fun View.setVisible(visible: Boolean, mode: Int = View.GONE) {
    visibility = if (visible) View.VISIBLE else mode
}

fun View.disable() {
    isEnabled = false
    animate().alpha(0.3f).setDuration(200).start()
}

fun View.enable() {
    isEnabled = true
    animate().alpha(1f).setDuration(200).start()
}

fun View.setAllEnabled(enabled: Boolean) {
    isEnabled = enabled
    if (this is ViewGroup) children.forEach { child -> child.setAllEnabled(enabled) }
}
