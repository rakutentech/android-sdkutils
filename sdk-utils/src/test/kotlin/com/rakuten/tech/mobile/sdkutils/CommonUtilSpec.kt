package com.rakuten.tech.mobile.sdkutils

import android.os.Build
import org.amshove.kluent.*
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import java.text.SimpleDateFormat

@RunWith(RobolectricTestRunner::class)
class CommonUtilSpec {
    @Test
    @Config(sdk = [Build.VERSION_CODES.N])
    fun `should get UTC date format`() {
        val simpleDateFormat = SimpleDateFormat("dd.MM.yyyy HH:mm:ssX")
        val date = simpleDateFormat.parse("02.04.2014 15:00:00+09")
        val isoFormattedDate = CommonUtil.getUTCDateFormat().format(date!!)
        isoFormattedDate shouldBeEqualTo "2014-04-02T06:00:00Z"
    }

    @Test
    @Config(sdk = [Build.VERSION_CODES.M])
    fun `should get UTC date format for android 23`() {
        val simpleDateFormat = SimpleDateFormat("dd.MM.yyyy HH:mm:ssX")
        val date = simpleDateFormat.parse("02.04.2014 15:00:00+09")
        val isoFormattedDate = CommonUtil.getUTCDateFormat().format(date!!)
        isoFormattedDate shouldBeEqualTo "2014-04-02T06:00:00"
    }
}
