package com.telekom.citykey.view.home.events_list

import android.graphics.Paint
import android.view.View
import android.view.ViewGroup
import androidx.navigation.findNavController
import androidx.paging.PagingDataAdapter
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import com.telekom.citykey.R
import com.telekom.citykey.databinding.EventsListItemBinding
import com.telekom.citykey.domain.city.CityInteractor
import com.telekom.citykey.models.content.Event
import com.telekom.citykey.utils.extensions.AccessibilityRole
import com.telekom.citykey.utils.extensions.getShortMonthName
import com.telekom.citykey.utils.extensions.getShortWeekDay
import com.telekom.citykey.utils.extensions.inflateChild
import com.telekom.citykey.utils.extensions.loadFromURL
import com.telekom.citykey.utils.extensions.setAccessibilityRole
import com.telekom.citykey.utils.extensions.setVisible
import com.telekom.citykey.utils.extensions.toCalendar
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

class EventsListAdapter : PagingDataAdapter<Event, RecyclerView.ViewHolder>(diffCallback) {
    companion object {
        private val diffCallback = object : DiffUtil.ItemCallback<Event>() {
            override fun areItemsTheSame(oldItem: Event, newItem: Event): Boolean =
                oldItem.uid == newItem.uid

            override fun areContentsTheSame(oldItem: Event, newItem: Event): Boolean =
                oldItem == newItem
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int) =
        EventViewHolder(EventsListItemBinding.bind(parent.inflateChild(R.layout.events_list_item)))

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        if (holder is EventViewHolder) {
            getItem(holder.bindingAdapterPosition)?.let { holder.bindItem(it) }
        }
    }

    inner class EventViewHolder(val binding: EventsListItemBinding) : RecyclerView.ViewHolder(binding.root) {

        fun bindItem(item: Event) {
            binding.thumbnail.loadFromURL(item.thumbnail)
            binding.title.text = item.title
            binding.title.setAccessibilityRole(AccessibilityRole.Link)
            if (!item.locationName.isNullOrBlank()) {
                binding.location.visibility = View.VISIBLE
                binding.location.text = item.locationName
            }
            binding.eventDate.text =
                if (item.isSingleDay) oneDayDateFormat(item) else twoDaysDateFormat(item)
            if (!item.isSingleDay) {
                binding.eventDate.contentDescription =
                    SimpleDateFormat("EEEE, d. MMMM yyyy", Locale.getDefault()).format(
                        item.startDate ?: Date()
                    ) + " to " + SimpleDateFormat("EEEE, d. MMMM yyyy", Locale.getDefault()).format(
                        item.endDate ?: Date()
                    )
            }
            binding.eventDateCard.setCardBackgroundColor(CityInteractor.cityColorInt)

            if (item.isCancelled) {
                binding.eventStatus.apply {
                    setVisible(true)
                    setText(R.string.e_007_cancelled_events)
                }
                binding.title.paintFlags = binding.title.paintFlags or Paint.STRIKE_THRU_TEXT_FLAG
            } else if (item.isSoldOut) {
                binding.eventStatus.apply {
                    setVisible(true)
                    setText(R.string.e_007_events_sold_out_label)
                }
                binding.title.paintFlags = Paint.ANTI_ALIAS_FLAG
            } else if (item.isPostponed) {
                binding.eventStatus.apply {
                    backgroundTintList = binding.root.resources.getColorStateList(R.color.postponedColor, null)
                    setVisible(true)
                    setText(R.string.e_007_events_new_date_label)
                }
                binding.title.paintFlags = Paint.ANTI_ALIAS_FLAG
            } else {
                binding.eventStatus.visibility = View.GONE
                binding.title.paintFlags = Paint.ANTI_ALIAS_FLAG
            }

            binding.root.setOnClickListener {
                it.findNavController()
                    .navigate(EventsListDirections.actionEventsListToEventDetails().apply { event = item })
            }
        }

        private fun oneDayDateFormat(item: Event) =
            SimpleDateFormat("EEEE, d. MMMM yyyy", Locale.getDefault()).format(item.startDate ?: Date())

        private fun twoDaysDateFormat(item: Event): String {
            val startCalendar = item.startDate.toCalendar()
            val endCalendar = item.endDate.toCalendar()

            val startYear = startCalendar.get(Calendar.YEAR)
            val endYear = endCalendar.get(Calendar.YEAR)

            val startYearString = if (startYear == endYear)
                "" else "$startYear "

            val start = String.format(
                "%s, %s. %s %s",
                startCalendar.getShortWeekDay().replace(".", ""),
                startCalendar.get(Calendar.DAY_OF_MONTH).toString(),
                startCalendar.getShortMonthName().replace(".", ""),
                startYearString
            )

            val end = String.format(
                "%s, %s. %s %s",
                endCalendar.getShortWeekDay().replace(".", ""),
                endCalendar.get(Calendar.DAY_OF_MONTH),
                endCalendar.getShortMonthName().replace(".", ""),
                endYear.toString()
            )

            return String.format("%s- %s", start, end)
        }
    }
}
