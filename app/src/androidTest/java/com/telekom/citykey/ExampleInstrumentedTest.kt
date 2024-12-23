package com.telekom.citykey

import androidx.test.internal.runner.junit4.AndroidJUnit4ClassRunner
import androidx.test.platform.app.InstrumentationRegistry
import com.telekom.citykey.BuildConfig.*
import org.junit.Assert.assertEquals
import org.junit.Test
import org.junit.runner.RunWith

/**
 * Instrumented test, which will execute on an Android device.
 *
 * See [testing documentation](http://d.android.com/tools/testing).
 *
 * Also, implementation her is updated with the guidelines provided in
 * [this SO thread](https://stackoverflow.com/questions/52776716/androidjunit4-class-is-deprecated-how-to-use-androidx-test-ext-junit-runners-an)
 */
@RunWith(AndroidJUnit4ClassRunner::class)
class ExampleInstrumentedTest {
    @Test
    fun useAppContext() {
        // Context of the app under test.
        val appContext = InstrumentationRegistry.getInstrumentation().targetContext
        assertEquals("com.telekom.citykey.$FLAVOR", appContext.packageName)
    }
}
