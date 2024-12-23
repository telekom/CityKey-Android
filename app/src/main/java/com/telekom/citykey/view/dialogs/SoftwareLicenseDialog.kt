package com.telekom.citykey.view.dialogs

import android.os.Bundle
import android.view.View
import android.webkit.WebResourceRequest
import android.webkit.WebView
import android.webkit.WebViewClient
import com.telekom.citykey.R
import com.telekom.citykey.databinding.InformationDialogBinding
import com.telekom.citykey.utils.extensions.attemptOpeningWebViewUri
import com.telekom.citykey.utils.extensions.getColor
import com.telekom.citykey.utils.extensions.setAccessibilityRoleForToolbarTitle
import com.telekom.citykey.utils.extensions.viewBinding
import com.telekom.citykey.view.FullScreenBottomSheetDialogFragment
import java.util.Locale

class SoftwareLicenseDialog : FullScreenBottomSheetDialogFragment(R.layout.information_dialog) {

    private val binding by viewBinding(InformationDialogBinding::bind)

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.toolbar.setTitle(R.string.p_001_profile_software_license_title)
        binding.toolbar.setNavigationIcon(R.drawable.ic_profile_close)
        binding.toolbar.setNavigationIconTint(getColor(R.color.onSurface))
        binding.toolbar.setNavigationContentDescription(R.string.accessibility_btn_close)
        binding.toolbar.setNavigationOnClickListener { dismiss() }
        setAccessibilityRoleForToolbarTitle(binding.toolbar)

        loadSoftwareLicense()
    }

    private fun loadSoftwareLicense() {
        val inputStream = try {
            resources.assets.open("software_license-${Locale.getDefault().language}.html")
        } catch (exception: Exception) {
            resources.assets.open("software_license-en.html")
        }

        val inputAsString = inputStream.bufferedReader().use { it.readText() }
        binding.webView.webViewClient = object : WebViewClient() {
            override fun shouldOverrideUrlLoading(view: WebView?, request: WebResourceRequest?): Boolean {
                attemptOpeningWebViewUri(request?.url)
                return true
            }
        }
        binding.webView.loadDataWithBaseURL(null, inputAsString, "text/html", "utf-8", null)
    }
}
