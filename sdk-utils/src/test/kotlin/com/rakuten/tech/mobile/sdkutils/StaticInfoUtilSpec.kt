package com.rakuten.tech.mobile.sdkutils

import android.content.Context
import android.content.res.Resources
import android.os.Build
import org.amshove.kluent.*
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.Mockito
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import org.robolectric.shadows.ShadowBuild

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [Build.VERSION_CODES.O_MR1], shadows = [ShadowBuild::class])
class StaticInfoUtilSpec {
    private val stubContext = Mockito.mock(Context::class.java)
    private val stubResources = Mockito.mock(Resources::class.java)

    @Before
    fun setUp() {
        Mockito.`when`(stubContext.packageName).thenReturn("")
        Mockito.`when`(stubContext.resources).thenReturn(stubResources)
    }

    @Test
    fun `should return the system properties`() {
        ShadowBuild.setVersionRelease("TestReleaseVersion")
        ShadowBuild.setModel("TestModel")
        ShadowBuild.setId("TestId")
        ShadowBuild.setManufacturer("TestManufacturer")
        ShadowBuild.setDevice("TestDevice")
        ShadowBuild.setFingerprint("TestFingerprint")
        val systemProperties = StaticInfoUtil.getSystemProperties()
        systemProperties.toString() shouldContainAll System.getProperties().stringPropertyNames()
    }

    @Test
    fun `should return the packages`() {
        val packages = StaticInfoUtil.getPackages()
        packages.size shouldNotBe 0
    }

    @Test
    fun `should return the build map`() {
        ShadowBuild.setModel("TestModel")
        ShadowBuild.setManufacturer("TestManufacturer")
        ShadowBuild.setDevice("TestDevice")
        ShadowBuild.setFingerprint("TestFingerprint")

        val buildMap = StaticInfoUtil.getBuildMap()
        buildMap.toString() shouldContain "TestModel"
        buildMap.toString() shouldContain "TestManufacturer"
        buildMap.toString() shouldContain "TestDevice"
        buildMap.toString() shouldContain "TestModel"
    }

    @Test
    fun `should return the app info in get app info`() {
        ShadowBuild.setVersionRelease("TestReleaseVersion")
        ShadowBuild.setModel("TestModel")
        ShadowBuild.setId("TestId")
        ShadowBuild.setManufacturer("TestManufacturer")
        ShadowBuild.setDevice("TestDevice")
        ShadowBuild.setFingerprint("TestFingerprint")
        val appInfo = StaticInfoUtil.getAppInfo()
        appInfo shouldContainAll listOf(
            "TestReleaseVersion",
            "TestModel",
            "TestId",
            "TestManufacturer", "TestDevice", "TestFingerprint"
        )
        appInfo shouldContainAll System.getProperties().stringPropertyNames()
    }
}
