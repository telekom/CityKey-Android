package com.telekom.citykey.view.widget.waste_calendar.small_widget

import android.app.PendingIntent
import android.appwidget.AppWidgetManager
import android.appwidget.AppWidgetProvider
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.view.View
import android.widget.RemoteViews
import com.telekom.citykey.R
import com.telekom.citykey.domain.track.AdjustManager
import com.telekom.citykey.utils.extensions.isInCurrentMonth
import com.telekom.citykey.utils.extensions.removeMidnightUpdateForWidget
import com.telekom.citykey.utils.extensions.scheduleMidnightUpdateForWidget
import com.telekom.citykey.view.main.MainActivity
import com.telekom.citykey.view.widget.waste_calendar.WasteCalendarWidgetConstants
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject
import java.util.*


class SmallWasteCalendarWidget : AppWidgetProvider(), KoinComponent {
    companion object {
        const val SMALL_WASTE_CALENDAR_WIDGET_REQUEST_CODE = 7654812
    }

    val adjustManager: AdjustManager by inject()

    override fun onReceive(context: Context?, intent: Intent) {
        when (intent.action) {
            WasteCalendarWidgetConstants.ACTION_WASTE_CALENDAR_WIDGET_SCHEDULED_UPDATE,
            Intent.ACTION_TIMEZONE_CHANGED,
            Intent.ACTION_TIME_CHANGED,
            Intent.ACTION_DATE_CHANGED -> {
                val manager = AppWidgetManager.getInstance(context)
                val ids =
                    manager.getAppWidgetIds(context?.let { ComponentName(it, SmallWasteCalendarWidget::class.java) })
                manager.notifyAppWidgetViewDataChanged(ids, R.id.wasteList)
                onUpdate(context!!, manager, ids)
            }
        }
        super.onReceive(context, intent)
    }

    override fun onUpdate(context: Context, appWidgetManager: AppWidgetManager, appWidgetIds: IntArray) {
        // There may be multiple widgets active, so update all of them
        for (appWidgetId in appWidgetIds) {
            val views = RemoteViews(context.packageName, R.layout.small_waste_calendar_widget)
            val tomorrow = Calendar.getInstance().apply { add(Calendar.DAY_OF_YEAR, 1) }
            val intent = Intent(context, SmallWasteCalendarWidgetRemoteViewService::class.java).apply {
                putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, appWidgetId)
            }
            views.setViewVisibility(R.id.smallWidgetGradientView, View.VISIBLE)
            views.setRemoteAdapter(R.id.wasteList, intent)
            views.setEmptyView(R.id.wasteList, R.id.emptyViewForWasteCalendarPickupsUnavailable)
            val activityIntent = Intent(context, MainActivity::class.java).apply {
                flags = Intent.FLAG_ACTIVITY_CLEAR_TASK or Intent.FLAG_ACTIVITY_NEW_TASK
                data = Uri.parse("citykey://services/waste/overview/${tomorrow.isInCurrentMonth().not()}")
                putExtra(WasteCalendarWidgetConstants.EXTRA_WIDGET_TAPPED, true)
            }
            val pendingIntent = PendingIntent.getActivity(
                context,
                SMALL_WASTE_CALENDAR_WIDGET_REQUEST_CODE,
                activityIntent,
                PendingIntent.FLAG_MUTABLE
            )
            views.setOnClickPendingIntent(R.id.emptyViewForWasteCalendarPickupsUnavailable, pendingIntent)
            views.setPendingIntentTemplate(R.id.wasteList, pendingIntent)
            views.setOnClickPendingIntent(R.id.wasteCalendarSmallWidgetFrame, pendingIntent)
            views.setOnClickPendingIntent(R.id.frame, pendingIntent)
            appWidgetManager.updateAppWidget(appWidgetId, views)
        }
    }

    override fun onEnabled(context: Context) {
        // Enter relevant functionality for when the first widget is created
        adjustManager.trackEvent(R.string.waste_calendar_small_widget)
        context.scheduleMidnightUpdateForWidget<SmallWasteCalendarWidget>(
            WasteCalendarWidgetConstants.ACTION_WASTE_CALENDAR_WIDGET_SCHEDULED_UPDATE
        )
    }

    override fun onDisabled(context: Context) {
        // Enter relevant functionality for when the last widget is removed
        adjustManager.trackEvent(R.string.remove_waste_calendar_small_widget)
        context.removeMidnightUpdateForWidget<SmallWasteCalendarWidget>(
            WasteCalendarWidgetConstants.ACTION_WASTE_CALENDAR_WIDGET_SCHEDULED_UPDATE
        )
    }
}
