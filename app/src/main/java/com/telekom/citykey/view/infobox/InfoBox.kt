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

package com.telekom.citykey.view.infobox

import android.content.Intent
import android.content.res.ColorStateList
import android.os.Bundle
import android.view.View
import androidx.core.view.forEach
import androidx.core.widget.TextViewCompat
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import com.google.android.material.tabs.TabLayoutMediator
import com.telekom.citykey.R
import com.telekom.citykey.databinding.InfoboxFragmentBinding
import com.telekom.citykey.domain.city.CityInteractor
import com.telekom.citykey.domain.track.AdjustManager
import com.telekom.citykey.utils.DialogUtil
import com.telekom.citykey.utils.NetworkConnection
import com.telekom.citykey.utils.extensions.AccessibilityRole
import com.telekom.citykey.utils.extensions.getColor
import com.telekom.citykey.utils.extensions.setAccessibilityRole
import com.telekom.citykey.utils.extensions.setAndPerformAccessibilityFocusAction
import com.telekom.citykey.utils.extensions.setVisible
import com.telekom.citykey.utils.extensions.showActionSnackbar
import com.telekom.citykey.utils.extensions.showDialog
import com.telekom.citykey.utils.extensions.startActivity
import com.telekom.citykey.utils.extensions.viewBinding
import com.telekom.citykey.view.city_selection.CitySelectionFragment
import com.telekom.citykey.view.main.MainActivity
import com.telekom.citykey.view.user.login.LoginActivity
import com.telekom.citykey.view.user.profile.ProfileActivity
import com.telekom.citykey.view.user.registration.RegistrationActivity
import org.koin.android.ext.android.inject
import org.koin.androidx.viewmodel.ext.android.sharedViewModel

class InfoBox : Fragment(R.layout.infobox_fragment) {

    private val viewModel: InfoBoxViewModel by sharedViewModel()
    private val binding by viewBinding(InfoboxFragmentBinding::bind)
    private val adjustManager: AdjustManager by inject()
    private val args: InfoBoxArgs by navArgs()

    private var unreadMailsAdapter: InfoBoxAdapter? = null
    private var allMailsAdapter: InfoBoxAdapter? = null
    private var loggedInState: Boolean = false
    private var infoboxMailList: List<InfoboxItem>? = null
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        unreadMailsAdapter =
            InfoBoxAdapter(viewModel::onToggleRead, viewModel::onDelete, R.string.b_001_infobox_no_unread_messages)
        allMailsAdapter =
            InfoBoxAdapter(viewModel::onToggleRead, viewModel::onDelete, R.string.b_001_infobox_no_messages)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupToolbar()
        setupInfoBox()
        setupWelcomeScreen()
        if (viewModel.loggedInState.value == true) {
            binding.toolbarInfoBox.setAndPerformAccessibilityFocusAction()
        } else {
            binding.welcomeView.descHeading.setAndPerformAccessibilityFocusAction()
        }
        binding.tabLayoutInfoBox.setSelectedTabIndicatorColor(CityInteractor.cityColorInt)

        binding.welcomeView.welcomeBox.visibility = View.GONE
        binding.infoBox.visibility = View.GONE

        subscribeUi()
        if (args.messageId == 0) {
            with(requireActivity() as MainActivity) {
                markLoadCompleteIfFromDeeplink(getString(R.string.deeplink_infobox))
            }
        }
        adjustManager.trackOneTimeEvent(R.string.open_infobox)
    }

    private fun setupToolbar() {
        setHasOptionsMenu(true)
        binding.toolbarInfoBox.inflateMenu(R.menu.home_menu)
        binding.toolbarInfoBox.setOnMenuItemClickListener {
            when (it.itemId) {
                R.id.actionSelectCity -> {
                    CitySelectionFragment().showDialog(requireActivity().supportFragmentManager)
                    true
                }

                R.id.actionProfile -> {
                    adjustManager.trackEvent(R.string.open_profile)
                    startActivity<ProfileActivity>()
                    true
                }

                else -> false
            }
        }
        binding.toolbarInfoBox.menu.forEach {
            it.icon?.setTint(getColor(R.color.onSurface))
        }
        binding.toolbarInfoBox.setAccessibilityRole(AccessibilityRole.Heading)
    }

    private fun setupInfoBox() {
        binding.pager.isUserInputEnabled = false
        binding.pager.adapter = InfoBoxPagerAdapter(allMailsAdapter!!, unreadMailsAdapter!!)
        binding.swipeRefreshLayout.setOnRefreshListener(viewModel::onRefresh)
        TabLayoutMediator(binding.tabLayoutInfoBox, binding.pager) { tab, index ->
            tab.text =
                getString(if (index == 0) R.string.b_001_infobox_btn_filter_unread else R.string.b_001_infobox_btn_filter_all)
        }.attach()

        binding.retryButton.setTextColor(CityInteractor.cityColorInt)
        TextViewCompat.setCompoundDrawableTintList(
            binding.retryButton,
            ColorStateList.valueOf(CityInteractor.cityColorInt)
        )

        binding.retryButton.setOnClickListener { viewModel.onRefresh() }
    }

    private fun setupWelcomeScreen() {
        binding.welcomeView.descHeading.setAccessibilityRole(AccessibilityRole.Heading)
        binding.welcomeView.loginLink.setAccessibilityRole(AccessibilityRole.Button)
        binding.welcomeView.loginLink.setOnClickListener {
            startActivity<LoginActivity>()
        }

        binding.welcomeView.registerBtn.setOnClickListener {
            startActivity<RegistrationActivity>()
        }
    }

    private fun subscribeUi() {
        findNavController().currentBackStackEntry
            ?.savedStateHandle
            ?.getLiveData<Boolean>("MailDeletionSuccessful")
            ?.observe(viewLifecycleOwner) { result ->
                if (result) {
                    showUndoDeletionSnackBar()
                    findNavController().currentBackStackEntry?.savedStateHandle?.set(
                        "MailDeletionSuccessful",
                        false
                    )
                }
            }
        viewModel.loggedInState.observe(viewLifecycleOwner) {
            view?.run {
                loggedInState = it
                binding.infoBox.setVisible(it)
                binding.welcomeView.welcomeBox.setVisible(!it)
                if (it) {
                    binding.toolbarInfoBox.setAndPerformAccessibilityFocusAction()
                } else {
                    binding.welcomeView.descHeading.setAndPerformAccessibilityFocusAction()
                }
            }
        }

        viewModel.shouldPromptLogin.observe(viewLifecycleOwner) {
            DialogUtil.showLoginRequired(requireContext())
        }

        viewModel.content.observe(viewLifecycleOwner) {
            infoboxMailList = it.first
            allMailsAdapter?.updateData(it.first)
            unreadMailsAdapter?.updateData(it.second)
            handelNoIntenetConnection()
        }

        viewModel.deletionSuccessful.observe(viewLifecycleOwner) {
            showUndoDeletionSnackBar()
        }

        viewModel.state.observe(viewLifecycleOwner) {
            binding.swipeRefreshLayout.isRefreshing = false
            if (args.messageId == 0 || viewModel.messageId == 0)
                binding.loading.visibility = View.GONE
            binding.errorLayout.setVisible(it == InfoBoxStates.ANYTHING_ELSE || it == InfoBoxStates.NO_INTERNET)
            binding.pager.setVisible(it == InfoBoxStates.OK || it == InfoBoxStates.REFRESH_ERROR)
            if (it == InfoBoxStates.NO_INTERNET || it == InfoBoxStates.REFRESH_ERROR) {
                DialogUtil.showNoInternetDialog(requireContext())
            }
        }

        viewModel.showNoInternetDialog.observe(viewLifecycleOwner) {
            DialogUtil.showNoInternetDialog(requireContext())
        }

        viewModel.infoboxCount.observe(viewLifecycleOwner) { infoboxItemCount ->
            infoboxItemCount?.let { itemCount ->
                infoboxMailList?.let { mailList ->
                    if (args.messageId != 0) {
                        // If cityData is initialized or has a value, handle the deep link directly.
                        if (viewModel.cityData.isInitialized && viewModel.cityData.value != null) {
                            handelInfoboxNotificationDeepLink(mailList)
                            binding.loading.visibility = View.GONE
                        } else {
                            // If cityData is not initialized, observe it once.
                            viewModel.cityData.observe(viewLifecycleOwner) { cityData ->
                                cityData?.let {
                                    handelInfoboxNotificationDeepLink(mailList)
                                    binding.loading.visibility = View.GONE
                                    viewModel.cityData.removeObservers(viewLifecycleOwner) // Remove observer after first update
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    private fun showUndoDeletionSnackBar() {
        showActionSnackbar(
            R.string.b_002_infobox_snackbar_content,
            R.string.b_002_infobox_snackbar_action_undo,
            viewModel::onUndoDeletion
        )
    }

    override fun onDestroy() {
        super.onDestroy()
        allMailsAdapter = null
        unreadMailsAdapter = null
    }

    private fun handelInfoboxNotificationDeepLink(infoboxMailList: List<InfoboxItem>) {
        if (args.messageId != 0 && viewModel.messageId == null) {
            viewModel.messageId = args.messageId
            if (!loggedInState) {
                (activity as? MainActivity)?.hideSplashScreen()
                startActivity(
                    Intent(activity, LoginActivity::class.java).apply {
                        putExtra(LoginActivity.LAUNCH_INFOBOX, true)
                    }
                )
            } else {
                navigateDeeplink(infoboxMailList)
            }
        } else if (viewModel.messageId != 0 && viewModel.messageId != null && loggedInState) {
            viewModel.messageId = args.messageId
            navigateDeeplink(infoboxMailList)
        }
    }

    private fun navigateDeeplink(infoboxMailList: List<InfoboxItem>) {
        if (infoboxMailList.isNotEmpty()) {
            infoboxMailList.find { infoboxItem -> (infoboxItem as InfoboxItem.Mail).item.messageId == viewModel.messageId }
                ?.let { infoboxItem ->
                    viewModel.messageId = 0
                    val item = (infoboxItem as InfoboxItem.Mail).item
                    if (!item.isRead) {
                        item.isRead = true
                        viewModel.onToggleRead(false, item.userInfoId)
                    }
                    findNavController().navigate(InfoBoxDirections.actionInfoBoxToDetailedInfoBox(item))
                }
                .also {
                    if (viewModel.messageId != 0) {
                        viewModel.messageId = 0
                        findNavController().popBackStack(R.id.home, false)
                        with(requireActivity() as MainActivity) {
                            markLoadCompleteIfFromDeeplink(getString(R.string.deeplink_infobox_message), true)
                        }
                    }
                }
        }
    }

    private fun handelNoIntenetConnection() {
        if (viewModel.messageId != 0 && viewModel.messageId != null && loggedInState && !NetworkConnection.checkInternetConnection(
                requireContext()
            )
        ) {
            DialogUtil.showNoInternetDialog(requireContext())
            viewModel.messageId = 0
            binding.loading.visibility = View.GONE
        }
    }
}
