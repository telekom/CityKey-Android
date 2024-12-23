package com.telekom.citykey.view.services.citizen_surveys.data_privacy

import android.os.Bundle
import android.view.View
import com.telekom.citykey.R
import com.telekom.citykey.databinding.SurveyDataPrivacyFragmentBinding
import com.telekom.citykey.utils.extensions.decodeHTML
import com.telekom.citykey.utils.extensions.getColor
import com.telekom.citykey.utils.extensions.setAccessibilityRoleForToolbarTitle
import com.telekom.citykey.utils.extensions.viewBinding
import com.telekom.citykey.view.FullScreenBottomSheetDialogFragment

class DataPrivacyNotice(
    private val surveyDataPrivacy: String,
    private val cityColor: Int,
    private val resultListener: (Boolean) -> Unit,
) : FullScreenBottomSheetDialogFragment(R.layout.survey_data_privacy_fragment) {

    private val binding by viewBinding(SurveyDataPrivacyFragmentBinding::bind)

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.toolbarDataPrivacy.setNavigationIcon(R.drawable.ic_profile_close)
        binding.toolbarDataPrivacy.setNavigationIconTint(getColor(R.color.onSurface))
        binding.toolbarDataPrivacy.setNavigationContentDescription(R.string.accessibility_btn_close)
        binding.toolbarDataPrivacy.setNavigationOnClickListener { dismiss() }
        setAccessibilityRoleForToolbarTitle(binding.toolbarDataPrivacy)
        binding.btnDataPrivacy.setupNormalStyle(cityColor)
        binding.dataPrivacyContent.text = surveyDataPrivacy.decodeHTML()
        binding.btnDataPrivacy.setOnClickListener {
            resultListener.invoke(true)
            dismiss()
        }
    }
}
