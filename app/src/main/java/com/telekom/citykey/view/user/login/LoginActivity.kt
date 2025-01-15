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
 * SPDX-FileCopyrightText: 2025 Deutsche Telekom AG
 * SPDX-License-Identifier: Apache-2.0
 * License-Filename: LICENSES/Apache-2.0.txt
 */

package com.telekom.citykey.view.user.login

import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.view.MenuItem
import android.view.WindowManager
import androidx.annotation.DrawableRes
import androidx.annotation.StringRes
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.vectordrawable.graphics.drawable.AnimatedVectorDrawableCompat
import com.telekom.citykey.R
import com.telekom.citykey.databinding.LoginActivityBinding
import com.telekom.citykey.utils.extensions.fadeIn
import com.telekom.citykey.utils.extensions.fadeOut
import com.telekom.citykey.utils.extensions.setAccessibilityRoleForToolbarTitle
import com.telekom.citykey.utils.extensions.shouldPreventContentSharing
import com.telekom.citykey.utils.extensions.viewBinding
import com.telekom.citykey.view.user.profile.ProfileActivity
import io.reactivex.disposables.Disposable

class LoginActivity : AppCompatActivity() {

    companion object {
        const val LAUNCH_PROFILE = "LaunchProfile"
        const val LAUNCH_INFOBOX = "LaunchInfoBoX"
    }

    private val binding by viewBinding(LoginActivityBinding::inflate)

    private var isCalledByProfile = false
    private var isCalledByInfobox = false
    private var topIconAnimator: Disposable? = null
    private val toX: AnimatedVectorDrawableCompat by lazy {
        AnimatedVectorDrawableCompat.create(applicationContext, R.drawable.back_to_x)!!
    }
    private val toBack: AnimatedVectorDrawableCompat by lazy {
        AnimatedVectorDrawableCompat.create(applicationContext, R.drawable.x_to_back)!!
    }
    private var isIndicatorCross = true

    var backAction: (() -> Unit)? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            setRecentsScreenshotEnabled(false)
        } else if (shouldPreventContentSharing) {
            window.setFlags(WindowManager.LayoutParams.FLAG_SECURE, WindowManager.LayoutParams.FLAG_SECURE)
        }
        setContentView(binding.root)
        isCalledByProfile = intent.getBooleanExtra(LAUNCH_PROFILE, false)
        isCalledByInfobox = intent.getBooleanExtra(LAUNCH_INFOBOX, false)

        initToolbar()
    }

    private fun initToolbar() {

        binding.toolbarLogin.setNavigationIcon(R.drawable.ic_profile_close)
        binding.toolbarLogin.setNavigationIconTint(getColor(R.color.onSurface))
        binding.toolbarLogin.setNavigationContentDescription(R.string.accessibility_btn_close)
        binding.toolbarLogin.setNavigationOnClickListener { onBackPressed() }
        setAccessibilityRoleForToolbarTitle(binding.toolbarLogin)
        window.statusBarColor = ContextCompat.getColor(this, R.color.black5a)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            android.R.id.home -> {
                onBackPressed()
                true
            }

            else -> super.onOptionsItemSelected(item)
        }
    }

    fun finishOrOpenProfile() {
        if (isCalledByProfile) {
            startActivity(Intent(applicationContext, ProfileActivity::class.java))
            finish()
        } else if (isCalledByInfobox) {
            finish()
        } else {
            setResult(RESULT_OK)
            finish()
        }
    }

    fun adaptToolbarForClose() {
        if (!isIndicatorCross) {
            binding.toolbarLogin.navigationIcon = toX.apply { start() }
            isIndicatorCross = true
        }
    }

    fun adaptToolbarForBack() {
        if (isIndicatorCross) {
            binding.toolbarLogin.navigationIcon = toBack.apply { start() }
            isIndicatorCross = false
        }
    }

    fun setTopIcon(@DrawableRes iconRes: Int) {
        topIconAnimator?.dispose()
        topIconAnimator = binding.topIcon.fadeOut(200)
            .doOnComplete { binding.topIcon.setImageResource(iconRes) }
            .andThen(binding.topIcon.fadeIn(200))
            .subscribe()
    }

    fun setPageTitle(@StringRes resId: Int) {
        binding.toolbarLogin.title = getString(resId)
    }

    override fun onBackPressed() {
        if (backAction == null)
            super.onBackPressed()
        else {
            backAction?.invoke()
            backAction = null
        }
    }

    override fun onDestroy() {
        topIconAnimator?.dispose()
        super.onDestroy()
    }
}
