package com.telekom.citykey.custom.views.inputfields

import android.content.Context
import android.graphics.Rect
import android.util.AttributeSet
import androidx.appcompat.widget.AppCompatAutoCompleteTextView

class AutoCompleteTextView(context: Context, attrs: AttributeSet) : AppCompatAutoCompleteTextView(context, attrs) {

    companion object {
        private const val MINIMAL_HEIGHT = 50
    }

    override fun showDropDown() {
        val displayFrame = Rect()
        getWindowVisibleDisplayFrame(displayFrame)
        val locationOnScreen = IntArray(2)
        getLocationOnScreen(locationOnScreen)
        val bottom = locationOnScreen[1] + height
        val availableHeightBelow: Int = displayFrame.bottom - bottom
        if (availableHeightBelow >= MINIMAL_HEIGHT) {
            dropDownHeight = availableHeightBelow
        }
        super.showDropDown()
    }
}
