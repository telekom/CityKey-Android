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

package com.telekom.citykey.view.widget.waste_calendar.medium_widget

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.view.View
import android.widget.RemoteViews
import android.widget.RemoteViewsService
import com.telekom.citykey.R
import com.telekom.citykey.domain.widget.WidgetInteractor
import com.telekom.citykey.networkinterface.models.waste_calendar.Pickups
import com.telekom.citykey.networkinterface.models.waste_calendar.WasteItems
import com.telekom.citykey.utils.ColorUtils
import com.telekom.citykey.utils.NetworkConnection
import com.telekom.citykey.utils.extensions.isInCurrentMonth
import com.telekom.citykey.utils.isDarkMode
import com.telekom.citykey.view.widget.waste_calendar.WasteCalendarWidgetConstants
import org.koin.android.ext.android.inject
import java.util.Calendar

class DATPickupsRemoteViewService : RemoteViewsService() {

    private val widgetInteractor: WidgetInteractor by inject()

    override fun onGetViewFactory(intent: Intent?): RemoteViewsFactory {
        return WasteMediumItemViewsFactory(applicationContext, widgetInteractor.pickups)
    }

    inner class WasteMediumItemViewsFactory(
        private val context: Context,
        private val pickupData: Pickups
    ) : RemoteViewsFactory {

        override fun onCreate() {}

        override fun getCount(): Int = pickupData.dAT.size

        override fun hasStableIds(): Boolean = true

        override fun getViewTypeCount(): Int = 1

        override fun getItemId(position: Int): Long = position.toLong()

        override fun getLoadingView(): RemoteViews? = null

        override fun getViewAt(position: Int): RemoteViews {
            val view = RemoteViews(context.packageName, R.layout.waste_calender_widget_pickup_item)
            populateWastePickupData(view, pickupData.dAT[position])
            val dayAfterTomorrow = Calendar.getInstance().apply { add(Calendar.DAY_OF_YEAR, 1) }
            view.setViewVisibility(R.id.lastItemPaddingView, if (position == count - 1) View.VISIBLE else View.GONE)
            val fillIntent = Intent().apply {
                putExtra(WasteCalendarWidgetConstants.EXTRA_WIDGET_TAPPED, true)
                flags = Intent.FLAG_ACTIVITY_CLEAR_TASK or Intent.FLAG_ACTIVITY_NEW_TASK
                data = Uri.parse("citykey://services/waste/overview/${dayAfterTomorrow.isInCurrentMonth().not()}/")
            }
            view.setOnClickFillInIntent(R.id.wasteCalendarPickupItemOuterContainer, fillIntent)
            return view
        }

        override fun onDataSetChanged() {
            if (NetworkConnection.checkInternetConnection(context)) {
                widgetInteractor.getWasteCalenderData(isSingleItemWidget = true)
            }
        }

        override fun onDestroy() {
            widgetInteractor.clearWasteList()
        }

        private fun populateWastePickupData(view: RemoteViews, pickup: WasteItems.WasteItem) {
            view.setImageViewResource(R.id.pickupIcon, R.drawable.ic_waste_trash_icon)
            
            val wasteIconColorInt = if (resources.isDarkMode) {
                ColorUtils.invertIfDark(pickup.wasteIconColorInt)
            } else {
                pickup.wasteIconColorInt
            }
            
            view.setInt(
                R.id.pickupIcon,
                "setColorFilter",
                wasteIconColorInt
            )
            view.setInt(
                R.id.wasteCalendarPickupItemContainer,
                "setBackgroundColor",
                ColorUtils.setAlpha(wasteIconColorInt, 51)
            )
            view.setTextViewText(R.id.pickupName, pickup.wasteType)
            view.setContentDescription(R.id.pickupName, pickup.wasteType)
        }
    }
}
