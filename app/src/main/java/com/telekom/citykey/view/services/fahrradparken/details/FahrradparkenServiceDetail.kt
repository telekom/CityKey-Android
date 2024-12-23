package com.telekom.citykey.view.services.fahrradparken.details

import android.os.Bundle
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import com.telekom.citykey.BuildConfig
import com.telekom.citykey.R
import com.telekom.citykey.common.GlideApp
import com.telekom.citykey.databinding.FahrradparkenServiceDetailFragmentBinding
import com.telekom.citykey.domain.city.CityInteractor
import com.telekom.citykey.domain.track.AdjustManager
import com.telekom.citykey.utils.DialogUtil
import com.telekom.citykey.utils.extensions.dpToPixel
import com.telekom.citykey.utils.extensions.loadBasicHtml
import com.telekom.citykey.utils.extensions.viewBinding
import com.telekom.citykey.view.MainFragment
import org.koin.android.ext.android.inject
import org.koin.androidx.viewmodel.ext.android.viewModel

class FahrradparkenServiceDetail : MainFragment(R.layout.fahrradparken_service_detail_fragment) {

    private val binding by viewBinding(FahrradparkenServiceDetailFragmentBinding::bind)

    private val viewModel: FahrradparkenServiceDetailViewModel by viewModel()
    private val args: FahrradparkenServiceDetailArgs by navArgs()

    private val adjustManager: AdjustManager by inject()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        adjustManager.trackEvent(R.string.open_service_fahrradparken)
        initViews()
        initSubscribers()
    }

    private fun initViews() {
        with(binding) {
            toolbarDefectReporter.title = args.service.service
            setupToolbar(toolbarDefectReporter)

            GlideApp.with(this@FahrradparkenServiceDetail)
                .load(BuildConfig.IMAGE_URL + args.service.image)
                .centerCrop()
                .into(binding.serviceDetailImageView)

            detailsWebView.loadBasicHtml(args.service.description)

            existingReportsButton.apply {
                text = args.service.serviceAction?.firstOrNull { it.actionOrder == 1 }?.visibleText
                setupOutlineStyle(CityInteractor.cityColorInt)
                updateButtonLayout(button)
                setOnClickListener {
                    startLoading()
                    viewModel.onShowExistingReports()
                }
            }
            createNewReportButton.apply {
                text = args.service.serviceAction?.firstOrNull { it.actionOrder == 2 }?.visibleText
                setupNormalStyle(CityInteractor.cityColorInt)
                updateButtonLayout(button)
                setOnClickListener {
                    startLoading()
                    viewModel.onCreateNewReport()
                }
            }
        }
    }

    private fun updateButtonLayout(button: Button) {
        // Make the button take width of parent i.e. ProgressButton
        button.layoutParams.width = ViewGroup.LayoutParams.MATCH_PARENT
        // Reduce the horizontal padding of button set through `buttonBlueFilledStyle` style
        val paddingVerticalPx = button.paddingTop
        val paddingHorizontalPx = 12.dpToPixel(requireContext())
        button.setPadding(paddingHorizontalPx, paddingVerticalPx, paddingHorizontalPx, paddingVerticalPx)
    }

    private fun initSubscribers() {
        viewModel.fahrradparkenCategoriesAvailable.observe(viewLifecycleOwner) {
            if (binding.createNewReportButton.isLoading) {
                binding.createNewReportButton.stopLoading()
                findNavController().navigate(
                    FahrradparkenServiceDetailDirections.toFahrradparkenCategorySelection(args.service, true)
                )
            } else if (binding.existingReportsButton.isLoading) {
                binding.existingReportsButton.stopLoading()
                findNavController().navigate(
                    FahrradparkenServiceDetailDirections.toFahrradparkenCategorySelection(args.service, false)
                )
            }
        }
        viewModel.showRetryDialog.observe(viewLifecycleOwner) {
            DialogUtil.showRetryDialog(requireContext(), viewModel::onRetryRequired) {
                stopButtonLoader()
                viewModel.onRetryCanceled()
            }
        }

        viewModel.technicalError.observe(viewLifecycleOwner) {
            stopButtonLoader()
            DialogUtil.showTechnicalError(requireContext())
        }
    }

    private fun stopButtonLoader() = with(binding) {
        if (createNewReportButton.isLoading)
            createNewReportButton.stopLoading()
        else
            existingReportsButton.stopLoading()
    }
}
