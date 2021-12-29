package com.rakuten.tech.mobile.sdkutils

import com.rakuten.tech.mobile.sdkutils.StringExtension.getColorValue
import com.rakuten.tech.mobile.sdkutils.StringExtension.getMD5HashData
import com.rakuten.tech.mobile.sdkutils.StringExtension.getSha256HashData
import com.rakuten.tech.mobile.sdkutils.StringExtension.getUTCDate
import org.amshove.kluent.shouldBeEqualTo
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import java.text.DateFormat
import java.text.SimpleDateFormat

@RunWith(RobolectricTestRunner::class)
class StringExtensionSpec {
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
    fun `should get MD5 hash data`() {
        "Test".getMD5HashData() shouldBeEqualTo "0cbc6611f5540bd0809a388dc95a615b"
    }

    @Test
    fun `should get UTC date from a date string`() {
        val dateFormat: DateFormat = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ssX")
        val isoFormattedDate = "2021-01-01T12:00:00Z".getUTCDate()
        isoFormattedDate shouldBeEqualTo dateFormat.parse("2021-01-01T12:00:00Z")
    }
}