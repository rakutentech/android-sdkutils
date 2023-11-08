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

@RunWith(RobolectricTestRunner::class)
class EventBuilderSpec {

    private val mockContext = mock(Context::class.java)
    private val mockPm = mock(PackageManager::class.java)
    private val eventBuilder: EventBuilder

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

        eventBuilder = EventBuilder(mockContext)
    }

    @Test
    fun `should build critical event with metadata`() {
        val event = eventBuilder.buildEvent(
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
            appVer shouldBeEqualTo "1.0.0"
            rmcSdks shouldBeEqualTo """{"rmc_inappmessaging":"2.0.0"}"""
            osVer shouldBeEqualTo "Android ${Build.VERSION.RELEASE}"
            deviceModel shouldBeEqualTo Build.MODEL
            deviceBrand shouldBeEqualTo Build.MANUFACTURER
        }
    }

    @Test
    fun `should build warning event with metadata`() {
        val event = eventBuilder.buildEvent(
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
            appVer shouldBeEqualTo "1.0.0"
            rmcSdks shouldBeEqualTo """{"rmc_inappmessaging":"2.0.0"}"""
            osVer shouldBeEqualTo "Android ${Build.VERSION.RELEASE}"
            deviceModel shouldBeEqualTo Build.MODEL
            deviceBrand shouldBeEqualTo Build.MANUFACTURER
        }
    }

    @Test
    fun `should set appVer to empty if exception is encountered`() {
        `when`(mockPm.getPackageInfo(mockContext.packageName, 0))
            .thenThrow(PackageManager.NameNotFoundException())

        val eventBuilder = EventBuilder(mockContext)
        val event = eventBuilder.buildEvent(
            EventType.CRITICAL,
            "push",
            "3.0.0",
            "400",
            "invalid key",
        )

        event.appVer shouldBeEqualTo ""
    }

    @Test
    fun `should set rmcSdks to null if not exists`() {
        `when`(mockContext.getString(anyInt()))
            .thenAnswer { throw Exception() }

        val eventBuilder = EventBuilder(mockContext)
        val event = eventBuilder.buildEvent(
            EventType.CRITICAL,
            "push",
            "3.0.0",
            "400",
            "invalid key",
        )

        event.rmcSdks shouldBeEqualTo null
    }
}
