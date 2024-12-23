package com.telekom.citykey.view.services.egov.details

import android.content.res.ColorStateList
import android.os.Bundle
import android.view.View
import androidx.core.widget.TextViewCompat
import androidx.interpolator.view.animation.LinearOutSlowInInterpolator
import androidx.navigation.NavOptions
import androidx.navigation.fragment.FragmentNavigatorExtras
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import androidx.transition.Transition
import androidx.transition.TransitionInflater
import androidx.transition.TransitionListenerAdapter
import com.telekom.citykey.R
import com.telekom.citykey.custom.views.SpacingDecoration
import com.telekom.citykey.databinding.EgovServiceDetailsBinding
import com.telekom.citykey.domain.city.CityInteractor
import com.telekom.citykey.domain.services.egov.EgovState
import com.telekom.citykey.utils.DialogUtil
import com.telekom.citykey.utils.extensions.AccessibilityRole
import com.telekom.citykey.utils.extensions.dpToPixel
import com.telekom.citykey.utils.extensions.loadFromOSCA
import com.telekom.citykey.utils.extensions.safeRun
import com.telekom.citykey.utils.extensions.setAccessibilityRole
import com.telekom.citykey.utils.extensions.setVisible
import com.telekom.citykey.utils.extensions.viewBinding
import com.telekom.citykey.view.MainFragment
import com.telekom.citykey.view.main.MainActivity
import org.koin.androidx.viewmodel.ext.android.viewModel

class EgovServiceDetails : MainFragment(R.layout.egov_service_details) {

    private val viewModel: EgovServiceDetailsViewModel by viewModel()
    private val binding: EgovServiceDetailsBinding by viewBinding(EgovServiceDetailsBinding::bind)
    private val args: EgovServiceDetailsArgs by navArgs()

    private var egovCategoriesAdapter: EgovCategoriesAdapter? = null

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        postponeEnterTransition()

        egovCategoriesAdapter = EgovCategoriesAdapter(args.service.service, viewModel)

        binding.image.loadFromOSCA(args.service.image)
        binding.fullDescription.loadData(args.service.description, "text/html", "UTF-8")
        binding.fullDescription.post {
            safeRun {
                startPostponedEnterTransition()
                (exitTransition as? Transition)?.addListener(object : TransitionListenerAdapter() {
                    override fun onTransitionEnd(transition: Transition) {
                        exitTransition = null
                        enterTransition = null
                    }
                })
            }
        }
        binding.infoBtn.apply {
            setAccessibilityRole(AccessibilityRole.Button)
            text = args.service.helpLinkTitle ?: getString(R.string.egovs_001_details_info_btn)
            setOnClickListener {
                findNavController().navigate(EgovServiceDetailsDirections.actionEgovServiceDetailsToServiceHelp(args.service))
            }
        }
        setupToolbar(binding.toolbarDetailedServices)
        binding.toolbarDetailedServices.title = args.service.service

        binding.categoriesList.addItemDecoration(SpacingDecoration(6.dpToPixel(requireContext())))
        binding.categoriesList.adapter = egovCategoriesAdapter

        binding.loadingBar.setColor(CityInteractor.cityColorInt)
        binding.retryButton.setTextColor(CityInteractor.cityColorInt)
        TextViewCompat.setCompoundDrawableTintList(
            binding.retryButton,
            ColorStateList.valueOf(CityInteractor.cityColorInt)
        )
        binding.retryButton.setOnClickListener { viewModel.onRetryClicked() }
        binding.editText.setAccessibilityRole(AccessibilityRole.Button)
        binding.searchBar.editText?.setOnClickListener {

            exitTransition =
                TransitionInflater.from(requireContext()).inflateTransition(android.R.transition.fade).apply {
                    interpolator = LinearOutSlowInInterpolator()
                    duration = 500
                }

            findNavController().navigate(
                R.id.action_EgovServiceDetails_to_egovSearch,
                null,
                NavOptions.Builder().apply {
                    this.setEnterAnim(R.anim.fade_in_linear)
                    this.setExitAnim(R.anim.fade_out_linear)
                    this.setPopEnterAnim(R.anim.fade_in_linear)
                    this.setPopExitAnim(R.anim.fade_out_linear)
                }.build(),
                FragmentNavigatorExtras(binding.searchBar to binding.searchBar.transitionName)
            )
        }
        binding.categoriesTitle.setAccessibilityRole(
            AccessibilityRole.Heading,
            getString(R.string.accessibility_heading_level_3)
        )
        subscribeUi()
        with(requireActivity() as MainActivity) {
            markLoadCompleteIfFromDeeplink(getString(R.string.deeplink_egov))
        }
    }

    private fun subscribeUi() {
        viewModel.state.observe(viewLifecycleOwner) {
            binding.errorLayout.setVisible(it is EgovState.ERROR)
            binding.loadingBar.setVisible(it is EgovState.LOADING)
            binding.scrollView.setVisible(it is EgovState.Success)

            if (it is EgovState.Success) {
                egovCategoriesAdapter?.submitList(it.egovItems)
            }
        }

        viewModel.showRetryDialog.observe(viewLifecycleOwner) {
            DialogUtil.showRetryDialog(requireContext(), viewModel::onRetryRequired, viewModel::onRetryCanceled)
        }

        viewModel.technicalError.observe(viewLifecycleOwner) {
            DialogUtil.showTechnicalError(requireContext())
        }
    }

    override fun onDestroy() {
        egovCategoriesAdapter = null
        super.onDestroy()
    }
}
