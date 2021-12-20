package com.rakuten.tech.mobile.sdkutils

import android.content.Context
import android.content.res.Resources
import android.os.Build
import com.nhaarman.mockitokotlin2.any
import com.nhaarman.mockitokotlin2.eq
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
    private var mockResourceId = 0

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

    @Test
    fun `should return the sdk module versions in get sdk info`() {
        stubSdkModuleVersions()
        val sdkInfo = StaticInfoUtil.getSdkInfo(stubContext)
        sdkInfo shouldContainAll listOf(
            "analytics/1.0",
            "analytics_core/1.1",
            "analytics_rat/1.2",
            "analytics_idtoken/1.3",
            "discover/1.9",
            "feedback/1.10",
            "ping/1.12",
            "push/1.14",
            "inappmessaging/4.0.0"
        )
        sdkInfo shouldNotContainAll listOf(
            "core/1.3",
            "raeengine/1.4",
            "raeidinformation/1.5",
            "raemenberinformation/1.6",
            "raepnp/1.7",
            "deviceinformation/1.8",
            "mock/1.11",
            "points/1.13",
            "user/1.15",
            "contentpush/1.16",
            "stitch/1.17"
        )
    }

    @Test
    fun `should not return the sdk module versions if they do not exist in get sdk info`() {
        val sdkInfo = StaticInfoUtil.getSdkInfo(stubContext)
        sdkInfo shouldNotContainAll listOf(
            "analytics/",
            "analytics_core/",
            "analytics_rat/",
            "core/1",
            "raeengine/",
            "raeidinformation/",
            "raemenberinformation/",
            "raepnp/",
            "deviceinformation/",
            "discover/",
            "feedback/",
            "mock/",
            "ping/",
            "points/",
            "push/",
            "user/",
            "contentpush/",
            "stitch/"
        )
    }

    @Test
    fun getSdkInfoShouldReturnOnlyTheSdkVersionWhenItExists() {
        stubSdkModuleVersions()
        stubStringResource("sdk__version", "1.0")
        val sdkInfo = StaticInfoUtil.getSdkInfo(stubContext)
        sdkInfo shouldBeEqualTo "sdk/1.0"
    }

    @Test
    fun `get sdk info json string should return the sdk module versions as json string`() {
        stubSdkModuleVersions()
        val sdkInfo = StaticInfoUtil.getSdkInfoMap(stubContext!!)
        sdkInfo["analytics"] shouldBeEqualTo "1.0"
        sdkInfo["analytics_core"] shouldBeEqualTo "1.1"
        sdkInfo["analytics_rat"] shouldBeEqualTo "1.2"
        sdkInfo["analytics_idtoken"] shouldBeEqualTo "1.3"
        sdkInfo["core"].shouldBeNull()
        sdkInfo["raeengine"].shouldBeNull()
        sdkInfo["raeidinformation"].shouldBeNull()
        sdkInfo["raemenberinformation"].shouldBeNull()
        sdkInfo["raepnp"].shouldBeNull()
        sdkInfo["deviceinformation"].shouldBeNull()
        sdkInfo["discover"] shouldBeEqualTo "1.9"
        sdkInfo["feedback"] shouldBeEqualTo "1.10"
        sdkInfo["ping"] shouldBeEqualTo "1.12"
        sdkInfo["push"] shouldBeEqualTo "1.14"
        sdkInfo["inappmessaging"] shouldBeEqualTo "4.0.0"
    }

    @Test
    fun `should not return the sdk module versions if dne in get sdk info json string`() {
        val sdkInfo = StaticInfoUtil.getSdkInfoMap(stubContext!!)
        sdkInfo.keys shouldNotContainAny listOf(
            "\"analytics\"",
            "\"analytics_core\"",
            "\"analytics_rat\"",
            "\"core\"",
            "\"raeengine\"",
            "\"raeidinformation\"",
            "\"raemenberinformation\"",
            "\"raepnp\"",
            "\"deviceinformation\"",
            "\"discover\"",
            "\"feedback\"",
            "\"mock\"",
            "\"ping\"",
            "\"points\"",
            "\"push\"",
            "\"user\"",
            "\"contentpush\"",
            "\"stitch\""
        )
    }

    private fun stubSdkModuleVersions() {
        stubStringResource("analytics__version", "1.0")
        stubStringResource("analytics_core__version", "1.1")
        stubStringResource("analytics_rat__version", "1.2")
        stubStringResource("analytics_idtoken__version", "1.3")
        stubStringResource("core__version", "1.3")
        stubStringResource("raeengine__version", "1.4")
        stubStringResource("raeidinformation__version", "1.5")
        stubStringResource("raemenberinformation__version", "1.6")
        stubStringResource("raepnp__version", "1.7")
        stubStringResource("deviceinformation__version", "1.8")
        stubStringResource("discover__version", "1.9")
        stubStringResource("feedback__version", "1.10")
        stubStringResource("mock__version", "1.11")
        stubStringResource("ping__version", "1.12")
        stubStringResource("points__version", "1.13")
        stubStringResource("push__version", "1.14")
        stubStringResource("user__version", "1.15")
        stubStringResource("contentpush__version", "1.16")
        stubStringResource("stitch__version", "1.17")
        stubStringResource("inappmessaging__version", "4.0.0")
    }

    private fun stubStringResource(name: String, version: String) {
        mockResourceId++
        Mockito.`when`(stubResources.getIdentifier(eq(name), any(), any()))
            .thenReturn(mockResourceId)
        Mockito.`when`(stubContext.getString(mockResourceId)).thenReturn(version)
    }
}
