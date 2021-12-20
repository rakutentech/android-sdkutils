package com.rakuten.tech.mobile.sdkutils

import android.content.Context

/**
 * Helper class to get/set values in shared preferences.
 * Should be accessed via [PreferencesUtil.getInstance].
 */
@SuppressWarnings("TooManyFunctions")
class PreferencesUtil private constructor(context: Context, name: String) {
    private val prefs = context.getSharedPreferences(name, Context.MODE_PRIVATE)

    /**
     * Get integer value from shared preferences.
     *
     * @param key the preference key
     * @return return the integer value
     */
    fun getInt(key: String) = prefs.getInt(key, -1)

    /**
     * Get long value from shared preferences.
     *
     * @param key the preference key
     * @return return the long value
     */
    fun getLong(key: String) = prefs.getLong(key, -1)

    /**
     * Get floating value from shared preferences.
     *
     * @param key the preference key
     * @return return the floating value
     */
    fun getFloat(key: String) = prefs.getFloat(key, -1.0f)

    /**
     * Get boolean value from shared preferences.
     *
     * @param key the preference key
     * @return return the boolean value
     */
    fun getBoolean(key: String) = prefs.getBoolean(key, false)

    /**
     * Get string value from shared preferences.
     *
     * @param key the preference key
     * @return return the string value
     */
    fun getString(key: String) = prefs.getString(key, null)

    /**
     * Get string set values from shared preferences.
     *
     * @param key the preference key
     * @return return the string values
     */
    fun getStringSet(key: String): MutableSet<String>? = prefs.getStringSet(key, null)

    /**
     * Put integer value to shared preferences.
     *
     * @param key the preference key
     * @param value the integer value
     */
    fun putInt(key: String, value: Int) {
        prefs.edit().putInt(key, value).apply()
    }

    /**
     * Put long value to shared preferences.
     *
     * @param key the preference key
     * @param value the long value
     */
    fun putLong(key: String, value: Long) {
        prefs.edit().putLong(key, value).apply()
    }

    /**
     * Put floating value to shared preferences.
     *
     * @param key the preference key
     * @param value the floating value
     */
    fun putFloat(key: String, value: Float) {
        prefs.edit().putFloat(key, value).apply()
    }

    /**
     * Put boolean value to shared preferences.
     *
     * @param key the preference key
     * @param value the boolean value
     */
    fun putBoolean(key: String, value: Boolean) {
        prefs.edit().putBoolean(key, value).apply()
    }

    /**
     * Put string value to shared preferences.
     *
     * @param key the preference key
     * @param value the string value
     */
    fun putString(key: String, value: String) {
        prefs.edit().putString(key, value).apply()
    }

    /**
     * Put string set value to shared preferences.
     *
     * @param key the preference key
     * @param values the string set value
     */
    fun putStringSet(key: String, values: Set<String>) {
        prefs.edit().putStringSet(key, values).apply()
    }

    /**
     * Clear data from shared preferences.
     *
     * @param key the preference key
     */
    /** Clear data from shared preferences. */
    fun clear(key: String) {
        prefs.edit().remove(key).apply()
    }

    companion object {
        private var instance: PreferencesUtil? = null

        /**
         * Get the preferences instance to cache and retrieve values.
         *
         * @param context the context
         * @param name the shared file name
         */
        fun getInstance(context: Context, name: String): PreferencesUtil {
            if (instance == null) {
                instance = PreferencesUtil(context, name)
            }
            return instance as PreferencesUtil
        }
    }
}
