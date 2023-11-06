package com.rakuten.tech.mobile.sdkutils.eventlogger

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner

@RunWith(RobolectricTestRunner::class)
class EventLoggerSpec {

    private val context = ApplicationProvider.getApplicationContext<Context>()

    @Test
    fun `should initialize`() {
        EventLogger.initialize(context) // do nothing as of now
    }

    @Test
    fun `should log critical event`() {
        EventLogger.critical(
            "inappmessaging",
            "1.0.0",
            "500",
            "server error"
        ) // do nothing as of now
    }

    @Test
    fun `should log warning event`() {
        EventLogger.warning(
            "inappmessaging",
            "1.0.0",
            "500",
            "server error"
        ) // do nothing as of now
    }
}
