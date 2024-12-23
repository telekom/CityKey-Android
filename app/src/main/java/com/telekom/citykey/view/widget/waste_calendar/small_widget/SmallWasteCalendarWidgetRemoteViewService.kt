package com.telekom.citykey.view.widget.waste_calendar.small_widget

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.widget.RemoteViews
import android.widget.RemoteViewsService
import com.telekom.citykey.R
import com.telekom.citykey.domain.widget.WidgetInteractor
import com.telekom.citykey.models.Pickups
import com.telekom.citykey.models.WasteItems
import com.telekom.citykey.utils.ColorUtils
import com.telekom.citykey.utils.NetworkConnection
import com.telekom.citykey.utils.extensions.isInCurrentMonth
import com.telekom.citykey.utils.isDarkMode
import com.telekom.citykey.view.widget.waste_calendar.WasteCalendarWidgetConstants
import org.koin.android.ext.android.inject
import java.util.*

class SmallWasteCalendarWidgetRemoteViewService : RemoteViewsService() {

    private val widgetInteractor: WidgetInteractor by inject()

    override fun onGetViewFactory(intent: Intent?): RemoteViewsFactory {

        return WasteSingleItemViewsFactory(applicationContext, widgetInteractor.pickups)
    }

    inner class WasteSingleItemViewsFactory(private val context: Context, private var pickupData: Pickups) :
        RemoteViewsFactory {
        override fun onCreate() {}

        override fun getCount(): Int = pickupData.tomorrow.size

        override fun hasStableIds(): Boolean = true

        override fun getViewTypeCount(): Int = 1

        override fun getItemId(position: Int): Long = position.toLong()

        override fun getLoadingView(): RemoteViews? = null

        override fun getViewAt(position: Int): RemoteViews {
            val view = RemoteViews(context.packageName, R.layout.waste_calender_widget_pickup_item)
            populateWastePickupData(view, pickupData.tomorrow[position])
            val tomorrow = Calendar.getInstance().apply { add(Calendar.DAY_OF_YEAR, 1) }
            val fillIntent = Intent().apply {
                putExtra(WasteCalendarWidgetConstants.EXTRA_WIDGET_TAPPED, true)
                flags = Intent.FLAG_ACTIVITY_CLEAR_TASK or Intent.FLAG_ACTIVITY_NEW_TASK
                data = Uri.parse("citykey://services/waste/overview/${tomorrow.isInCurrentMonth().not()}")
            }
            view.setOnClickFillInIntent(R.id.wasteCalendarPickupItemContainer, fillIntent)
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
            } else pickup.wasteIconColorInt
            view.setInt(
                R.id.pickupIcon, "setColorFilter",
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
