package com.telekom.citykey.utils

import android.app.ActivityManager
import android.content.Context

/**
 * Detect whether app is in foreground and running
 */
object ForegroundUtil {

    fun isApRunning(context: Context): Boolean {
        val activityManager = context.getSystemService(Context.ACTIVITY_SERVICE) as ActivityManager
        val runningAppProcesses = activityManager.runningAppProcesses ?: return false
        return runningAppProcesses.any {
            it.processName.contains(context.packageName) &&
                it.importance == ActivityManager.RunningAppProcessInfo.IMPORTANCE_FOREGROUND
        }
    }
}
