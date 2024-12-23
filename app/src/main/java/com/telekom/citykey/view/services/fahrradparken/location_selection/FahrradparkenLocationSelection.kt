package com.telekom.citykey.view.services.fahrradparken.location_selection

import android.Manifest
import android.content.Context
import android.content.Intent
import android.content.res.ColorStateList
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.PorterDuff
import android.graphics.PorterDuffColorFilter
import android.location.LocationManager
import android.os.Bundle
import android.provider.Settings
import android.view.View
import androidx.activity.result.contract.ActivityResultContracts
import androidx.annotation.DrawableRes
import androidx.core.graphics.drawable.toBitmap
import androidx.core.location.LocationManagerCompat
import androidx.lifecycle.lifecycleScope
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.model.BitmapDescriptor
import com.google.android.gms.maps.model.BitmapDescriptorFactory
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.Marker
import com.google.android.gms.maps.model.MarkerOptions
import com.telekom.citykey.R
import com.telekom.citykey.databinding.FahrradparkenLocationSelectionBinding
import com.telekom.citykey.domain.city.CityInteractor
import com.telekom.citykey.models.fahrradparken.FahrradparkenReport
import com.telekom.citykey.utils.BitmapUtil
import com.telekom.citykey.utils.DialogUtil
import com.telekom.citykey.utils.extensions.dpToPixel
import com.telekom.citykey.utils.extensions.getColor
import com.telekom.citykey.utils.extensions.getDrawable
import com.telekom.citykey.utils.extensions.hasPermission
import com.telekom.citykey.utils.extensions.isNotVisible
import com.telekom.citykey.utils.extensions.setAccessibilityRoleForToolbarTitle
import com.telekom.citykey.utils.extensions.setVisible
import com.telekom.citykey.utils.extensions.showDialog
import com.telekom.citykey.utils.extensions.tryParsingColorStringToInt
import com.telekom.citykey.utils.extensions.viewBinding
import com.telekom.citykey.utils.isDarkMode
import com.telekom.citykey.utils.tryLoadingNightStyle
import com.telekom.citykey.view.FullScreenBottomSheetDialogFragment
import com.telekom.citykey.view.services.fahrradparken.FahrradparkenService
import com.telekom.citykey.view.services.fahrradparken.existing_reports.FahrradparkenExistingReportsViewModel
import com.telekom.citykey.view.services.fahrradparken.existing_reports.FahrradparkenReportDetails
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import org.koin.androidx.viewmodel.ext.android.viewModel

class FahrradparkenLocationSelection(
    private var toolbarTitle: String,
    private var serviceCode: String,
    private var moreInformationBaseUrl: String?,
    private val previousSelectedLocation: LatLng? = null,
    private val locationResultListener: (LatLng) -> Unit
) : FullScreenBottomSheetDialogFragment(R.layout.fahrradparken_location_selection),
    OnMapReadyCallback, GoogleMap.OnMarkerClickListener {

    private val binding by viewBinding(FahrradparkenLocationSelectionBinding::bind)
    private val viewModel: FahrradparkenExistingReportsViewModel by viewModel()

    private var googleMap: GoogleMap? = null
    private val locationManager: LocationManager by lazy {
        requireActivity().getSystemService(Context.LOCATION_SERVICE) as LocationManager
    }

    private var userLocation: LatLng? = null
    private var cityLocation: LatLng? = null
    private var selectedLocation: LatLng? = null

    private var userLocationMarker: Marker? = null
    private var isFirstPassCompleted: Boolean = false

    private val requestPermissionLauncher =
        registerForActivityResult(ActivityResultContracts.RequestMultiplePermissions()) {
            if (it[Manifest.permission.ACCESS_FINE_LOCATION] == true && it[Manifest.permission.ACCESS_COARSE_LOCATION] == true) {
                if (!LocationManagerCompat.isLocationEnabled(locationManager)) {
                    showLocationDialog()
                }
                viewModel.onLocationPermissionAvailable()
                binding.userCurrentLocationButton.apply {
                    setImageDrawable(getDrawable(R.drawable.ic_locateme))
                    setOnClickListener {
                        showUserLocationMarker()
                    }
                }
            } else {
                if (viewModel.isFetchingReports.not()) invokeApiCallForExistingReports()
            }
        }

    private val locationDialogLauncher =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) {
            if (!LocationManagerCompat.isLocationEnabled(locationManager)) {
                binding.userCurrentLocationButton.setOnClickListener {
                    requestPermission()
                }
            }
        }

    private var shouldFetchExistingReportsForNewBounds = false
    private var fahrradparkenExistingReports: List<FahrradparkenReport>? = null

    private val reportMarkersBitmapDescriptorMap = mutableMapOf<Int, BitmapDescriptor>()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupToolbar()
        initMapView()
        initOtherViews()
        initSubscribers()
    }

    private fun setupToolbar() {
        with(binding.toolbar) {
            title = toolbarTitle
            setNavigationIcon(R.drawable.ic_profile_close)
            setNavigationIconTint(getColor(R.color.onSurface))
            setNavigationOnClickListener { dismiss() }
            navigationContentDescription = getString(R.string.accessibility_btn_close)
        }
        setAccessibilityRoleForToolbarTitle(binding.toolbar)
    }

    private fun initMapView() {
        binding.mapView.attachLifecycleToFragment(this@FahrradparkenLocationSelection)
        binding.mapView.getMapAsync(this)
    }

    private fun initOtherViews() {
        binding.locationSelectionPinImage.setImageBitmap(BitmapUtil.createMarker(requireContext()))
        if (arePermissionsGranted()) {
            viewModel.onLocationPermissionAvailable()
            binding.userCurrentLocationButton.apply {
                setImageDrawable(getDrawable(R.drawable.ic_locateme))
                setOnClickListener {
                    if (LocationManagerCompat.isLocationEnabled(locationManager)) {
                        showUserLocationMarker()
                    } else {
                        showLocationDialog()
                    }
                }
            }
        } else {
            binding.userCurrentLocationButton.apply {
                setImageDrawable(getDrawable(R.drawable.ic_locateoff))
                setOnClickListener {
                    requestPermission()
                }
            }
        }

        with(binding) {
            refreshReportsLoader.setColor(CityInteractor.cityColorInt)
            refreshReportsButton.apply {
                strokeColor = ColorStateList.valueOf(CityInteractor.cityColorInt)
                strokeWidth = 1.dpToPixel(context)
                setBackgroundColor(context.getColor(R.color.background))
                setTextColor(CityInteractor.cityColorInt)
            }

            refreshReportsButton.setOnClickListener {
                if (binding.refreshReportsLoader.isNotVisible) {
                    refreshReportsButton.text = ""
                    refreshReportsButton.setVisible(true)
                    refreshReportsLoader.setVisible(true)
                    invokeApiCallForExistingReports()
                }
            }
        }

        binding.saveLocationButton.setOnClickListener {
            selectedLocation?.let { locationResultListener.invoke(it) }
                ?: cityLocation?.let { locationResultListener.invoke(it) }
            dismiss()
        }
    }

    private fun initSubscribers() {
        viewModel.cityLocation.observe(viewLifecycleOwner) {
            cityLocation = it
        }

        viewModel.location.observe(viewLifecycleOwner) { location ->
            if (userLocation?.latitude == location?.latitude && userLocation?.longitude == location?.longitude) {
                return@observe
            }
            userLocation = location
            userLocation?.let {
                showUserLocationMarker()
                shouldFetchExistingReportsForNewBounds = true
            } ?: moveCameraToCityLocation()
        }

        viewModel.fahrradparkenExistingReports.observe(viewLifecycleOwner) {
            shouldFetchExistingReportsForNewBounds = false
            with(binding) {
                refreshReportsButton.setVisible(false)
                refreshReportsLoader.setVisible(false)
            }
            fahrradparkenExistingReports = it
            googleMap?.clear()
            showUserLocationMarker(shouldMoveCamera = false)
            populateReportsMarkers()
        }

        viewModel.technicalError.observe(viewLifecycleOwner) {
            DialogUtil.showInfoDialog(requireContext(), R.string.fa_012_unknow_error_title)
        }
    }

    private fun showUserLocationMarker(shouldMoveCamera: Boolean = true) {
        userLocation?.let {
            userLocationMarker = googleMap?.addMarker(
                MarkerOptions().position(it)
                    .icon(BitmapDescriptorFactory.fromBitmap(getDrawable(R.drawable.ic_icon_location_circle)!!.toBitmap()))
            )
            if (shouldMoveCamera) updateCameraToPosition(it)
        }
    }

    private fun moveCameraToCityLocation() {
        cityLocation?.let {
            shouldFetchExistingReportsForNewBounds = true
            updateCameraToPosition(it)
        }
    }

    private fun updateCameraToPosition(location: LatLng) {
        googleMap?.moveCamera(CameraUpdateFactory.newLatLngZoom(location, 14f))
    }

    private fun showLocationDialog() {
        DialogUtil.showDialogPositiveNegative(
            context = requireContext(),
            title = R.string.c_001_cities_cannot_access_location_dialog_title,
            message = R.string.c_001_cities_gps_turned_off,
            positiveBtnLabel = R.string.c_001_cities_cannot_access_location_btn_poitive,
            negativeBtnLabel = android.R.string.cancel,
            isCancelable = false,
            negativeClickListener = {
                binding.userCurrentLocationButton.setOnClickListener {
                    requestPermission()
                }
                if (isFirstPassCompleted.not()) {
                    moveCameraToCityLocation()
                    isFirstPassCompleted = true
                }
            },
            positiveClickListener = {
                locationDialogLauncher.launch(
                    Intent().apply {
                        flags = Intent.FLAG_ACTIVITY_NEW_TASK
                        action = Settings.ACTION_LOCATION_SOURCE_SETTINGS
                    }
                )
            }
        )
    }

    private fun populateReportsMarkers() {
        fahrradparkenExistingReports?.forEach { report ->
            val latLng = LatLng(report.lat!!, report.lng!!)
            val marker = googleMap?.addMarker(
                MarkerOptions().position(latLng)
                    .icon(
                        getReportMarkerBitmapDescriptor(
                            report.extendedAttributes?.markASpot?.statusIcon,
                            report.extendedAttributes?.markASpot?.statusHex
                        )
                    )
            )
            marker?.tag = report
        }
    }

    private var selectedMarker: Marker? = null
    private val markerStatusResourceMap: Map<String, Int> by lazy {
        mapOf(
            FahrradparkenService.REPORT_STATUS_ICON_ERROR to R.drawable.ic_fa_report_not_possible_marker,
            FahrradparkenService.REPORT_STATUS_ICON_DONE to R.drawable.ic_fa_report_implemented_marker,
            FahrradparkenService.REPORT_STATUS_ICON_IN_PROGRESS to R.drawable.ic_fa_report_in_progress_marker,
            FahrradparkenService.REPORT_STATUS_ICON_QUEUED to R.drawable.ic_fa_report_queued_marker,
        )
    }

    private val selectedMarkerStatusResourceMap: Map<String, Int> by lazy {
        mapOf(
            FahrradparkenService.REPORT_STATUS_ICON_ERROR to R.drawable.ic_fa_report_not_possible_marker_selected,
            FahrradparkenService.REPORT_STATUS_ICON_DONE to R.drawable.ic_fa_report_implemented_marker_selected,
            FahrradparkenService.REPORT_STATUS_ICON_IN_PROGRESS to R.drawable.ic_fa_report_in_progress_marker_selected,
            FahrradparkenService.REPORT_STATUS_ICON_QUEUED to R.drawable.ic_fa_report_queued_marker_selected,
        )
    }

    private fun getReportMarkerBitmapDescriptor(statusIcon: String?, statusHex: String?, selected: Boolean = false)
            : BitmapDescriptor {
        val markerResId = if (selected) {
            selectedMarkerStatusResourceMap[statusIcon] ?: R.drawable.ic_fa_report_unknown_marker_selected
        } else {
            markerStatusResourceMap[statusIcon] ?: R.drawable.ic_fa_report_unknown_marker
        }
        return reportMarkersBitmapDescriptorMap[markerResId] ?: kotlin.run {
            val descriptor = BitmapDescriptorFactory.fromBitmap(createReportMarker(markerResId, statusHex, selected))
            reportMarkersBitmapDescriptorMap[markerResId] = descriptor
            descriptor
        }
    }

    private fun invokeApiCallForExistingReports() {
        lifecycleScope.launch {
            delay(1000)
            getCurrentBoundingBox()?.let {
                viewModel.getExistingReports(serviceCode, it, googleMap?.cameraPosition?.zoom)
            }
        }
    }

    private fun getCurrentBoundingBox(): String? {
        return googleMap?.projection?.visibleRegion?.latLngBounds?.let {
            with(it.southwest) { "$longitude,$latitude" } + "," + with(it.northeast) { "$longitude,$latitude" }
        }
    }

    private fun createReportMarker(@DrawableRes drawableResId: Int, tintColorHex: String?, selected: Boolean): Bitmap {
        val markerBaseDrawable = if (selected)
            getDrawable(R.drawable.ic_fa_report_marker_selected_base)!!
        else
            getDrawable(R.drawable.ic_fa_report_marker_base)!!.apply {
                colorFilter = PorterDuffColorFilter(tryParsingColorStringToInt(tintColorHex), PorterDuff.Mode.SRC_IN)
            }
        val markerBaseBitmap = markerBaseDrawable.toBitmap().copy(Bitmap.Config.ARGB_8888, true)

        val markerDrawable = getDrawable(drawableResId)!!
        if (selected) {
            markerDrawable.apply {
                colorFilter = PorterDuffColorFilter(tryParsingColorStringToInt(tintColorHex), PorterDuff.Mode.SRC_IN)
            }
        }
        val markerBitmap = markerDrawable.toBitmap().copy(Bitmap.Config.ARGB_8888, true)

        val canvas = Canvas(markerBaseBitmap)
        canvas.drawBitmap(markerBitmap, 0f, 0f, null)
        return markerBaseBitmap
    }

    private fun arePermissionsGranted() =
        requireContext().hasPermission(Manifest.permission.ACCESS_FINE_LOCATION) &&
                requireContext().hasPermission(Manifest.permission.ACCESS_COARSE_LOCATION)

    private fun requestPermission() {
        requestPermissionLauncher.launch(
            arrayOf(Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION)
        )
    }

    override fun onMapReady(map: GoogleMap) {
        if (resources.isDarkMode) {
            map.tryLoadingNightStyle(requireContext())
        }
        googleMap = map
        shouldFetchExistingReportsForNewBounds = false
        googleMap?.uiSettings?.isMapToolbarEnabled = false

        if (previousSelectedLocation == null) {
            cityLocation?.let { updateCameraToPosition(it) }
        } else {
            updateCameraToPosition(previousSelectedLocation)
        }

        if (!arePermissionsGranted() || !LocationManagerCompat.isLocationEnabled(locationManager)) {
            requestPermission()
        }
        initGoogleMapListeners()
    }

    override fun onDestroyView() {
        googleMap?.clear()
        googleMap = null
        super.onDestroyView()
    }

    private fun initGoogleMapListeners() {
        googleMap?.setOnMarkerClickListener(this)

        googleMap?.setOnCameraMoveStartedListener { reason ->
            if (reason == GoogleMap.OnCameraMoveStartedListener.REASON_GESTURE) {
                binding.refreshReportsLoader.setVisible(false)
                binding.refreshReportsButton.apply {
                    text = getString(R.string.fa_004_refresh_reports_label)
                    setVisible(true)
                }
            }
        }

        googleMap?.setOnCameraIdleListener {
            selectedLocation = googleMap?.cameraPosition?.target
            if (shouldFetchExistingReportsForNewBounds) {
                invokeApiCallForExistingReports()
            }
        }
    }

    override fun onMarkerClick(marker: Marker): Boolean {
        if (marker.tag as? FahrradparkenReport == null) return false

        FahrradparkenReportDetails(toolbarTitle, marker.tag as FahrradparkenReport, moreInformationBaseUrl) {
            selectedMarker?.let { updateMarkerIcon(it, false) }
        }.showDialog(childFragmentManager)
        updateMarkerIcon(marker, true)
        return false
    }

    private fun updateMarkerIcon(marker: Marker, selected: Boolean) {
        val report = marker.tag as FahrradparkenReport
        marker.setIcon(
            getReportMarkerBitmapDescriptor(
                report.extendedAttributes?.markASpot?.statusIcon,
                report.extendedAttributes?.markASpot?.statusHex,
                selected
            )
        )
        marker.zIndex = 1f
        selectedMarker = if (selected) marker else null
    }

}
