package com.rakuten.tech.mobile.sdkutils

import android.app.Activity
import android.content.Context
import android.content.Intent
import androidx.test.core.app.ApplicationProvider
import androidx.test.platform.app.InstrumentationRegistry
import org.amshove.kluent.*
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.Robolectric
import org.robolectric.RobolectricTestRunner
import java.text.DateFormat
import java.text.SimpleDateFormat

@RunWith(RobolectricTestRunner::class)
class CommonUtilSpec {
    @Test
    fun `should get assets file path`() {
        val assetPath: String? = CommonUtil.getAssetsFilePath(
            InstrumentationRegistry.getInstrumentation().targetContext,
            "sample_assets_image.jpg"
        )
        assetPath shouldNotBe null
    }

    @Test
    fun `should get resource id for raw folder`() {
        val resourceId = CommonUtil.getRawResourceId(
            InstrumentationRegistry.getInstrumentation().targetContext,
            "sample_raw_image"
        )
        resourceId shouldNotBe 0
    }

    @Test
    fun `should get resource id for drawable folder`() {
        val resourceId = CommonUtil.getDrawableResourceId(
            InstrumentationRegistry.getInstrumentation().targetContext,
            "sample_drawable_image"
        )
        resourceId shouldNotBe 0
    }

    @Test
    fun `should get color from valid color string`() {
        val colorValue = CommonUtil.getColorValue("#FF0000")
        colorValue shouldBeEqualTo -65536
    }

    @Test
    fun `should not get color from invalid color string`() {
        val colorValue = CommonUtil.getColorValue("ABC")
        colorValue shouldBeEqualTo -1
    }

    @Test
    fun `should get dark mode UI`() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        context.resources.configuration.uiMode = 32
        val isDarkMode = CommonUtil.isDarkMode(context)
        isDarkMode shouldBeEqualTo true
    }

    @Test
    fun `should get light mode UI`() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        context.resources.configuration.uiMode = 16
        val isDarkMode = CommonUtil.isDarkMode(context)
        isDarkMode shouldBeEqualTo false
    }

    @Test
    fun `should get undefined mode UI`() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        context.resources.configuration.uiMode = 8
        val isDarkMode = CommonUtil.isDarkMode(context)
        isDarkMode shouldBeEqualTo false
    }

    @Test
    fun `should check play services`() {
        val isPlayServicesAvailable = CommonUtil.checkPlayServices(
            Robolectric.buildActivity(Activity::class.java, Intent()).get()
        )
        isPlayServicesAvailable shouldBeEqualTo false
    }

    @Test
    fun `should get Sha256 hash data`() {
        val value = CommonUtil.getSha256HashData("Test")
        value shouldBeEqualTo "532eaabd9574880dbf76b9b8cc00832c20a6ec113d682299550d7a6e0f345e25"
    }

    @Test
    fun `should get MD5 hash data`() {
        val value = CommonUtil.getMD5HashData("Test")
        value shouldBeEqualTo "0cbc6611f5540bd0809a388dc95a615b"
    }

    @Test
    fun `should check non null string`() {
        val value = CommonUtil.isNullOrEmpty("Test")
        value shouldBeEqualTo false
    }

    @Test
    fun `should check empty string`() {
        val value = CommonUtil.isNullOrEmpty("")
        value shouldBeEqualTo true
    }

    @Test
    fun `should check null string`() {
        val value = CommonUtil.isNullOrEmpty(null)
        value shouldBeEqualTo true
    }

    @Test
    fun `should check equal string`() {
        val value = CommonUtil.isEqual("Test", "Test")
        value shouldBeEqualTo true
    }

    @Test
    fun `should check non equal string`() {
        val value = CommonUtil.isEqual("Test1", "Test2")
        value shouldBeEqualTo false
    }

    @Test
    fun `should get UTC date format`() {
        val simpleDateFormat = SimpleDateFormat("dd.MM.yyyy HH:mm:ss")
        val date = simpleDateFormat.parse("02.04.2014 15:00:00")
        val isoFormattedDate = CommonUtil.getUTCDateFormat().format(date!!)
        isoFormattedDate shouldBeEqualTo "2014-04-02T15:00:00Z"
    }

    @Test
    fun `should get UTC date from a date string`() {
        val dateFormat: DateFormat = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ssX")
        val isoFormattedDate = CommonUtil.getUTCDate("2021-01-01T12:00:00Z")
        isoFormattedDate shouldBeEqualTo dateFormat.parse("2021-01-01T12:00:00Z")
    }
}