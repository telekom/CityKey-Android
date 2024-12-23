package com.telekom.citykey.view.services.egov.services.egovDesc

import android.os.Bundle
import android.view.View
import android.webkit.WebResourceRequest
import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import com.telekom.citykey.R
import com.telekom.citykey.databinding.EgovDescDetailBinding
import com.telekom.citykey.domain.track.AdjustManager
import com.telekom.citykey.models.egov.EgovLinkTypes
import com.telekom.citykey.utils.DialogUtil
import com.telekom.citykey.utils.extensions.attemptOpeningWebViewUri
import com.telekom.citykey.utils.extensions.loadBasicHtml
import com.telekom.citykey.utils.extensions.openLink
import com.telekom.citykey.utils.extensions.viewBinding
import com.telekom.citykey.view.MainFragment
import org.koin.android.ext.android.inject

class EgovDescDetail : MainFragment(R.layout.egov_desc_detail) {
    private val binding: EgovDescDetailBinding by viewBinding(EgovDescDetailBinding::bind)
    private val args: EgovDescDetailArgs by navArgs()
    private val adjustManager: AdjustManager by inject()

    private val pageLinkHandlerWebViewClient by lazy {
        object : WebViewClient() {
            override fun shouldOverrideUrlLoading(view: WebView?, request: WebResourceRequest?): Boolean {
                attemptOpeningWebViewUri(request?.url)
                return true
            }
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupToolbar(binding.toolbarEgovDescDetail)
        binding.longDescriptionWebView.apply {
            webViewClient = pageLinkHandlerWebViewClient
            loadBasicHtml(args.egovData.longDescription)
        }
        binding.toolbarEgovDescDetail.title = args.egovData.serviceName
        binding.linksBtnList.adapter = EgovDescDetailAdapter(args.egovData.linksInfo) { service ->
            when (service.linkType) {
                EgovLinkTypes.EID_FORM, EgovLinkTypes.FORM -> {
                    adjustManager.trackEvent(R.string.open_egov_external_url)
                    findNavController()
                        .navigate(EgovDescDetailDirections.toAuthWebView2(service.link, args.egovData.serviceName))
                }

                EgovLinkTypes.PDF, EgovLinkTypes.WEB -> {
                    adjustManager.trackEvent(R.string.open_egov_external_url)
                    openLink(service.link)
                }

                else -> DialogUtil.showTechnicalError(requireContext())
            }
        }
    }
}
