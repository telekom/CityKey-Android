package com.telekom.citykey.view.services.waste_calendar.export.options

import android.graphics.PorterDuff
import android.graphics.PorterDuffColorFilter
import android.graphics.drawable.LayerDrawable
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.telekom.citykey.R
import com.telekom.citykey.databinding.WasteExportOptionsItemBinding
import com.telekom.citykey.domain.city.CityInteractor
import com.telekom.citykey.models.waste_calendar.CalendarAccount
import com.telekom.citykey.utils.extensions.AccessibilityRole
import com.telekom.citykey.utils.extensions.inflateChild
import com.telekom.citykey.utils.extensions.setAccessibilityRole
import com.telekom.citykey.utils.extensions.setVisible

class WasteExportSelectionAdapter(val calendarAccount: CalendarAccount, val resultListener: (CalendarAccount) -> Unit) :
    ListAdapter<CalendarAccount, RecyclerView.ViewHolder>(diffCallback) {
    companion object {
        private val diffCallback = object : DiffUtil.ItemCallback<CalendarAccount>() {
            override fun areItemsTheSame(oldItem: CalendarAccount, newItem: CalendarAccount): Boolean =
                oldItem::class == newItem::class

            override fun areContentsTheSame(oldItem: CalendarAccount, newItem: CalendarAccount): Boolean =
                oldItem == newItem
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder =
        WasteListCategoryViewHolder(WasteExportOptionsItemBinding.bind(parent.inflateChild(R.layout.waste_export_options_item)))

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        if (holder is WasteListCategoryViewHolder) {
            holder.bind(getItem(position))
        }
    }

    private inner class WasteListCategoryViewHolder(val binding: WasteExportOptionsItemBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(item: CalendarAccount) {
            binding.wasteExportOptionsItem.contentDescription =
                item.calendarDisplayName + "\n" + item.calendarAccountName
            binding.wasteExportOptionsItem.setAccessibilityRole(AccessibilityRole.Button)
            binding.name.text = item.calendarDisplayName
            binding.calendarAccount.text = item.calendarAccountName
            binding.checkedIcon.setColorFilter(CityInteractor.cityColorInt)
            (binding.sideColor.background as LayerDrawable)
                .findDrawableByLayerId(R.id.mainLayer)
                .colorFilter = PorterDuffColorFilter(item.calendarColor, PorterDuff.Mode.SRC_IN)
            binding.checkedIcon.setVisible(calendarAccount.calId == item.calId)
            binding.root.setOnClickListener {
                resultListener(item)
            }
        }
    }
}
