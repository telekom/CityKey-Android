package com.telekom.citykey.view.city_selection

import android.Manifest
import android.appwidget.AppWidgetManager
import android.content.Context
import android.content.Intent
import android.location.LocationManager
import android.net.Uri
import android.os.Bundle
import android.provider.Settings
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.app.ActivityCompat
import androidx.core.location.LocationManagerCompat
import androidx.core.view.children
import androidx.fragment.app.DialogFragment
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.DividerItemDecoration
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.telekom.citykey.R
import com.telekom.citykey.databinding.CitySelectionFragmentBinding
import com.telekom.citykey.utils.DialogUtil
import com.telekom.citykey.utils.RegExUtils
import com.telekom.citykey.utils.extensions.*
import com.telekom.citykey.view.city_selection.Cities.City
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import org.koin.androidx.viewmodel.ext.android.viewModel

class CitySelectionFragment : BottomSheetDialogFragment() {

    companion object {
        private const val CONTACT_LINK_EMAIL = "citykey-support@telekom.de"
    }

    private val viewModel: CitySelectionViewModel by viewModel()
    private val binding by viewBinding(CitySelectionFragmentBinding::bind)

    private var citySelectionAdapter: CitySelectionAdapter? = null

    private val requestPermissionLauncher =
        registerForActivityResult(ActivityResultContracts.RequestMultiplePermissions()) {
            if (it[Manifest.permission.ACCESS_FINE_LOCATION] == true && it[Manifest.permission.ACCESS_COARSE_LOCATION] == true
            ) {
                getCurrentLocation()
            } else if (!ActivityCompat.shouldShowRequestPermissionRationale(
                    requireActivity(),
                    Manifest.permission.ACCESS_FINE_LOCATION
                ) &&
                !ActivityCompat.shouldShowRequestPermissionRationale(
                    requireActivity(),
                    Manifest.permission.ACCESS_COARSE_LOCATION
                )
            ) {
                showEnableLocationInSettingsDialog(
                    R.string.c_001_cities_cannot_access_location_dialog_message,
                    Settings.ACTION_APPLICATION_DETAILS_SETTINGS
                )
            }
        }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setStyle(DialogFragment.STYLE_NORMAL, R.style.AuthDialogTheme)
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View =
        inflater.inflate(R.layout.city_selection_fragment, container, false)

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.toolbarCitySelection.setNavigationIcon(R.drawable.ic_profile_close)
        binding.toolbarCitySelection.setNavigationIconTint(getColor(R.color.onSurface))
        binding.toolbarCitySelection.setNavigationOnClickListener { dismiss() }
        initViews()
        setAccessibilityRoles()
        subscribeUI()
    }

    private fun initViews() {
        citySelectionAdapter =
            CitySelectionAdapter(::onFindNearestCityClicked, ::showContactLinkConsentDialog, viewModel::selectCity)
        binding.citiesRecyclerView.addItemDecoration(DividerItemDecoration(context, DividerItemDecoration.VERTICAL))
        binding.citiesRecyclerView.adapter = citySelectionAdapter

        loadLocationBasedCitiesIfPossible()
    }

    private fun setAccessibilityRoles() {
        setAccessibilityRoleForToolbarTitle(binding.toolbarCitySelection)
        val icon = binding.toolbarCitySelection.children.firstOrNull { view -> view is ImageButton }
        icon?.setAccessibilityRole(AccessibilityRole.Button)
        icon?.contentDescription = context?.getString(R.string.dialog_button_ok)
    }

    private fun subscribeUI() {
        viewModel.contentAll.observe(viewLifecycleOwner) { cities ->
            if (cities.filterIsInstance<City>().size <= 3) {
                binding.citySelectionInfoBlock.setVisible(true)
            } else {
                binding.citySelectionInfoBlock.setVisible(false)
            }

            val updatedCitiesList = viewModel.getUpdatedCitiesList(emailSupportIsAvailable(), cities)
            citySelectionAdapter?.submitList(updatedCitiesList)
        }

        viewModel.cityUpdated.observe(viewLifecycleOwner) {
            lifecycleScope.launch {
                AppWidgetManager.getInstance(requireContext()).updateWasteCalendarWidget(requireContext())
                delay(1000)
                dismiss()
            }
        }

        viewModel.showRetryDialog.observe(viewLifecycleOwner) {
            DialogUtil.showRetryDialog(
                context = requireContext(),
                onCancel = viewModel::onRetryCanceled,
                onRetry = viewModel::onRetryRequired
            )
        }

        viewModel.isCityActive.observe(viewLifecycleOwner) {
            DialogUtil.showCityNoMoreActive(requireContext())
        }

        viewModel.technicalError.observe(viewLifecycleOwner) {
            DialogUtil.showTechnicalError(requireContext())
        }
    }

    private fun emailSupportIsAvailable(): Boolean {
        val sendToAvailable = Intent(Intent.ACTION_SENDTO).apply {
            data = Uri.parse("mailto:")
        }.resolveActivity(requireContext().packageManager) != null

        val sendAvailable = Intent(Intent.ACTION_SEND).apply {
            data = Uri.parse("mailto:")
        }.resolveActivity(requireContext().packageManager) != null

        return sendToAvailable || sendAvailable
    }

    private fun showEmailClientChooser() {
        val emailIntentUri = Uri.parse(RegExUtils.EMAIL_URI_PREFIX)
        val intent = Intent(Intent.ACTION_SEND).apply {
            data = emailIntentUri
            // NOTE: Do not set `type` for this intent
            putExtra(Intent.EXTRA_EMAIL, arrayOf(CONTACT_LINK_EMAIL))
            putExtra(Intent.EXTRA_SUBJECT, getString(R.string.c_003_mail_subject_text))
            putExtra(
                Intent.EXTRA_TEXT,
                StringBuilder()
                    .append(getString(R.string.c_003_mail_body_name_text))
                    .appendLine().appendLine()
                    .append(getString(R.string.c_003_mail_body_city_text))
                    .appendLine().appendLine()
                    .append(getString(R.string.c_003_mail_body_permission_one))
                    .appendLine().appendLine()
                    .append(getString(R.string.c_003_mail_body_permission_two))
                    .appendLine().appendLine().toString()
            )
            selector = Intent(Intent.ACTION_SENDTO).apply { data = emailIntentUri }
        }
        val chooser =
            Intent.createChooser(intent, getString(R.string.c_003_email_option_chooser_title))
        startActivity(chooser)
    }

    private fun showContactLinkConsentDialog() {
        DialogUtil.showDialogPositiveNegative(
            context = requireActivity(),
            title = null,
            message = R.string.c_003_contact_link_message_text,
            positiveBtnLabel = R.string.c_003_aleart_get_in_contact_button,
            negativeBtnLabel = R.string.c_003_aleart_cancel_button,
            positiveClickListener = {
                showEmailClientChooser()
            }
        )
    }

    private fun loadLocationBasedCitiesIfPossible() {
        if (hasLocationPermissions() && isLocationEnabled())
            getCurrentLocation()
        else
            viewModel.onPermissionsMissing()
    }

    private fun getCurrentLocation() {
        if (isLocationEnabled()) {
            viewModel.onNearestCityRequested()
        } else {
            viewModel.onLocationServicesDisabled()
            showEnableLocationInSettingsDialog(
                R.string.c_001_cities_gps_turned_off,
                Settings.ACTION_LOCATION_SOURCE_SETTINGS
            )
        }
    }

    private fun hasLocationPermissions() =
        requireContext().hasPermission(Manifest.permission.ACCESS_FINE_LOCATION) &&
                requireContext().hasPermission(Manifest.permission.ACCESS_COARSE_LOCATION)

    private fun isLocationEnabled() =
        LocationManagerCompat.isLocationEnabled(requireActivity().getSystemService(Context.LOCATION_SERVICE) as LocationManager)

    private fun onFindNearestCityClicked() {
        if (hasLocationPermissions())
            getCurrentLocation()
        else
            showAskForLocationPermissionsDialog()
    }

    private fun showEnableLocationInSettingsDialog(message: Int, actionName: String) {
        DialogUtil.showDialogPositiveNegative(
            context = requireActivity(),
            title = R.string.c_001_cities_cannot_access_location_dialog_title,
            message = message,
            positiveBtnLabel = R.string.c_001_cities_cannot_access_location_btn_poitive,
            negativeBtnLabel = android.R.string.cancel,
            positiveClickListener = {
                if (actionName == Settings.ACTION_LOCATION_SOURCE_SETTINGS) {
                    startActivity(
                        Intent().apply {
                            flags = Intent.FLAG_ACTIVITY_NEW_TASK
                            action = actionName
                        }
                    )
                } else {
                    startActivity(
                        Intent().apply {
                            flags = Intent.FLAG_ACTIVITY_NEW_TASK
                            action = actionName
                            data = Uri.fromParts("package", requireActivity().packageName, null)
                        }
                    )
                }
            }
        )
    }

    private fun showAskForLocationPermissionsDialog() {
        DialogUtil.showDialogLocationPermissionRequest(requireContext()) {
            requestPermissionLauncher.launch(
                arrayOf(
                    Manifest.permission.ACCESS_FINE_LOCATION,
                    Manifest.permission.ACCESS_COARSE_LOCATION
                )
            )
        }
    }
}
