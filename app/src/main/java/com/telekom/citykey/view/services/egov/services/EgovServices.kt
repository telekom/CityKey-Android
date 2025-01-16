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

package com.telekom.citykey.view.services.egov.services

import android.os.Bundle
import android.view.View
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import com.telekom.citykey.R
import com.telekom.citykey.databinding.EgovServicesOverviewBinding
import com.telekom.citykey.domain.track.AdjustManager
import com.telekom.citykey.models.egov.EgovLinkTypes
import com.telekom.citykey.utils.DialogUtil
import com.telekom.citykey.utils.extensions.openLink
import com.telekom.citykey.utils.extensions.viewBinding
import com.telekom.citykey.view.MainFragment
import org.koin.android.ext.android.inject
import org.koin.androidx.viewmodel.ext.android.viewModel

class EgovServices : MainFragment(R.layout.egov_services_overview) {

    private val binding: EgovServicesOverviewBinding by viewBinding(EgovServicesOverviewBinding::bind)
    private val args: EgovServicesArgs by navArgs()
    private var adapter: EgovServicesAdapter? = null
    private val adjustManager: AdjustManager by inject()
    private val viewModel: EgovServicesViewModel by viewModel()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupToolbar(binding.toolbarEgovServices)
        binding.toolbarEgovServices.title = viewModel.loadEgovGroupData(args.groupId)?.groupName

        adapter = EgovServicesAdapter { service ->
            if (service.longDescription.isNotEmpty() || service.linksInfo.size > 1)
                findNavController().navigate(EgovServicesDirections.toEgovDescDetails(service))
            else
                when (service.linksInfo[0].linkType) {
                    EgovLinkTypes.EID_FORM, EgovLinkTypes.FORM -> {
                        adjustManager.trackEvent(R.string.open_egov_external_url)
                        findNavController()
                            .navigate(
                                EgovServicesDirections.toAuthWebView(service.linksInfo[0].link, service.serviceName)
                                    .setHasSensitiveInfo(true)
                            )
                    }

                    EgovLinkTypes.PDF, EgovLinkTypes.WEB -> {
                        adjustManager.trackEvent(R.string.open_egov_external_url)
                        openLink(service.linksInfo[0].link)
                    }

                    else -> DialogUtil.showTechnicalError(requireContext())
                }
        }
        binding.servicesList.adapter = adapter
        adapter?.submitList(viewModel.loadEgovGroupData(args.groupId)?.services)
    }

    override fun onDestroy() {
        adapter = null
        super.onDestroy()
    }
}
