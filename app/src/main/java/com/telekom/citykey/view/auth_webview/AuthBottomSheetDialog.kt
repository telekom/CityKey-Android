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

package com.telekom.citykey.view.auth_webview

import android.app.Dialog
import android.content.Context
import android.content.Intent
import android.content.res.Resources
import android.graphics.Color
import android.nfc.NfcManager
import android.os.Bundle
import android.provider.Settings
import android.view.KeyEvent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.webkit.WebResourceRequest
import android.webkit.WebView
import android.webkit.WebViewClient
import android.widget.FrameLayout
import androidx.core.text.HtmlCompat
import androidx.fragment.app.DialogFragment
import androidx.lifecycle.lifecycleScope
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.telekom.citykey.R
import com.telekom.citykey.databinding.AuthBsdialogFragmentBinding
import com.telekom.citykey.domain.ausweiss_app.IdentState
import com.telekom.citykey.domain.track.AdjustManager
import com.telekom.citykey.utils.DialogUtil
import com.telekom.citykey.utils.extensions.attemptOpeningWebViewUri
import com.telekom.citykey.utils.extensions.getColor
import com.telekom.citykey.utils.extensions.linkifyAndLoadNonHtmlTaggedData
import com.telekom.citykey.utils.extensions.openApp
import com.telekom.citykey.utils.extensions.openLink
import com.telekom.citykey.utils.extensions.safeRun
import com.telekom.citykey.utils.extensions.setVisible
import com.telekom.citykey.utils.extensions.viewBinding
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import org.koin.android.ext.android.inject
import org.koin.androidx.viewmodel.ext.android.viewModel
import org.koin.core.parameter.parametersOf

class AuthBottomSheetDialog(private val url: String, private val resultListener: (String?) -> Unit) :
    BottomSheetDialogFragment() {

    companion object {
        private const val AUSWEISSAPP_HELP_LINK =
            "https://www.ausweisapp.bund.de/hilfe-und-support/haeufig-gestellte-fragen/"
        private const val AUSWEISSAPP_PACKAGE = "com.governikus.ausweisapp2"
    }

    private val viewModel: AuthBottomSheetDialogViewModel by viewModel { parametersOf(url) }
    private val binding by viewBinding(AuthBsdialogFragmentBinding::bind)

    private lateinit var visibleView: View
    private var backstackView: View? = null
    private val adjustManager: AdjustManager by inject()

    init {
        isCancelable = false
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setStyle(DialogFragment.STYLE_NORMAL, R.style.AuthDialogTheme)
    }

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        return super.onCreateDialog(savedInstanceState).also(this::setupDialog)
    }

    private fun setupDialog(dialogFragment: Dialog) {
        dialogFragment.setOnShowListener { dialog ->
            val params = binding.root.layoutParams as FrameLayout.LayoutParams
            params.height = Resources.getSystem().displayMetrics.heightPixels
            binding.root.layoutParams = params

            val bottomSheetDialog: BottomSheetDialog = dialog as BottomSheetDialog
            val bottomSheetLayout: FrameLayout? =
                bottomSheetDialog.findViewById(com.google.android.material.R.id.design_bottom_sheet)
            BottomSheetBehavior.from(bottomSheetLayout!!).state = BottomSheetBehavior.STATE_EXPANDED
        }

        dialogFragment.setOnKeyListener { _, keyCode, event ->
            if (keyCode == KeyEvent.KEYCODE_BACK && event.action == KeyEvent.ACTION_UP) {
                if (backstackView == null) dismiss()
                else {
                    replaceVisibleView(backstackView!!)
                    backstackView = null
                }
                return@setOnKeyListener true
            } else return@setOnKeyListener false
        }
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View =
        inflater.inflate(R.layout.auth_bsdialog_fragment, container, false)

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.toolbar.setNavigationIcon(R.drawable.ic_profile_close)
        binding.toolbar.setNavigationIconTint(getColor(R.color.onSurface))
        binding.toolbar.setNavigationContentDescription(R.string.accessibility_btn_close)
        binding.toolbar.setNavigationOnClickListener {
            DialogUtil.showCancelProcessDialog(requireContext()) { super.dismiss() }
        }

        val nfcAdapter = (requireContext().getSystemService(Context.NFC_SERVICE) as? NfcManager)?.defaultAdapter
        if (nfcAdapter?.isEnabled == true) {
            viewModel.onFeatureReady()
            visibleView = binding.loadingLayout
            binding.toolbar.setTitle(R.string.egov_info_title)
            binding.loadingLayout.setVisible(true)
        } else {
            visibleView = binding.nfcLayout
            binding.nfcLayout.setVisible(true)
        }

        binding.accessRightsBtn.setOnClickListener {
            viewModel.onAccessAccepted()
            binding.accessRightsBtn.startLoading()
        }

        binding.submitPin.setOnClickListener {
            binding.submitPin.startLoading()
            viewModel.onSubmitPin(binding.pinInputView.text)
        }

        binding.submitCan.setOnClickListener {
            binding.submitCan.startLoading()
            viewModel.onSubmitCan(binding.canInputView.text)
        }

        binding.submitPuk.setOnClickListener {
            binding.submitPuk.startLoading()
            viewModel.onSubmitPuk(binding.pukInputView.text)
        }

        binding.enterCanBtn.setOnClickListener {
            replaceVisibleView(binding.canLayout)
            binding.toolbar.setTitle(R.string.egov_can_title)
        }

        binding.enterPukBtn.setOnClickListener {
            replaceVisibleView(binding.pukLayout)
        }

        binding.detailedCertificateBtn.setOnClickListener {
            backstackView = visibleView
            replaceVisibleView(binding.certificateLayout)
        }

        binding.pinPad.attachToPinInput(binding.pinInputView)
        binding.canPad.attachToPinInput(binding.canInputView)
        binding.pukPad.attachToPinInput(binding.pukInputView)

        binding.tryAgainButton.setOnClickListener {
            super.dismiss()
            resultListener(null)
        }

        val helpClickListener: (View) -> Unit = {
            backstackView = visibleView
            replaceVisibleView(binding.helpLayout)
        }

        binding.fiveDigitPinMsg.setOnClickListener(helpClickListener)
        binding.pinHelpBtn.setOnClickListener(helpClickListener)
        binding.pukHelpBtn.setOnClickListener(helpClickListener)
        binding.canHelpBtn.setOnClickListener(helpClickListener)
        binding.cardHelpBtn.setOnClickListener(helpClickListener)

        binding.toAusweissAppBtn.setOnClickListener {
            openApp(AUSWEISSAPP_PACKAGE)
        }

        binding.toAusweissAppHelpBtn.setOnClickListener {
            openLink(AUSWEISSAPP_HELP_LINK)
        }

        binding.turnOnNfcBtn.setOnClickListener {
            startActivity(Intent(Settings.ACTION_NFC_SETTINGS))
        }

        subscribeUi()
    }

    override fun onResume() {
        super.onResume()

        val nfcAdapter = (requireContext().getSystemService(Context.NFC_SERVICE) as? NfcManager)?.defaultAdapter
        if (nfcAdapter?.isEnabled == true) {
            viewModel.onFeatureReady()
            if (visibleView == binding.nfcLayout) replaceVisibleView(binding.loadingLayout)
        }
    }

    private fun subscribeUi() {
        viewModel.newState.observe(viewLifecycleOwner) { state ->
            when (state) {
                is IdentState.LOADING -> {
                    replaceVisibleView(binding.loadingLayout)
                }

                is IdentState.ATTACH_CARD -> {
                    binding.toolbar.setTitle(R.string.egov_attach_card_title)
                    replaceVisibleView(binding.cardInfoLayout)
                }

                is IdentState.InsertPin -> {
                    binding.toolbar.setTitle(R.string.egov_pin_title)
                    binding.submitPin.stopLoading()
                    binding.pinInputView.errorText = when (state.retries) {
                        2 -> getString(R.string.egov_pin_error_two_retries)
                        1 -> getString(R.string.egov_pin_error_one_retry)
                        else -> null
                    }
                    replaceVisibleView(binding.pinLayout)
                }

                is IdentState.INSERT_CAN -> {
                    binding.toolbar.setTitle(R.string.egov_caninfo_title)
                    binding.submitCan.stopLoading()
                    if (visibleView == binding.canLayout) {
                        binding.canInputView.errorText = getString(R.string.egov_can_error)
                    } else {
                        replaceVisibleView(binding.canInfoLayout)
                    }
                }

                is IdentState.ShowInfo -> {
                    val accessRightsText = state.mappedAccessRights.joinToString(separator = "<br/>") {
                        "&#8250; ${getString(it)}"
                    }
                    binding.accessRightsText.text =
                        HtmlCompat.fromHtml(accessRightsText, HtmlCompat.FROM_HTML_MODE_LEGACY)
                    binding.provider.text = state.certificateInfo.subjectName

                    if (state.certificateInfo.purpose.isNotEmpty()) {
                        binding.purpose.text = state.certificateInfo.purpose
                        binding.purpose.setVisible(true)
                        binding.purposeLabel.setVisible(true)
                    }

                    binding.providerValidity.text = state.certificateValidity.validity

                    binding.subjectInfo.apply {
                        webViewClient = pageLinkHandlerWebViewClient
                        setBackgroundColor(Color.TRANSPARENT)
                        linkifyAndLoadNonHtmlTaggedData(state.certificateInfo.subject)
                    }
                    binding.issuerInfo.apply {
                        webViewClient = pageLinkHandlerWebViewClient
                        setBackgroundColor(Color.TRANSPARENT)
                        linkifyAndLoadNonHtmlTaggedData(state.certificateInfo.issuer)
                    }
                    binding.providerDetailed.apply {
                        webViewClient = pageLinkHandlerWebViewClient
                        setBackgroundColor(Color.TRANSPARENT)
                        linkifyAndLoadNonHtmlTaggedData(state.certificateInfo.termsOfUsage)
                    }

                    binding.accessRightsBtn.setupNormalStyle()
                    binding.toolbar.setTitle(R.string.egov_info_title)

                    replaceVisibleView(binding.accessRightsLayout)
                }

                is IdentState.INSERT_PUK -> {
                    binding.toolbar.setTitle(R.string.egov_caninfo_title)
                    binding.submitPuk.stopLoading()
                    if (visibleView == binding.pukLayout) {
                        binding.pukInputView.errorText = getString(R.string.egov_puk_error)
                    } else {
                        replaceVisibleView(binding.pukInfoLayout)
                    }
                }

                is IdentState.Error -> {
                    state.result?.let { result ->
                        binding.errorLabel.text = result.description
                        binding.errorMessage.text = result.message
                    }
                    replaceVisibleView(binding.errorLayout)
                }

                is IdentState.Success -> {
                    adjustManager.trackEvent(R.string.eid_authentication_successful)
                    binding.toolbar.setTitle(R.string.egov_success_title)
                    replaceVisibleView(binding.successLayout)
                    resultListener(state.url)
                    lifecycleScope.launch {
                        delay(3000)
                        safeRun { super.dismiss() }
                    }
                }

                IdentState.CARD_BLOCKED -> {
                    binding.toolbar.setTitle(R.string.egov_cardblocked_title)
                    replaceVisibleView(binding.cardInfoLayout)
                }
            }
        }
    }

    private val pageLinkHandlerWebViewClient by lazy {
        object : WebViewClient() {
            override fun shouldOverrideUrlLoading(view: WebView?, request: WebResourceRequest?): Boolean {
                attemptOpeningWebViewUri(request?.url)
                return true
            }
        }
    }

    private fun replaceVisibleView(targetView: View) {
        if (visibleView == targetView) return
        visibleView.setVisible(false)
        visibleView = targetView
        visibleView.setVisible(true)
    }

    override fun dismiss() {
        DialogUtil.showDialogPositiveNegative(
            context = requireContext(),
            title = R.string.egov_cancel_workflow_dialog_title,
            positiveBtnLabel = android.R.string.ok,
            negativeBtnLabel = android.R.string.cancel,
            message = R.string.egov_cancel_workflow_dialog_message,
            positiveClickListener = { super.dismiss() }
        )
    }
}
