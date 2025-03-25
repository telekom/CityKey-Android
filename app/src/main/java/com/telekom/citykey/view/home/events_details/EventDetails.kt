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

package com.telekom.citykey.view.home.events_details

import android.animation.LayoutTransition
import android.content.ActivityNotFoundException
import android.content.Intent
import android.content.res.ColorStateList
import android.graphics.drawable.StateListDrawable
import android.os.Bundle
import android.provider.CalendarContract
import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import android.view.View
import android.webkit.URLUtil
import android.webkit.WebResourceRequest
import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.annotation.ColorInt
import androidx.annotation.DrawableRes
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.updatePadding
import androidx.core.widget.TextViewCompat
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
import com.telekom.citykey.common.TimeConstants.MILLIS_IN_ONE_DAY
import com.telekom.citykey.common.TimeConstants.MILLIS_IN_ONE_HOUR
import com.telekom.citykey.databinding.EventDetailsFragmentBinding
import com.telekom.citykey.domain.city.CityInteractor
import com.telekom.citykey.domain.city.events.EventEngagementOption
import com.telekom.citykey.domain.track.AdjustManager
import com.telekom.citykey.networkinterface.models.content.Event
import com.telekom.citykey.network.extensions.isCancelled
import com.telekom.citykey.network.extensions.isPostponed
import com.telekom.citykey.network.extensions.isSingleDay
import com.telekom.citykey.network.extensions.isSoldOut
import com.telekom.citykey.utils.BitmapUtil
import com.telekom.citykey.utils.DialogUtil
import com.telekom.citykey.utils.NetworkConnection
import com.telekom.citykey.utils.ShareUtils
import com.telekom.citykey.utils.extensions.AccessibilityRole
import com.telekom.citykey.utils.extensions.attemptOpeningWebViewUri
import com.telekom.citykey.utils.extensions.dispatchInsetsToChildViews
import com.telekom.citykey.utils.extensions.dpToPixel
import com.telekom.citykey.utils.extensions.getColor
import com.telekom.citykey.utils.extensions.getDrawable
import com.telekom.citykey.utils.extensions.getHoursAndMins
import com.telekom.citykey.utils.extensions.getLongWeekDay
import com.telekom.citykey.utils.extensions.getShortMonthName
import com.telekom.citykey.utils.extensions.getShortWeekDay
import com.telekom.citykey.utils.extensions.linkifyAndLoadNonHtmlTaggedData
import com.telekom.citykey.pictures.loadFromURLwithProgress
import com.telekom.citykey.utils.extensions.longMonthName
import com.telekom.citykey.utils.extensions.openLink
import com.telekom.citykey.utils.extensions.openMapApp
import com.telekom.citykey.utils.extensions.safeRun
import com.telekom.citykey.utils.extensions.setAccessibilityRole
import com.telekom.citykey.utils.extensions.setVisible
import com.telekom.citykey.utils.extensions.showInfoSnackBar
import com.telekom.citykey.utils.extensions.toCalendar
import com.telekom.citykey.utils.extensions.viewBinding
import com.telekom.citykey.utils.isDarkMode
import com.telekom.citykey.utils.tryLoadingNightStyle
import com.telekom.citykey.view.MainFragment
import com.telekom.citykey.view.main.MainActivity
import org.koin.android.ext.android.inject
import java.util.Calendar

class EventDetails : MainFragment(R.layout.event_details_fragment), OnMapReadyCallback {

    companion object {
        private const val CATEGORIES_MAX_NUMBER = 3
    }

    private val adjustManager: AdjustManager by inject()
    private val args: EventDetailsArgs by navArgs()
    private val binding by viewBinding(EventDetailsFragmentBinding::bind)

    private var googleMap: GoogleMap? = null
    private val viewModel: EventDetailsViewModel by inject()

    private var onFavoriteClicked: () -> Unit = this::showLoginRequired
    private var onAddToCalendarClicked: () -> Unit = this::showLoginRequired
    private var pendingAction: (() -> Unit)? = null
    private lateinit var event: Event

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setupToolbar(binding.toolbarEventDetails)
        binding.layoutEventDetails.layoutTransition.enableTransitionType(LayoutTransition.CHANGING)
        binding.layoutEventDetails.layoutTransition.enableTransitionType(LayoutTransition.CHANGE_APPEARING)
        binding.layoutEventDetails.layoutTransition.enableTransitionType(LayoutTransition.APPEARING)
        initMap()
        setAccessibilityRoles()
        handleWindowInsets()
        subscribeUi()

        if (args.event == null) {
            viewModel.getEventsDetails(args.eventId)
            setFlagIsOpenDeepLink()
        } else {
            event = args.event!!
            setupView(event)
            viewModel.onViewCreated(event)
        }
        adjustManager.trackEvent(R.string.open_event_detail_page)
    }

    override fun handleWindowInsets() {
        ViewCompat.setOnApplyWindowInsetsListener(binding.root) { _, insets ->

            val safeInsetType = WindowInsetsCompat.Type.displayCutout() + WindowInsetsCompat.Type.systemBars()
            val systemInsets = insets.getInsets(safeInsetType)

            binding.eventDetailsABL.updatePadding(
                left = systemInsets.left,
                right = systemInsets.right
            )

            insets
        }
        binding.scrollView.dispatchInsetsToChildViews(
            binding.loading,
            binding.errorLayout,
            binding.categories,
            binding.eventName,
            binding.eventStatusDesc,
            binding.clDateTimeInfo,
            binding.locationContainer,
            binding.descriptionContainer,
            binding.buttonLayout,
            binding.pdfLayout,
        ) { displayCutoutInsets ->
            binding.eventCredits.updatePadding(
                left = displayCutoutInsets.left + 6.dpToPixel(context),
                right = displayCutoutInsets.right
            )
        }
    }

    private fun setupView(event: Event) {
        binding.descriptionContainer.setVisible(event.description.isNullOrBlank().not())
        binding.retryButton.setTextColor(CityInteractor.cityColorInt)
        TextViewCompat.setCompoundDrawableTintList(
            binding.retryButton,
            ColorStateList.valueOf(CityInteractor.cityColorInt)
        )
        if (URLUtil.isValidUrl(event.image)) {
            binding.topLayout.visibility = View.VISIBLE
            binding.image.loadFromURLwithProgress(
                event.image,
                {
                    safeRun {
                        binding.loading.setVisible(false)
                        binding.errorLayout.setVisible(it)
                        binding.image.isClickable = !it
                    }
                }
            )
        }
        binding.eventName.text = event.title
        binding.locationAddress.text = event.locationAddress?.replace("\\n", "\n")?.replace("\\r", "\r")
        event.locationName?.let { binding.locationName.setVisible(it.isNotEmpty()) }
        binding.locationName.text = event.locationName
        binding.categories.text = event.cityEventCategories
            .filter { it.categoryName.isNullOrBlank().not() }
            .sortedBy { it.categoryName }
            .take(CATEGORIES_MAX_NUMBER)
            .joinToString(separator = " | ") { it.categoryName!! }

        binding.image.setOnClickListener {
            event.image?.let { eventImage ->
                event.imageCredit?.let { eventImageCredit ->
                    findNavController().navigate(
                        EventDetailsDirections.actionEventDetailsToDetailedImageDialog(eventImage, eventImageCredit)
                    )
                }
            }
        }

        binding.toolbarEventDetails.title = event.title

        if (event.imageCredit.isNullOrBlank()) binding.eventCredits.visibility = View.GONE
        else binding.eventCredits.text = event.imageCredit

        if (event.locationAddress.isNullOrBlank()) {
            binding.addressContainer.visibility = View.GONE
        }

        binding.retryButton.setOnClickListener {
            loadImage(event)
        }
        binding.locationNavigation.setTextColor(CityInteractor.cityColorInt)

        setupActionButtons()
        setupLinkAndPdf(event)
        setupDates(event)
        setupEventStatus(event)
        setupDescription(event)
    }

    private fun loadImage(event: Event) {
        if (!NetworkConnection.checkInternetConnection(requireContext())) {
            DialogUtil.showRetryDialog(requireContext(), { loadImage(event) })
        } else {
            binding.errorLayout.setVisible(false)
            binding.loading.setVisible(true)
            binding.image.loadFromURLwithProgress(
                event.image,
                {
                    safeRun {
                        binding.loading.setVisible(false)
                        binding.errorLayout.setVisible(it)
                        binding.image.isClickable = !it
                    }
                }
            )
        }
    }

    private fun setupEventStatus(event: Event) {
        binding.run {
            with(event) {
                if (isCancelled) {
                    eventStatusDesc.apply {
                        setVisible(true)
                        setText(R.string.e_007_event_cancelled_desc)
                    }
                    eventStatusSpace.setVisible(true)
                } else if (isSoldOut) {
                    eventStatusDesc.apply {
                        setVisible(true)
                        setText(R.string.e_007_events_sold_out_desc)
                    }
                    eventStatusSpace.setVisible(true)
                } else if (isPostponed) {
                    eventStatusDesc.apply {
                        backgroundTintList =
                            root.resources.getColorStateList(R.color.postponedColor, null)
                        setVisible(true)
                        setText(R.string.e_007_events_new_date_desc)
                    }
                    eventStatusSpace.setVisible(true)
                }
            }
        }
    }

    private val pageLinkHandlerWebViewClient by lazy {
        object : WebViewClient() {
            override fun shouldOverrideUrlLoading(view: WebView?, request: WebResourceRequest?): Boolean {
                attemptOpeningWebViewUri(request?.url)
                return true
            }
        }
    }

    private fun setupDescription(event: Event) {
        binding.description.apply {
            webViewClient = pageLinkHandlerWebViewClient
            event.description?.let { linkifyAndLoadNonHtmlTaggedData(it) }
        }
    }

    private fun setupDates(event: Event) {
        if (event.hasEndTime) {
            binding.endTime.setTextColor(getColor(R.color.onSurface))
            binding.endTime.text = event.endDate?.getHoursAndMins()
            binding.layoutEndTime.contentDescription =
                context?.getString(R.string.e_005_end_time_label) + event.endDate?.getHoursAndMins()
        } else {
            binding.layoutEndTime.contentDescription = context?.getString(R.string.e_005_end_time_label) + "-"
        }

        if (event.hasStartTime) {
            binding.startTime.setTextColor(getColor(R.color.onSurface))
            binding.startTime.text = event.startDate?.getHoursAndMins()
            binding.layoutStartTime.contentDescription =
                context?.getString(R.string.e_005_start_time_label) + event.startDate?.getHoursAndMins()
        } else {
            binding.layoutStartTime.contentDescription = context?.getString(R.string.e_005_start_time_label) + "-"
        }

        if (event.isSingleDay) {
            val calendar = event.startDate.toCalendar()
            val date = calendar.getShortWeekDay() + ", " +
                    calendar.getShortMonthName().replace(".", "")
            val dateDay = calendar.get(Calendar.DAY_OF_MONTH)

            binding.dateStart.dateTextS.text = date
            binding.dateStart.dateNumberS.text = dateDay.toString()
            binding.dateStart.dateCardS.contentDescription = calendar.getLongWeekDay() + ", " +
                    calendar.getShortMonthName().replace(".", "") + dateDay

            binding.dateStart.dateCardS.setCardBackgroundColor(CityInteractor.cityColorInt)

            binding.dateStart.dateCardS.visibility = View.VISIBLE
        } else {
            val startDate = event.startDate.toCalendar()
            val endDate = event.endDate.toCalendar()

            binding.dateSpan.weekdayStart.text = startDate.getLongWeekDay()
            binding.dateSpan.dayNumStart.text = startDate.get(Calendar.DAY_OF_MONTH).toString()
            binding.dateSpan.monthStart.text = startDate.getShortMonthName().replace(".", "")

            binding.dateSpan.weekdayEnd.text = endDate.getLongWeekDay()
            binding.dateSpan.dayNumEnd.text = endDate.get(Calendar.DAY_OF_MONTH).toString()
            binding.dateSpan.monthEnd.text = endDate.getShortMonthName().replace(".", "")

            binding.dateSpan.dateCardM.setCardBackgroundColor(CityInteractor.cityColorInt)
            binding.dateSpan.dateCardM.visibility = View.VISIBLE

            binding.dateSpan.dateCardM.contentDescription =
                startDate.getLongWeekDay() + ", " + startDate.get(Calendar.DAY_OF_MONTH).toString() +
                        startDate.longMonthName.replace(
                            ".",
                            ""
                        ) + "\nto\n" + endDate.getLongWeekDay() + ", " + endDate.get(Calendar.DAY_OF_MONTH).toString() +
                        endDate.longMonthName.replace(".", "")
        }
    }

    private fun setupLinkAndPdf(event: Event) {
        if (!event.link.isNullOrBlank()) {
            binding.buttonLayout.visibility = View.VISIBLE
            binding.btnMoreInformation.setTextColor(CityInteractor.cityColorInt)
            binding.btnMoreInformation.visibility = View.VISIBLE
            binding.btnMoreInformation.setOnClickListener {
                with(viewModel) {
                    trackAdjustEngagement(event.uid.toString(), EventEngagementOption.MORE_INFORMATION)
                    trackAdjustEngagement(event.uid.toString(), EventEngagementOption.WEBSITE)
                }
                openLink(event.link.orEmpty())
            }
        }

        binding.eventPdf.setLinkTextColor(CityInteractor.cityColorInt)
        event.pdf?.forEach {
            binding.eventPdf.append(it + "\n")
            binding.pdfLayout.visibility = View.VISIBLE
        }
    }

    private fun setupActionButtons() {
        binding.addToFavoriteBtn.setCompoundDrawablesWithIntrinsicBounds(
            null,
            createSelectionSelector(
                CityInteractor.cityColorInt,
                R.drawable.ic_icon_favourite_active,
                R.drawable.ic_icon_favourite_available
            ),
            null,
            null
        )

        binding.addToFavoriteBtn.setOnClickListener {
            pendingAction = this::setFavored
            onFavoriteClicked()
        }

        binding.addToCalendarBtn.setOnClickListener {
            viewModel.trackAdjustEngagement(event.uid.toString(), EventEngagementOption.ADD_TO_CALENDAR)
            pendingAction = this::addEventToCalendar
            onAddToCalendarClicked()
        }
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        inflater.inflate(R.menu.share_menu, menu)
        super.onCreateOptionsMenu(menu, inflater)
    }

    private fun showLoginRequired() {
        context?.let { DialogUtil.showLoginRequired(it) }
    }

    private fun setFavored() {
        pendingAction = null
        binding.addToFavoriteBtn.isSelected = !binding.addToFavoriteBtn.isSelected
        viewModel.onFavoriteClicked(binding.addToFavoriteBtn.isSelected, event)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.actionShare -> {
                viewModel.trackAdjustEngagement(event.uid.toString(), EventEngagementOption.SHARE)
                event.link?.let { url ->
                    startActivity(
                        ShareUtils.createShareIntent(
                            event.title ?: "",
                            url,
                            getString(R.string.share_store_header)
                        )
                    )
                }

                return true
            }
        }
        return super.onOptionsItemSelected(item)
    }

    private fun createSelectionSelector(
        @ColorInt cityColor: Int,
        @DrawableRes drawableSelected: Int,
        @DrawableRes drawable: Int
    ): StateListDrawable =
        StateListDrawable().apply {
            addState(
                intArrayOf(android.R.attr.state_selected),
                getDrawable(drawableSelected)?.apply {
                    setTint(cityColor)
                }
            )
            addState(
                intArrayOf(-android.R.attr.state_selected),
                getDrawable(drawable)?.apply {
                    setTint(getColor(R.color.onSurfaceSecondary))
                }
            )
        }

    private fun createCalendarIntent(event: Event) = Intent(Intent.ACTION_INSERT)
        .setData(CalendarContract.Events.CONTENT_URI)
        .putExtra(CalendarContract.Events.TITLE, event.title)
        .putExtra(CalendarContract.Events.DESCRIPTION, event.description)
        .putExtra(CalendarContract.Events.EVENT_LOCATION, event.locationAddress)
        .putExtra(CalendarContract.Events.AVAILABILITY, CalendarContract.Events.AVAILABILITY_BUSY)
        .putExtra(CalendarContract.EXTRA_EVENT_BEGIN_TIME, event.startDate?.time)
        .putExtra(
            CalendarContract.EXTRA_EVENT_END_TIME,
            if (event.hasEndTime) {
                event.endDate?.time
            } else {
                event.startDate?.time?.let { startTime ->
                    startTime - startTime % MILLIS_IN_ONE_DAY + MILLIS_IN_ONE_DAY - MILLIS_IN_ONE_HOUR
                }
            }
        )

    private fun initMap() {
        binding.mapView.attachLifecycleToFragment(this@EventDetails)
        binding.mapView.getMapAsync(this)
    }

    override fun onMapReady(map: GoogleMap) {
        if (resources.isDarkMode) {
            map.tryLoadingNightStyle(requireContext())
        }
        googleMap = map
        viewModel.latLng.observe(viewLifecycleOwner) {
            if (it != null) setupEventLocation(it) else binding.locationContainer.setVisible(false)
        }
    }

    private fun setupEventLocation(location: LatLng) {
        googleMap?.addMarker(
            MarkerOptions().position(location)
                .icon(BitmapDescriptorFactory.fromBitmap(BitmapUtil.createMarker(requireContext())))
        )
        googleMap?.moveCamera(CameraUpdateFactory.newLatLngZoom(location, 17f))
        googleMap?.uiSettings?.isZoomGesturesEnabled = false
        googleMap?.uiSettings?.isScrollGesturesEnabled = false
        googleMap?.uiSettings?.isRotateGesturesEnabled = false

        googleMap?.setOnMapClickListener {
            findNavController().navigate(
                R.id.action_eventDetails_to_detailedMapGraph,
                DetailedMapGraphArgs.Builder(
                    event.locationName,
                    event.title ?: "",
                    event.locationAddress,
                    location
                ).build().toBundle()
            )
        }

        binding.mapView.setMapsAccessibility {
            findNavController().navigate(
                R.id.action_eventDetails_to_detailedMapGraph,
                DetailedMapGraphArgs.Builder(
                    event.locationName,
                    event.title ?: "",
                    event.locationAddress,
                    location
                ).build().toBundle()
            )
        }
        binding.locationNavigation.setOnClickListener {
            viewModel.trackAdjustEngagement(event.uid.toString(), EventEngagementOption.DIRECTIONS)
            openMapApp(location.latitude, location.longitude)
        }
    }

    private fun subscribeUi() {
        viewModel.userLoggedIn.observe(viewLifecycleOwner) {
            if (it) {
                onFavoriteClicked = this::setFavored
                onAddToCalendarClicked = this::addEventToCalendar
                pendingAction?.invoke()
            } else {
                onFavoriteClicked = this::showLoginRequired
                onAddToCalendarClicked = this::showLoginRequired
                pendingAction = null
            }
        }

        viewModel.favored.observe(viewLifecycleOwner) {
            if (it) {
                viewModel.trackAdjustEngagement(event.uid.toString(), EventEngagementOption.FAVORITE)
                adjustManager.trackEvent(R.string.event_favorite_marked)
            }
            binding.addToFavoriteBtn.isSelected = it
        }

        viewModel.showFavoritesLoadError.observe(viewLifecycleOwner) {
            DialogUtil.showInfoDialog(requireContext(), R.string.a_message_favorites_load_error)
        }

        viewModel.promptLoginRequired.observe(viewLifecycleOwner) { showLoginRequired() }
        viewModel.events.observe(viewLifecycleOwner) {
            if (it.isNotEmpty()) {
                event = it[0]
                setupView(event)
                viewModel.onViewCreated(event)
            }

            if (args.cityId.isNotEmpty() || args.cityId == "0" || viewModel.getDeepLinkCity() == 0) {
                if (activity?.intent?.data != null) {
                    (activity as? MainActivity)?.hideSplashScreen()
                }
            }
        }

        viewModel.newCity.observe(viewLifecycleOwner) {
            if (it.cityName.isNullOrBlank().not()) showCityInfoSnack(cityName = it.cityName!!)
        }
    }

    private fun addEventToCalendar() {
        pendingAction = null
        try {
            startActivity(createCalendarIntent(event))
        } catch (ex: ActivityNotFoundException) {
            DialogUtil.showInfoDialog(
                requireContext(),
                R.string.e_005_dialog_no_calendar_title,
                R.string.e_005_dialog_no_calendar_message
            )
        }
    }

    override fun onDestroyView() {
        googleMap?.clear()
        googleMap = null
        super.onDestroyView()
    }

    private fun showCityInfoSnack(cityName: String) {
        if (args.cityId.isNotEmpty() && args.cityId != "0") {
            if (requireActivity().intent.data != null) {
                (requireActivity() as MainActivity).hideSplashScreen()
            }
            showInfoSnackBar(getString(R.string.e_006_event_switch_city_info, cityName))

            if (this::event.isInitialized) {
                setupView(event)
            } else {
                viewModel.getEventsDetails(args.eventId)
            }

            (requireActivity() as MainActivity).setIsOpenDeeplink(true)
        }
    }

    private fun setFlagIsOpenDeepLink() {
        if (args.cityId.isNotEmpty() && args.cityId == "0") {
            (requireActivity() as MainActivity).setIsOpenDeeplink(true)
        }
    }

    private fun setAccessibilityRoles() {
        binding.eventName.setAccessibilityRole(
            AccessibilityRole.Heading,
            (getString(R.string.accessibility_heading_level_2))
        )
        binding.retryButton.setAccessibilityRole(AccessibilityRole.Button)
        binding.addToCalendarBtn.setAccessibilityRole(AccessibilityRole.Button)
        binding.addToFavoriteBtn.setAccessibilityRole(AccessibilityRole.Button)
        binding.btnMoreInformation.setAccessibilityRole(AccessibilityRole.Button)
        binding.locationNavigation.setAccessibilityRole(AccessibilityRole.Button)
        binding.eventPdf.setAccessibilityRole(AccessibilityRole.Link)
    }
}
