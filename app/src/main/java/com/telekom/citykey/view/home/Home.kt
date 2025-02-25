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

package com.telekom.citykey.view.home

import android.annotation.SuppressLint
import android.appwidget.AppWidgetManager
import android.content.Intent
import android.os.Bundle
import android.util.TypedValue
import android.view.Gravity
import android.view.View
import androidx.activity.OnBackPressedCallback
import androidx.core.view.forEach
import androidx.core.widget.TextViewCompat
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.whenResumed
import com.google.android.material.appbar.AppBarLayout
import com.telekom.citykey.R
import com.telekom.citykey.custom.ItemDecorationBetweenOnly
import com.telekom.citykey.custom.views.OscaAppBarLayout
import com.telekom.citykey.databinding.HomeFragmentBinding
import com.telekom.citykey.domain.city.weather.WeatherState
import com.telekom.citykey.domain.track.AdjustManager
import com.telekom.citykey.utils.extensions.*
import com.telekom.citykey.view.city_selection.CitySelectionFragment
import com.telekom.citykey.view.main.MainActivity
import com.telekom.citykey.view.user.login.LoginActivity
import com.telekom.citykey.view.user.profile.ProfileActivity
import io.github.douglasjunior.androidSimpleTooltip.SimpleTooltip
import io.github.douglasjunior.androidSimpleTooltip.SimpleTooltipUtils
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import org.koin.android.ext.android.inject
import org.koin.androidx.viewmodel.ext.android.viewModel
import kotlin.math.roundToInt

class Home : Fragment(R.layout.home_fragment) {
    private val viewModel: HomeViewModel by viewModel()
    private val adjustManager: AdjustManager by inject()
    private val binding by viewBinding(HomeFragmentBinding::bind)

    private var feedAdapter: FeedAdapter? = null
    private var isUserLoggedIn = false
    private var citySelectionTooltip: SimpleTooltip? = null
    private var onBackPressedCallback: OnBackPressedCallback? = null

    override fun onResume() {
        super.onResume()
        trackOpenHomeEvent()
    }

    private fun trackOpenHomeEvent() {
        if (viewModel.isFtuInteractionCompleted()) adjustManager.trackOneTimeEvent(R.string.open_home)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setHasOptionsMenu(true)
        binding.homeToolbar.setAndPerformAccessibilityFocusAction()
        feedAdapter = FeedAdapter()
        binding.mainFeed.apply {
            addItemDecoration(ItemDecorationBetweenOnly(getDrawable(R.drawable.home_item_decoration)!!))
            adapter = feedAdapter
        }
        binding.homeToolbar.inflateMenu(R.menu.home_menu)
        binding.swipeRefreshLayout.setOnRefreshListener(viewModel::onRefresh)
        setupToolbar(binding.appBarLayout)
        subscribeUi()
        handleBackAction()
    }

    @SuppressLint("SetTextI18n")
    private fun subscribeUi() {
        viewModel.homeData.observe(viewLifecycleOwner) {
            binding.toolbarCoat.loadFromOSCA(it.municipalCoat)

            feedAdapter?.init(it.viewTypes, it.cityColor)

            binding.headerCityName.apply {
                TextViewCompat.setAutoSizeTextTypeUniformWithConfiguration(this, 20, 45, 1, TypedValue.COMPLEX_UNIT_SP)
                text = it.city
            }
            binding.cityHeaderLayout.setAccessibilityRole(AccessibilityRole.Heading)
            binding.cityHeaderLayout.contentDescription = getString(R.string.h_001_home_title_part) + it.city
            binding.toolbarTitle.text = it.city
            with(requireActivity() as MainActivity) {
                markLoadCompleteIfFromDeeplink(getString(R.string.deeplink_home))
            }
            trackOpenHomeEvent()
        }
        viewModel.cityNews.observe(viewLifecycleOwner) {
            feedAdapter?.updateNews(it)
            if (viewModel.shouldUpdateWidget()) {
                AppWidgetManager.getInstance(requireContext()).updateNewsWidget(requireContext())
                viewModel.widgetUpdateDone()
            }
        }

        viewModel.cityWeather.observe(viewLifecycleOwner) {
            when (it) {
                is WeatherState.Success -> {
                    binding.zoomableImage.loadFromOSCA(it.cityPicture)
                    binding.headerCityInfo.apply {
                        setVisible(true)
                        text = "${it.content.description}, ${it.content.temperature.roundToInt()}${0x00B0.toChar()}"
                    }
                }

                is WeatherState.Error -> {
                    binding.headerCityInfo.apply {
                        text = ""
                        setVisible(false)
                    }
                    binding.zoomableImage.loadFromOSCA(it.cityPicture)
                }

                is WeatherState.Loading -> {
                    binding.headerCityInfo.apply {
                        text = ""
                        setVisible(false)
                    }
                }
            }
        }

        viewModel.userState.observe(viewLifecycleOwner) {
            isUserLoggedIn = it
        }

        viewModel.eventsHomeData.observe(viewLifecycleOwner) { data ->
            data.events?.let { feedAdapter?.updateEvents(data) }
        }

        viewModel.eventsState.observe(viewLifecycleOwner) {
            feedAdapter?.updateEventsState(it)
        }

        viewModel.refreshFinished.observe(viewLifecycleOwner) {
            binding.swipeRefreshLayout.isRefreshing = false
        }

        viewModel.citySelectionTooltipState.observe(viewLifecycleOwner) { shouldShowTooltip ->
            if (!shouldShowTooltip) return@observe
            citySelectionTooltip = SimpleTooltip.Builder(requireContext())
                .anchorView(binding.appBarLayout.findViewById(R.id.actionSelectCity))
                .text(getString(R.string.h_001_home_tooltip_switch_location))
                .gravity(Gravity.BOTTOM)
                .animated(false)
                .backgroundColor(getColor(R.color.white))
                .arrowColor(getColor(R.color.background80a))
                .transparentOverlay(true)
                .contentView(R.layout.tooltip_custom, R.id.tooltipText)
                .margin(SimpleTooltipUtils.pxFromDp(0F))
                .arrowWidth(SimpleTooltipUtils.pxFromDp(20F))
                .dismissOnInsideTouch(false)
                .dismissOnOutsideTouch(false)
                .onDismissListener { viewModel.onTooltipDismissed(); citySelectionTooltip = null }
                .build()
            showCitySelectionToolTip()
        }
    }

    private fun setupToolbar(appBarLayout: OscaAppBarLayout) {

        appBarLayout.onCollapse { collapsed ->
            val menuItemColor = getColor(if (collapsed) R.color.onSurface else R.color.white)

            binding.toolbarCityBox.setVisible(collapsed)

            binding.homeToolbar.menu.forEach {
                it.icon?.setTint(menuItemColor)
            }
        }

        appBarLayout.findViewById<View>(R.id.actionSelectCity).setOnClickListener {
            CitySelectionFragment()
                .showDialog(requireActivity().supportFragmentManager)
            citySelectionTooltip?.dismiss()
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

        appBarLayout.addOnOffsetChangedListener(
            AppBarLayout.OnOffsetChangedListener { _, i ->
                if (!binding.swipeRefreshLayout.isRefreshing) {
                    binding.swipeRefreshLayout.isEnabled = i == 0
                }
            }
        )
    }

    private fun showCitySelectionToolTip() {
        citySelectionTooltip?.run {
            viewLifecycleOwner.lifecycleScope.launch {
                whenResumed {
                    delay(500L)
                    show()
                    delay(6000L)
                    dismiss()
                }
            }
        }
    }

    private fun handleBackAction() {
        onBackPressedCallback = object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                adjustManager.resetOneTimeEventsTracker()
                requireActivity().finishAffinity()
            }
        }
        requireActivity().onBackPressedDispatcher.addCallback(viewLifecycleOwner, onBackPressedCallback!!)
    }

    override fun onDestroyView() {
        citySelectionTooltip?.dismiss()
        super.onDestroyView()
        feedAdapter = null
    }

}
