package com.telekom.citykey.view.services.defect_reporter.category_selection.defectsubcategory

import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.telekom.citykey.R
import com.telekom.citykey.databinding.DefectSubcategoryListItemBinding
import com.telekom.citykey.models.defect_reporter.DefectSubCategory
import com.telekom.citykey.utils.extensions.AccessibilityRole
import com.telekom.citykey.utils.extensions.inflateChild
import com.telekom.citykey.utils.extensions.setAccessibilityRole

class DefectSubcategorySelectionAdapter(
    val categoryResultListener: (DefectSubCategory) -> Unit,
) : ListAdapter<DefectSubCategory, RecyclerView.ViewHolder>(diffCallback) {
    companion object {
        private val diffCallback = object : DiffUtil.ItemCallback<DefectSubCategory>() {
            override fun areItemsTheSame(oldItem: DefectSubCategory, newItem: DefectSubCategory): Boolean =
                oldItem == newItem

            override fun areContentsTheSame(oldItem: DefectSubCategory, newItem: DefectSubCategory): Boolean =
                oldItem == newItem
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder =
        DefectSubcategoryViewHolder(DefectSubcategoryListItemBinding.bind(parent.inflateChild(R.layout.defect_subcategory_list_item)))

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        if (holder is DefectSubcategoryViewHolder) {
            holder.bind(getItem(position), position)
        }
    }

    private inner class DefectSubcategoryViewHolder(private val binding: DefectSubcategoryListItemBinding) :
        RecyclerView.ViewHolder(binding.root) {
        fun bind(item: DefectSubCategory, position: Int) {
            binding.serviceName.text = item.serviceName
            binding.root.setOnClickListener {
                categoryResultListener(item)
            }
            binding.defectSubCategoryListItem.apply {
                contentDescription = context.getString(
                    R.string.a11y_list_item_position, position + 1, itemCount
                ) + item.serviceName

                setAccessibilityRole(AccessibilityRole.Button)
            }
        }
    }
}
