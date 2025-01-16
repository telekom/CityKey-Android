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

package com.telekom.citykey.view.infobox.detailed_infobox

import android.content.ActivityNotFoundException
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.text.SpannableString
import android.text.style.ForegroundColorSpan
import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import android.view.View
import android.webkit.WebResourceRequest
import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.core.view.forEach
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import com.telekom.citykey.R
import com.telekom.citykey.databinding.InfoboxDetailedFragmentBinding
import com.telekom.citykey.domain.city.CityInteractor
import com.telekom.citykey.utils.DialogUtil
import com.telekom.citykey.utils.extensions.*
import com.telekom.citykey.view.MainFragment
import com.telekom.citykey.view.infobox.InfoBoxViewModel
import com.telekom.citykey.view.main.MainActivity
import org.koin.androidx.viewmodel.ext.android.sharedViewModel
import timber.log.Timber
import java.util.*

class DetailedInfoBox : MainFragment(R.layout.infobox_detailed_fragment) {
    private val viewModel: InfoBoxViewModel by sharedViewModel()
    private val args: DetailedInfoBoxArgs by navArgs()
    private val binding by viewBinding(InfoboxDetailedFragmentBinding::bind)

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupToolbar(binding.toolbarDetailedInfo)

        binding.icon.loadFromOSCA(args.content.category.icon)
        binding.title.text = args.content.headline
        binding.description.text = args.content.description
        binding.category.text = args.content.category.name

        if (args.content.creationDate.isToday) {
            binding.date.text = args.content.creationDate.getHoursAndMins()
                .format(args.content.creationDate)
        } else {
            val dateAsCalendar = args.content.creationDate.toCalendar()
            binding.date.text = "${dateAsCalendar.getShortMonthName()} ${dateAsCalendar.get(Calendar.DAY_OF_MONTH)}"
        }
        binding.fullDescription.apply {
            webViewClient = pageLinkHandlerWebViewClient
            linkifyAndLoadNonHtmlTaggedData(args.content.details)
        }
        binding.callToActionBtn.setVisible(!args.content.buttonText.isNullOrEmpty() && !args.content.buttonAction.isNullOrEmpty())

        binding.callToActionBtn.apply {
            button.setBackgroundColor(CityInteractor.cityColorInt)
            text = args.content.buttonText
            setOnClickListener { args.content.buttonAction?.let { link -> openLink(link) } }
        }

        binding.labelLinks.setVisible(args.content.attachments.isNotEmpty())
        binding.linksList.adapter = DetailedInfoBoxLinksAdapter(
            args.content.attachments,
            this::openLink,
        )

        observeUi()
        with(requireActivity() as MainActivity) {
            markLoadCompleteIfFromDeeplink(getString(R.string.deeplink_infobox_message), true)
        }
    }

    private fun observeUi() {
        viewModel.deletionSuccessful.observe(viewLifecycleOwner) {
            findNavController().previousBackStackEntry?.savedStateHandle?.set("MailDeletionSuccessful", true)
            findNavController().popBackStack(R.id.infoBox, false)
        }
        viewModel.shouldPromptLogin.observe(viewLifecycleOwner) {
            findNavController().popBackStack()
        }
        viewModel.showNoInternetDialog.observe(viewLifecycleOwner) {
            DialogUtil.showNoInternetDialog(requireContext())
        }
    }

    private val pageLinkHandlerWebViewClient by lazy {
        object : WebViewClient() {

            override fun shouldOverrideUrlLoading(view: WebView?, request: WebResourceRequest?): Boolean {
                request?.url?.let {
                    try {
                        openLink(it.toString())
                    } catch (e: Exception) {
                        Timber.e(e, "Error in opening Custom Tab link  => $it")
                        tryDelegatingUriToSystem(it)
                    }
                }
                return true
            }
        }
    }

    private fun tryDelegatingUriToSystem(uri: Uri) {
        try {
            startActivity(Intent(Intent.ACTION_VIEW, uri))
        } catch (e: ActivityNotFoundException) {
            Timber.e(e, "No app found to handle the URI => $uri")
            DialogUtil.showTechnicalError(requireContext())
        }
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        inflater.inflate(R.menu.infobox_detailed_menu, menu)
        menu.forEach {
            val spannableTitle = SpannableString(it.title)
            spannableTitle.setSpan(ForegroundColorSpan(CityInteractor.cityColorInt), 0, spannableTitle.length, 0)
            it.title = spannableTitle
        }
        super.onCreateOptionsMenu(menu, inflater)
    }

    override fun onOptionsItemSelected(item: MenuItem) = when (item.itemId) {
        R.id.actionDelete -> {
            viewModel.onDelete(args.content)
            true
        }
        else -> super.onOptionsItemSelected(item)
    }
}
