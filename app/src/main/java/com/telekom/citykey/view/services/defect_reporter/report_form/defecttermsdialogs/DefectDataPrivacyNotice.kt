package com.telekom.citykey.view.services.defect_reporter.report_form.defecttermsdialogs

import android.os.Bundle
import android.view.View
import com.telekom.citykey.R
import com.telekom.citykey.databinding.DefectWebDialogBinding
import com.telekom.citykey.utils.extensions.getColor
import com.telekom.citykey.utils.extensions.setAccessibilityRoleForToolbarTitle
import com.telekom.citykey.utils.extensions.viewBinding
import com.telekom.citykey.view.FullScreenBottomSheetDialogFragment

class DefectDataPrivacyNotice(private val dataPrivacyUrl: String?) :
    FullScreenBottomSheetDialogFragment(R.layout.defect_web_dialog) {
    private val binding by viewBinding(DefectWebDialogBinding::bind)

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.toolbar.setNavigationIcon(R.drawable.ic_profile_close)
        binding.toolbar.setNavigationOnClickListener { dismiss() }
        binding.toolbar.setNavigationIconTint(getColor(R.color.onSurface))
        binding.toolbar.setTitle(R.string.data_privacy_survey_title)
        setAccessibilityRoleForToolbarTitle(binding.toolbar)

        if (dataPrivacyUrl != null) {
            binding.webView.loadUrl(dataPrivacyUrl.trim())
        }
    }
}
