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

package com.telekom.citykey.view.services.waste_calendar.service_details

import android.os.Bundle
import android.view.View
import androidx.navigation.NavOptions
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import com.iammonk.htmlspanner.HtmlSpanner
import com.telekom.citykey.BuildConfig
import com.telekom.citykey.R
import com.telekom.citykey.common.GlideApp
import com.telekom.citykey.databinding.ServicePageWasteCalendarFragmentBinding
import com.telekom.citykey.domain.city.CityInteractor
import com.telekom.citykey.domain.track.AdjustManager
import com.telekom.citykey.utils.DialogUtil
import com.telekom.citykey.utils.extensions.AccessibilityRole
import com.telekom.citykey.utils.extensions.autoLinkAll
import com.telekom.citykey.utils.extensions.setAccessibilityRole
import com.telekom.citykey.utils.extensions.setVisible
import com.telekom.citykey.utils.extensions.showDialog
import com.telekom.citykey.utils.extensions.viewBinding
import com.telekom.citykey.view.MainFragment
import com.telekom.citykey.view.main.MainActivity
import com.telekom.citykey.view.services.waste_calendar.address_change.WasteCalendarAddress
import com.telekom.citykey.view.services.waste_calendar.address_change.WasteCalendarAddress.Companion.FRAGMENT_TAG_ADDRESS
import com.telekom.citykey.view.services.waste_calendar.filters.WasteFilters
import com.telekom.citykey.view.services.waste_calendar.filters.WasteFilters.Companion.FRAGMENT_TAG_FILTERS
import org.koin.android.ext.android.inject
import org.koin.androidx.viewmodel.ext.android.viewModel

class WasteCalendarDetails : MainFragment(R.layout.service_page_waste_calendar_fragment) {

    private val adjustManager: AdjustManager by inject()
    private val viewModel: WasteCalendarDetailsViewModel by viewModel()
    private val binding by viewBinding(ServicePageWasteCalendarFragmentBinding::bind)
    private val args: WasteCalendarDetailsArgs by navArgs()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupToolbar(binding.toolbarWasteCalendarServices)
        binding.showWasteCalendarButton.button.setBackgroundColor(CityInteractor.cityColorInt)
        subscribeUi()
    }

    private fun subscribeUi() {

        viewModel.wasteCalendarAvailable.observe(viewLifecycleOwner) {
            viewModel.getWasteCalendarFilterOptions()
        }

        viewModel.showRetryDialog.observe(viewLifecycleOwner) {
            DialogUtil.showRetryDialog(requireContext(), viewModel::onRetryRequired) {
                binding.showWasteCalendarButton.stopLoading()
                viewModel.onRetryCanceled()
            }
        }

        viewModel.technicalError.observe(viewLifecycleOwner) {
            DialogUtil.showTechnicalError(requireContext())
            binding.showWasteCalendarButton.stopLoading()
        }

        viewModel.launchFtu.observe(viewLifecycleOwner) {
            WasteCalendarAddress { isSuccess ->
                if (isSuccess) {
                    WasteFilters { filtersApplied ->
                        if (filtersApplied) {
                            adjustManager.trackEvent(R.string.open_waste_calendar)
                            findNavController().navigate(
                                WasteCalendarDetailsDirections.actionWasteCalendarDetailsToWasteCalendar(
                                    binding.toolbarWasteCalendarServices.title.toString(), false,
                                    args.shouldNavigateToNextMonth
                                )
                            )
                        } else {
                            binding.showWasteCalendarButton.stopLoading()
                        }
                    }.showDialog(childFragmentManager, FRAGMENT_TAG_FILTERS)
                } else {
                    binding.showWasteCalendarButton.stopLoading()
                }
            }.showDialog(childFragmentManager, FRAGMENT_TAG_ADDRESS)
        }

        viewModel.userLoggedOut.observe(viewLifecycleOwner) {
            findNavController().popBackStack(R.id.services, false)
        }

        viewModel.appliedFilters.observe(viewLifecycleOwner) {
            binding.showWasteCalendarButton.stopLoading()
            if ((viewModel.appliedFilters.value?.isEmpty() == true) || (viewModel.checkSelectedPickupInFilterOptions()
                    .isNotEmpty())
            ) {
                adjustManager.trackEvent(R.string.open_waste_calendar)
                findNavController().navigate(
                    WasteCalendarDetailsDirections.actionWasteCalendarDetailsToWasteCalendar(
                        binding.toolbarWasteCalendarServices.title.toString(),
                        false,
                        args.shouldNavigateToNextMonth
                    )
                )
            } else {
                DialogUtil.showInfoDialog(
                    requireContext(),
                    R.string.wc_005_dialog_options_Title,
                    R.string.wc_005_error_message_select_pickups,
                    okBtnClickListener = {
                        adjustManager.trackEvent(R.string.open_waste_calendar)
                        findNavController().navigate(
                            WasteCalendarDetailsDirections.actionWasteCalendarDetailsToWasteCalendar(
                                binding.toolbarWasteCalendarServices.title.toString(),
                                true,
                                args.shouldNavigateToNextMonth
                            )
                        )
                    }
                )
            }
        }

        viewModel.service.observe(viewLifecycleOwner) { service ->
            if (service == null) {
                findNavController().navigate(
                    R.id.services,
                    args = null,
                    NavOptions.Builder().setPopUpTo(R.id.services_graph, false).build()
                )
            } else {
                binding.toolbarWasteCalendarServices.title = service.service
                setupToolbar(binding.toolbarWasteCalendarServices)
                binding.showWasteCalendarButton.text = service.serviceAction?.first()?.visibleText
                GlideApp.with(this)
                    .load(BuildConfig.IMAGE_URL + service.image)
                    .centerCrop()
                    .into(binding.image)
                binding.fullDescription.text = HtmlSpanner().fromHtml(service.description)
                binding.fullDescription.autoLinkAll()
                if (!service.helpLinkTitle.isNullOrBlank()) {
                    binding.wasteInfoButton.apply {
                        setVisible(true)
                        setAccessibilityRole(AccessibilityRole.Button)
                        contentDescription = service.helpLinkTitle
                    }
                    binding.wasteInfo.text = service.helpLinkTitle
                    binding.wasteInfoButton.setOnClickListener {
                        findNavController().navigate(
                            WasteCalendarDetailsDirections.actionWasteCalendarDetailsToServiceHelp(service)
                        )
                    }
                }
                binding.showWasteCalendarButton.setOnClickListener {
                    binding.showWasteCalendarButton.startLoading()
                    viewModel.onOpenWasteCalendarClicked()
                }

                if (requireActivity().intent.data != null) {
                    binding.showWasteCalendarButton.performClick()
                }
            }
            (requireActivity() as MainActivity).hideSplashScreen()
        }
    }
}
