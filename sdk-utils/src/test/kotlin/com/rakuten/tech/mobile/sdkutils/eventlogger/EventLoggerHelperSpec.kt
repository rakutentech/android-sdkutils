package com.rakuten.tech.mobile.sdkutils.eventlogger

import android.content.Context
import android.content.pm.ApplicationInfo
import android.content.pm.PackageInfo
import android.content.pm.PackageManager
import android.content.res.Resources
import android.os.Build
import org.amshove.kluent.shouldBeEqualTo
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.Mockito.*
import org.robolectric.RobolectricTestRunner
import java.lang.ref.WeakReference

@RunWith(RobolectricTestRunner::class)
class EventLoggerHelperSpec {

    private val mockContext = mock(Context::class.java)
    private val mockPm = mock(PackageManager::class.java)
    private val eventLoggerHelper: EventLoggerHelper

    init {
        val mockAppInfo = mock(ApplicationInfo::class.java)
        val mockPackageInfo = mock(PackageInfo::class.java)

        `when`(mockContext.packageName)
            .thenReturn("com.sdkutils")
        `when`(mockContext.packageManager)
            .thenReturn(mockPm)
        `when`(mockPm.getPackageInfo(mockContext.packageName, 0))
            .thenReturn(mockPackageInfo)
        `when`(mockContext.applicationInfo)
            .thenReturn(mockAppInfo)
        `when`(mockAppInfo.loadLabel(mockPm))
            .thenReturn("sdk utils sample app")
        `when`(mockContext.resources)
            .thenReturn(mock(Resources::class.java))
        `when`(mockContext.getString(anyInt()))
            .thenReturn("2.0.0")
        mockPackageInfo.versionName = "1.0.0"

        eventLoggerHelper = EventLoggerHelper(WeakReference(mockContext))
    }

    @Test
    fun `should return metadata`() {
        eventLoggerHelper.getMetadata() shouldBeEqualTo
            EventLoggerHelper.Metadata(
                "com.sdkutils",
                "sdk utils sample app",
                "1.0.0",
                "Android ${Build.VERSION.RELEASE}",
                Build.MODEL,
                Build.MANUFACTURER,
                "",
                mapOf("rmc_inappmessaging" to "2.0.0")
            )
    }

    @Test
    fun `should build critical event with metadata`() {
        val event = eventLoggerHelper.buildEvent(
            EventType.CRITICAL,
            "push",
            "3.0.0",
            "400",
            "invalid key",
        )
        event.run {
            eventType shouldBeEqualTo EventType.CRITICAL.displayName
            appId shouldBeEqualTo "com.sdkutils"
            appName shouldBeEqualTo "sdk utils sample app"
            appVersion shouldBeEqualTo "1.0.0"
            rmcSdks shouldBeEqualTo mapOf("rmc_inappmessaging" to "2.0.0")
            osVersion shouldBeEqualTo "Android ${Build.VERSION.RELEASE}"
            deviceModel shouldBeEqualTo Build.MODEL
            deviceBrand shouldBeEqualTo Build.MANUFACTURER
        }
    }

    @Test
    fun `should build warning event with metadata`() {
        val event = eventLoggerHelper.buildEvent(
            EventType.WARNING,
            "push",
            "3.0.0",
            "400",
            "invalid key",
        )
        event.run {
            eventType shouldBeEqualTo EventType.WARNING.displayName
            appId shouldBeEqualTo "com.sdkutils"
            appName shouldBeEqualTo "sdk utils sample app"
            appVersion shouldBeEqualTo "1.0.0"
            rmcSdks shouldBeEqualTo mapOf("rmc_inappmessaging" to "2.0.0")
            osVersion shouldBeEqualTo "Android ${Build.VERSION.RELEASE}"
            deviceModel shouldBeEqualTo Build.MODEL
            deviceBrand shouldBeEqualTo Build.MANUFACTURER
        }
    }

    @Test
    fun `should set appVer to empty if exception is encountered`() {
        `when`(mockPm.getPackageInfo(mockContext.packageName, 0))
            .thenThrow(PackageManager.NameNotFoundException())

        val eventLoggerHelper = EventLoggerHelper(WeakReference(mockContext))
        val event = eventLoggerHelper.buildEvent(
            EventType.CRITICAL,
            "push",
            "3.0.0",
            "400",
            "invalid key",
        )

        event.appVersion shouldBeEqualTo ""
    }

    @Test
    @SuppressWarnings("TooGenericExceptionThrown")
    fun `should set rmcSdks to null if not exists`() {
        `when`(mockContext.getString(anyInt()))
            .thenAnswer { throw Exception() }

        val eventLoggerHelper = EventLoggerHelper(WeakReference(mockContext))
        val event = eventLoggerHelper.buildEvent(
            EventType.CRITICAL,
            "push",
            "3.0.0",
            "400",
            "invalid key",
        )

        event.rmcSdks shouldBeEqualTo null
    }
}
