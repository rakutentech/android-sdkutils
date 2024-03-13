package com.rakuten.tech.mobile.sdkutils.eventlogger

import android.content.Context
import android.content.SharedPreferences
import androidx.test.core.app.ApplicationProvider
import com.nhaarman.mockitokotlin2.any
import org.amshove.kluent.shouldBeEqualTo
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.Mockito
import org.mockito.Mockito.`when`
import org.robolectric.RobolectricTestRunner

@RunWith(RobolectricTestRunner::class)
class EventLoggerCacheSpec {
    private val context = ApplicationProvider.getApplicationContext<Context>()
    private val sharedPref = context.getSharedPreferences("test-cache", Context.MODE_PRIVATE)
    private val eventLoggerCache = SharedPreferencesEventLoggerCache(sharedPref)

    @Before
    fun setup() {
        clearSharedPrefs()
    }

    @Test
    fun `should set TTL reference time`() {
        eventLoggerCache.setTtlReferenceTime(1234)

        eventLoggerCache.getTtlReferenceTime() shouldBeEqualTo 1234
    }

    @Test
    fun `should return -1 when TTL reference time does not exist`() {
        eventLoggerCache.getTtlReferenceTime() shouldBeEqualTo -1
    }

    @Test
    fun `should return -1 when failed to retrieve TTL reference time`() {
        val mockSharedPrefs = Mockito.mock(SharedPreferences::class.java)
        `when`(mockSharedPrefs.getLong(any(), any()))
            .thenThrow(ClassCastException())

        SharedPreferencesEventLoggerCache(mockSharedPrefs).getTtlReferenceTime() shouldBeEqualTo -1
    }

    private fun clearSharedPrefs() {
        with(sharedPref.edit()) {
            clear()
            commit()
        }
    }
}
