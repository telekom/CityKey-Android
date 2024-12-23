package com.telekom.citykey.view.services.poi.details

import android.os.Bundle
import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import android.view.View
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.model.BitmapDescriptorFactory
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MarkerOptions
import com.telekom.citykey.DetailedMapGraphArgs
import com.telekom.citykey.R
import com.telekom.citykey.databinding.PoiGuideDetailsFragmentBinding
import com.telekom.citykey.domain.city.CityInteractor
import com.telekom.citykey.utils.BitmapUtil
import com.telekom.citykey.utils.ShareUtils
import com.telekom.citykey.utils.extensions.AccessibilityRole
import com.telekom.citykey.utils.extensions.decodeHTML
import com.telekom.citykey.utils.extensions.loadFromDrawable
import com.telekom.citykey.utils.extensions.openMapApp
import com.telekom.citykey.utils.extensions.setAccessibilityRole
import com.telekom.citykey.utils.extensions.setVisible
import com.telekom.citykey.utils.extensions.viewBinding
import com.telekom.citykey.utils.isDarkMode
import com.telekom.citykey.utils.tryLoadingNightStyle
import com.telekom.citykey.view.MainFragment

class PoiGuideDetails : MainFragment(R.layout.poi_guide_details_fragment), OnMapReadyCallback {
    private val binding by viewBinding(PoiGuideDetailsFragmentBinding::bind)
    private val args: PoiGuideDetailsArgs by navArgs()
    private var googleMap: GoogleMap? = null

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupToolbar(binding.poiToolbar)
        initMap()
        initViews()
    }

    private fun initViews() {
        binding.descriptionLabel.setVisible(args.poiData.description.isNotEmpty())
        binding.description.setVisible(args.poiData.description.isNotEmpty())
        binding.openingHours.setVisible(args.poiData.openHours.isNotEmpty())
        binding.openingHoursLabel.setVisible(args.poiData.openHours.isNotEmpty())
        binding.websiteLabel.setVisible(args.poiData.url.isNotEmpty())
        binding.websiteLink.setVisible(args.poiData.url.isNotEmpty())
        binding.addressContainer.setVisible(args.poiData.address.isNotEmpty())

        binding.poiToolbar.title = args.categoryName
        binding.categoryTitle.text = args.poiData.title
        binding.categorySubTitle.text = args.poiData.subtitle
        binding.categorySubTitle.setVisible(args.poiData.subtitle.isNotBlank())
        binding.openingHours.text = args.poiData.openHours.decodeHTML()
        binding.description.text = args.poiData.description.decodeHTML()
        binding.websiteLink.text = args.poiData.url
        binding.locationAddress.text = args.poiData.address
        binding.categoryGroupIcon.loadFromDrawable(args.poiData.categoryGroupIconId)
        binding.categoryGroupIcon.setColorFilter(CityInteractor.cityColorInt)

        binding.websiteLink.setLinkTextColor(CityInteractor.cityColorInt)
        binding.openingHours.setLinkTextColor(CityInteractor.cityColorInt)
        binding.description.setLinkTextColor(CityInteractor.cityColorInt)
        binding.locationNavigation.setTextColor(CityInteractor.cityColorInt)

        binding.locationNavigation.setAccessibilityRole(AccessibilityRole.Button)
        binding.websiteLink.setAccessibilityRole(AccessibilityRole.Link)
    }

    private fun initMap() {
        binding.mapView.attachLifecycleToFragment(this@PoiGuideDetails)
        binding.mapView.getMapAsync(this)
    }

    private fun setUpLocation(location: LatLng) {
        googleMap?.let { googleMap ->
            googleMap.addMarker(
                MarkerOptions().position(location)
                    .icon(BitmapDescriptorFactory.fromBitmap(BitmapUtil.createMarker(requireContext())))
            )
            googleMap.moveCamera(CameraUpdateFactory.newLatLngZoom(location, 17f))
            googleMap.uiSettings.isZoomGesturesEnabled = false
            googleMap.uiSettings.isScrollGesturesEnabled = false
            googleMap.uiSettings.isRotateGesturesEnabled = false

            googleMap.setOnMapClickListener {
                findNavController().navigate(
                    R.id.action_poiGuideDetails_to_detailed_map_graph,
                    DetailedMapGraphArgs.Builder(
                        args.poiData.title,
                        args.categoryName,
                        args.poiData.address,
                        location,
                    ).build().toBundle()
                )
            }
        }

        binding.mapView.setMapsAccessibility {
            findNavController().navigate(
                R.id.action_poiGuideDetails_to_detailed_map_graph,
                DetailedMapGraphArgs.Builder(
                    args.poiData.title,
                    args.categoryName,
                    args.poiData.address,
                    location,
                ).build().toBundle()
            )
        }

        binding.locationNavigation.setOnClickListener {
            openMapApp(args.poiData.latitude, args.poiData.longitude)
        }
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        inflater.inflate(R.menu.share_menu, menu)
        super.onCreateOptionsMenu(menu, inflater)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.actionShare -> {
                startActivity(
                    ShareUtils.createShareIntent(
                        createTextToShare(),
                        args.poiData.url,
                        getString(R.string.poi_share_text_footer)
                    )
                )
                return true
            }
        }
        return super.onOptionsItemSelected(item)
    }

    private fun createTextToShare() = StringBuilder()
        .append(args.poiData.title.ifBlank { "" })
        .append(if (args.poiData.address.isBlank()) "" else "\n" + args.poiData.address)
        .append(if (args.poiData.subtitle.isBlank()) "" else "\n" + args.poiData.subtitle)
        .append(if (args.poiData.description.isBlank()) "" else "\n" + args.poiData.description)
        .append(if (args.poiData.openHours.isBlank()) "" else "\n" + args.poiData.openHours)
        .toString()

    override fun onMapReady(map: GoogleMap) {
        if (resources.isDarkMode) {
            map.tryLoadingNightStyle(requireContext())
        }
        googleMap = map
        setUpLocation(LatLng(args.poiData.latitude, args.poiData.longitude))
    }

    override fun onDestroyView() {
        googleMap?.clear()
        googleMap = null
        super.onDestroyView()
    }
}
