package com.telekom.citykey.view.services.fahrradparken.report_submission

import android.os.Bundle
import android.view.View
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import com.telekom.citykey.R
import com.telekom.citykey.databinding.FahrradparkenReportSubmittedFragmentBinding
import com.telekom.citykey.domain.city.CityInteractor
import com.telekom.citykey.domain.track.AdjustManager
import com.telekom.citykey.utils.extensions.setAccessibilityRoleForToolbarTitle
import com.telekom.citykey.utils.extensions.setVisible
import com.telekom.citykey.utils.extensions.toDateString
import com.telekom.citykey.utils.extensions.viewBinding
import com.telekom.citykey.view.MainFragment
import org.koin.android.ext.android.inject
import java.util.Calendar

class FahrradparkenReportSubmitted : MainFragment(R.layout.fahrradparken_report_submitted_fragment) {

    private val binding by viewBinding(FahrradparkenReportSubmittedFragmentBinding::bind)
    private val args: FahrradparkenReportSubmittedArgs by navArgs()

    private val adjustManager: AdjustManager by inject()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        adjustManager.trackEvent(R.string.fahrradparken_submitted)
        initViews()
    }

    fun initViews() {
        binding.thankYouLabel1.text = getString(R.string.fa_011_thank_you_msg1)
        binding.okButton.setupNormalStyle(CityInteractor.cityColorInt)
        binding.toolbar.title = args.defectCategory
        setAccessibilityRoleForToolbarTitle(binding.toolbar)
        binding.uniqueIdLabel.setVisible(args.uniqueId.isNotEmpty())
        binding.uniqueId.setVisible(args.uniqueId.isNotEmpty())
        binding.uniqueId.text = args.uniqueId
        binding.category.text = args.defectCategory
        binding.reportedOn.text = Calendar.getInstance().time.toDateString()
        binding.okButton.setOnClickListener { findNavController().popBackStack() }
        binding.submitMessageInfo.setVisible(args.email.isNullOrBlank().not())
    }

}
