package com.telekom.citykey.view.services.defect_reporter.location_selection

import android.Manifest
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.PorterDuff
import android.graphics.PorterDuffColorFilter
import android.location.LocationManager
import android.os.Bundle
import android.provider.Settings
import android.view.View
import android.widget.RelativeLayout
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.graphics.drawable.toBitmap
import androidx.core.location.LocationManagerCompat
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.model.BitmapDescriptorFactory
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.Marker
import com.google.android.gms.maps.model.MarkerOptions
import com.telekom.citykey.R
import com.telekom.citykey.databinding.DefectLocationSelectionBinding
import com.telekom.citykey.domain.city.CityInteractor
import com.telekom.citykey.utils.DialogUtil
import com.telekom.citykey.utils.extensions.getColor
import com.telekom.citykey.utils.extensions.getDrawable
import com.telekom.citykey.utils.extensions.hasPermission
import com.telekom.citykey.utils.extensions.setAccessibilityRoleForToolbarTitle
import com.telekom.citykey.utils.extensions.viewBinding
import com.telekom.citykey.utils.isDarkMode
import com.telekom.citykey.utils.tryLoadingNightStyle
import com.telekom.citykey.view.FullScreenBottomSheetDialogFragment
import org.koin.androidx.viewmodel.ext.android.viewModel

class DefectLocationSelection(
    private val prevDefectLocation: LatLng? = null,
    private val locationResultListener: (LatLng) -> Unit
) : FullScreenBottomSheetDialogFragment(R.layout.defect_location_selection),
    OnMapReadyCallback {
    private val binding by viewBinding(DefectLocationSelectionBinding::bind)
    private var gMap: GoogleMap? = null
    private val viewModel: DefectLocationSelectionViewModel by viewModel()
    private var pendingPosition: LatLng? = null
    private var cityLocation: LatLng? = null
    private var defectLocation: LatLng? = null
    private val locationManager: LocationManager by lazy { requireActivity().getSystemService(Context.LOCATION_SERVICE) as LocationManager }
    private var marker: Marker? = null

    private val requestPermissionLauncher =
        registerForActivityResult(ActivityResultContracts.RequestMultiplePermissions()) {
            if (it[Manifest.permission.ACCESS_FINE_LOCATION] == true && it[Manifest.permission.ACCESS_COARSE_LOCATION] == true) {
                if (!LocationManagerCompat.isLocationEnabled(locationManager)) {
                    showLocationDialog()
                }
                viewModel.onLocationPermissionAvailable()
                binding.locateMe.setImageDrawable(getDrawable(R.drawable.ic_locateme))
                binding.locateMe.setOnClickListener {
                    pendingPosition?.let { latLng -> moveToPosition(latLng) }
                }
            }
        }
    private val locationDialogLauncher =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) {
            if (!LocationManagerCompat.isLocationEnabled(locationManager)) {
                binding.locateMe.setOnClickListener {
                    requestPermission()
                }
            }
        }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.toolbarLocation.setNavigationIcon(R.drawable.ic_profile_close)
        binding.toolbarLocation.setNavigationIconTint(getColor(R.color.onSurface))
        binding.toolbarLocation.setNavigationOnClickListener { dismiss() }
        binding.toolbarLocation.navigationContentDescription =
            getString(R.string.accessibility_btn_close)
        setAccessibilityRoleForToolbarTitle(binding.toolbarLocation)
        initMap()
        initViews()
        subscribeUi()
    }

    fun subscribeUi() {
        viewModel.cityLocation.observe(viewLifecycleOwner) {
            cityLocation = it
        }

        viewModel.location.observe(viewLifecycleOwner) { location ->
            pendingPosition = location
            if (prevDefectLocation == null) {
                pendingPosition?.let { moveToPosition(it) }
            }
            marker?.remove()
            pendingPosition?.let {
                marker = gMap?.addMarker(
                    MarkerOptions().position(it)
                        .icon(BitmapDescriptorFactory.fromBitmap(getDrawable(R.drawable.ic_icon_location_circle)!!.toBitmap()))
                )
            }
        }
    }

    fun initViews() {
        setMapToolbarPosition()
        binding.markerImage.setImageBitmap(createMarker())
        binding.progressBtnLocation.setupNormalStyle(CityInteractor.cityColorInt)
        if (arePermissionsGranted()) {
            viewModel.onLocationPermissionAvailable()
            binding.locateMe.setImageDrawable(getDrawable(R.drawable.ic_locateme))
            binding.locateMe.setOnClickListener {
                if (LocationManagerCompat.isLocationEnabled(locationManager))
                    pendingPosition?.let { moveToPosition(it) }
                else
                    showLocationDialog()
            }
        } else {
            binding.locateMe.setImageDrawable(getDrawable(R.drawable.ic_locateoff))
            binding.locateMe.setOnClickListener {
                requestPermission()
            }
        }
        binding.progressBtnLocation.setOnClickListener {
            if (defectLocation != null)
                defectLocation?.let { location -> locationResultListener.invoke(location) }
            else
                cityLocation?.let { cityLocation -> locationResultListener.invoke(cityLocation) }

            dismiss()
        }
    }

    private fun setMapToolbarPosition() {
        (binding.mapView.findViewById<View>("1".toInt())?.rootView
            ?.findViewById<View>("4".toInt())?.layoutParams as? RelativeLayout.LayoutParams)?.apply {
            removeRule(RelativeLayout.ALIGN_PARENT_TOP)
            addRule(RelativeLayout.ALIGN_PARENT_TOP, RelativeLayout.TRUE)
            setMargins(0, 30, 30, 0)
        }
    }

    private fun moveToPosition(location: LatLng) {
        gMap?.moveCamera(CameraUpdateFactory.newLatLngZoom(location, 14f))
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
                binding.locateMe.setOnClickListener {
                    requestPermission()
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

    private fun initMap() {
        binding.mapView.attachLifecycleToFragment(this@DefectLocationSelection)
        binding.mapView.getMapAsync(this)
    }

    override fun onMapReady(map: GoogleMap) {
        if (resources.isDarkMode) {
            map.tryLoadingNightStyle(requireContext())
        }
        gMap = map
        if (prevDefectLocation == null) {
            cityLocation?.let { moveToPosition(it) }
        } else {
            moveToPosition(prevDefectLocation)
        }
        if (!arePermissionsGranted() || !LocationManagerCompat.isLocationEnabled(locationManager)) {
            requestPermission()
        }
        gMap?.setOnCameraIdleListener {
            defectLocation = gMap?.cameraPosition?.target
        }
    }

    private fun createMarker(): Bitmap {
        val base = getDrawable(R.drawable.ic_icon_navigation_location_default)!!.apply {
            colorFilter = PorterDuffColorFilter(CityInteractor.cityColorInt, PorterDuff.Mode.SRC_IN)
        }.toBitmap().copy(Bitmap.Config.ARGB_8888, true)

        val canvas = Canvas(base)
        canvas.translate(base.width / 2f, base.height / 3f)
        canvas.drawCircle(
            0.0f, 0.0f, 12.0f,
            Paint().apply {
                color = Color.WHITE
                isAntiAlias = true
            }
        )

        return base
    }

    private fun arePermissionsGranted() =
        requireContext().hasPermission(Manifest.permission.ACCESS_FINE_LOCATION) &&
                requireContext().hasPermission(Manifest.permission.ACCESS_COARSE_LOCATION)

    private fun requestPermission() {
        requestPermissionLauncher.launch(
            arrayOf(
                Manifest.permission.ACCESS_FINE_LOCATION,
                Manifest.permission.ACCESS_COARSE_LOCATION
            )
        )
    }

    override fun onDestroyView() {
        gMap?.clear()
        gMap = null
        super.onDestroyView()
    }

}
