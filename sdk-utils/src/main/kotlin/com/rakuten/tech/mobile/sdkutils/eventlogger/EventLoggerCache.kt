package com.rakuten.tech.mobile.sdkutils.eventlogger

import android.content.SharedPreferences

/**
 * Interface to access general data or settings that the SDK needs to persist.
 */
internal interface EventLoggerCache {
    /**
     * Retrieves the reference time in checking TTL expiry, or -1 if not exists.
     */
    fun getTtlReferenceTime(): Long

    /**
     * Updates the reference time in checking TTL expiry. Call this whenever all events were successfully sent to
     * server.
     */
    fun setTtlReferenceTime(pushedTimeMs: Long)
}

internal class SharedPreferencesEventLoggerCache(private val sharedPref: SharedPreferences) : EventLoggerCache {

    override fun getTtlReferenceTime(): Long {
        return try {
            sharedPref.getLong(KEY_LAST_PUSHED_TIME, -1)
        } catch (_: Exception) {
            -1
        }
    }

    override fun setTtlReferenceTime(pushedTimeMs: Long) {
        with(sharedPref.edit()) {
            putLong(KEY_LAST_PUSHED_TIME, pushedTimeMs)
            apply()
        }
    }

    companion object {
        private const val KEY_LAST_PUSHED_TIME = "ttl_reference_time"
    }
}
