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
 * SPDX-License-Identifier: Apache-2.0
 * License-Filename: LICENSES/Apache-2.0.txt
 */

package com.telekom.citykey.view.widget.news.news_single_item

import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.widget.RemoteViews
import android.widget.RemoteViewsService
import com.bumptech.glide.Glide
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.bumptech.glide.load.resource.bitmap.CenterInside
import com.bumptech.glide.load.resource.bitmap.RoundedCorners
import com.bumptech.glide.request.RequestOptions
import com.telekom.citykey.R
import com.telekom.citykey.domain.widget.WidgetInteractor
import com.telekom.citykey.models.content.CityContent
import com.telekom.citykey.utils.NetworkConnection
import com.telekom.citykey.view.widget.news.news_single_item.NewsSingleItemWidget.Companion.ITEM_CORNER_RADIUS
import com.telekom.citykey.view.widget.news.news_single_item.NewsSingleItemWidget.Companion.NEWS_SINGLE_IMAGE_SIZE
import org.koin.android.ext.android.inject

class NewsSingleItemWidgetRemoteViewService : RemoteViewsService() {

    private val widgetInteractor: WidgetInteractor by inject()

    override fun onGetViewFactory(intent: Intent?): RemoteViewsFactory =
        NewsSingleItemViewsFactory(applicationContext, widgetInteractor.newsList)

    inner class NewsSingleItemViewsFactory(private val context: Context, private val newsList: List<CityContent>) :
        RemoteViewsFactory {

        private val newsImageGlideRequestOptions: RequestOptions by lazy {
            RequestOptions()
                .override(NEWS_SINGLE_IMAGE_SIZE, NEWS_SINGLE_IMAGE_SIZE)
                .transform(CenterInside(), RoundedCorners(ITEM_CORNER_RADIUS))
                .placeholder(R.drawable.ic_news_widget_empty_image_placeholder)
                .error(R.drawable.ic_news_widget_empty_image_placeholder)
        }

        override fun onCreate() {}

        override fun getCount(): Int = newsList.size

        override fun hasStableIds(): Boolean = true

        override fun getViewTypeCount(): Int = 1

        override fun getItemId(position: Int): Long = position.toLong()

        override fun getLoadingView(): RemoteViews? = null

        override fun getViewAt(position: Int): RemoteViews {
            val view = RemoteViews(context.packageName, R.layout.news_single_widget_item)

            val newsItem = newsList[position]
            populateNewsData(view, newsItem)

            val fillIntent = Intent().putExtra("newsItem", newsItem)
            view.setOnClickFillInIntent(R.id.newsSingleItemContainer, fillIntent)
            return view
        }

        override fun onDataSetChanged() {
            if (NetworkConnection.checkInternetConnection(context))
                widgetInteractor.getNewsForCurrentCity(isSingleItemWidget = true)
        }

        override fun onDestroy() {
            widgetInteractor.clearNewsList()
        }

        private fun populateNewsData(view: RemoteViews, cityContent: CityContent) {
            view.setTextViewText(R.id.description, cityContent.contentTeaser)
            view.setContentDescription(R.id.description, cityContent.contentTeaser)
            cityContent.thumbnail?.let { loadNewsImage(view, it) }
        }

        private fun loadNewsImage(view: RemoteViews, thumbnail: String) {
            try {
                val bitmap: Bitmap = Glide.with(context)
                    .asBitmap()
                    .load(thumbnail)
                    .centerInside()
                    .diskCacheStrategy(DiskCacheStrategy.ALL)
                    .apply(newsImageGlideRequestOptions)
                    .submit()
                    .get()
                view.setImageViewBitmap(R.id.newsImage, bitmap)
            } catch (e: Exception) {
                view.setImageViewResource(R.id.newsImage, R.drawable.ic_news_widget_empty_image_placeholder)
            }
        }
    }

}
