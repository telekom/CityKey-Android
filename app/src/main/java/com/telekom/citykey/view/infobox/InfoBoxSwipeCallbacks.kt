package com.telekom.citykey.view.infobox

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.RectF
import android.graphics.Typeface
import androidx.annotation.ColorInt
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.RecyclerView
import com.telekom.citykey.R
import com.telekom.citykey.domain.city.CityInteractor
import com.telekom.citykey.utils.extensions.dpToPixel

class InfoBoxSwipeCallbacks(
    private val adapter: InfoBoxAdapter,
    context: Context
) : ItemTouchHelper.SimpleCallback(
    0,
    ItemTouchHelper.LEFT or ItemTouchHelper.RIGHT
) {
    companion object {
        private const val SWIPE_LEFT_DIRECTION = 4
        private const val SWIPE_RIGHT_DIRECTION = 8
        private const val TEXT_SIZE = 12f
        private const val DELETE_COLOR = 0xFFD90000.toInt()
    }

    private val readText: String = context.getString(R.string.b_002_infobox_swiped_btn_read)
    private val unreadText: String = context.getString(R.string.b_002_infobox_swiped_btn_unread)
    private val deleteText: String = context.getString(R.string.b_002_infobox_swiped_btn_delete)
    private val textPaint: Paint by lazy {
        Paint().apply {
            color = Color.WHITE
            textSize = TEXT_SIZE.dpToPixel(context)
            typeface = Typeface.create("sans-serif", Typeface.NORMAL)
            isAntiAlias = true
        }
    }

    @ColorInt
    private val actionColor = CityInteractor.cityColorInt

    override fun onMove(
        recyclerView: RecyclerView,
        viewHolder: RecyclerView.ViewHolder,
        target: RecyclerView.ViewHolder
    ): Boolean {
        return false
    }

    override fun onSwiped(viewHolder: RecyclerView.ViewHolder, direction: Int) {
        when (direction) {
            SWIPE_LEFT_DIRECTION -> {
                adapter.deleteItem(viewHolder.bindingAdapterPosition)
            }
            SWIPE_RIGHT_DIRECTION -> {
                adapter.toggleItemRead(viewHolder.bindingAdapterPosition)
            }
        }
    }

    override fun onChildDraw(
        c: Canvas,
        recyclerView: RecyclerView,
        viewHolder: RecyclerView.ViewHolder,
        dX: Float,
        dY: Float,
        actionState: Int,
        isCurrentlyActive: Boolean
    ) {
        super.onChildDraw(c, recyclerView, viewHolder, dX, dY, actionState, isCurrentlyActive)
        val itemView = viewHolder.itemView
        if (viewHolder.bindingAdapterPosition == -1) return

        when {
            dX > 0 -> { // Swiping to the right
                val isItemRead = adapter.getItem(viewHolder.bindingAdapterPosition).isRead
                val paint = Paint()
                val leftButton =
                    RectF(itemView.left.toFloat(), itemView.top.toFloat(), dX + 100, itemView.bottom.toFloat())
                paint.color = actionColor
                c.drawRect(leftButton, paint)

                val text = if (isItemRead) unreadText else readText
                drawText(text, c, leftButton)
            }
            dX < 0 -> { // Swiping to the left
                val paint = Paint()
                val leftButton =
                    RectF(
                        dX + itemView.right,
                        itemView.top.toFloat(),
                        itemView.right.toFloat(),
                        itemView.bottom.toFloat()
                    )
                paint.color = DELETE_COLOR
                c.drawRect(leftButton, paint)
                drawText(deleteText, c, leftButton)
            }
        }
    }

    override fun getSwipeDirs(recyclerView: RecyclerView, viewHolder: RecyclerView.ViewHolder): Int {
        if (viewHolder is InfoBoxAdapter.InfoBoxEmptyHolder) return 0
        return super.getSwipeDirs(recyclerView, viewHolder)
    }

    private fun drawText(text: String, c: Canvas, button: RectF) {
        val textWidth = textPaint.measureText(text)
        c.drawText(text, button.centerX() - (textWidth / 2), button.centerY() + (TEXT_SIZE / 2), textPaint)
    }
}
