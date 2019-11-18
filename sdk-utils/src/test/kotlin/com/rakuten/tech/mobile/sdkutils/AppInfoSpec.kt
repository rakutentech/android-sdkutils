package com.rakuten.tech.mobile.sdkutils

import android.content.Context
import android.content.pm.PackageInfo
import android.content.pm.PackageManager
import com.nhaarman.mockitokotlin2.any
import com.nhaarman.mockitokotlin2.mock
import org.amshove.kluent.*
import org.junit.Before
import org.junit.Test
import org.mockito.ArgumentMatchers

class AppInfoSpec {

    private val mockContext: Context = mock()
    private val mockPackageInfo: PackageInfo = mock()

    @Before
    fun setup() {
        val mockPackageManager: PackageManager = mock()
        When calling mockContext.packageManager itReturns mockPackageManager
        When calling mockPackageManager.getPackageInfo(ArgumentMatchers.anyString(), any()) itReturns mockPackageInfo
        When calling mockContext.packageName itReturns "com.test.application.name"
        mockPackageInfo.versionName = "1.0.0"

        AppInfo.init(mockContext)
    }

    @Test
    fun `should return App Name`() {
        When calling mockContext.packageName itReturns "com.test.application.name"

        AppInfo.instance.name shouldEqual "com.test.application.name"
    }

    @Test
    fun `should return App Version`() {
        mockPackageInfo.versionName = "1.0.0"

        AppInfo.instance.version shouldEqual "1.0.0"
    }
}