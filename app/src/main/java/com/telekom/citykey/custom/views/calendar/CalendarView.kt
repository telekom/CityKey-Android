package com.telekom.citykey.custom.views.calendar

import android.content.Context
import android.util.AttributeSet
import androidx.annotation.ColorInt
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView

class CalendarView(context: Context, attrs: AttributeSet) : RecyclerView(context, attrs) {

    private var selectionListener: ((DateSelection?) -> Unit)? = null

    init {
        layoutManager = LinearLayoutManager(context)
        adapter = MonthAdapter {
            if (it.start == null) selectionListener?.invoke(null)
            else selectionListener?.invoke(it)
        }
    }

    fun onDateSelected(listener: (DateSelection?) -> Unit) {
        selectionListener = listener
    }

    fun setSelectedDates(selection: DateSelection) {
        (adapter as MonthAdapter).updateSelection(selection)
    }

    fun setPrimaryColor(@ColorInt color: Int) {
        (adapter as MonthAdapter).setPrimaryColor(color)
    }
}
