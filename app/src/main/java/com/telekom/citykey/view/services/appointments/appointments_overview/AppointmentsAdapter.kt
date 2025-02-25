/**
 * Copyright (C) 2025 Deutsche Telekom AG
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * 
 * In accordance with Sections 4 and 6 of the License, the following exclusions apply:
 *
 *  1. Trademarks & Logos – The names, logos, and trademarks of the Licensor are not covered by this License and may not be used without separate permission.
 *  2. Design Rights – Visual identities, UI/UX designs, and other graphical elements remain the property of their respective owners and are not licensed under the Apache License 2.0.
 *  3: Non-Coded Copyrights – Documentation, images, videos, and other non-software materials require separate authorization for use, modification, or distribution.
 *
 * These elements are not considered part of the licensed Work or Derivative Works unless explicitly agreed otherwise. All elements must be altered, removed, or replaced before use or distribution. All rights to these materials are reserved, and Contributor accepts no liability for any infringing use. By using this repository, you agree to indemnify and hold harmless Contributor against any claims, costs, or damages arising from your use of the excluded elements.
 *
 * SPDX-FileCopyrightText: 2025 Deutsche Telekom AG
 * SPDX-License-Identifier: Apache-2.0 AND LicenseRef-Deutsche-Telekom-Brand
 * License-Filename: LICENSES/Apache-2.0.txt LICENSES/LicenseRef-Deutsche-Telekom-Brand.txt
 */

package com.telekom.citykey.view.services.appointments.appointments_overview

import android.annotation.SuppressLint
import android.content.res.ColorStateList
import android.os.Bundle
import android.view.View
import android.view.ViewGroup
import android.view.accessibility.AccessibilityNodeInfo
import android.view.accessibility.AccessibilityNodeInfo.ACTION_DISMISS
import androidx.annotation.ColorInt
import androidx.annotation.StringRes
import androidx.core.view.isVisible
import androidx.core.widget.TextViewCompat
import androidx.navigation.findNavController
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import com.telekom.citykey.R
import com.telekom.citykey.databinding.AppointmentsOverviewListItemBinding
import com.telekom.citykey.domain.city.CityInteractor
import com.telekom.citykey.models.appointments.Appointment
import com.telekom.citykey.utils.diffutil_callbacks.AppointmentsDiffUtils
import com.telekom.citykey.utils.extensions.*

class AppointmentsAdapter(
    private val deleteListener: (id: Appointment) -> Unit,
    private val itemSwipeListener: (position: Int) -> Unit
) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {
    companion object {
        private const val STATE_COLOR_HISTORIC = 0xFF_C7_C7_CC.toInt()
        private const val STATE_COLOR_PENDING = 0xFF_FF_87_00.toInt()
        private const val STATE_COLOR_CANCELED = 0xFF_D0_02_1B.toInt()
        private const val STATE_COLOR_UPDATED = 0xFF_0F_69_CB.toInt()
    }

    private val items = mutableListOf<Appointment>()

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder =
        AppointmentsViewHolder(AppointmentsOverviewListItemBinding.bind(parent.inflateChild(R.layout.appointments_overview_list_item)))

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        val mPosition = holder.absoluteAdapterPosition
        (holder as AppointmentsViewHolder).bind(items[mPosition])
        holder.itemView.accessibilityDelegate = object : View.AccessibilityDelegate() {
            override fun onInitializeAccessibilityNodeInfo(host: View, info: AccessibilityNodeInfo) {
                super.onInitializeAccessibilityNodeInfo(host, info)
                val customAction = AccessibilityNodeInfo.AccessibilityAction(ACTION_DISMISS, "Delete")
                info.addAction(customAction)
            }

            override fun performAccessibilityAction(host: View, action: Int, args: Bundle?): Boolean {
                if (action == ACTION_DISMISS) {
                    if (items[mPosition].canBeDeleted)
                        deleteItem(mPosition)
                    else
                        changeItem(mPosition)
                }
                return super.performAccessibilityAction(host, action, args)
            }
        }
    }

    override fun getItemCount() = items.size

    fun getItem(position: Int) = items[position]

    fun deleteItem(position: Int) {
        val item = items[position]
        deleteListener(item)
        items.removeAt(position)
        notifyItemRemoved(position)
    }

    fun changeItem(position: Int) {
        itemSwipeListener(position)
    }

    fun updateData(data: List<Appointment>) {
        val oldList = items.toList()
        items.clear()
        items.addAll(data)

        DiffUtil.calculateDiff(AppointmentsDiffUtils(oldList, items))
            .dispatchUpdatesTo(this)
    }

    inner class AppointmentsViewHolder(val binding: AppointmentsOverviewListItemBinding) :
        RecyclerView.ViewHolder(binding.root) {
        @SuppressLint("SetTextI18n")
        fun bind(item: Appointment) {

            binding.cardTitle.text = item.title

            binding.placeName.text = item.location.addressDesc
            binding.placeAddress.text = "${item.location.street} ${item.location.houseNumber}"
            binding.placeTown.text = "${item.location.postalCode} ${item.location.place}"

            val calendar = item.startTime.toCalendar()
            binding.dateTime.text = "${calendar.getShortWeekDay()}, ${calendar.time.toDateString("dd.MM.yyyy")}"
            binding.dateTime.contentDescription =
                "${calendar.getLongWeekDay()} ${calendar.time.toDateString("MM.dd.yyyy")}"
            binding.dateHours.text =
                "${calendar.time.toDateString("HH:mm")} - ${item.endTime.toDateString("HH:mm")}"
            TextViewCompat.setCompoundDrawableTintList(
                binding.moreInfoBtn,
                ColorStateList.valueOf(CityInteractor.cityColorInt)
            )
            TextViewCompat.setCompoundDrawableTintList(
                binding.qrCodeBtn,
                ColorStateList.valueOf(CityInteractor.cityColorInt)
            )

            when (item.apptStatus) {
                Appointment.STATE_REJECTED -> {
                    setAppointmentState(
                        resId = R.string.apnmt_002_item_state_rejected,
                        color = STATE_COLOR_CANCELED
                    )
                    binding.qrCodeBtn.disable()
                    binding.moreInfoBtn.enable()
                }

                Appointment.STATE_CANCELED -> {
                    setAppointmentState(
                        resId = R.string.apnmt_002_item_state_canceled,
                        color = STATE_COLOR_CANCELED
                    )
                    binding.qrCodeBtn.disable()
                    binding.moreInfoBtn.enable()
                }

                Appointment.STATE_PENDING -> {
                    setAppointmentState(
                        resId = R.string.apnmt_002_item_state_pending,
                        color = STATE_COLOR_PENDING
                    )
                    binding.qrCodeBtn.disable()
                    binding.moreInfoBtn.enable()
                }

                Appointment.STATE_UPDATED -> {
                    setAppointmentState(
                        resId = R.string.apnmt_002_item_state_updated,
                        color = STATE_COLOR_UPDATED
                    )
                    binding.qrCodeBtn.enable()
                    binding.moreInfoBtn.enable()
                }

                Appointment.STATE_CONFIRMED -> {
                    if (item.endTime.isInPast) {
                        setAppointmentState(
                            resId = R.string.apnmt_002_item_state_historic,
                            color = STATE_COLOR_HISTORIC
                        )
                        binding.moreInfoBtn.enable()
                        binding.qrCodeBtn.disable()
                        binding.appoinmentCard.disable()
                    } else {
                        binding.appointmentStateText.setVisible(false)
                        binding.moreInfoBtn.enable()
                        binding.qrCodeBtn.enable()
                    }
                }
            }
            binding.appointmentCardContainer.setAccessibilityRole(AccessibilityRole.Button)
            binding.appointmentCardContainer.setOnClickListener {
                it.findNavController().navigate(
                    AppointmentsOverviewDirections.actionAppointmentsOverviewToAppointmentDetails(
                        item
                    )
                )
            }
            binding.moreInfoBtn.setOnClickListener {
                it.findNavController().navigate(
                    AppointmentsOverviewDirections.actionAppointmentsOverviewToAppointmentDetails(
                        item
                    )
                )
            }
            binding.qrCodeBtn.setOnClickListener {
                it.findNavController().navigate(
                    AppointmentsOverviewDirections.actionAppointmentsOverviewToAppointmentQR(item.waitingNo, item.uuid)
                )
            }
        }

        private fun setAppointmentState(@StringRes resId: Int, @ColorInt color: Int) {
            binding.appointmentStateText.text =
                binding.root.context.getString(resId)
            binding.appointmentStateText.setBackgroundColor(color)
            binding.appointmentStateText.isVisible = true
        }
    }
}
