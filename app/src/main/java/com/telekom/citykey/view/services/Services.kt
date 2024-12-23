package com.telekom.citykey.view.services

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.TextView
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.view.forEach
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.google.android.material.appbar.AppBarLayout
import com.telekom.citykey.R
import com.telekom.citykey.custom.ItemDecorationBetweenOnly
import com.telekom.citykey.custom.views.OscaAppBarLayout
import com.telekom.citykey.databinding.ServicesFragmentBinding
import com.telekom.citykey.domain.services.main.ServicesStates
import com.telekom.citykey.domain.track.AdjustManager
import com.telekom.citykey.domain.track.AnalyticsParameterKey
import com.telekom.citykey.models.content.CitizenService
import com.telekom.citykey.models.content.ServicesData
import com.telekom.citykey.utils.DialogUtil
import com.telekom.citykey.utils.RegExUtils
import com.telekom.citykey.utils.extensions.AccessibilityRole
import com.telekom.citykey.utils.extensions.getColor
import com.telekom.citykey.utils.extensions.getDrawable
import com.telekom.citykey.utils.extensions.loadFromOSCA
import com.telekom.citykey.utils.extensions.openLink
import com.telekom.citykey.utils.extensions.safeNavigate
import com.telekom.citykey.utils.extensions.safeRun
import com.telekom.citykey.utils.extensions.setAccessibilityRole
import com.telekom.citykey.utils.extensions.setAndPerformAccessibilityFocusAction
import com.telekom.citykey.utils.extensions.setVisible
import com.telekom.citykey.utils.extensions.showDialog
import com.telekom.citykey.utils.extensions.startActivity
import com.telekom.citykey.utils.extensions.viewBinding
import com.telekom.citykey.view.city_selection.CitySelectionFragment
import com.telekom.citykey.view.main.MainActivity
import com.telekom.citykey.view.user.login.LoginActivity
import com.telekom.citykey.view.user.profile.ProfileActivity
import org.koin.android.ext.android.inject
import org.koin.androidx.viewmodel.ext.android.viewModel

class Services : Fragment(R.layout.services_fragment) {

    private val viewModel: ServicesViewModel by viewModel()
    private val adjustManager: AdjustManager by inject()
    private val binding by viewBinding(ServicesFragmentBinding::bind)

    private var isUserLoggedIn = false
    private var pendingService: CitizenService? = null

    private val loginARL: ActivityResultLauncher<Intent> =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) {
            subscribeUi()
        }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.recyclerView.apply {
            adapter = ServicesAdapter(::onServiceSelected)
            addItemDecoration(
                ItemDecorationBetweenOnly(getDrawable(R.drawable.home_item_decoration)!!)
            )
        }

        binding.swipeRefreshLayout.setOnRefreshListener(viewModel::onRefresh)
        binding.servicesToolbar.inflateMenu(R.menu.home_menu)
        binding.labelCityServices.setAccessibilityRole(AccessibilityRole.Heading)

        setupToolbar(binding.appBarLayout)
        subscribeUi()
        adjustManager.trackOneTimeEvent(R.string.open_service)
    }

    override fun onResume() {
        super.onResume()
        viewModel.reloadServicesIfNeeded()
    }

    private fun subscribeUi() {
        viewModel.cityData.observe(viewLifecycleOwner) {
            binding.toolbarTitle.text = it.cityName
            binding.zoomableImage.loadFromOSCA(it.servicePicture)
            binding.toolbarCoat.loadFromOSCA(it.municipalCoat)
            binding.labelFindServices.text = it.serviceDesc
        }

        viewModel.servicesData.observe(viewLifecycleOwner) {
            (binding.recyclerView.adapter as ServicesAdapter).setupData(it)
            binding.swipeRefreshLayout.isRefreshing = false
            if (it is ServicesStates.Success && pendingService == null) {
                processDeeplinkIfPresent(it.data)
            }
        }

        viewModel.isUserLoggedIn.observe(viewLifecycleOwner) {
            isUserLoggedIn = it
            if (isUserLoggedIn) {
                pendingService?.let { ::navigateToService }
                pendingService = null
            }
        }

        viewModel.cancelLoading.observe(viewLifecycleOwner) {
            binding.swipeRefreshLayout.isRefreshing = false
        }

        viewModel.shouldPromptLogin.observe(viewLifecycleOwner) {
            DialogUtil.showLoginRequired(requireContext())
        }

        viewModel.updates.observe(viewLifecycleOwner) { map ->
            binding.recyclerView.post {
                safeRun {
                    map.forEach { pair ->
                        binding.recyclerView.findViewWithTag<View>(pair.key)
                            ?.findViewById<TextView>(R.id.badgeText)
                            ?.apply {
                                text = pair.value.toString()
                                setVisible(pair.value > 0)
                            }
                    }
                }
            }
        }
    }

    private fun processDeeplinkIfPresent(servicesData: ServicesData) {
        with(requireActivity() as MainActivity) {
            if (currentDeeplinkString == getString(R.string.deeplink_services)) {
                hideSplashScreen()
                clearDeeplinkInfo()
            }

            val service = when (currentDeeplinkString) {
                getString(R.string.deeplink_polls_list) -> servicesData.services.firstOrNull { it.function == ServicesFunctions.SURVEYS }
                getString(R.string.deeplink_defect_reporter) -> servicesData.services.firstOrNull { it.function == ServicesFunctions.MANGELMELDER }
                getString(R.string.deeplink_egov) -> servicesData.services.firstOrNull { it.function == ServicesFunctions.EGOV }
                else -> null
            }
            service?.let {
                onServiceSelected(it)
            } ?: kotlin.run {
                hideSplashScreen()
                clearDeeplinkInfo()
            }
        }
    }

    private fun setupToolbar(appBarLayout: OscaAppBarLayout) {

        appBarLayout.onCollapse { collapsed ->
            val menuItemColor = getColor(if (collapsed) R.color.onSurface else R.color.white)

            binding.toolbarCityBox.setVisible(collapsed)

            binding.servicesToolbar.menu.forEach {
                it.icon?.setTint(menuItemColor)
            }
        }

        appBarLayout.findViewById<View>(R.id.actionProfile).setOnClickListener {
            if (isUserLoggedIn) {
                adjustManager.trackEvent(R.string.open_profile)
                startActivity<ProfileActivity>()
            } else {
                startActivity(
                    Intent(it.context, LoginActivity::class.java).apply {
                        putExtra(LoginActivity.LAUNCH_PROFILE, true)
                    }
                )
            }
        }
        appBarLayout.findViewById<View>(R.id.actionSelectCity).setOnClickListener {
            CitySelectionFragment()
                .showDialog(requireActivity().supportFragmentManager)
        }
        appBarLayout.addOnOffsetChangedListener(
            AppBarLayout.OnOffsetChangedListener { _, i ->
                if (!binding.swipeRefreshLayout.isRefreshing) {
                    binding.swipeRefreshLayout.isEnabled = i == 0
                }
            }
        )
        binding.servicesToolbar.setAndPerformAccessibilityFocusAction()

    }

    private fun navigateToService(service: CitizenService) {
        findNavController().safeNavigate(
            when (service.function) {
                ServicesFunctions.TERMINE -> {
                    if (isUserLoggedIn) {
                        adjustManager.trackEvent(R.string.open_service_appointments)
                        ServicesDirections.actionServicesToAppointmentService(service)
                    } else return
                }

                ServicesFunctions.WASTE_CALENDAR -> {
                    if (isUserLoggedIn) {
                        adjustManager.trackEvent(R.string.open_service_waste_calendar)
                        ServicesDirections.actionServicesToWasteCalendarDetails()
                    } else return
                }

                ServicesFunctions.POI -> {
                    adjustManager.trackEvent(R.string.open_service_poi_guide)
                    ServicesDirections.toPoiGuide(service.service)
                }

                ServicesFunctions.EGOV -> {
                    adjustManager.trackEvent(R.string.open_service_egov)
                    ServicesDirections.toEgovServiceDetails(service)
                }

                ServicesFunctions.MANGELMELDER -> {
                    adjustManager.trackEvent(R.string.open_service_defect_reporter)
                    ServicesDirections.actionServicesToDefectServiceDetail(service)
                }

                ServicesFunctions.FAHRRADPARKEN -> {
                    ServicesDirections.actionServicesToFahrradparkenServiceGraph(service)
                }

                else -> {
                    if (service.restricted && !isUserLoggedIn) return
                    if (service.function == ServicesFunctions.TOURISM) {
                        adjustManager.trackEvent(R.string.open_service_destinations)
                    } else if (service.function == ServicesFunctions.SURVEYS) {
                        if (isUserLoggedIn) {
                            adjustManager.trackEvent(R.string.open_service_survey_detail)
                        } else return
                    } else if (service.function == ServicesFunctions.TERMINDETAILS && isUserLoggedIn) {
                        adjustManager.trackEvent(R.string.open_service_appointments)
                    }
                    ServicesDirections.actionServicesToDetailedService(service)
                }
            }
        )
    }

    private fun onServiceSelected(service: CitizenService) {
        val mainActivity = (requireActivity() as MainActivity)
        mainActivity.hideSplashScreen()
        if (service.loginLocked) {
            val dialog = DialogUtil.showLoginRequired(
                requireContext(),
                positiveClickListener = {
                    pendingService = service
                    loginARL.launch(Intent(requireContext(), LoginActivity::class.java))
                },
                negativeClickListener = {
                    mainActivity.clearDeeplinkInfo()
                }
            )
            mainActivity.markLoginDialogShown(dialog)
        } else {
            adjustManager.trackEvent(
                R.string.web_tile_tapped,
                mapOf(
                    AnalyticsParameterKey.serviceType to (service.serviceType ?: "").toString()
                )
            )
            if (service.templateId == 1) {
                if (service.description.isNullOrBlank().not()) {
                    openLink(service.description)
                }
            } else if (!service.function.isNullOrBlank() && RegExUtils.webUrl.matcher(service.function).matches()) {
                try {
                    requireActivity().openLink(service.function)
                } catch (e: Exception) {
                    DialogUtil.showTechnicalError(requireContext())
                }
            } else {
                navigateToService(service)
            }
        }
    }
}
