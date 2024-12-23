package com.telekom.citykey.utils.extensions

import android.content.res.ColorStateList
import androidx.core.content.ContextCompat
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.telekom.citykey.R

fun BottomNavigationView.setItemsColor(color: Int) {
    val colors: IntArray = intArrayOf(ContextCompat.getColor(context, R.color.onSurfaceSecondary), color)
    val bottomNavigationStates: Array<IntArray> = arrayOf(
        intArrayOf(-android.R.attr.state_checked), // not pressed
        intArrayOf(android.R.attr.state_checked) // pressed
    )
    val colorStates = ColorStateList(bottomNavigationStates, colors)
    itemIconTintList = colorStates
    itemTextColor = colorStates
}
