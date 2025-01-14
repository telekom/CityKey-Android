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

package com.telekom.citykey.custom.views

import android.content.ComponentCallbacks2
import android.content.Context
import android.content.res.Configuration
import android.util.AttributeSet
import androidx.fragment.app.Fragment
import androidx.lifecycle.DefaultLifecycleObserver
import androidx.lifecycle.LifecycleObserver
import androidx.lifecycle.LifecycleOwner
import com.google.android.gms.maps.MapView

open class LifecycleAwareMapView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet,
    defStyle: Int = 0
) : MapView(context, attrs, defStyle) {

    private var mLifecycleOwnerFragment: Fragment? = null
    private val mComponentCallbacks by lazy {
        object : ComponentCallbacks2 {
            override fun onConfigurationChanged(newConfig: Configuration) {
                this@LifecycleAwareMapView.onConfigurationChanged(newConfig)
            }

            // NOTE: For < SDK_14
            override fun onLowMemory() {
                this@LifecycleAwareMapView.onLowMemory()
            }

            // NOTE: For SDK_14 onwards
            override fun onTrimMemory(state: Int) {
                this@LifecycleAwareMapView.onLowMemory()
            }
        }
    }

    private val mLifecycleObserver: LifecycleObserver by lazy {
        object : DefaultLifecycleObserver {
            // NOTE: We must not override `onCreate()` as it will try to recreate the map,
            // which causes the map to go in an inconsistent state.

            override fun onStart(owner: LifecycleOwner) {
                super.onStart(owner)
                this@LifecycleAwareMapView.onStart()
            }

            override fun onResume(owner: LifecycleOwner) {
                super.onResume(owner)
                this@LifecycleAwareMapView.onResume()
            }

            override fun onPause(owner: LifecycleOwner) {
                super.onPause(owner)
                this@LifecycleAwareMapView.onPause()
            }

            override fun onStop(owner: LifecycleOwner) {
                super.onStop(owner)
                this@LifecycleAwareMapView.onStop()
            }

            override fun onDestroy(owner: LifecycleOwner) {
                super.onDestroy(owner)
                this@LifecycleAwareMapView.onDestroy()
            }
        }
    }

    init {
        // NOTE: The `onCreate()` must be called here to perform the map view creation,
        // as the view is being initialized here.
        this@LifecycleAwareMapView.onCreate(null)
    }

    override fun onDestroy() {
        super.onDestroy()
        removeAllLifecycleObservers()
        mLifecycleOwnerFragment = null
    }

    private fun removeAllLifecycleObservers() {
        mLifecycleOwnerFragment?.let {
            it.viewLifecycleOwner.lifecycle.removeObserver(mLifecycleObserver)
            it.requireContext().unregisterComponentCallbacks(mComponentCallbacks)
        } ?: kotlin.run {
            if (context is LifecycleOwner) {
                (context as LifecycleOwner).lifecycle.removeObserver(mLifecycleObserver)
                context.unregisterComponentCallbacks(mComponentCallbacks)
            }
        }
    }

    /**
     * This function must be called before the MapView.getMapAsync(),
     * to make sure the pre-requisite map view lifecycle callbacks are being invoked.
     */
    fun attachLifecycleToFragment(fragment: Fragment) {
        removeAllLifecycleObservers()
        fragment.viewLifecycleOwner.lifecycle.addObserver(mLifecycleObserver)
        fragment.requireContext().registerComponentCallbacks(mComponentCallbacks)
        mLifecycleOwnerFragment = fragment
    }

}
