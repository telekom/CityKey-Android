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

package com.telekom.citykey.view.services.appointments

import android.graphics.PorterDuff
import android.graphics.PorterDuffColorFilter
import android.graphics.drawable.LayerDrawable
import android.os.Bundle
import android.view.View
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import com.telekom.citykey.BuildConfig
import com.telekom.citykey.R
import com.telekom.citykey.common.GlideApp
import com.telekom.citykey.databinding.ServicePageAppointmentFragmentBinding
import com.telekom.citykey.domain.city.CityInteractor
import com.telekom.citykey.utils.DialogUtil
import com.telekom.citykey.utils.NetworkConnection
import com.telekom.citykey.utils.extensions.AccessibilityRole
import com.telekom.citykey.utils.extensions.setAccessibilityRole
import com.telekom.citykey.utils.extensions.setVisible
import com.telekom.citykey.utils.extensions.viewBinding
import com.telekom.citykey.view.MainFragment
import org.koin.androidx.viewmodel.ext.android.viewModel

class AppointmentService : MainFragment(R.layout.service_page_appointment_fragment) {

    private val viewModel: AppointmentServiceViewModel by viewModel()
    private val args: AppointmentServiceArgs by navArgs()
    private val binding by viewBinding(ServicePageAppointmentFragmentBinding::bind)

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.toolbarAppointmentServices.title = args.service.service
        setupToolbar(binding.toolbarAppointmentServices)
        setupViews()
        subscribeUI()
    }

    private fun subscribeUI() {
        viewModel.updates.observe(viewLifecycleOwner) {
            if (it > 0) binding.badge.text = it.toString()
            binding.badge.setVisible(it > 0)
        }
    }

    private fun setupViews() {
        binding.makeAppointmentButton.button.setBackgroundColor(CityInteractor.cityColorInt)
        (binding.badge.background as LayerDrawable)
            .findDrawableByLayerId(R.id.mainLayer)
            .colorFilter = PorterDuffColorFilter(CityInteractor.cityColorInt, PorterDuff.Mode.SRC_IN)

        GlideApp.with(this)
            .load(BuildConfig.IMAGE_URL + args.service.image)
            .centerCrop()
            .into(binding.image)

        binding.myAppointmentsLabel.setAccessibilityRole(AccessibilityRole.Button)
        binding.fullDescription.loadData(args.service.description, "text/html", "UTF-8")
        val myAppointmentsLabel = args.service.helpLinkTitle ?: getString(R.string.apnmt_001_my_appointments_button)
        binding.myAppointmentsLabel.text = myAppointmentsLabel
        // There will be only one action in case of appointments (double checked it with SOL team)
        val appointmentUri = args.service.serviceAction?.first()?.androidUri ?: ""
        binding.makeAppointmentButton.text = args.service.serviceAction?.first()?.visibleText
        binding.makeAppointmentButton.setOnClickListener {
            if (!NetworkConnection.checkInternetConnection(requireContext())) {
                DialogUtil.showNoInternetDialog(requireContext())
            } else {
                findNavController().navigate(
                    AppointmentServiceDirections
                        .actionAppointmentServiceToAppointmentWeb(appointmentUri, args.service)
                )
            }
        }

        binding.existingAppointments.setOnClickListener {
            findNavController().navigate(AppointmentServiceDirections.actionAppointmentServiceToAppointmentsOverview())
        }
    }
}
