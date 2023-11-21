package com.rakuten.tech.mobile.sdkutils.eventlogger

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import kotlin.random.Random

internal object EventLoggerTestUtil {

    private val context: Context = ApplicationProvider.getApplicationContext()

    fun generateRandomEvent(): Event {
        val randomText = System.currentTimeMillis().toString()
        return EventBuilder(context)
            .buildEvent(
                EventType.values()[Random.nextInt(EventType.values().size)],
                randomText,
                randomText,
                randomText,
                randomText
            )
    }
}
