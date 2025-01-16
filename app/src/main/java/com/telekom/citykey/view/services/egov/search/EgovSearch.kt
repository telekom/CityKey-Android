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

package com.telekom.citykey.view.services.egov.search

import android.app.Activity
import android.os.Bundle
import android.view.View
import android.view.animation.AccelerateDecelerateInterpolator
import android.view.inputmethod.InputMethodManager
import androidx.core.widget.doOnTextChanged
import androidx.navigation.fragment.findNavController
import androidx.transition.Transition
import androidx.transition.TransitionInflater
import androidx.transition.TransitionListenerAdapter
import com.telekom.citykey.R
import com.telekom.citykey.databinding.EgovSearchBinding
import com.telekom.citykey.domain.track.AdjustManager
import com.telekom.citykey.models.egov.EgovLinkTypes
import com.telekom.citykey.utils.DialogUtil
import com.telekom.citykey.utils.extensions.openLink
import com.telekom.citykey.utils.extensions.viewBinding
import com.telekom.citykey.view.MainFragment
import org.koin.android.ext.android.inject
import org.koin.androidx.viewmodel.ext.android.viewModel

class EgovSearch : MainFragment(R.layout.egov_search) {

    private val binding: EgovSearchBinding by viewBinding(EgovSearchBinding::bind)
    private val viewModel: EgovSearchViewModel by viewModel()

    private var adapter: EgovServicesResultsAdapter? = null
    private val adjustManager: AdjustManager by inject()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        sharedElementEnterTransition =
            TransitionInflater.from(requireContext()).inflateTransition(android.R.transition.move).apply {
                interpolator = AccelerateDecelerateInterpolator()
                duration = 500
                addListener(object : TransitionListenerAdapter() {
                    override fun onTransitionEnd(transition: Transition) {
                        binding.editText.setText("")
                        binding.editText.requestFocus()
                        (requireContext().getSystemService(Activity.INPUT_METHOD_SERVICE) as InputMethodManager)
                            .showSoftInput(binding.editText, 0)
                    }
                })
            }

        adapter = EgovServicesResultsAdapter(
            { service ->
                viewModel.onServiceSelected(service)
                (requireContext().getSystemService(Activity.INPUT_METHOD_SERVICE) as InputMethodManager)
                    .hideSoftInputFromWindow(binding.editText.windowToken, 0)
                if (service.longDescription.isNotEmpty() || service.linksInfo.size > 1)
                    findNavController().navigate(EgovSearchDirections.toEgovDescDetails(service))
                else {
                    when (service.linksInfo[0].linkType) {
                        EgovLinkTypes.EID_FORM, EgovLinkTypes.FORM -> {
                            adjustManager.trackEvent(R.string.open_egov_external_url)
                            findNavController()
                                .navigate(
                                    EgovSearchDirections.toAuthWebView2(
                                        service.linksInfo[0].link,
                                        service.serviceName
                                    ).setHasSensitiveInfo(true)
                                )
                        }
                        EgovLinkTypes.PDF, EgovLinkTypes.WEB -> {
                            adjustManager.trackEvent(R.string.open_egov_external_url)
                            openLink(service.linksInfo[0].link)
                        }
                        else -> DialogUtil.showTechnicalError(requireContext())
                    }
                }
            },
            {
                binding.editText.setText(it)
            }
        )
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setupToolbar(binding.toolbarEgovSearch)

        binding.searchResults.adapter = adapter

        binding.editText.doOnTextChanged { text, _, _, _ ->
            viewModel.onSearchQueryChanged(text.toString())
        }

        subscribeUi()
    }

    private fun subscribeUi() {
        viewModel.searchResults.observe(viewLifecycleOwner) {
            adapter?.submitList(it)
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        adapter = null
    }
}
