package com.telekom.citykey

import androidx.annotation.NonNull
import io.reactivex.Scheduler
import io.reactivex.android.plugins.RxAndroidPlugins
import io.reactivex.disposables.Disposable
import io.reactivex.internal.schedulers.ExecutorScheduler
import io.reactivex.plugins.RxJavaPlugins
import org.junit.jupiter.api.extension.AfterEachCallback
import org.junit.jupiter.api.extension.BeforeEachCallback
import org.junit.jupiter.api.extension.ExtensionContext
import java.util.concurrent.TimeUnit

class RxImmediateSchedulerExtension : BeforeEachCallback, AfterEachCallback {

    private val immediateScheduler = object : Scheduler() {
        @NonNull
        override fun scheduleDirect(run: Runnable, delay: Long, unit: TimeUnit): Disposable {
            return super.scheduleDirect(run, 0, unit)
        }

        @NonNull
        override fun createWorker(): Worker {
            return ExecutorScheduler.ExecutorWorker(Runnable::run, true)
        }
    }

    override fun beforeEach(context: ExtensionContext?) {
        RxJavaPlugins.setInitIoSchedulerHandler { immediateScheduler }
        RxJavaPlugins.setInitComputationSchedulerHandler { immediateScheduler }
        RxJavaPlugins.setInitNewThreadSchedulerHandler { immediateScheduler }
        RxJavaPlugins.setInitSingleSchedulerHandler { immediateScheduler }
        RxAndroidPlugins.setInitMainThreadSchedulerHandler { immediateScheduler }
    }

    override fun afterEach(context: ExtensionContext?) {
        RxJavaPlugins.reset()
        RxAndroidPlugins.reset()
    }
}
