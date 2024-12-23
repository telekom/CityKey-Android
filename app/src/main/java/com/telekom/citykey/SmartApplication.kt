package com.telekom.citykey

import android.annotation.SuppressLint
import android.app.ActivityManager
import android.app.Application
import android.os.Build
import android.os.Process
import android.util.Log
import com.microsoft.appcenter.AppCenter
import com.microsoft.appcenter.distribute.Distribute
import com.telekom.citykey.common.FileLoggingTree
import com.telekom.citykey.di.remote_datasource_module
import com.telekom.citykey.di.smart_app_modules
import com.telekom.citykey.domain.notifications.TpnsManager
import com.telekom.citykey.domain.track.AdjustManager
import com.telekom.citykey.utils.PreferencesHelper
import org.koin.android.ext.android.inject
import org.koin.android.ext.koin.androidContext
import org.koin.core.context.startKoin
import timber.log.Timber

class SmartApplication : Application() {

    companion object {
        private const val AA2_PROCESS = "ausweisapp2_service"
    }

    private val adjustManager: AdjustManager by inject()
    private val tpnsManager: TpnsManager by inject()
    private val preferencesHelper: PreferencesHelper by inject()

    @SuppressLint("HardwareIds")
    override fun onCreate() {
        super.onCreate()

        logBugReportInfo()

        when {
            BuildConfig.FLAVOR != "production" -> Timber.plant(FileLoggingTree(this))
            BuildConfig.DEBUG -> Timber.plant(Timber.DebugTree())
        }

        if (isAA2Process()) {
            Timber.i("Application is instantiated again by AA2 Process")
            return
        }

        startKoin {
            androidContext(this@SmartApplication)
            modules(smart_app_modules + remote_datasource_module)
        }

        if (BuildConfig.APPCENTER_ID.isNotEmpty()) {
            AppCenter.start(this, BuildConfig.APPCENTER_ID, Distribute::class.java)
        }

        tpnsManager.initPushNotifications()
        adjustManager.initialiseMoEngage(this)
        preferencesHelper.togglePreviewMode(false)
    }

    private fun isAA2Process(): Boolean {
        if (Build.VERSION.SDK_INT >= 28) {
            return getProcessName().endsWith(AA2_PROCESS)
        }
        val pid = Process.myPid()
        val manager = getSystemService(ACTIVITY_SERVICE) as ActivityManager
        for (appProcess in manager.runningAppProcesses) {
            if (appProcess.pid == pid) {
                return appProcess.processName.endsWith(AA2_PROCESS)
            }
        }
        return false
    }

    /**
     * A quick helper utility method to log the BugReport information for the Open Source developers, used in
     * issue / bug reports
     */
    @SuppressLint("LogNotTimber")
    private fun logBugReportInfo() {
        Log.i(
            "BugReport",
            """
        --- Bug Report Info ---
        App Version: ${BuildConfig.APP_VERSION}
        Device Model: ${Build.MODEL} (${Build.PRODUCT})
        Manufacturer: ${Build.MANUFACTURER}
        Android Version: ${Build.VERSION.RELEASE} (API Level ${Build.VERSION.SDK_INT})
        Device Architecture: ${Build.SUPPORTED_ABIS.joinToString(", ")}
        -----------------------
    """.trimIndent()
        )
    }
}
