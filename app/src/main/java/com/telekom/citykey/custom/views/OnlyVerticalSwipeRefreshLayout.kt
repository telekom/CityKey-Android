package com.telekom.citykey.custom.views

import android.content.Context
import android.util.AttributeSet
import android.view.*
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout
import com.telekom.citykey.utils.extensions.dpToPixel
import kotlin.math.abs

class OnlyVerticalSwipeRefreshLayout(context: Context, attrs: AttributeSet) : SwipeRefreshLayout(context, attrs) {

    private val touchSlop: Int = ViewConfiguration.get(context).scaledTouchSlop
    private var prevX: Float = 0f
    private var declined: Boolean = false

    init {
        setProgressViewOffset(true, -50, 70.dpToPixel(context))
    }

    override fun onInterceptTouchEvent(event: MotionEvent): Boolean {
        when (event.action) {
            MotionEvent.ACTION_DOWN -> {
                MotionEvent.obtain(event).run {
                    prevX = x
                    recycle()
                }
                declined = false // New action
            }

            MotionEvent.ACTION_MOVE -> {
                val eventX = event.x
                val xDiff = abs(eventX - prevX)
                if (declined || xDiff > touchSlop) {
                    declined = true // Memorize
                    return false
                }
            }
        }
        return super.onInterceptTouchEvent(event)
    }
}
