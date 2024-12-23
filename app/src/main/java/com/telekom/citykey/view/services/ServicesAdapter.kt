package com.telekom.citykey.view.services

import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.telekom.citykey.R
import com.telekom.citykey.databinding.ServiceOursContainerBinding
import com.telekom.citykey.databinding.ServiceStateContainerBinding
import com.telekom.citykey.domain.services.main.ServicesStates
import com.telekom.citykey.domain.services.main.ServicesViewTypes
import com.telekom.citykey.models.content.CitizenService
import com.telekom.citykey.utils.extensions.inflateChild
import com.telekom.citykey.utils.extensions.setVisible
import com.telekom.citykey.view.services.service_adapter.OurServicesAdapter

class ServicesAdapter(
    onServiceSelected: (CitizenService) -> Unit
) :
    RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    private val viewPool: RecyclerView.RecycledViewPool = RecyclerView.RecycledViewPool()
    private val ourServicesAdapter = OurServicesAdapter(onServiceSelected)
    private val viewTypes = mutableListOf<Int>()
    private var currentState: ServicesStates = ServicesStates.Loading
    private val showStateView: Boolean get() = currentState is ServicesStates.Loading || currentState is ServicesStates.Error

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder =
        if (viewType == ServicesViewTypes.VIEW_TYPE_OUR_SERVICES)
            OurServicesViewHolder(ServiceOursContainerBinding.bind(parent.inflateChild(R.layout.service_ours_container)))
        else StateViewHolder(ServiceStateContainerBinding.bind(parent.inflateChild(R.layout.service_state_container)))

    override fun getItemCount() = 1 // if (showStateView) 1 else viewTypes.size

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        when (holder) {
            is StateViewHolder -> {
                holder.bind()
            }
        }
    }

    override fun getItemViewType(position: Int) =
        if (showStateView) ServicesViewTypes.VIEW_TYPE_STATE else ServicesViewTypes.VIEW_TYPE_OUR_SERVICES

    fun setupData(state: ServicesStates) {
        currentState = state
        viewTypes.clear()
        if (showStateView) {
            notifyDataSetChanged()
            return
        }
        val data = (state as ServicesStates.Success).data
        viewTypes.addAll(data.viewTypes)
        ourServicesAdapter.updateData(data.services)
        notifyDataSetChanged()
    }

    private inner class StateViewHolder(val binding: ServiceStateContainerBinding) :
        RecyclerView.ViewHolder(binding.root) {
        fun bind() {
            binding.loadingView.setVisible(currentState == ServicesStates.Loading)
            binding.errorText.setVisible(currentState == ServicesStates.Error)
        }
    }

    private inner class OurServicesViewHolder(binding: ServiceOursContainerBinding) :
        RecyclerView.ViewHolder(binding.root) {
        init {
            binding.fewServices.apply {
                setRecycledViewPool(viewPool)
                adapter = ourServicesAdapter
            }
        }
    }
}
