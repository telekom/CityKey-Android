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

package com.telekom.citykey.view.user.registration

import android.os.Build
import android.os.Bundle
import android.view.MenuItem
import android.view.WindowManager
import androidx.annotation.DrawableRes
import androidx.annotation.StringRes
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import com.telekom.citykey.R
import com.telekom.citykey.databinding.RegistrationActivityBinding
import com.telekom.citykey.utils.extensions.fadeIn
import com.telekom.citykey.utils.extensions.fadeOut
import com.telekom.citykey.utils.extensions.setAccessibilityRoleForToolbarTitle
import com.telekom.citykey.utils.extensions.shouldPreventContentSharing
import com.telekom.citykey.utils.extensions.startActivity
import com.telekom.citykey.utils.extensions.viewBinding
import com.telekom.citykey.view.user.login.LoginActivity
import com.telekom.citykey.view.welcome.WelcomeActivity
import io.reactivex.disposables.Disposable

class RegistrationActivity : AppCompatActivity() {

    private val binding by viewBinding(RegistrationActivityBinding::inflate)

    private var topIconAnimator: Disposable? = null
    var isRegistrationFinished = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            setRecentsScreenshotEnabled(false)
        } else if (shouldPreventContentSharing) {
            window.setFlags(WindowManager.LayoutParams.FLAG_SECURE, WindowManager.LayoutParams.FLAG_SECURE)
        }
        setContentView(binding.root)
        initToolbar()
    }

    private fun initToolbar() {

        binding.toolbar.setNavigationIcon(R.drawable.ic_profile_close)
        binding.toolbar.setNavigationIconTint(getColor(R.color.onSurface))
        binding.toolbar.setNavigationContentDescription(R.string.accessibility_btn_close)
        binding.toolbar.setNavigationOnClickListener { onBackPressed() }
        setAccessibilityRoleForToolbarTitle(binding.toolbar)
        window.statusBarColor = ContextCompat.getColor(this, R.color.black5a)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        if (item.itemId == android.R.id.home) onBackPressed()
        return true
    }

    fun setTopIcon(@DrawableRes iconRes: Int) {
        topIconAnimator?.dispose()
        topIconAnimator = binding.topIcon.fadeOut(200)
            .doOnComplete { binding.topIcon.setImageResource(iconRes) }
            .andThen(binding.topIcon.fadeIn(200))
            .subscribe()
    }

    fun setToolbarTitle(@StringRes resId: Int) {
        binding.toolbar.title = getString(resId)
    }

    override fun onBackPressed() {
        if (intent.getBooleanExtra("isFirstTime", false)) {
            if (isRegistrationFinished || intent.getBooleanExtra("isLaunchedByLogin", false))
                setResult(WelcomeActivity.RESULT_CODE_REGISTRATION_TO_LOGIN)
            finish()
        } else if (intent.getBooleanExtra(LoginActivity.LAUNCH_INFOBOX, false)) {
            finish()
        } else {
            finish()
            if (intent.getBooleanExtra("isLaunchedByLogin", false) || isRegistrationFinished)
                startActivity<LoginActivity>()
        }
    }
}
