package com.telekom.citykey.view.auth_webview

import android.os.Bundle
import android.view.View
import android.webkit.HttpAuthHandler
import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.activity.OnBackPressedCallback
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import com.telekom.citykey.R
import com.telekom.citykey.databinding.WebviewFragmentBinding
import com.telekom.citykey.utils.extensions.viewBinding
import com.telekom.citykey.view.MainFragment


class WebView : MainFragment(R.layout.webview_fragment) {
    private val binding by viewBinding(WebviewFragmentBinding::bind)
    private val args: WebViewArgs by navArgs()
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.webviewToolbar.title = args.name
        setupToolbar(binding.webviewToolbar)
        setupWebView()
        activity?.onBackPressedDispatcher?.addCallback(
            viewLifecycleOwner,
            object : OnBackPressedCallback(true) {
                override fun handleOnBackPressed() {
                    if (binding.webView.canGoBack()) {
                        binding.webView.goBack()
                    } else {
                        binding.webView.destroy()
                        findNavController().navigateUp()
                    }
                }
            }
        )
    }

    private fun setupWebView() {
        binding.webView.apply {
            binding.webView.webViewClient = object : WebViewClient() {
                override fun onReceivedHttpAuthRequest(
                    view: WebView?,
                    handler: HttpAuthHandler?,
                    host: String?,
                    realm: String?
                ) {
                    handler?.proceed(args.username, args.password)
                }
            }
            loadUrl(args.link)
        }
    }

}