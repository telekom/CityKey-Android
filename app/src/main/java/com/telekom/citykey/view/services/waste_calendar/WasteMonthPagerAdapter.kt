package com.telekom.citykey.view.services.waste_calendar

import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.telekom.citykey.R
import com.telekom.citykey.databinding.WastecalendarMonthPageBinding
import com.telekom.citykey.models.waste_calendar.WasteCalendarPickups
import com.telekom.citykey.utils.extensions.inflateChild
import java.util.*

class WasteMonthPagerAdapter(private val dateClickListener: (Date) -> Unit) :
    RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    private val currentDate = Calendar.getInstance()
    private val currMonth = currentDate.get(Calendar.MONTH)
    private val currYear = currentDate.get(Calendar.YEAR)
    var availableMonths = 12 - currMonth
        set(value) {
            if (field == value) return
            field = value
            notifyDataSetChanged()
        }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder =
        MonthViewHolder(WastecalendarMonthPageBinding.bind(parent.inflateChild(R.layout.wastecalendar_month_page)))

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        if (holder is MonthViewHolder) {
            val newMonth = currMonth + position
            if (newMonth > 11) holder.binding.monthView.setDate(currYear + 1, newMonth - 12)
            else holder.binding.monthView.setDate(currYear, newMonth)
            holder.binding.monthView.onDateSelected(dateClickListener)
        }
    }

    override fun getItemCount() = availableMonths

    internal class MonthViewHolder(val binding: WastecalendarMonthPageBinding) : RecyclerView.ViewHolder(binding.root) {

        fun setPickups(items: List<WasteCalendarPickups>, cityColor: Int) {
            binding.monthView.setPrimaryColor(cityColor)
            binding.monthView.setPickups(items)
        }
    }
}
