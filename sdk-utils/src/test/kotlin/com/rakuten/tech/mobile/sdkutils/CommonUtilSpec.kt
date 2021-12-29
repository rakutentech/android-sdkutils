package com.rakuten.tech.mobile.sdkutils

import android.app.Activity
import android.content.Context
import android.content.Intent
import androidx.test.core.app.ApplicationProvider
import com.rakuten.tech.mobile.sdkutils.CommonUtil.checkPlayServices
import com.rakuten.tech.mobile.sdkutils.CommonUtil.getAssetsFilePath
import com.rakuten.tech.mobile.sdkutils.CommonUtil.getColorValue
import com.rakuten.tech.mobile.sdkutils.CommonUtil.getDrawableResourceId
import com.rakuten.tech.mobile.sdkutils.CommonUtil.getMD5HashData
import com.rakuten.tech.mobile.sdkutils.CommonUtil.getRawResourceId
import com.rakuten.tech.mobile.sdkutils.CommonUtil.getSha256HashData
import com.rakuten.tech.mobile.sdkutils.CommonUtil.isDarkMode
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
        val assetPath: String? =
            ApplicationProvider.getApplicationContext<Context>().getAssetsFilePath(
                "sample_assets_image.jpg"
            )
        assetPath shouldNotBe null
    }

    @Test
    fun `should get resource id for raw folder`() {
        val resourceId = ApplicationProvider.getApplicationContext<Context>().getRawResourceId(
            "sample_raw_image"
        )
        resourceId shouldNotBe 0
    }

    @Test
    fun `should get resource id for drawable folder`() {
        val resourceId = ApplicationProvider.getApplicationContext<Context>().getDrawableResourceId(
            "sample_drawable_image"
        )
        resourceId shouldNotBe 0
    }

    @Test
    fun `should get color from valid color string`() {
        "#FF0000".getColorValue() shouldBeEqualTo -65536
    }

    @Test
    fun `should not get color from invalid color string`() {
        "ABC".getColorValue() shouldBeEqualTo -1
    }

    @Test
    fun `should get dark mode UI`() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        context.resources.configuration.uiMode = 32
        context.isDarkMode() shouldBeEqualTo true
    }

    @Test
    fun `should get light mode UI`() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        context.resources.configuration.uiMode = 16
        context.isDarkMode() shouldBeEqualTo false
    }

    @Test
    fun `should get undefined mode UI`() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        context.resources.configuration.uiMode = 8
        context.isDarkMode() shouldBeEqualTo false
    }

    @Test
    fun `should check play services`() {
        Robolectric.buildActivity(Activity::class.java, Intent()).get()
            .checkPlayServices() shouldBeEqualTo false
    }

    @Test
    fun `should get Sha256 hash data`() {
        "Test".getSha256HashData() shouldBeEqualTo "532eaabd9574880dbf76b9b8cc00832c20a6ec113d682299550d7a6e0f345e25"
    }

    @Test
    fun `should get MD5 hash data`() {
        "Test".getMD5HashData() shouldBeEqualTo "0cbc6611f5540bd0809a388dc95a615b"
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