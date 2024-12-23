package com.telekom.citykey.view.services.citizen_surveys

import android.os.Bundle
import android.view.View
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import androidx.recyclerview.widget.DividerItemDecoration
import com.telekom.citykey.R
import com.telekom.citykey.databinding.SurveyOverviewFragmentBinding
import com.telekom.citykey.domain.services.surveys.SurveysState
import com.telekom.citykey.utils.extensions.AccessibilityRole
import com.telekom.citykey.utils.extensions.isNotVisible
import com.telekom.citykey.utils.extensions.setAccessibilityRole
import com.telekom.citykey.utils.extensions.setVisible
import com.telekom.citykey.utils.extensions.viewBinding
import com.telekom.citykey.view.MainFragment
import com.telekom.citykey.view.main.MainActivity
import org.koin.androidx.viewmodel.ext.android.viewModel

class SurveysOverview : MainFragment(R.layout.survey_overview_fragment) {

    private val viewModel: SurveysOverviewViewModel by viewModel()
    private val args: SurveysOverviewArgs by navArgs()
    private val binding by viewBinding(SurveyOverviewFragmentBinding::bind)

    private var listAdapter: SurveysAdapter? = null

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupToolbar(binding.toolbarSurveys)
        listAdapter = SurveysAdapter(viewModel.isPreview()) {
            findNavController().navigate(
                SurveysOverviewDirections.actionSurveysOverviewToSurveyDetails(it, args.serviceImage)
            )
        }

        binding.surveysList.adapter = listAdapter
        binding.surveysList.addItemDecoration(DividerItemDecoration(context, DividerItemDecoration.VERTICAL))

        binding.swipeRefreshLayout.setOnRefreshListener {
            viewModel.onRefreshRequested()
        }
        binding.retryButton.setAccessibilityRole(AccessibilityRole.Button)
        binding.retryButton.setOnClickListener {
            viewModel.onRefreshRequested()
        }
        binding.textEmptyState.apply {
            if (viewModel.isPreview()) {
                text = context.getString(R.string.cs_002_preview_error_no_surveys)
            } else {
                text = context.getString(R.string.cs_002_error_no_surveys)
            }
        }
        subscribeUi()
        with(requireActivity() as MainActivity) {
            markLoadCompleteIfFromDeeplink(getString(R.string.deeplink_polls_list))
        }
    }

    private fun subscribeUi() {
        viewModel.listItems.observe(viewLifecycleOwner) {
            binding.swipeRefreshLayout.isRefreshing = false
            binding.loader.setVisible(false)
            listAdapter?.submitList(it)
        }

        viewModel.state.observe(viewLifecycleOwner) {
            with(binding) {
                when (it) {
                    SurveysState.ServiceNotAvailable -> {
                        swipeRefreshLayout.isRefreshing = false
                        findNavController().popBackStack(R.id.services, false)
                    }

                    SurveysState.Loading -> {
                        val shouldShowLoader =
                            surveysList.isNotVisible && textEmptyState.isNotVisible && swipeRefreshLayout.isRefreshing.not()
                        loader.setVisible(shouldShowLoader)
                    }

                    else -> {
                        swipeRefreshLayout.isRefreshing = false
                        loader.setVisible(false)
                        errorLayout.setVisible(it == SurveysState.Error)
                        textEmptyState.setVisible(it == SurveysState.Empty)
                        surveysList.setVisible(it == SurveysState.Success)
                    }
                }
            }
        }

        viewModel.stopRefresh.observe(viewLifecycleOwner) {
            binding.swipeRefreshLayout.isRefreshing = false
            binding.loader.setVisible(false)
        }
    }

    override fun onDestroyView() {
        listAdapter = null
        super.onDestroyView()
    }
}
