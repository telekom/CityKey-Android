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

package com.telekom.citykey.view.services.appointments.web

import android.os.Bundle
import android.view.MenuItem
import android.view.View
import android.webkit.WebResourceRequest
import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.activity.OnBackPressedCallback
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import com.telekom.citykey.R
import com.telekom.citykey.databinding.WebviewFragmentBinding
import com.telekom.citykey.domain.track.AdjustManager
import com.telekom.citykey.utils.DialogUtil
import com.telekom.citykey.utils.extensions.getColor
import com.telekom.citykey.utils.extensions.viewBinding
import com.telekom.citykey.view.MainFragment
import com.telekom.citykey.view.main.MainActivity
import org.koin.android.ext.android.inject
import org.koin.androidx.viewmodel.ext.android.viewModel

class AppointmentWeb : MainFragment(R.layout.webview_fragment, true) {

    private val webViewModel: AppointmentWebViewModel by viewModel()
    private val args: AppointmentWebArgs by navArgs()
    private val binding by viewBinding(WebviewFragmentBinding::bind)
    private val adjustManager: AdjustManager by inject()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupAppointmentToolbar()
        (requireActivity() as? MainActivity)?.hideBottomNavBar()
        setupWebView()
        subscribeUi()
        askForPermission()
    }

    private fun setupWebView() {
        binding.webView.webViewClient = object : WebViewClient() {
            override fun shouldOverrideUrlLoading(view: WebView?, request: WebResourceRequest?): Boolean {
                if (request?.url?.toString()?.equals(webViewModel.callbackUrl) == true) {
                    adjustManager.trackEvent(R.string.appointment_created)
                    findNavController().navigateUp()
                }
                // Prevent the webview from redirecting to external browser so all the cookies will stay here
                return false
            }
        }
    }

    private fun setupAppointmentToolbar() {
        setupToolbar(binding.webviewToolbar)
        (requireActivity() as? MainActivity)?.supportActionBar?.setHomeAsUpIndicator(R.drawable.ic_profile_close)
        binding.webviewToolbar.setNavigationIconTint(getColor(R.color.onSurface))
        binding.webviewToolbar.setTitle(R.string.appointment_webview_title)
        requireActivity().onBackPressedDispatcher.addCallback(
            viewLifecycleOwner,
            object : OnBackPressedCallback(true) {
                override fun handleOnBackPressed() {
                    onProcessCancel()
                }
            }
        )
    }

    private fun onProcessCancel() {
        DialogUtil.showDialogPositiveNegative(
            context = requireContext(),
            title = R.string.appointment_title,
            message = R.string.appointment_web_cancel_dialog_message,
            positiveBtnLabel = R.string.appointment_web_cancel_dialog_btnClose,
            negativeBtnLabel = R.string.appointment_web_cancel_dialog_btnCancel,
            positiveClickListener = {
                binding.webView.destroy()
                findNavController().navigateUp()
            }
        )
    }

    private fun subscribeUi() {
        webViewModel.userDataPost.observe(viewLifecycleOwner) { userData ->
            if (args.baseUrl.isEmpty()) {
                DialogUtil.showDialog(
                    context = requireContext(),
                    message = getString(R.string.dialog_technical_error_message),
                    buttonLabel = R.string.dialog_button_ok,
                    listener = {
                        findNavController().navigateUp()
                    }
                )
            } else {
                binding.webView.postUrl(args.baseUrl, userData.toByteArray())
            }
        }

        webViewModel.userIsLoggedOut.observe(viewLifecycleOwner) {
            if (it) {
                findNavController().popBackStack(R.id.services, false)
            }
        }
    }

    private fun askForPermission() {
        DialogUtil.showDialogPositiveNegative(
            context = requireContext(),
            title = R.string.appointment_web_private_data_permission_title,
            message = R.string.appointment_web_private_data_permission_message,
            positiveBtnLabel = R.string.appointment_web_private_data_permission_btnPositive,
            negativeBtnLabel = R.string.appointment_web_private_data_permission_btnNegative,
            positiveClickListener = {
                webViewModel.onGivePermissionClicked(
                    true,
                    args.service.serviceParams ?: emptyMap()
                )
            },
            negativeClickListener = {
                webViewModel.onGivePermissionClicked(false, emptyMap())
            }
        )
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            android.R.id.home -> {
                onProcessCancel()
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        (requireActivity() as? MainActivity)?.revealBottomNavBar()
    }
}
