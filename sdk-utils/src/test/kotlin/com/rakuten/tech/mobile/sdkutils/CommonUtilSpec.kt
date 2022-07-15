package com.rakuten.tech.mobile.sdkutils

import android.os.Build
import org.amshove.kluent.*
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.util.ReflectionHelpers
import java.text.SimpleDateFormat

@RunWith(RobolectricTestRunner::class)
class CommonUtilSpec {
    @Test
    fun `should get UTC date format`() {
        val simpleDateFormat = SimpleDateFormat("dd.MM.yyyy HH:mm:ss")
        val date = simpleDateFormat.parse("02.04.2014 15:00:00")
        val isoFormattedDate = CommonUtil.getUTCDateFormat().format(date!!)
        isoFormattedDate shouldBeEqualTo "2014-04-02T15:00:00Z"
    }

    @Test
    fun `should get UTC date format for android 23`() {
        ReflectionHelpers.setStaticField(Build.VERSION::class.java, "SDK_INT", 23)
        val simpleDateFormat = SimpleDateFormat("dd.MM.yyyy HH:mm:ss")
        val date = simpleDateFormat.parse("02.04.2014 15:00:00")
        val isoFormattedDate = CommonUtil.getUTCDateFormat().format(date!!)
        isoFormattedDate shouldBeEqualTo "2014-04-02T15:00:00"
    }
}
