package com.rakuten.tech.mobile.sdkutils

import android.content.Context
import android.content.pm.ApplicationInfo
import android.content.pm.PackageInfo
import android.content.pm.PackageManager
import com.nhaarman.mockitokotlin2.any
import com.nhaarman.mockitokotlin2.mock
import org.amshove.kluent.*
import org.junit.Before
import org.junit.Test
import org.mockito.ArgumentMatchers
import org.mockito.Mockito.`when`

class AppInfoSpec {

    private val mockContext: Context = mock()
    private val mockPackageInfo: PackageInfo = mock()
    private val mockApplicationInfo: ApplicationInfo = mock()

    @Before
    fun setup() {
        val mockPackageManager: PackageManager = mock()
        `when`(mockContext.packageManager).thenReturn(mockPackageManager)
        `when`(mockPackageManager.getPackageInfo(ArgumentMatchers.anyString(), any())).thenReturn(mockPackageInfo)
        `when`(mockContext.packageName).thenReturn("com.test.application.name")
        `when`(mockContext.applicationInfo).thenReturn(mockApplicationInfo)
        `when`(mockContext.applicationInfo.loadLabel(mockContext.packageManager)).thenReturn("TestApp")
        mockPackageInfo.versionName = "1.0.0"

        AppInfo.init(mockContext)
    }

    @Test
    fun `should return Package Name`() {
        `when`(mockContext.packageName).thenReturn("com.test.application.name")

        AppInfo.instance.packageName shouldBeEqualTo "com.test.application.name"
    }

    @Test
    fun `should return App Version`() {
        mockPackageInfo.versionName = "1.0.0"

        AppInfo.instance.version shouldBeEqualTo "1.0.0"
    }

    @Test
    fun `should return App Name`() {
        `when`(mockApplicationInfo.loadLabel(mock())).thenReturn("TestApp")
        AppInfo.instance.appName shouldBeEqualTo "TestApp"
    }
}
