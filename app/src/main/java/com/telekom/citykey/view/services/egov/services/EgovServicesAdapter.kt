package com.telekom.citykey.view.services.egov.services

import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.telekom.citykey.R
import com.telekom.citykey.databinding.EgovServicesItemBinding
import com.telekom.citykey.models.egov.EgovLinkTypes
import com.telekom.citykey.models.egov.EgovService
import com.telekom.citykey.utils.extensions.AccessibilityRole
import com.telekom.citykey.utils.extensions.inflateChild
import com.telekom.citykey.utils.extensions.loadFromDrawable
import com.telekom.citykey.utils.extensions.setAccessibilityRole
import com.telekom.citykey.utils.extensions.setVisible

class EgovServicesAdapter(private val clickListener: (EgovService) -> Unit) :
    ListAdapter<EgovService, EgovServicesAdapter.EgovServiceViewHolder>(diffCallback) {

    companion object {
        private val diffCallback = object : DiffUtil.ItemCallback<EgovService>() {
            override fun areItemsTheSame(oldItem: EgovService, newItem: EgovService): Boolean =
                oldItem === newItem

            override fun areContentsTheSame(oldItem: EgovService, newItem: EgovService): Boolean =
                oldItem == newItem
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): EgovServiceViewHolder =
        EgovServiceViewHolder(EgovServicesItemBinding.bind(parent.inflateChild(R.layout.egov_services_item)))

    override fun onBindViewHolder(holder: EgovServiceViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    inner class EgovServiceViewHolder(private val binding: EgovServicesItemBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(service: EgovService) {
            binding.serviceTypeText.setAccessibilityRole(AccessibilityRole.Button)
            binding.serviceTitle.text = service.serviceName
            binding.serviceTypeText.text = service.linksInfo[0].title
            binding.serviceDescription.text = service.shortDescription
            binding.serviceDescription.setVisible(service.shortDescription.isNotEmpty())

            if (service.linksInfo[0].title.isEmpty()) {
                binding.serviceTypeText.setText(
                    if (service.longDescription.isEmpty()) {
                        when (service.linksInfo[0].linkType) {
                            EgovLinkTypes.WEB -> R.string.egovs_002_services_type_web
                            EgovLinkTypes.FORM -> R.string.egovs_002_services_type_form
                            EgovLinkTypes.EID_FORM -> R.string.egovs_002_services_type_form_eid
                            EgovLinkTypes.PDF -> R.string.egovs_002_services_type_pdf
                            else -> R.string.egovs_002_services_type_web
                        }
                    } else R.string.egovs_002_services_type_web

                )
            }

            binding.serviceTypeIcon.loadFromDrawable(
                if (service.longDescription.isEmpty()) {
                    when (service.linksInfo[0].linkType) {
                        EgovLinkTypes.WEB -> R.drawable.ic_egov_type_web
                        EgovLinkTypes.FORM -> R.drawable.ic_egov_type_form
                        EgovLinkTypes.EID_FORM -> R.drawable.ic_egov_type_eidform
                        EgovLinkTypes.PDF -> R.drawable.ic_egov_type_pdf
                        else -> R.drawable.ic_egov_type_web
                    }
                } else R.drawable.ic_egov_icon_content_info
            )

            binding.root.setOnClickListener {
                clickListener(service)
            }
        }
    }
}
