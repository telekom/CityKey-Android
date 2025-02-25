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

package com.telekom.citykey.view.welcome

import android.Manifest
import android.annotation.SuppressLint
import android.os.Build
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import com.telekom.citykey.R
import com.telekom.citykey.databinding.WelcomeActivityBinding
import com.telekom.citykey.utils.extensions.AccessibilityRole
import com.telekom.citykey.utils.extensions.hasPermission
import com.telekom.citykey.utils.extensions.setAccessibilityRole
import com.telekom.citykey.utils.extensions.showDialog
import com.telekom.citykey.utils.extensions.startActivity
import com.telekom.citykey.utils.extensions.viewBinding
import com.telekom.citykey.view.dialogs.FtuDataPrivacyDialog
import com.telekom.citykey.view.main.MainActivity
import org.koin.android.ext.android.inject

class WelcomeActivity : AppCompatActivity() {
    private val viewModel: WelcomeViewModel by inject()
    private val binding by viewBinding(WelcomeActivityBinding::inflate)

    companion object {
        const val RESULT_CODE_LOGIN_TO_REGISTRATION = 103
        const val RESULT_CODE_REGISTRATION_TO_LOGIN = 104
    }

    private val requestPermissionLauncher = registerForActivityResult(ActivityResultContracts.RequestPermission()) {}

    private fun subscribeUI() {
        viewModel.confirmTrackingTerms.observe(this) { confirmed ->
            if (!confirmed) {
                FtuDataPrivacyDialog()
                    .showDialog(supportFragmentManager)
                checkPermission()
            }

        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            setRecentsScreenshotEnabled(false)
        }
        setContentView(binding.root)
        window.statusBarColor = ContextCompat.getColor(this, R.color.black5a)
        binding.skipBtn.setAccessibilityRole(AccessibilityRole.Button)
        binding.skipBtn.setOnClickListener {
            viewModel.onSkipBtnClicked()
            startActivity<MainActivity>()
        }

        subscribeUI()
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.welcome_menu, menu)
        return super.onCreateOptionsMenu(menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.actionSkipLogin -> {
                viewModel.onSkipBtnClicked()
                startActivity<MainActivity>()
                return true
            }
        }
        return super.onOptionsItemSelected(item)
    }

    override fun onBackPressed() {
        finishAffinity()
    }

    @SuppressLint("MissingPermission")
    private fun arePermissionsGranted() =
        this.hasPermission(Manifest.permission.POST_NOTIFICATIONS)

    private fun requestPermission() {
        requestPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
    }

    private fun checkPermission() {
        if (android.os.Build.VERSION.SDK_INT > android.os.Build.VERSION_CODES.S_V2) {
            if (!arePermissionsGranted()) {
                requestPermission()
            }
        }
    }
}
