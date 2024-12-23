package com.telekom.citykey.view.services.citizen_surveys.survey

import android.animation.ObjectAnimator
import android.content.res.ColorStateList
import android.os.Bundle
import android.view.MenuItem
import android.view.View
import android.view.animation.AccelerateDecelerateInterpolator
import androidx.activity.OnBackPressedCallback
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import androidx.viewpager2.widget.ViewPager2
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.telekom.citykey.R
import com.telekom.citykey.databinding.SurveyQuestionsFragmentBinding
import com.telekom.citykey.domain.services.surveys.SurveysState
import com.telekom.citykey.utils.DialogUtil
import com.telekom.citykey.utils.extensions.viewBinding
import com.telekom.citykey.view.MainFragment
import org.koin.androidx.viewmodel.ext.android.viewModel
import org.koin.core.parameter.parametersOf

class SurveyQuestions : MainFragment(R.layout.survey_questions_fragment) {

    private val viewModel: SurveyQuestionsViewModel by viewModel { parametersOf(args.surveyId) }
    private val args: SurveyQuestionsArgs by navArgs()
    private val binding by viewBinding(SurveyQuestionsFragmentBinding::bind)

    private var pagerAdapter: SurveyQuestionsPagerAdapter? = null
    private var cityName = ""

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupToolbar(binding.toolbarSurvey)

        binding.pager.offscreenPageLimit = 3
        binding.pager.isUserInputEnabled = false

        pagerAdapter = SurveyQuestionsPagerAdapter(
            onNextClicked = {
                onNextClick()
                binding.pager.currentItem = binding.pager.currentItem + 1
            },
            onPrevClicked = { binding.pager.currentItem = binding.pager.currentItem - 1 },
            onDoneClicked = this::onDoneClick,
            args.surveyName,
            viewModel.isPreview()
        )

        binding.pager.registerOnPageChangeCallback(object : ViewPager2.OnPageChangeCallback() {
            override fun onPageSelected(position: Int) {
                ObjectAnimator.ofInt(
                    binding.pagerProgress,
                    "progress",
                    binding.pagerProgress.progress,
                    (position + 1) * 100
                ).apply {
                    duration = 300L
                    interpolator = AccelerateDecelerateInterpolator()
                    start()
                }
            }
        })

        binding.pager.adapter = pagerAdapter

        requireActivity().onBackPressedDispatcher.addCallback(
            viewLifecycleOwner,
            object : OnBackPressedCallback(true) {
                override fun handleOnBackPressed() {
                    MaterialAlertDialogBuilder(requireContext(), R.style.AlertDialogMaterialTheme)
                        .setMessage(R.string.cs_004_back_button_dialog)
                        .setPositiveButton(R.string.positive_button_dialog) { _, _ ->
                            findNavController().navigateUp()
                        }
                        .setNegativeButton(R.string.negative_button_dialog, null)
                        .show()
                }
            }
        )
        subscribeUi()
    }

    private fun subscribeUi() {
        viewModel.color.observe(viewLifecycleOwner) {
            pagerAdapter?.color = it
            binding.pagerProgress.progressTintList = ColorStateList.valueOf(it)
        }

        viewModel.surveyDataSubmitted.observe(viewLifecycleOwner) { surveySubmitted ->
            if (surveySubmitted.isSuccessful)
                findNavController().popBackStack(R.id.surveysOverview, false)
        }

        viewModel.state.observe(viewLifecycleOwner) {
            if (it == SurveysState.ServiceNotAvailable) {
                findNavController().popBackStack(R.id.services, false)
            } else {
                DialogUtil.showTechnicalError(requireContext())
            }
        }

        viewModel.showRetryDialog.observe(viewLifecycleOwner) {
            DialogUtil.showRetryDialog(requireContext(), viewModel::onRetryRequired, viewModel::onRetryCanceled)
        }

        viewModel.technicalError.observe(viewLifecycleOwner) {
            DialogUtil.showTechnicalError(requireContext())
        }

        viewModel.questionData.observe(viewLifecycleOwner) {
            pagerAdapter?.submitList(it)
            binding.pagerProgress.max = it.size * 100
        }
        viewModel.cityName.observe(viewLifecycleOwner) { cityName ->
            this.cityName = cityName
        }
    }

    private fun onDoneClick() {
        MaterialAlertDialogBuilder(requireContext(), R.style.AlertDialogMaterialTheme)
            .setTitle(getString(R.string.cs_004_survey_submission_dialog_title, cityName))
            .setMessage(getString(R.string.cs_004_survey_submission_dialog_message, cityName))
            .setPositiveButton(R.string.p_001_profile_confirm_email_change_btn) { _, _ ->
                pagerAdapter?.let {
                    viewModel.onNextClick(it.mapTopicAnswer, binding.pager.currentItem + 1, true)
                }
            }
            .setNegativeButton(R.string.c_001_cities_dialog_gps_btn_cancel, null)
            .show()
    }

    private fun onNextClick() {
        pagerAdapter?.let {
            viewModel.onNextClick(it.mapTopicAnswer, binding.pager.currentItem + 1)
        }
    }

    override fun onDestroyView() {
        pagerAdapter = null
        super.onDestroyView()
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            android.R.id.home -> {
                MaterialAlertDialogBuilder(requireContext(), R.style.AlertDialogMaterialTheme)
                    .setMessage(R.string.cs_004_back_button_dialog)
                    .setPositiveButton(R.string.positive_button_dialog) { _, _ ->
                        findNavController().navigateUp()
                    }
                    .setNegativeButton(R.string.negative_button_dialog, null)
                    .show()
                true
            }

            else -> super.onOptionsItemSelected(item)
        }
    }
}
