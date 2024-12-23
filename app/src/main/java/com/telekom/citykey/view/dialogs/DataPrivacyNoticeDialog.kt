package com.telekom.citykey.view.dialogs

import android.annotation.SuppressLint
import android.graphics.Color
import android.os.Bundle
import android.view.View
import android.webkit.WebResourceRequest
import android.webkit.WebView
import android.webkit.WebViewClient
import com.telekom.citykey.BuildConfig
import com.telekom.citykey.R
import com.telekom.citykey.databinding.DataPrivacyNoticeDialogBinding
import com.telekom.citykey.domain.legal_data.LegalDataManager
import com.telekom.citykey.utils.extensions.*
import com.telekom.citykey.view.FullScreenBottomSheetDialogFragment
import org.koin.android.ext.android.inject

class DataPrivacyNoticeDialog(private val isLaunchedFromSettings: Boolean = false) :
    FullScreenBottomSheetDialogFragment(R.layout.data_privacy_notice_dialog) {

    private val binding: DataPrivacyNoticeDialogBinding by viewBinding(DataPrivacyNoticeDialogBinding::bind)
    private val legalData: LegalDataManager by inject()

    @SuppressLint("SetTextI18n")
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.btnOpenSettings.setOnClickListener {
            if (isLaunchedFromSettings) dismiss()
            else DataPrivacySettingsDialog(isLaunchedFromNotice = true)
                .showDialog(parentFragmentManager, "DataPrivacySettingsDialog")
        }

        binding.version.text = "App-Version: ${BuildConfig.VERSION_NAME}"

        binding.toolbar.setNavigationIcon(R.drawable.ic_profile_close)
        binding.toolbar.setNavigationIconTint(getColor(R.color.onSurface))
        binding.toolbar.setNavigationContentDescription(R.string.accessibility_btn_close)
        binding.toolbar.setNavigationOnClickListener { dismiss() }
        setAccessibilityRoleForToolbarTitle(binding.toolbar)

        subscribeUi()
    }

    private fun subscribeUi() {
        legalData.legalInfo.observe(viewLifecycleOwner) { terms ->
            binding.dataPrivacyText1.apply {
                webViewClient = pageLinkHandlerWebViewClient
                setBackgroundColor(Color.TRANSPARENT)
                linkifyAndLoadNonHtmlTaggedData(terms.dataSecurity.dataUsage)
            }
            binding.dataPrivacyText2.apply {
                webViewClient = pageLinkHandlerWebViewClient
                setBackgroundColor(Color.TRANSPARENT)
                linkifyAndLoadNonHtmlTaggedData(terms.dataSecurity.dataUsage2)
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

}
