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
 * SPDX-FileCopyrightText: 2025 Deutsche Telekom AG
 * SPDX-License-Identifier: Apache-2.0
 * License-Filename: LICENSES/Apache-2.0.txt
 */

package com.telekom.citykey.view.services.appointments.qr

import android.os.Bundle
import android.view.View
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import com.telekom.citykey.R
import com.telekom.citykey.databinding.AppointmentsQrFragmentBinding
import com.telekom.citykey.utils.extensions.viewBinding
import com.telekom.citykey.view.MainFragment
import org.koin.androidx.viewmodel.ext.android.viewModel

class AppointmentQR : MainFragment(R.layout.appointments_qr_fragment) {

    private val viewModel: AppointmentQRViewModel by viewModel()
    private val args: AppointmentQRArgs by navArgs()
    private val binding by viewBinding(AppointmentsQrFragmentBinding::bind)

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        viewModel.onViewCreated(args.uuid)
        setupToolbar(binding.toolbarAppointmentQR)

        binding.waitingNumber.text = args.waitingNo

        subscribeUi()
    }

    private fun subscribeUi() {
        viewModel.qrBitmap.observe(viewLifecycleOwner) {
            binding.qrCode.setImageBitmap(it)
        }

        viewModel.userLoggedOut.observe(viewLifecycleOwner) {
            findNavController().popBackStack(R.id.services, false)
        }
    }
}
