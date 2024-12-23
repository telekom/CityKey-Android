package com.telekom.citykey.view.dialogs

import android.os.Bundle
import android.view.View
import android.webkit.WebViewClient
import com.telekom.citykey.R
import com.telekom.citykey.databinding.HelpSectionDialogBinding
import com.telekom.citykey.utils.extensions.getColor
import com.telekom.citykey.utils.extensions.setAccessibilityRoleForToolbarTitle
import com.telekom.citykey.utils.extensions.viewBinding
import com.telekom.citykey.view.FullScreenBottomSheetDialogFragment
import java.util.*

class HelpSectionDialog : FullScreenBottomSheetDialogFragment(R.layout.help_section_dialog) {
    private val binding by viewBinding(HelpSectionDialogBinding::bind)

    companion object {
        private const val HELP_FAQ_LINK: String = "https://citykey.app/faq"
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initViews()
    }

    private fun initViews() {
        binding.toolbar.setNavigationIcon(R.drawable.ic_profile_close)
        binding.toolbar.setNavigationIconTint(getColor(R.color.onSurface))
        binding.toolbar.setNavigationContentDescription(R.string.accessibility_btn_close)
        binding.toolbar.setNavigationOnClickListener { dismiss() }
        setAccessibilityRoleForToolbarTitle(binding.toolbar)

        val helpUrl =
            if (Locale.getDefault().language == "en") HELP_FAQ_LINK + "-" + Locale.getDefault().language
            else HELP_FAQ_LINK
        binding.webViewHelp.webViewClient = WebViewClient()
        binding.webViewHelp.loadUrl(helpUrl)
    }
}
