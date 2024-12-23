package com.telekom.citykey.view.services.citizen_surveys.survey_details

import android.os.Bundle
import android.view.View
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import com.telekom.citykey.R
import com.telekom.citykey.databinding.SurveyDetailsFragmentBinding
import com.telekom.citykey.domain.city.CityInteractor
import com.telekom.citykey.models.citizen_survey.Survey
import com.telekom.citykey.utils.DialogUtil
import com.telekom.citykey.utils.extensions.AccessibilityRole
import com.telekom.citykey.utils.extensions.loadFromOSCA
import com.telekom.citykey.utils.extensions.setAccessibilityRole
import com.telekom.citykey.utils.extensions.setHtmlText
import com.telekom.citykey.utils.extensions.setVisible
import com.telekom.citykey.utils.extensions.showDialog
import com.telekom.citykey.utils.extensions.toDateString
import com.telekom.citykey.utils.extensions.viewBinding
import com.telekom.citykey.view.MainFragment
import com.telekom.citykey.view.services.citizen_surveys.data_privacy.DataPrivacyNotice
import org.koin.androidx.viewmodel.ext.android.viewModel

class SurveyDetails : MainFragment(R.layout.survey_details_fragment) {

    private val viewModel: SurveyDetailsViewModel by viewModel()
    private val args: SurveyDetailsArgs by navArgs()
    private val binding by viewBinding(SurveyDetailsFragmentBinding::bind)

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupToolbar(binding.toolbarSurveyDetails)

        binding.image.loadFromOSCA(args.serviceImage)
        binding.surveyName.setAccessibilityRole(
            AccessibilityRole.Heading,
            getString(R.string.accessibility_heading_level_2)
        )
        binding.surveyName.text = args.survey.name
        binding.contentHeader.text = args.survey.name
        binding.daysCountDown.setColor(CityInteractor.cityColorInt)
        binding.daysCountDown.setValues(args.survey.daysTotal, args.survey.daysLeft)
        args.survey.description?.let {
            binding.contentText.setHtmlText(it)
        }
        binding.daysLeftText.text = args.survey.daysLeft.toString().padStart(2, '0')

        binding.daysLeftText.contentDescription = args.survey.daysLeft.toString()
        binding.endTimeDate.text = args.survey.endDate.toDateString()
        binding.endTimeDate.contentDescription = args.survey.endDate.toDateString().replace(".", "")
        binding.creationDate.text =
            getString(R.string.cs_003_creation_date_format, args.survey.startDate.toDateString())
        binding.popularLabel.setVisible(args.survey.isPopular)
        binding.stateIcon.setVisible(args.survey.status == Survey.STATUS_COMPLETED)
        binding.stateIcon.setColorFilter(CityInteractor.cityColorInt)

        if (args.survey.daysLeft < 2) {
            binding.daysLabel.text = getString(R.string.cs_002_day_label)
        }
        binding.creationDate.contentDescription =
            getString(R.string.cs_003_creation_date_format, args.survey.startDate.toDateString().replace(".", ""))

        binding.btnStartSurvey.setupNormalStyle(CityInteractor.cityColorInt)
        if (args.survey.status == Survey.STATUS_COMPLETED) {
            binding.btnStartSurvey.visibility = View.GONE
            binding.surveyCompletedText.visibility = View.VISIBLE
            binding.surveyCompletedText.text = getString(R.string.cs_002_survey_completed_message)
        }
        binding.btnStartSurvey.setOnClickListener {
            binding.btnStartSurvey.startLoading()
            viewModel.onStartSurveyClicked(args.survey.id)
        }
        subscribeUi()
    }

    private fun subscribeUi() {
        viewModel.surveyAvailable.observe(viewLifecycleOwner) {
            binding.btnStartSurvey.stopLoading()
            findNavController().navigate(
                SurveyDetailsDirections.actionSurveyDetailsToSurveyQuestions(
                    args.survey.id,
                    args.survey.name
                )
            )
        }
        viewModel.surveyDataPrivacy.observe(viewLifecycleOwner) {
            if (viewModel.surveyDataPrivacyAccepted.value == false) {
                binding.btnStartSurvey.stopLoading()
                DataPrivacyNotice(it.surveyDataPrivacyText, CityInteractor.cityColorInt) {
                    if (it) {
                        viewModel.onDataPrivacyAccepted(args.survey.id)
                        binding.btnStartSurvey.startLoading()
                        viewModel.onStartSurveyClicked(args.survey.id)
                    }
                }.showDialog(childFragmentManager)
            }
        }

        viewModel.technicalError.observe(viewLifecycleOwner) {
            binding.btnStartSurvey.stopLoading()
            DialogUtil.showTechnicalError(requireContext())
        }

        viewModel.showRetryDialog.observe(viewLifecycleOwner) {
            DialogUtil.showRetryDialog(requireContext(), viewModel::onRetryRequired) {
                binding.btnStartSurvey.stopLoading()
                viewModel.onRetryCanceled()
            }
        }
    }
}
