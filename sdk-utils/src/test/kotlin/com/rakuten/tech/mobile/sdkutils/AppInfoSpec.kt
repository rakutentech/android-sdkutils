package com.rakuten.tech.mobile.sdkutils

import android.content.Context
import android.content.pm.PackageInfo
import android.content.pm.PackageManager
import com.nhaarman.mockitokotlin2.any
import com.nhaarman.mockitokotlin2.mock
import org.amshove.kluent.*
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.ArgumentMatchers
import org.mockito.Mockito
import org.mockito.Mockito.`when`
import org.robolectric.RobolectricTestRunner

@RunWith(RobolectricTestRunner::class)
class AppInfoSpec {

    private val mockContext: Context = mock()
    private val mockPackageInfo: PackageInfo = mock()

    @Before
    fun setup() {
        val mockPackageManager: PackageManager = mock()
        `when`(mockContext.packageManager).thenAnswer { mockPackageManager }
        `when`(mockPackageManager.getPackageInfo(ArgumentMatchers.anyString(), any()))
            .thenAnswer { mockPackageInfo }
        `when`(mockContext.packageName).thenAnswer { "com.test.application.name" }
        mockPackageInfo.versionName = "1.0.0"

        AppInfo.init(mockContext)
    }

    @Test
    fun `should return App Name`() {
        `when`(mockContext.packageName).thenAnswer { "com.test.application.name" }

        AppInfo.instance.name shouldBeEqualTo "com.test.application.name"
    }

    @Test
    fun `should return App Version`() {
        mockPackageInfo.versionName = "1.0.0"

        AppInfo.instance.version shouldBeEqualTo "1.0.0"
    }

    @Test
    fun `should return null App Version`() {
        `when`(mockContext.packageManager.getPackageInfo(Mockito.anyString(), Mockito.anyInt()))
            .thenThrow(PackageManager.NameNotFoundException::class.java)
        AppInfo.init(mockContext)
        AppInfo.instance.version shouldBeEqualTo null
    }
}
