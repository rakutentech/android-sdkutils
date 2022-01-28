package com.rakuten.tech.mobile.sdkutils

import android.annotation.SuppressLint
import android.os.Build
import java.text.DateFormat
import java.text.SimpleDateFormat
import java.util.*

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

    /**
     * @return 1/8th of the given memory in byte.
     */
    fun getMemoryCacheSize(memory: Int = getMaxMemory()): Int = if (memory > 0) memory / MEM_FRACTION else 1

    /**
     * @return the available VM memory in byte.
     * Get max available VM memory, exceeding this amount will throw an
     * OutOfMemory exception.
     */
    private fun getMaxMemory() = Runtime.getRuntime().maxMemory().toInt()

    private const val MEM_FRACTION = 8
}