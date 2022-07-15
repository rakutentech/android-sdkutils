package com.rakuten.tech.mobile.sdkutils

import android.os.Build
import com.rakuten.tech.mobile.sdkutils.StringExtension.getColorValue
import com.rakuten.tech.mobile.sdkutils.StringExtension.getMD5HashData
import com.rakuten.tech.mobile.sdkutils.StringExtension.getSha256HashData
import com.rakuten.tech.mobile.sdkutils.StringExtension.getUTCDate
import com.rakuten.tech.mobile.sdkutils.StringExtension.getUTF16ByteArray
import com.rakuten.tech.mobile.sdkutils.StringExtension.getUTF16UrlEncoded
import com.rakuten.tech.mobile.sdkutils.StringExtension.getUTF8UrlEncoded
import org.amshove.kluent.shouldBeEqualTo
import org.junit.After
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.util.ReflectionHelpers
import java.text.DateFormat
import java.text.SimpleDateFormat

@RunWith(RobolectricTestRunner::class)
class StringExtensionSpec {

    @After
    fun tearDown() {
        StringExtension.stringForTest = null
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
    fun `should get Sha256 hash data`() {
        "Test".getSha256HashData() shouldBeEqualTo "532eaabd9574880dbf76b9b8cc00832c20a6ec113d682299550d7a6e0f345e25"
    }

    @Test
    fun `should return null when Sha256 hash data not available`() {
        StringExtension.stringForTest = "anyAlgorithm"
        "Test".getSha256HashData() shouldBeEqualTo null
    }

    @Test
    fun `should get MD5 hash data`() {
        "Test".getMD5HashData() shouldBeEqualTo "0cbc6611f5540bd0809a388dc95a615b"
    }

    @Test
    fun `should return null when MD5 hash data not available`() {
        StringExtension.stringForTest = "anyAlgorithm"
        "Test".getMD5HashData() shouldBeEqualTo null
    }

    @Test
    fun `should get UTC date from a date string`() {
        val dateFormat: DateFormat = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ssX")
        val isoFormattedDate = "2021-01-01T12:00:00Z".getUTCDate()
        isoFormattedDate shouldBeEqualTo dateFormat.parse("2021-01-01T12:00:00Z")
    }

    @Test
    fun `should get UTC date from a date string for android 23`() {
        ReflectionHelpers.setStaticField(Build.VERSION::class.java, "SDK_INT", 23)
        val dateFormat: DateFormat = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ssX")
        val isoFormattedDate = "2021-01-01T12:00:00Z".getUTCDate()
        isoFormattedDate shouldBeEqualTo dateFormat.parse("2021-01-01T12:00:00Z")
    }

    @Test
    fun `should get UTF-16 Byte Array`() {
        "Test".getUTF16ByteArray() shouldBeEqualTo byteArrayOf(-2, -1, 0, 84, 0, 101, 0, 115, 0, 116)
    }

    @Test
    fun `should get UTF-8 URL encoded value`() {
        "this is a test".getUTF8UrlEncoded() shouldBeEqualTo "this+is+a+test"
    }

    @Test
    fun `should return empty when UTF-8 URL encoded value not available`() {
        StringExtension.stringForTest = "anyEncoding"
        "this is a test".getUTF8UrlEncoded() shouldBeEqualTo ""
    }

    @Test
    fun `should get UTF-16 URL encoded value`() {
        "this is a test".getUTF16UrlEncoded() shouldBeEqualTo "this+is+a+test"
    }

    @Test
    fun `should return empty when UTF-16 URL encoded value not available`() {
        StringExtension.stringForTest = "anyEncoding"
        "this is a test".getUTF16UrlEncoded() shouldBeEqualTo ""
    }

    @Test
    fun `should get UTF-8 URL encoded value for empty string`() {
        "".getUTF8UrlEncoded() shouldBeEqualTo ""
    }

    @Test
    fun `should get UTF-16 URL encoded value for empty string`() {
        "".getUTF16UrlEncoded() shouldBeEqualTo ""
    }
}
