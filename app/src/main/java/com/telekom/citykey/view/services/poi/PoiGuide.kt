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

package com.telekom.citykey.view.services.poi

import android.Manifest
import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.content.res.ColorStateList
import android.graphics.Typeface
import android.location.LocationManager
import android.os.Bundle
import android.provider.Settings
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.TextView
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.location.LocationManagerCompat
import androidx.core.widget.TextViewCompat
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import com.google.android.material.tabs.TabLayout
import com.google.android.material.tabs.TabLayout.OnTabSelectedListener
import com.google.android.material.tabs.TabLayoutMediator
import com.telekom.citykey.R
import com.telekom.citykey.databinding.PoiFragmentBinding
import com.telekom.citykey.domain.city.CityInteractor
import com.telekom.citykey.utils.DialogUtil
import com.telekom.citykey.utils.extensions.AccessibilityRole
import com.telekom.citykey.utils.extensions.setAccessibilityRole
import com.telekom.citykey.utils.extensions.setVisible
import com.telekom.citykey.utils.extensions.showDialog
import com.telekom.citykey.utils.extensions.viewBinding
import com.telekom.citykey.view.MainFragment
import com.telekom.citykey.view.services.poi.categories.PoiCategorySelection
import org.koin.androidx.viewmodel.ext.android.sharedViewModel

class PoiGuide : MainFragment(R.layout.poi_fragment) {
    private val viewModel: PoiGuideViewModel by sharedViewModel()
    private val binding by viewBinding(PoiFragmentBinding::bind)
    private val args: PoiGuideArgs by navArgs()

    private var pagerAdapter: PoiPagerAdapter? = null
    private val locationManager: LocationManager by lazy { requireActivity().getSystemService(Context.LOCATION_SERVICE) as LocationManager }
    private val requestPermissionLauncher =
        registerForActivityResult(ActivityResultContracts.RequestMultiplePermissions()) {
            if (it[Manifest.permission.ACCESS_FINE_LOCATION] == true && it[Manifest.permission.ACCESS_COARSE_LOCATION] == true &&
                !LocationManagerCompat.isLocationEnabled(locationManager)
            ) {
                showLocationDialog()
            } else {
                viewModel.onServiceReady()
            }
        }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupToolbar(binding.toolbarPoiGuide)
        binding.toolbarPoiGuide.title = args.title
        binding.retryButton.setTextColor(CityInteractor.cityColorInt)
        TextViewCompat.setCompoundDrawableTintList(
            binding.retryButton,
            ColorStateList.valueOf(CityInteractor.cityColorInt)
        )

        pagerAdapter = PoiPagerAdapter(this)
        binding.pager.adapter = pagerAdapter

        initViews()
        subscribeUi()
        viewModel.onRequestPermission()
    }

    private fun initViews() {
        binding.poiCategoriesBtn.setAccessibilityRole(AccessibilityRole.Button)
        binding.poiCategoriesBtn.setOnClickListener {
            viewModel.onCategorySelectionRequested()
        }
        binding.tabLayout.setSelectedTabIndicatorColor(CityInteractor.cityColorInt)
        setTabListener()
        TabLayoutMediator(binding.tabLayout, binding.pager) { tab, pos ->
            tab.setText(
                if (pos == 0) {
                    R.string.poi_001_tab_map_label
                } else {
                    R.string.poi_001_tab_list_label
                }
            )
        }.attach()

        binding.poiCategoriesBtn.setOnClickListener {
            viewModel.onCategorySelectionRequested()
        }

        binding.retryButton.setOnClickListener {
            viewModel.onServiceReady()
        }

        binding.tabLayout.setSelectedTabIndicatorColor(CityInteractor.cityColorInt)
    }

    private fun showLocationDialog() {
        DialogUtil.showDialogPositiveNegative(
            context = requireContext(),
            title = R.string.c_001_cities_cannot_access_location_dialog_title,
            message = R.string.c_001_cities_gps_turned_off,
            positiveBtnLabel = R.string.c_001_cities_cannot_access_location_btn_poitive,
            negativeBtnLabel = android.R.string.cancel,
            negativeClickListener = {
                viewModel.onServiceReady()
            },
            positiveClickListener = {
                startActivity(
                    Intent().apply {
                        flags = Intent.FLAG_ACTIVITY_NEW_TASK
                        action = Settings.ACTION_LOCATION_SOURCE_SETTINGS
                    }
                )
                viewModel.onServiceReady()
            }
        )
    }

    private fun subscribeUi() {
        viewModel.launchCategorySelection.observe(viewLifecycleOwner) { category ->
            PoiCategorySelection(category) { success ->
                if (category == null && !success) {
                    findNavController().popBackStack(R.id.services, false)
                }
            }.showDialog(childFragmentManager)
        }

        viewModel.activeCategory.observe(viewLifecycleOwner) {
            binding.activeCategory.text = it.categoryName
            binding.poiCategoriesBtn.contentDescription = it.categoryName
        }

        viewModel.showRetryDialog.observe(viewLifecycleOwner) {
            DialogUtil.showRetryDialog(requireContext(), viewModel::onRetryRequired, viewModel::onRetryCanceled)
        }

        viewModel.isFirstTime.observe(viewLifecycleOwner) { isFirstTime ->
            if (isFirstTime) requestPermission()
            else viewModel.onServiceReady()
        }

        viewModel.poiState.observe(viewLifecycleOwner) {
            binding.loading.setVisible(false)
            binding.errorLayout.setVisible(false)
            binding.pager.setVisible(false)

            when (it) {
                PoiState.LOADING -> binding.loading.setVisible(true)
                PoiState.ERROR, PoiState.EMPTY -> binding.errorLayout.setVisible(true)
                PoiState.SUCCESS -> binding.pager.setVisible(true)
            }
        }
    }

    private fun setTabListener() {
        binding.tabLayout.addOnTabSelectedListener(object : OnTabSelectedListener {

            override fun onTabSelected(tab: TabLayout.Tab) {
                val tabLayout = (binding.tabLayout.getChildAt(0) as ViewGroup).getChildAt(tab.position) as LinearLayout
                val tabTextView = tabLayout.getChildAt(1) as TextView
                tabTextView.setTypeface(null, Typeface.BOLD)
            }

            override fun onTabUnselected(tab: TabLayout.Tab) {
                val tabLayout = (binding.tabLayout.getChildAt(0) as ViewGroup).getChildAt(tab.position) as LinearLayout
                val tabTextView = tabLayout.getChildAt(1) as TextView
                tabTextView.typeface = Typeface.create("sans-serif-medium", Typeface.NORMAL)
            }

            override fun onTabReselected(tab: TabLayout.Tab?) {
            }
        })
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

    override fun onDestroyView() {
        super.onDestroyView()
        pagerAdapter = null
    }
}
