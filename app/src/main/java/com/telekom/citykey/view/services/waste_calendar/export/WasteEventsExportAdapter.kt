package com.telekom.citykey.view.services.waste_calendar.export

import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.telekom.citykey.R
import com.telekom.citykey.databinding.WasteCategoriesItemBinding
import com.telekom.citykey.models.waste_calendar.GetWasteTypeResponse
import com.telekom.citykey.utils.extensions.inflateChild

class WasteEventsExportAdapter : ListAdapter<GetWasteTypeResponse, RecyclerView.ViewHolder>(diffCallback) {
    companion object {
        private val diffCallback = object : DiffUtil.ItemCallback<GetWasteTypeResponse>() {
            override fun areItemsTheSame(oldItem: GetWasteTypeResponse, newItem: GetWasteTypeResponse): Boolean =
                oldItem::class == newItem::class

            override fun areContentsTheSame(oldItem: GetWasteTypeResponse, newItem: GetWasteTypeResponse): Boolean =
                oldItem == newItem
        }
    }

    var sideColor: Int = 0
        set(value) {
            field = value
            notifyDataSetChanged()
        }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder =
        WasteListCategoryViewHolder(WasteCategoriesItemBinding.bind(parent.inflateChild(R.layout.waste_categories_item)))

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        if (holder is WasteListCategoryViewHolder) {
            holder.bind(getItem(position).name)
        }
    }

    private inner class WasteListCategoryViewHolder(val binding: WasteCategoriesItemBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(item: String) {
            binding.sideColor.setBackgroundColor(sideColor)
            binding.categoryName.text = item
        }
    }
}