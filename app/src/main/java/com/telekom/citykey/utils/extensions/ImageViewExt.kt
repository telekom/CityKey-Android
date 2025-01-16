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

package com.telekom.citykey.utils.extensions

import android.graphics.drawable.Drawable
import android.widget.ImageView
import androidx.annotation.DrawableRes
import androidx.core.net.toUri
import com.bumptech.glide.load.DataSource
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.bumptech.glide.load.engine.GlideException
import com.bumptech.glide.load.resource.drawable.DrawableTransitionOptions
import com.bumptech.glide.request.RequestListener
import com.bumptech.glide.request.target.Target
import com.telekom.citykey.BuildConfig
import com.telekom.citykey.R
import com.telekom.citykey.common.GlideApp

fun ImageView.loadFromOSCA(imageUrl: String?) {
    if (imageUrl.isNullOrBlank()) return
    GlideApp.with(context)
        .load(
            "${BuildConfig.IMAGE_URL}$imageUrl".toUri()
        )
        .transition(DrawableTransitionOptions.withCrossFade(300))
        .diskCacheStrategy(DiskCacheStrategy.ALL)
        .into(this)
}

fun ImageView.loadFromOSCA(imageUrl: String, placeholderResId: Int = 0, errorResId: Int = 0) {
    var glideRequestRemoteImage = GlideApp.with(context)
        .load(
            "${BuildConfig.IMAGE_URL}$imageUrl".toUri()
        )
        .transition(DrawableTransitionOptions.withCrossFade(300))
        .diskCacheStrategy(DiskCacheStrategy.ALL)
    if (placeholderResId != 0) {
        glideRequestRemoteImage = glideRequestRemoteImage.placeholder(placeholderResId)
    }
    if (errorResId != 0) {
        glideRequestRemoteImage = glideRequestRemoteImage.error(errorResId)
    } else if (placeholderResId != 0) {
        glideRequestRemoteImage = glideRequestRemoteImage.error(placeholderResId)
    }
    glideRequestRemoteImage.into(this)
}

fun ImageView.loadFromURL(
    imageUrl: String?,
    cacheStrategy: DiskCacheStrategy = DiskCacheStrategy.ALL
) {
    imageUrl?.let {
        GlideApp.with(context)
            .load(it.toUri())
            .transition(DrawableTransitionOptions.withCrossFade(300))
            .centerInside()
            .diskCacheStrategy(cacheStrategy)
            .into(this)
    }
}

fun ImageView.loadFromURLwithProgress(
    imageUrl: String?,
    successListener: (Boolean) -> Unit,
    cacheStrategy: DiskCacheStrategy = DiskCacheStrategy.ALL
) {
    imageUrl?.let {
        GlideApp.with(context)
            .load(it.toUri())
            .transition(DrawableTransitionOptions.withCrossFade(300))
            .centerInside()
            .placeholder(R.drawable.ic_bg_detail_page)
            .listener(object : RequestListener<Drawable> {
                override fun onLoadFailed(
                    e: GlideException?,
                    model: Any?,
                    target: Target<Drawable>?,
                    isFirstResource: Boolean
                ): Boolean {
                    successListener.invoke(true)
                    return false
                }

                override fun onResourceReady(
                    resource: Drawable?,
                    model: Any?,
                    target: Target<Drawable>?,
                    dataSource: DataSource?,
                    isFirstResource: Boolean
                ): Boolean {
                    successListener.invoke(false)
                    return false
                }
            })
            .diskCacheStrategy(cacheStrategy)
            .into(this)
    }
}

fun ImageView.loadFromDrawable(@DrawableRes resourceId: Int) {
    GlideApp.with(context)
        .load(resourceId)
        .transition(DrawableTransitionOptions.withCrossFade(300))
        .diskCacheStrategy(DiskCacheStrategy.NONE)
        .into(this)
}
