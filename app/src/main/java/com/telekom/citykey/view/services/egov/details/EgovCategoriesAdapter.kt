package com.telekom.citykey.view.services.egov.details

import android.content.res.ColorStateList
import android.view.ViewGroup
import androidx.navigation.findNavController
import androidx.recyclerview.widget.RecyclerView
import com.telekom.citykey.R
import com.telekom.citykey.databinding.EgovServiceDetailsCategoryBinding
import com.telekom.citykey.domain.city.CityInteractor
import com.telekom.citykey.models.egov.EgovGroup
import com.telekom.citykey.utils.extensions.AccessibilityRole
import com.telekom.citykey.utils.extensions.inflateChild
import com.telekom.citykey.utils.extensions.loadFromURL
import com.telekom.citykey.utils.extensions.setAccessibilityRole

class EgovCategoriesAdapter(service: String, viewModel: EgovServiceDetailsViewModel) :
    RecyclerView.Adapter<EgovCategoriesAdapter.EgovCategoryViewHolder>() {

    private val categories = mutableListOf<EgovGroup>()
    private val service = service
    private val viewModel = viewModel

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): EgovCategoryViewHolder =
        EgovCategoryViewHolder(
            EgovServiceDetailsCategoryBinding.bind(parent.inflateChild(R.layout.egov_service_details_category))
        )

    override fun onBindViewHolder(holder: EgovCategoryViewHolder, position: Int) {
        holder.bind(categories[position], this.service)
    }

    override fun getItemCount() = categories.size

    fun submitList(list: List<EgovGroup>) {
        categories.clear()
        categories.addAll(list)

        notifyDataSetChanged()
    }

    inner class EgovCategoryViewHolder(val binding: EgovServiceDetailsCategoryBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(category: EgovGroup, service: String) {
            binding.root.setStrokeColor(ColorStateList.valueOf(CityInteractor.cityColorInt))
            binding.category.setAccessibilityRole(AccessibilityRole.Button)
            binding.category.text = category.groupName
            binding.icon.loadFromURL(category.groupIcon)
            binding.root.setOnClickListener {
                viewModel.clickSubCategory(service, category.groupName)
                it.findNavController().navigate(EgovServiceDetailsDirections.toEgovServices(category.groupId))
            }
        }


    }
}
