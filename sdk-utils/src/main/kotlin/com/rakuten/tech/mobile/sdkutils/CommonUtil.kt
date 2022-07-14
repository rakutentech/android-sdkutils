package com.rakuten.tech.mobile.sdkutils

import android.annotation.SuppressLint
import android.os.Build
import java.text.DateFormat
import java.text.SimpleDateFormat
import java.util.TimeZone

@SuppressWarnings("LongParameterList", "UndocumentedPublicClass")
object CommonUtil {
    /**
     * Get the UTC date format from the simple date format, i.e. "yyyy-MM-dd'T'HH:mm:ssX"
     *
     * @return the date format
     */
    @SuppressLint("SimpleDateFormat")
    fun getUTCDateFormat(): DateFormat {
        val dateFormat = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ssX")
        } else {
            SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss")
        }
        dateFormat.timeZone = TimeZone.getTimeZone("UTC")
        return dateFormat
    }
}
