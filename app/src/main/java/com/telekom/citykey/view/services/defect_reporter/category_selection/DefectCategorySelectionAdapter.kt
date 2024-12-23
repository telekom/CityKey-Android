package com.telekom.citykey.view.services.defect_reporter.category_selection

import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.telekom.citykey.R
import com.telekom.citykey.databinding.DefectCategoryListItemBinding
import com.telekom.citykey.models.defect_reporter.DefectCategory
import com.telekom.citykey.utils.extensions.AccessibilityRole
import com.telekom.citykey.utils.extensions.inflateChild
import com.telekom.citykey.utils.extensions.setAccessibilityRole

class DefectCategorySelectionAdapter(
    val categoryResultListener: (DefectCategory) -> Unit,
) : ListAdapter<DefectCategory, RecyclerView.ViewHolder>(diffCallback) {
    companion object {
        private val diffCallback = object : DiffUtil.ItemCallback<DefectCategory>() {
            override fun areItemsTheSame(oldItem: DefectCategory, newItem: DefectCategory): Boolean = oldItem == newItem

            override fun areContentsTheSame(oldItem: DefectCategory, newItem: DefectCategory): Boolean =
                oldItem == newItem
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder =
        DefectListCategoryViewHolder(DefectCategoryListItemBinding.bind(parent.inflateChild(R.layout.defect_category_list_item)))

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        if (holder is DefectListCategoryViewHolder) {
            holder.bind(getItem(position), position)
        }
    }

    private inner class DefectListCategoryViewHolder(val binding: DefectCategoryListItemBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(item: DefectCategory, position: Int) {
            binding.serviceName.text = item.serviceName
            binding.root.setOnClickListener {
                categoryResultListener(item)
            }
            binding.defectCategoryListItem.apply {
                contentDescription = context.getString(
                    R.string.a11y_list_item_position, position + 1, itemCount
                ) + item.serviceName

                setAccessibilityRole(AccessibilityRole.Button)
            }
        }
    }
}
