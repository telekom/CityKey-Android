/**
 * Copyright (C) 2025 Deutsche Telekom AG
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 * In accordance with Sections 4 and 6 of the License, the following exclusions apply:
 *
 *  1. Trademarks & Logos – The names, logos, and trademarks of the Licensor are not covered by this License and may not be used without separate permission.
 *  2. Design Rights – Visual identities, UI/UX designs, and other graphical elements remain the property of their respective owners and are not licensed under the Apache License 2.0.
 *  3: Non-Coded Copyrights – Documentation, images, videos, and other non-software materials require separate authorization for use, modification, or distribution.
 *
 * These elements are not considered part of the licensed Work or Derivative Works unless explicitly agreed otherwise. All elements must be altered, removed, or replaced before use or distribution. All rights to these materials are reserved, and Contributor accepts no liability for any infringing use. By using this repository, you agree to indemnify and hold harmless Contributor against any claims, costs, or damages arising from your use of the excluded elements.
 *
 * SPDX-FileCopyrightText: 2025 Deutsche Telekom AG
 * SPDX-License-Identifier: Apache-2.0 AND LicenseRef-Deutsche-Telekom-Brand
 * License-Filename: LICENSES/Apache-2.0.txt LICENSES/LicenseRef-Deutsche-Telekom-Brand.txt
 */

package com.telekom.citykey.view.services.poi.map

import android.Manifest
import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.PorterDuff
import android.graphics.PorterDuffColorFilter
import android.location.LocationManager
import android.os.Bundle
import android.provider.Settings
import android.view.View
import android.widget.RelativeLayout
import androidx.activity.result.contract.ActivityResultContracts
import androidx.annotation.DrawableRes
import androidx.core.graphics.drawable.toBitmap
import androidx.core.location.LocationManagerCompat
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.google.android.gms.maps.CameraUpdate
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.model.BitmapDescriptor
import com.google.android.gms.maps.model.BitmapDescriptorFactory
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.LatLngBounds
import com.google.android.gms.maps.model.Marker
import com.google.android.gms.maps.model.MarkerOptions
import com.telekom.citykey.R
import com.telekom.citykey.databinding.PoiMapFragmentBinding
import com.telekom.citykey.domain.city.CityInteractor
import com.telekom.citykey.network.extensions.categoryGroupIconId
import com.telekom.citykey.network.extensions.mapMarker
import com.telekom.citykey.utils.DialogUtil
import com.telekom.citykey.utils.KoverIgnore
import com.telekom.citykey.utils.extensions.getDrawable
import com.telekom.citykey.utils.extensions.hasPermission
import com.telekom.citykey.utils.extensions.showDialog
import com.telekom.citykey.utils.extensions.viewBinding
import com.telekom.citykey.utils.isDarkMode
import com.telekom.citykey.utils.tryLoadingNightStyle
import com.telekom.citykey.view.services.poi.PoiGuideDirections
import com.telekom.citykey.view.services.poi.PoiGuideViewModel
import org.koin.androidx.viewmodel.ext.android.sharedViewModel
import timber.log.Timber

@KoverIgnore
class PoiMap : Fragment(R.layout.poi_map_fragment), GoogleMap.OnMarkerClickListener {

    private val binding by viewBinding(PoiMapFragmentBinding::bind)
    private val viewModel: PoiGuideViewModel by sharedViewModel()

    private var gMap: GoogleMap? = null
    private var pendingMarkers = mutableListOf<MarkerOptions>()
    private lateinit var markerBitmap: BitmapDescriptor
    private var defaultCameraUpdate: CameraUpdate? = null
    private var zoomLevel = 14f
    private var userPosition: LatLng? = null
    var categoryName: String = ""
    private var isRefreshRequired = false
    private val locationManager: LocationManager by lazy { requireActivity().getSystemService(Context.LOCATION_SERVICE) as LocationManager }
    private var isMapLoaded = false

    @SuppressLint("MissingPermission")
    private val requestPermissionLauncher =
        registerForActivityResult(ActivityResultContracts.RequestMultiplePermissions()) {
            if (it[Manifest.permission.ACCESS_FINE_LOCATION] == true && it[Manifest.permission.ACCESS_COARSE_LOCATION] == true) {
                if (!LocationManagerCompat.isLocationEnabled(locationManager)) {
                    showLocationDialog()
                } else {
                    viewModel.onLocationPermissionAvailable()
                    viewModel.onServiceReady(true)
                    gMap?.isMyLocationEnabled = true
                    gMap?.uiSettings?.isMyLocationButtonEnabled = false
                    binding.myLocation.setOnClickListener {
                        userPosition?.let { pos ->
                            moveCamera(CameraUpdateFactory.newLatLngZoom(pos, zoomLevel))
                        }
                    }
                }
                binding.myLocation.setImageDrawable(getDrawable(R.drawable.ic_locateme))
            }
        }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        subscribeUi()
        initViews()
    }

    @SuppressLint("MissingPermission")
    private fun initViews() {
        binding.mapView.attachLifecycleToFragment(this@PoiMap)
        binding.mapView.getMapAsync { map ->
            if (resources.isDarkMode) {
                map.tryLoadingNightStyle(requireContext())
            }
            gMap = map
            map.setOnMarkerClickListener(this)
            showPendingMarkers()
            map.setOnMapLoadedCallback {
                isMapLoaded = true
                moveCamera()
                binding.progressSpinner.visibility = View.GONE
            }
        }
        setMapToolbarPosition()

        if (arePermissionsGranted()) {
            binding.myLocation.setOnClickListener {
                if (LocationManagerCompat.isLocationEnabled(locationManager)) {
                    userPosition?.let { pos ->
                        moveCamera(CameraUpdateFactory.newLatLngZoom(pos, zoomLevel))
                    }
                    gMap?.isMyLocationEnabled = true
                    gMap?.uiSettings?.isMyLocationButtonEnabled = false
                } else {
                    showLocationDialog()
                }
            }
            binding.myLocation.setImageDrawable(getDrawable(R.drawable.ic_locateme))
            viewModel.onLocationPermissionAvailable()
        } else {
            binding.myLocation.setImageDrawable(getDrawable(R.drawable.ic_locateoff))
            binding.myLocation.setOnClickListener { requestPermission() }
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

    private fun subscribeUi() {
        viewModel.poiData.observe(viewLifecycleOwner) {
            if (it.items.isNotEmpty()) {
                markerBitmap = BitmapDescriptorFactory.fromBitmap(createMarker(it.items[0].categoryGroupIconId))
            }
            pendingMarkers.clear()
            pendingMarkers.addAll(it.items.map { poi -> poi.mapMarker })
            showPendingMarkers()
            zoomLevel = it.zoomLevel
            defaultCameraUpdate = it.bounds?.let { bounds ->
                CameraUpdateFactory.newLatLngBounds(bounds, 100)
            } ?: CameraUpdateFactory.newLatLngZoom(it.cityLocation, zoomLevel)
            moveCamera()
        }
        viewModel.userLocation.observe(viewLifecycleOwner) { location ->
            userPosition = location
        }

        viewModel.activeCategory.observe(viewLifecycleOwner) {
            categoryName = it.categoryName
        }

        viewModel.showDetails.observe(viewLifecycleOwner) { poi ->
            PoiMarkerOverlay(poi, categoryName) {
                findNavController().navigate(
                    PoiGuideDirections.actionPointsOfInterestToPoiGuideDetails(
                        poi,
                        categoryName
                    )
                )
            }.showDialog(childFragmentManager)
        }
    }

    override fun onMarkerClick(marker: Marker): Boolean {
        val position = marker.tag as? LatLng
        viewModel.onMarkerClick(position)
        return false
    }

    override fun onPause() {
        super.onPause()
        isMapLoaded = false
    }

    private fun showPendingMarkers() {
        gMap?.let { map ->
            map.clear()
            val builder = LatLngBounds.Builder()
            pendingMarkers.forEach {
                builder.include(it.position)
                map.addMarker(
                    MarkerOptions().position(it.position).title(it.title).icon(markerBitmap)
                )?.apply { tag = it.position }
            }
            if (pendingMarkers.isNotEmpty() && isMapLoaded) {
                try {
                    val bounds = builder.build()
                    val cu = CameraUpdateFactory.newLatLngBounds(bounds, 100)
                    map.moveCamera(cu)
                } catch (e: Exception) {
                    Timber.e(e)
                }
            }
        }
    }

    private fun createMarker(@DrawableRes iconRes: Int): Bitmap {
        val base = getDrawable(R.drawable.ic_poi_marker_base)!!.apply {
            colorFilter = PorterDuffColorFilter(CityInteractor.cityColorInt, PorterDuff.Mode.SRC_IN)
        }.toBitmap().copy(Bitmap.Config.ARGB_8888, true)
        val icon = getDrawable(iconRes)!!.apply {
            colorFilter = PorterDuffColorFilter(Color.WHITE, PorterDuff.Mode.SRC_IN)
        }.toBitmap()

        val iconStart = (base.width / 2) - (icon.width / 2).toFloat()

        val canvas = Canvas(base)
        canvas.drawBitmap(icon, iconStart, 7f, null)

        return base
    }

    private fun showLocationDialog() {
        DialogUtil.showNoLocationServiceDialog(
            context = requireContext(),
            positiveClickListener = {
                isRefreshRequired = true
                startActivity(
                    Intent().apply {
                        flags = Intent.FLAG_ACTIVITY_NEW_TASK
                        action = Settings.ACTION_LOCATION_SOURCE_SETTINGS
                    }
                )
            }
        )
    }

    private fun moveCamera(cameraUpdate: CameraUpdate? = defaultCameraUpdate) {
        if (cameraUpdate != null && isMapLoaded) {
            if (gMap?.cameraPosition?.zoom == 1f)
                gMap?.animateCamera(cameraUpdate)
            else
                gMap?.moveCamera(cameraUpdate)
        }
    }

    @SuppressLint("MissingPermission")
    private fun requestPermission() {
        requestPermissionLauncher.launch(
            arrayOf(
                Manifest.permission.ACCESS_FINE_LOCATION,
                Manifest.permission.ACCESS_COARSE_LOCATION
            )
        )
    }

    override fun onResume() {
        super.onResume()
        if (LocationManagerCompat.isLocationEnabled(locationManager)) {
            viewModel.onServiceReady(isRefreshRequired)
            isRefreshRequired = false
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        gMap = null
    }

    @SuppressLint("MissingPermission")
    private fun arePermissionsGranted() =
        requireContext().hasPermission(Manifest.permission.ACCESS_FINE_LOCATION) &&
                requireContext().hasPermission(Manifest.permission.ACCESS_COARSE_LOCATION)
}
