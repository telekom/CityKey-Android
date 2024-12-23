package com.telekom.citykey.view.services.defect_reporter.details

import android.os.Bundle
import android.view.View
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import com.telekom.citykey.BuildConfig
import com.telekom.citykey.R
import com.telekom.citykey.common.GlideApp
import com.telekom.citykey.databinding.DefectReporterServiceDetailFragmentBinding
import com.telekom.citykey.domain.city.CityInteractor
import com.telekom.citykey.utils.DialogUtil
import com.telekom.citykey.utils.extensions.setVisible
import com.telekom.citykey.utils.extensions.viewBinding
import com.telekom.citykey.view.MainFragment
import com.telekom.citykey.view.main.MainActivity
import org.koin.androidx.viewmodel.ext.android.viewModel

class DefectServiceDetail : MainFragment(R.layout.defect_reporter_service_detail_fragment) {

    private val viewModel: DefectServiceDetailViewModel by viewModel()
    private val binding by viewBinding(DefectReporterServiceDetailFragmentBinding::bind)
    private val args: DefectServiceDetailArgs by navArgs()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setUpViews()
        subscribeUi()
        (activity as? MainActivity)?.markLoadCompleteIfFromDeeplink(getString(R.string.deeplink_defect_reporter))
    }

    private fun setUpViews() {

        binding.toolbarDefectReporter.title = args.service.service
        setupToolbar(binding.toolbarDefectReporter)

        binding.reportDefectBtn.setupNormalStyle(CityInteractor.cityColorInt)

        // https://jira.telekom.de/browse/SMARTC-37299
        val imageToLoad = args.service.headerImage.takeUnless { it.isNullOrBlank() } ?: args.service.image

        GlideApp.with(this)
            .load(BuildConfig.IMAGE_URL + imageToLoad)
            .centerCrop()
            .into(binding.image)

        binding.reportDefectBtn.text = args.service.serviceAction?.first()?.visibleText
        binding.fullDescription.loadData(args.service.description, "text/html", "UTF-8")

        if (!args.service.helpLinkTitle.isNullOrBlank()) {
            binding.defectInfoButton.setVisible(true)
            binding.defectReporterInfoBtn.text = args.service.helpLinkTitle
            binding.defectInfoButton.setOnClickListener {
                findNavController().navigate(
                    DefectServiceDetailDirections.actionDefectServiceDetailToServiceHelp(args.service)
                )
            }
        }

        binding.reportDefectBtn.setOnClickListener {
            binding.reportDefectBtn.startLoading()
            viewModel.onOpenDefectReporterClicked()
        }
    }

    fun subscribeUi() {
        viewModel.defectCategoryAvailable.observe(viewLifecycleOwner) {
            binding.reportDefectBtn.stopLoading()
            findNavController().navigate(DefectServiceDetailDirections.toDefectCategorySelection(args.service))
        }
        viewModel.showRetryDialog.observe(viewLifecycleOwner) {
            context?.let {
                DialogUtil.showRetryDialog(it, viewModel::onRetryRequired) {
                    binding.reportDefectBtn.stopLoading()
                    viewModel.onRetryCanceled()
                }
            }
        }
        viewModel.technicalError.observe(viewLifecycleOwner) {
            binding.reportDefectBtn.stopLoading()
            context?.let { DialogUtil.showTechnicalError(it) }
        }
    }
}
