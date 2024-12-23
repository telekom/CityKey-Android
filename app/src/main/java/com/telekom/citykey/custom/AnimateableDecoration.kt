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
