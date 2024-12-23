package com.telekom.citykey.view.detailed_map

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.View
import androidx.navigation.fragment.navArgs
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.model.BitmapDescriptorFactory
import com.google.android.gms.maps.model.MarkerOptions
import com.telekom.citykey.R
import com.telekom.citykey.databinding.DetailedMapFragmentBinding
import com.telekom.citykey.domain.city.CityInteractor
import com.telekom.citykey.utils.BitmapUtil
import com.telekom.citykey.utils.extensions.getColor
import com.telekom.citykey.utils.extensions.openMapApp
import com.telekom.citykey.utils.extensions.viewBinding
import com.telekom.citykey.utils.isDarkMode
import com.telekom.citykey.utils.tryLoadingNightStyle
import com.telekom.citykey.view.MainFragment
import com.telekom.citykey.view.main.MainActivity

class DetailedMap : MainFragment(R.layout.detailed_map_fragment) {

    private val args: DetailedMapArgs by navArgs()
    private val binding by viewBinding(DetailedMapFragmentBinding::bind)

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupToolbar(binding.toolbarMapDetails)
        binding.toolbarMapDetails.setNavigationIcon(R.drawable.ic_profile_close)
        binding.toolbarMapDetails.setNavigationIconTint(getColor(R.color.onSurface))

        binding.toolbarMapDetails.title = args.title

        binding.locationAddress.text = args.streetName
            ?.replace("\\n", "\n")
            ?.replace("\\r", "\r")
        if (args.streetName.isNullOrBlank()) {
            binding.locationContainer.visibility = View.GONE
        }
        if (!args.locationName.isNullOrBlank()) {
            binding.locationName.visibility = View.VISIBLE
            binding.locationName.text = args.locationName
        }
        binding.getDirectionsTv.setTextColor(CityInteractor.cityColorInt)

        binding.mapView.attachLifecycleToFragment(this@DetailedMap)
        binding.mapView.getMapAsync {
            if (resources.isDarkMode) {
                it.tryLoadingNightStyle(requireContext())
            }
            val location = args.location
            it.addMarker(
                MarkerOptions().position(location)
                    .icon(BitmapDescriptorFactory.fromBitmap(BitmapUtil.createMarker(requireContext())))
            )
            it.moveCamera(CameraUpdateFactory.newLatLngZoom(location, 14f))
        }

        binding.directionsButton.setOnClickListener {
            openMapApp(args.location.latitude, args.location.longitude)
        }

        (activity as? MainActivity)?.hideBottomNavBar()
    }

    private val directionsQuery
        get() = if (args.streetName.isNullOrEmpty())
            "${args.location.latitude},${args.location.longitude}"
        else args.streetName

    override fun onDetach() {
        super.onDetach()
        (activity as? MainActivity)?.revealBottomNavBar()
    }

}
