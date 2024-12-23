package com.telekom.citykey.custom.views

import android.graphics.Rect
import android.view.View
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.RecyclerView.ItemDecoration

class SpacingDecoration(private val space: Int) : ItemDecoration() {

    override fun getItemOffsets(outRect: Rect, view: View, parent: RecyclerView, state: RecyclerView.State) {
        val position = parent.getChildAdapterPosition(view)

        if (position % 2 == 0) {
            outRect.right = space
            outRect.bottom = space
            outRect.top = space
        } else {
            outRect.left = space
            outRect.bottom = space
            outRect.top = space
        }
    }
}
