package com.telekom.citykey.view.home.feed_adapters

import android.annotation.SuppressLint
import android.graphics.Paint
import android.view.View
import android.view.ViewGroup
import androidx.core.view.isVisible
import androidx.navigation.findNavController
import androidx.recyclerview.widget.RecyclerView
import com.telekom.citykey.R
import com.telekom.citykey.databinding.HomeEventItemBinding
import com.telekom.citykey.databinding.HomeEventItemMDaysBinding
import com.telekom.citykey.databinding.HomeEventStateItemBinding
import com.telekom.citykey.domain.city.CityInteractor
import com.telekom.citykey.domain.city.events.EventsState
import com.telekom.citykey.models.content.Event
import com.telekom.citykey.utils.extensions.*
import com.telekom.citykey.view.home.HomeDirections
import java.util.*

@SuppressLint("NotifyDataSetChanged")
class EventsFeedAdapter : RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    companion object {
        private const val VIEW_TYPE_SINGLE_DAY = 13
        private const val VIEW_TYPE_MULTI_DAYS = 14
        private const val VIEW_TYPE_STATE = 15
    }

    private var state: EventsState? = null
    private val hasExtraRow get() = state != null && state != EventsState.SUCCESS

    private val items = mutableListOf<Event>()

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder =
        when (viewType) {
            VIEW_TYPE_SINGLE_DAY -> EventViewHolder(HomeEventItemBinding.bind(parent.inflateChild(R.layout.home_event_item)))
            VIEW_TYPE_MULTI_DAYS -> EventViewHolderM(HomeEventItemMDaysBinding.bind(parent.inflateChild(R.layout.home_event_item_m_days)))
            else -> StateViewHolder(HomeEventStateItemBinding.bind(parent.inflateChild(R.layout.home_event_state_item)))
        }

    override fun getItemCount() = if (items.isEmpty()) 1 else items.size

    override fun getItemViewType(position: Int) =
        when {
            hasExtraRow || items.isEmpty() -> VIEW_TYPE_STATE
            items[position].isSingleDay -> VIEW_TYPE_SINGLE_DAY
            !items[position].isSingleDay -> VIEW_TYPE_MULTI_DAYS
            else -> VIEW_TYPE_STATE
        }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        when (holder) {
            is EventViewHolder -> holder.bindData(items[position])

            is EventViewHolderM -> holder.bindData(items[position])

            is StateViewHolder -> holder.setState(state)
        }
    }

    fun updateData(newItems: List<Event>) {
        items.clear()
        items.addAll(newItems)
        notifyDataSetChanged()
    }

    fun updateState(newState: EventsState) {
        if (state == newState) return
        if (state == EventsState.SUCCESS && newState != EventsState.FORCELOADING) return
        state = newState
        notifyDataSetChanged()
    }

    private inner class EventViewHolderM(val binding: HomeEventItemMDaysBinding) :
        RecyclerView.ViewHolder(binding.root) {
        fun bindData(item: Event) {
            val cityColor = CityInteractor.cityColorInt
            val startDate = item.startDate.toCalendar()
            val endDate = item.endDate.toCalendar()

            binding.dateCard.setCardBackgroundColor(cityColor)

            if (!item.locationName.isNullOrBlank()) {
                binding.locationM.visibility = View.VISIBLE
                binding.locationM.text = item.locationName
            } else {
                binding.locationM.visibility = View.GONE
            }
            binding.eventNameM.text = item.title

            binding.dayOfWeek.text = startDate.getLongWeekDay()
            binding.day.text = startDate.get(Calendar.DAY_OF_MONTH).toString()
            binding.month.text = startDate.getShortMonthName().replace(".", "")

            binding.dayOfWeekEnd.text = endDate.getLongWeekDay()
            binding.dayEnd.text = endDate.get(Calendar.DAY_OF_MONTH).toString()
            binding.monthEnd.text = endDate.getShortMonthName().replace(".", "")
            val dateCardText = binding.root.resources.getString(
                R.string.event_card_dateM_description,
                startDate.getLongWeekDay(),
                startDate.longMonthName,
                startDate.get(Calendar.DAY_OF_MONTH).toString(),
                endDate.getLongWeekDay(),
                endDate.longMonthName,
                endDate.get(Calendar.DAY_OF_MONTH).toString()
            )

            if (item.isCancelled) {
                binding.eventStatus.apply {
                    setVisible(true)
                    setText(R.string.e_007_cancelled_events)
                }
                binding.eventNameM.paintFlags = binding.eventNameM.paintFlags or Paint.STRIKE_THRU_TEXT_FLAG
            } else if (item.isSoldOut) {
                binding.eventStatus.apply {
                    setVisible(true)
                    setText(R.string.e_007_events_sold_out_label)
                }
            } else if (item.isPostponed) {
                binding.eventStatus.apply {
                    backgroundTintList = binding.root.resources.getColorStateList(R.color.postponedColor, null)
                    setVisible(true)
                    setText(R.string.e_007_events_new_date_label)
                }
            }

            binding.root.setOnClickListener {
                it.findNavController().navigate(HomeDirections.actionHomeToEventDetails().apply { event = item })
            }

            binding.favSignM.setColorFilter(cityColor)
            binding.favSignM.setVisible(item.isFavored)
            binding.eventItem.apply {
                contentDescription = context.getString(
                    R.string.a11y_list_item_position, bindingAdapterPosition + 1, items.size
                ) + "\n" + dateCardText + "\n" + item.locationName + "\n" + item.title + if (binding.eventStatus.isVisible) {
                    binding.eventStatus.text
                } else "" + "\n" + if (item.isFavored) context.getString(R.string.e_005_favourite) else ""
                setAccessibilityRole(AccessibilityRole.Link)
            }
        }
    }

    private inner class EventViewHolder(val binding: HomeEventItemBinding) : RecyclerView.ViewHolder(binding.root) {
        fun bindData(item: Event) {
            val cityColor = CityInteractor.cityColorInt
            val calendar = item.startDate.toCalendar()
            val dateText = calendar.getShortWeekDay() + ", " + calendar.getShortMonthName().replace(".", "")

            val dateDay = calendar.get(Calendar.DAY_OF_MONTH)

            binding.dateCard.setCardBackgroundColor(cityColor)
            if (item.isCancelled) {
                binding.eventStatus.apply {
                    setVisible(true)
                    setText(R.string.e_007_cancelled_events)
                }
                binding.appointmentName.paintFlags = binding.appointmentName.paintFlags or Paint.STRIKE_THRU_TEXT_FLAG
            } else if (item.isSoldOut) {
                binding.eventStatus.apply {
                    setVisible(true)
                    setText(R.string.e_007_events_sold_out_label)
                }
            } else if (item.isPostponed) {
                binding.eventStatus.apply {
                    backgroundTintList = binding.root.resources.getColorStateList(R.color.postponedColor, null)
                    setVisible(true)
                    setText(R.string.e_007_events_new_date_label)
                }
            }

            if (!item.locationName.isNullOrBlank()) {
                binding.location.visibility = View.VISIBLE
                binding.location.text = item.locationName
            } else {
                binding.location.visibility = View.GONE
            }
            binding.appointmentName.text = item.title
            binding.dateTextS.text = dateText
            binding.dateNumberS.text = dateDay.toString()
            val dateCardText =
                calendar.getLongWeekDay() + calendar.longMonthName + calendar.get(Calendar.DAY_OF_MONTH).toString()

            binding.root.setOnClickListener {
                it.findNavController().navigate(HomeDirections.actionHomeToEventDetails().apply { event = item })
            }

            binding.favSign.setColorFilter(cityColor)
            binding.favSign.setVisible(item.isFavored)
            binding.eventItem.apply {
                contentDescription = context.getString(
                    R.string.a11y_list_item_position, bindingAdapterPosition + 1, items.size
                ) + "\n" + dateCardText + "\n" + item.locationName + "\n" + item.title + if (binding.eventStatus.isVisible) {
                    binding.eventStatus.text
                } else "" + "\n" + if (item.isFavored) context.getString(R.string.e_005_favourite) else ""
                setAccessibilityRole(AccessibilityRole.Link)
            }
        }
    }

    private inner class StateViewHolder(val binding: HomeEventStateItemBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun setState(state: EventsState?) {
            binding.progress.setVisible(state == EventsState.RUNNING || state == EventsState.FORCELOADING)
            binding.errorText.setVisible(state == EventsState.EMPTY || state == EventsState.ERRORACTION || state == EventsState.FAILED)
            if (binding.errorText.isVisible) {
                binding.errorText.setText(
                    when (state) {
                        EventsState.EMPTY -> {
                            R.string.h_001_events_no_events_msg
                        }

                        EventsState.ERRORACTION -> {
                            R.string.h_001_events_load_action_error
                        }

                        else -> {
                            R.string.h_001_events_load_error
                        }
                    }
                )
            }
        }
    }
}
