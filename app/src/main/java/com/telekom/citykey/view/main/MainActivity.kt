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

package com.telekom.citykey.view.main

import android.animation.Animator
import android.animation.ValueAnimator
import android.app.ActivityManager
import android.content.DialogInterface
import android.content.Intent
import android.graphics.Color
import android.net.Uri
import android.nfc.NfcAdapter
import android.nfc.Tag
import android.os.Build
import android.os.Bundle
import android.view.View
import android.view.animation.AnimationUtils
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.core.content.ContextCompat
import androidx.core.os.bundleOf
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.interpolator.view.animation.FastOutLinearInInterpolator
import androidx.navigation.findNavController
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.ui.NavigationUI
import androidx.navigation.ui.setupWithNavController
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.telekom.citykey.R
import com.telekom.citykey.databinding.MainActivityBinding
import com.telekom.citykey.domain.city.CityInteractor
import com.telekom.citykey.domain.notifications.OscaPushService
import com.telekom.citykey.domain.track.AdjustManager
import com.telekom.citykey.models.content.City
import com.telekom.citykey.models.content.CityContent
import com.telekom.citykey.utils.DialogUtil
import com.telekom.citykey.utils.extensions.listen
import com.telekom.citykey.utils.extensions.setItemsColor
import com.telekom.citykey.utils.extensions.setVisible
import com.telekom.citykey.utils.extensions.showDialog
import com.telekom.citykey.utils.extensions.startActivity
import com.telekom.citykey.utils.extensions.viewBinding
import com.telekom.citykey.utils.isDarkMode
import com.telekom.citykey.view.city_selection.CitySelectionFragment
import com.telekom.citykey.view.dialogs.ForceUpdateDialog
import com.telekom.citykey.view.dialogs.WhatsNewInAppDialog
import com.telekom.citykey.view.dialogs.dpn_updates.DpnUpdatesDialog
import com.telekom.citykey.view.user.login.LoginActivity
import com.telekom.citykey.view.user.login.LogoutReason
import com.telekom.citykey.view.welcome.WelcomeActivity
import com.telekom.citykey.view.widget.waste_calendar.WasteCalendarWidgetConstants
import org.koin.android.ext.android.inject
import org.koin.androidx.viewmodel.ext.android.viewModel
import timber.log.Timber
import kotlin.system.exitProcess

class MainActivity : AppCompatActivity() {

    private val viewModel: MainViewModel by viewModel()
    private val binding by viewBinding(MainActivityBinding::inflate)

    private val adjustManager: AdjustManager by inject()

    private var infoBoxUpdates = 0
    private var servicesUpdates = 0
    private var nfcIntentDispatcher: NfcIntentDispatcher? = null
    private var isCityActive = true
    private val clearedDestinations = mutableListOf<Int>()

    private var cityid: Int? = 0
    private var selectedCityId = 0
    private var isCitySwitch: Boolean = false
    var tabWasSelected = false
    private var isOpenDeeplink = false

    private var loginDialogInterface: DialogInterface? = null

    private var isSplashAnimationFinalFramesPlayed = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            setRecentsScreenshotEnabled(false)
        }
        setContentView(binding.root)
        nfcIntentDispatcher = NfcIntentDispatcher(this)

        initStatusBar()
        binding.btnRetry.setOnClickListener {
            it.setVisible(false)
            binding.splashLAV.setVisible(true)
            viewModel.onRetryClicked()
        }

        ViewCompat.setOnApplyWindowInsetsListener(binding.root) { _, insets ->
            binding.navigationBottom.setVisible(!insets.isVisible(WindowInsetsCompat.Type.ime()))
            insets
        }

        subscribeUi()
        showSplashViewIfRequired()
    }

    override fun onStart() {
        if (viewModel.needsResettingEventTracking()) {
            adjustManager.resetOneTimeEventsTracker()
        }
        super.onStart()
    }

    override fun onStop() {
        super.onStop()
        val tasks = (getSystemService(ACTIVITY_SERVICE) as ActivityManager).appTasks
        if (tasks.isNotEmpty()) {
            viewModel.onActivityStopped(tasks[0], MainActivity::class.java.name)
        }
    }

    override fun onResume() {
        checkForAppLaunchFromHistory()
        super.onResume()
        viewModel.onActivityResumed()
        binding.previewModeBannerTextView.setVisible(viewModel.isPreviewMode())
    }

    private fun checkForAppLaunchFromHistory() {
        if ((intent.flags and Intent.FLAG_ACTIVITY_LAUNCHED_FROM_HISTORY) != 0) {
            adjustManager.resetOneTimeEventsTracker()
        }
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        val nfcTag = with(intent) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                getParcelableExtra(NfcAdapter.EXTRA_TAG, Tag::class.java)
            } else {
                @Suppress("DEPRECATION")
                getParcelableExtra(NfcAdapter.EXTRA_TAG)
            }
        }
        nfcTag?.let {
            viewModel.onNfcTagReceived(it)
            return
        }

        if ((intent.data == null) && intent.extras != null && intent.getStringExtra(OscaPushService.DEEPLINK_PARAM)
                ?.contains(OscaPushService.EVENT_DEEP_LINK_URI_IDENTIFIER) == true
        ) {
            val extraDeepLinkString = intent.getStringExtra(OscaPushService.DEEPLINK_PARAM)
            this.intent.data = Uri.parse(extraDeepLinkString)
            isOpenDeeplink = false
            setupBottomNavigation()
        } else {
            setupBottomNavigation(intent)
        }
    }

    private fun subscribeUi() {
        viewModel.promptLogin.observe(this) {
            startActivity<LoginActivity>()
        }
        viewModel.isFirstLaunch.observe(this) {
            startActivity<WelcomeActivity>()
            viewModel.whatsNewShown()
        }

        viewModel.error.observe(this) {
            if (it) {
                binding.splashLAV.setVisible(false)
                binding.btnRetry.setVisible(true)
            }
        }

        viewModel.unexpectedLogout.observe(this) {
            startActivity<LoginActivity>()
            if (it != LogoutReason.TECHNICAL_LOGOUT) return@observe
            adjustManager.trackEvent(R.string.UnexpectedLogout)
        }

        viewModel.isCityActive.observe(this) {
            isCityActive = it
        }
        viewModel.newCity.observe(this, this::onCityUpdated)

        viewModel.notifications.observe(this) { map ->
            map.forEach { pair ->
                when (pair.key) {
                    R.id.services_graph -> {
                        servicesUpdates = 0
                        pair.value.forEach {
                            servicesUpdates += it.value
                        }
                        updateServicesBadge()
                    }

                    R.id.infobox_graph -> {
                        infoBoxUpdates = 0
                        pair.value.forEach {
                            infoBoxUpdates += it.value
                        }
                        updateInfoBoxBadge()
                    }
                }
            }
        }

        viewModel.enableNfc.observe(this) {
            if (it) nfcIntentDispatcher?.enable()
            else nfcIntentDispatcher?.disable()
        }

        viewModel.showDpnUpdates.observe(this) {
            DpnUpdatesDialog {}.showDialog(supportFragmentManager)
        }

        viewModel.rootDetected.observe(this) {
            MaterialAlertDialogBuilder(this, R.style.AlertDialogMaterialTheme)
                .setCancelable(false)
                .setTitle(R.string.security_alert)
                .setMessage(R.string.dialog_rooted_msg)
                .setPositiveButton(R.string.dialog_button_ok) { _, _ -> finish(); exitProcess(0) }
                .show()
        }

        viewModel.forceUpdate.observe(this) {
            ForceUpdateDialog()
                .showDialog(supportFragmentManager, ForceUpdateDialog.TAG)
        }

        if (viewModel.shouldShowWhatsNewDialog()) {
            WhatsNewInAppDialog().showDialog(supportFragmentManager, WhatsNewInAppDialog.TAG)
            viewModel.whatsNewShown()
        }
    }

    private fun onCityUpdated(it: City) {

        selectedCityId = it.cityId
        binding.navigationBottom.menu.findItem(R.id.services_graph).isVisible =
            it.cityConfig?.showServicesOption ?: false

        if (!binding.navigationBottom.menu.findItem(binding.navigationBottom.selectedItemId).isVisible) {
            binding.navigationBottom.selectedItemId = R.id.home_graph
        }
        binding.navigationBottom.setItemsColor(it.cityColorInt)
        setupBottomNavigation()
        if (intent.data == null) {
            if (isCityActive) {
                hideSplashScreen()
            } else {
                DialogUtil.showCityNoMoreActive(
                    this,
                    positiveClickListener = {
                        hideSplashScreen()
                        CitySelectionFragment()
                            .showDialog(supportFragmentManager)
                    }
                )
                isCityActive = true
            }
        }
    }

    fun hideBottomNavBar() {
        binding.navigationBottom.visibility = View.GONE
    }

    fun revealBottomNavBar() {
        binding.navigationBottom.visibility = View.VISIBLE
    }

    private fun setupBottomNavigation(intent: Intent = this.intent) {

        var extraDeepLink: Uri? = null
        intent.extras
            ?.getString(OscaPushService.DEEPLINK_PARAM)
            ?.let { extraDeepLink = Uri.parse(it) }

        if (intent.data == null && extraDeepLink != null) intent.data = extraDeepLink

        intent.data?.let {

            cityid = getCityId()
            if (!isOpenDeeplink && intent.data.toString()
                    .contains(OscaPushService.EVENT_DEEP_LINK_URI_IDENTIFIER) && !viewModel.isUserLoggedIn()
            ) {
                intent.data = Uri.parse("")
                viewModel.setDeepLinkCity(0)
                hideSplashScreen()
                isOpenDeeplink = true
                startActivity<LoginActivity>()
            } else if ((cityid != 0 && cityid != selectedCityId && !isOpenDeeplink && viewModel.isUserLoggedIn())) {
                isCitySwitch = true
                intent.data = getDeepLink(it.toString())
                viewModel.setDeepLinkCity(cityid!!)
                viewModel.setReloadCityData()
                viewModel.onActivityResumed()
                return
            } else if (isOpenDeeplink && intent.data.toString()
                    .contains(OscaPushService.EVENT_DEEP_LINK_URI_IDENTIFIER)
            ) {
                intent.data = Uri.parse("")
                viewModel.setDeepLinkCity(0)
            } else {
                isCitySwitch = false
                intent.data = getDeepLink(it.toString())
            }
            cityid = getCityId()

        }

        clearedDestinations.clear()
        clearedDestinations.add(binding.navigationBottom.selectedItemId)

        val navHostFragment = supportFragmentManager.findFragmentById(R.id.container) as NavHostFragment
        val navController = navHostFragment.navController

        binding.navigationBottom.setupWithNavController(navController)

        navController.addOnDestinationChangedListener { controller, destination, arguments ->
            // NOTE: Keep this, as needed for investigations of issues with navigation
            Timber.i("Controller: $controller | New Destination: ${destination.displayName} | Arguments: $arguments")
        }

        binding.navigationBottom.setOnItemReselectedListener {
            if (navController.currentDestination?.id == navController.currentDestination?.parent?.startDestinationId) {
                if (navController.currentDestination?.parent?.id == R.id.detailed_map_graph || navController.currentDestination?.parent?.id == R.id.fahrradparken_service_graph) {
                    navController.popBackStack()
                } else {
                    return@setOnItemReselectedListener
                }
            }
            navController.popBackStack(navController.currentDestination!!.parent!!.startDestinationId, false)
        }

        binding.navigationBottom.setOnItemSelectedListener {
            tabWasSelected = true

            // This section is needed for maintaining the backstack of last selected menu item,
            // when another menu item is visited by other means except user-tap, e.g. deeplink.
            val currentGraphLastDestinationEntry = navController.currentBackStack.value.findLast { navBackStackEntry ->
                navBackStackEntry.destination.parent?.id == it.itemId
            }
            currentGraphLastDestinationEntry?.let { entry ->
                navController.popBackStack(entry.destination.id, false)
                return@setOnItemSelectedListener true
            }

            NavigationUI.onNavDestinationSelected(it, navController)
            if (clearedDestinations.contains(it.itemId)) return@setOnItemSelectedListener true
            clearedDestinations.add(it.itemId)
            if (navController.currentDestination?.id == navController.currentDestination?.parent?.startDestinationId)
                return@setOnItemSelectedListener true

            navController.popBackStack(navController.currentDestination!!.parent!!.startDestinationId, false)
            true
        }

        //Todo optimize following block
        if (intent.hasExtra("newsItem")) {
            adjustManager.trackEvent(R.string.news_widget_tapped)
            viewModel.isFirstLaunch.observe(this) {
                startActivity<WelcomeActivity>()
            }
            if (navController.currentDestination?.id != R.id.home) {

                if (navController.currentDestination?.parent?.startDestinationId == R.id.home) {
                    if (navController.currentDestination?.id == R.id.article) {
                        navController.popBackStack(R.id.article, true)
                        navController.currentDestination?.id = R.id.home
                    }
                    navController.currentDestination?.id = R.id.home
                } else {
                    navController.popBackStack(navController.currentDestination!!.parent!!.startDestinationId, true)
                    navController.currentDestination?.id = R.id.home
                }
            }
            intent.extras?.getParcelable<CityContent>("newsItem")?.let { newsItem ->
                navController.navigate(R.id.article, bundleOf("newsItem" to newsItem))
            }
            intent.removeExtra("newsItem")
            return
        }
        if (intent.hasExtra(WasteCalendarWidgetConstants.EXTRA_WIDGET_TAPPED)) {
            if (viewModel.isUserLoggedIn().not()) {
                intent.data = null
            }
            trackIntentExtraBasedEvents()
        }
        if (intent.data.toString().contains(OscaPushService.EVENT_DEEP_LINK_URI_IDENTIFIER)) {
            if (viewModel.getDeepLinkCity() != selectedCityId || !isCitySwitch) {
                navController.handleDeepLink(intent)
            }
        } else {
            navController.handleDeepLink(intent)
        }
    }

    private fun trackIntentExtraBasedEvents() {
        if (intent.hasExtra(WasteCalendarWidgetConstants.EXTRA_WIDGET_TAPPED)) {
            adjustManager.trackEvent(R.string.waste_widget_tapped)
            intent.removeExtra(WasteCalendarWidgetConstants.EXTRA_WIDGET_TAPPED)
        }
    }

    private fun updateInfoBoxBadge() {
        binding.navigationBottom.getOrCreateBadge(R.id.infobox_graph).apply {
            number = infoBoxUpdates
            backgroundColor = CityInteractor.cityColorInt
            isVisible = infoBoxUpdates > 0
            verticalOffset = 10
            badgeTextColor = Color.WHITE
        }
    }

    private fun updateServicesBadge() {
        binding.navigationBottom.getOrCreateBadge(R.id.services_graph).apply {
            number = servicesUpdates
            backgroundColor = CityInteractor.cityColorInt
            isVisible = servicesUpdates > 0
            verticalOffset = 10
            badgeTextColor = Color.WHITE
        }
    }

    override fun onSupportNavigateUp() = findNavController(R.id.container).navigateUp()

    fun setupActionBar(toolbar: Toolbar) {
        setSupportActionBar(toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
    }

    private fun initStatusBar() {
        window.statusBarColor = ContextCompat.getColor(this, R.color.black5a)
    }

    fun hideSplashScreen() {
        if (binding.splash.visibility == View.VISIBLE) {
            binding.splash.startAnimation(
                AnimationUtils.loadAnimation(applicationContext, R.anim.splash_exit).apply {
                    interpolator = FastOutLinearInInterpolator()
                    listen {
                        onAnimationStart {
                            finishLoading()
                        }
                        onAnimationEnd {
                            finishLoading()
                            intent.data = null
                        }
                    }
                }
            )
        }
    }

    private fun showSplashViewIfRequired() {
        if (viewModel.shouldKeepSplashViewHidden) {
            binding.splash.visibility = View.GONE
        } else {
            try {
                playCitykeySplashAnimation()
            } catch (e: Exception) {
                Timber.e(e)
            }
        }
    }

    private fun playCitykeySplashAnimation() {
        with(binding.splashLAV) {
            setIgnoreDisabledSystemAnimations(true)
            addAnimatorListener(
                object : Animator.AnimatorListener {
                    override fun onAnimationStart(animation: Animator) {
                    }

                    override fun onAnimationEnd(animation: Animator) {
                        try {
                            if (isSplashAnimationFinalFramesPlayed) {
                                binding.splash.visibility = View.GONE
                            } else {
                                setMinAndMaxFrame(30, 59)
                                repeatCount = ValueAnimator.INFINITE
                                playAnimation()
                            }
                        } catch (e: Exception) {
                            Timber.e(e)
                        }
                    }

                    override fun onAnimationCancel(animation: Animator) {
                    }

                    override fun onAnimationRepeat(animation: Animator) {
                    }
                }
            )
            isSplashAnimationFinalFramesPlayed = false
            setAnimation(if (resources.isDarkMode) R.raw.lottie_animation_splash_dark else R.raw.lottie_animation_splash_light)
            setMinAndMaxFrame(0, 29)
            playAnimation()
        }
    }

    private fun finishLoading() {
        try {
            with(binding.splashLAV) {
                isSplashAnimationFinalFramesPlayed = true
                repeatCount = 0
                setMinAndMaxFrame(60, 69)
                playAnimation()
            }
        } catch (e: Exception) {
            Timber.e(e)
        }
    }

    override fun onDestroy() {
        clearLoginRequiredDialog()
        nfcIntentDispatcher = null
        super.onDestroy()
    }

    private fun getDeepLink(deepLinkUrl: String): Uri =
        if (deepLinkUrl.contains("eventId=")) {
            var deeplink = deepLinkUrl.replace("?eventId=", "/")
            deeplink = deeplink.replace("&cityId=$cityid", if (!isCitySwitch) "/0/" else "/$cityid/")
            Uri.parse(deeplink.trim())
        } else if (isCitySwitch && deepLinkUrl.contains("/0/")) {
            val deeplink = deepLinkUrl.replace("/0/", "/$cityid/")
            Uri.parse(deeplink.trim())
        } else
            Uri.parse(deepLinkUrl)

    private fun getCityId(): Int? =
        if (intent.data.toString().contains("cityId=")) intent.data?.getQueryParameter("cityId")?.toInt()
        else 0

    fun setIsOpenDeeplink(isOpenDeeplink: Boolean) {
        this.isOpenDeeplink = isOpenDeeplink
        viewModel.setDeepLinkCity(0)
    }

    fun markLoadCompleteIfFromDeeplink(deeplink: String, hasQueryParams: Boolean = false) {
        if (activityIntentHasDeeplink()) {
            if (hasQueryParams) {
                val uri = Uri.parse(deeplink)
                val deeplinkContainsAllQueryParams = uri.queryParameterNames.all { param ->
                    currentDeeplinkString?.contains(param, true) == true
                }
                if (deeplinkContainsAllQueryParams) {
                    hideSplashScreen()
                    clearDeeplinkInfo()
                }
            } else if (currentDeeplinkString == deeplink) {
                hideSplashScreen()
                clearDeeplinkInfo()
            }
        }
    }

    @Suppress("MemberVisibilityCanBePrivate")
    val currentDeeplinkString: String?
        get() {
            val intentUriString = intent.data.toString()
            val deeplinkParamString = intent.getStringExtra(OscaPushService.DEEPLINK_PARAM)
            return when {
                intentUriString.startsWith(OscaPushService.CITYKEY_DEEP_LINK_URI_IDENTIFIER) -> intentUriString
                deeplinkParamString?.startsWith(OscaPushService.CITYKEY_DEEP_LINK_URI_IDENTIFIER) == true -> deeplinkParamString
                else -> null
            }
        }

    private fun activityIntentHasDeeplink(): Boolean = (currentDeeplinkString != null)

    fun clearDeeplinkInfo() {
        intent.apply {
            data = null
            removeExtra(OscaPushService.DEEPLINK_PARAM)
        }
    }

    fun markLoginDialogShown(dialogInterface: DialogInterface) {
        clearLoginRequiredDialog()
        loginDialogInterface = dialogInterface
    }

    private fun clearLoginRequiredDialog() {
        loginDialogInterface?.let {
            it.dismiss()
            loginDialogInterface = null
        }
    }

}
