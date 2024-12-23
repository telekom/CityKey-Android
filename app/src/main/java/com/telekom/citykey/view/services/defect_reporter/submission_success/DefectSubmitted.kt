package com.telekom.citykey.view.services.defect_reporter.submission_success

import android.os.Bundle
import android.view.View
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import com.telekom.citykey.R
import com.telekom.citykey.databinding.DefectSubmittedFragmentBinding
import com.telekom.citykey.domain.city.CityInteractor
import com.telekom.citykey.domain.track.AdjustManager
import com.telekom.citykey.utils.extensions.setAccessibilityRoleForToolbarTitle
import com.telekom.citykey.utils.extensions.setVisible
import com.telekom.citykey.utils.extensions.toDateString
import com.telekom.citykey.utils.extensions.viewBinding
import com.telekom.citykey.view.MainFragment
import org.koin.android.ext.android.inject
import java.util.*

class DefectSubmitted : MainFragment(R.layout.defect_submitted_fragment) {
    private val adjustManager: AdjustManager by inject()
    private val binding by viewBinding(DefectSubmittedFragmentBinding::bind)
    private val args: DefectSubmittedArgs by navArgs()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        adjustManager.trackEvent(R.string.defect_submitted)
        initViews()
    }

    fun initViews() {
        binding.thankYouLabel1.text = getString(R.string.dr_004_thank_you_msg1, args.cityName)
        binding.okButton.setupNormalStyle(CityInteractor.cityColorInt)
        binding.toolbar.title = args.defectCategory
        setAccessibilityRoleForToolbarTitle(binding.toolbar)
        binding.uniqueIdLabel.setVisible(args.uniqueId.isNotEmpty())
        binding.uniqueId.setVisible(args.uniqueId.isNotEmpty())
        binding.uniqueId.text = args.uniqueId
        binding.category.text = args.defectCategory
        binding.reportedOn.text = Calendar.getInstance().time.toDateString()
        binding.okButton.setOnClickListener { findNavController().popBackStack() }
    }
}
