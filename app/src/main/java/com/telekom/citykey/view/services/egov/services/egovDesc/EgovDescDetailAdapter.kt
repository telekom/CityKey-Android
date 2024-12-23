package com.telekom.citykey.view.services.egov.services.egovDesc

import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.telekom.citykey.R
import com.telekom.citykey.databinding.EgovLinksItemBinding
import com.telekom.citykey.models.egov.EgovLinkInfo
import com.telekom.citykey.models.egov.EgovLinkTypes
import com.telekom.citykey.utils.extensions.AccessibilityRole
import com.telekom.citykey.utils.extensions.inflateChild
import com.telekom.citykey.utils.extensions.loadFromDrawable
import com.telekom.citykey.utils.extensions.setAccessibilityRole

class EgovDescDetailAdapter(
    private val linksInfo: List<EgovLinkInfo>,
    private val clickListener: (EgovLinkInfo) -> Unit
) : RecyclerView.Adapter<EgovDescDetailAdapter.LinkViewButtonHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): LinkViewButtonHolder =
        LinkViewButtonHolder(EgovLinksItemBinding.bind(parent.inflateChild(R.layout.egov_links_item)))

    override fun onBindViewHolder(holder: LinkViewButtonHolder, position: Int) {
        holder.bind(linksInfo[position])
    }

    override fun getItemCount() = linksInfo.size

    inner class LinkViewButtonHolder(private val binding: EgovLinksItemBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(itemInfo: EgovLinkInfo) {
            binding.serviceTypeText.text = itemInfo.title
            binding.serviceTypeText.setAccessibilityRole(AccessibilityRole.Button)
            if (itemInfo.title.isEmpty()) {
                binding.serviceTypeText.setText(
                    when (itemInfo.linkType) {
                        EgovLinkTypes.WEB -> R.string.egovs_002_services_type_web
                        EgovLinkTypes.FORM -> R.string.egovs_002_services_type_form
                        EgovLinkTypes.EID_FORM -> R.string.egovs_002_services_type_form_eid
                        EgovLinkTypes.PDF -> R.string.egovs_002_services_type_pdf
                        else -> R.string.egovs_002_services_type_web
                    }
                )
            }

            binding.serviceTypeIcon.loadFromDrawable(
                when (itemInfo.linkType) {
                    EgovLinkTypes.WEB -> R.drawable.ic_egov_type_web
                    EgovLinkTypes.FORM -> R.drawable.ic_egov_type_form
                    EgovLinkTypes.EID_FORM -> R.drawable.ic_egov_type_eidform
                    EgovLinkTypes.PDF -> R.drawable.ic_egov_type_pdf
                    else -> R.drawable.ic_egov_type_web
                }
            )
            binding.root.setOnClickListener {
                clickListener(itemInfo)
            }
        }
    }
}
