package com.rakuten.tech.mobile.sdkutils

import android.content.Context
import android.content.SharedPreferences

/**
 * Helper class to get/set values in shared preferences.
 */
object PreferencesUtil {
    /**
     * Get integer value from shared preferences.
     *
     * @param context the application context
     * @param name the name of the shared file
     * @param key the preference key
     * @param defValue the default value
     * @return return the integer value
     */
    fun getInt(context: Context, name: String, key: String, defValue: Int = -1) =
        getSharedPreferences(context, name).getInt(key, defValue)

    /**
     * Get long value from shared preferences.
     *
     * @param context the application context
     * @param name the name of the shared file
     * @param key the preference key
     * @param defValue the default value
     * @return return the long value
     */
    fun getLong(context: Context, name: String, key: String, defValue: Long = -1) =
        getSharedPreferences(context, name).getLong(key, defValue)

    /**
     * Get floating value from shared preferences.
     *
     * @param context the application context
     * @param name the name of the shared file
     * @param key the preference key
     * @param defValue the default value
     * @return return the floating value
     */
    fun getFloat(context: Context, name: String, key: String, defValue: Float = -1.0f) =
        getSharedPreferences(context, name).getFloat(key, defValue)

    /**
     * Get boolean value from shared preferences.
     *
     * @param context the application context
     * @param name the name of the shared file
     * @param key the preference key
     * @param defValue the default value
     * @return return the boolean value
     */
    fun getBoolean(context: Context, name: String, key: String, defValue: Boolean = false) =
        getSharedPreferences(context, name).getBoolean(key, defValue)

    /**
     * Get string value from shared preferences.
     *
     * @param context the application context
     * @param name the name of the shared file
     * @param key the preference key
     * @param defValue the default value
     * @return return the string value
     */
    fun getString(context: Context, name: String, key: String, defValue: String?) =
        getSharedPreferences(context, name).getString(key, defValue)

    /**
     * Get string set values from shared preferences.
     *
     * @param context the application context
     * @param name the name of the shared file
     * @param key the preference key
     * @param defValue the default value
     * @return return the string values
     */
    fun getStringSet(
        context: Context,
        name: String,
        key: String,
        defValue: MutableSet<String>?
    ): MutableSet<String>? =
        getSharedPreferences(context, name).getStringSet(key, defValue)

    /**
     * Put integer value to shared preferences.
     *
     * @param context the application context
     * @param name the name of the shared file
     * @param key the preference key
     * @param value the integer value
     */
    fun putInt(context: Context, name: String, key: String, value: Int) {
        getSharedPreferences(context, name).edit().putInt(key, value).apply()
    }

    /**
     * Put long value to shared preferences.
     *
     * @param context the application context
     * @param name the name of the shared file
     * @param key the preference key
     * @param value the long value
     */
    fun putLong(context: Context, name: String, key: String, value: Long) {
        getSharedPreferences(context, name).edit().putLong(key, value).apply()
    }

    /**
     * Put floating value to shared preferences.
     *
     * @param context the application context
     * @param name the name of the shared file
     * @param key the preference key
     * @param value the floating value
     */
    fun putFloat(context: Context, name: String, key: String, value: Float) {
        getSharedPreferences(context, name).edit().putFloat(key, value).apply()
    }

    /**
     * Put boolean value to shared preferences.
     *
     * @param context the application context
     * @param name the name of the shared file
     * @param key the preference key
     * @param value the boolean value
     */
    fun putBoolean(context: Context, name: String, key: String, value: Boolean) {
        getSharedPreferences(context, name).edit().putBoolean(key, value).apply()
    }

    /**
     * Put string value to shared preferences.
     *
     * @param context the application context
     * @param name the name of the shared file
     * @param key the preference key
     * @param value the string value
     */
    fun putString(context: Context, name: String, key: String, value: String) {
        getSharedPreferences(context, name).edit().putString(key, value).apply()
    }

    /**
     * Put string set value to shared preferences.
     *
     * @param context the application context
     * @param name the name of the shared file
     * @param key the preference key
     * @param values the string set value
     */
    fun putStringSet(context: Context, name: String, key: String, values: Set<String>) {
        getSharedPreferences(context, name).edit().putStringSet(key, values).apply()
    }

    /**
     * Remove value based on key from shared preferences.
     *
     * @param context the application context
     * @param name the name of the shared file
     * @param key the preference key
     */
    fun remove(context: Context, name: String, key: String) {
        getSharedPreferences(context, name).edit().remove(key).apply()
    }

    /**
     * Clear all data from shared preferences.
     *
     * @param context the application context
     * @param name the name of the shared file
     */
    fun clear(context: Context, name: String) {
        getSharedPreferences(context, name).edit().clear().apply()
    }

    private fun getSharedPreferences(context: Context, name: String): SharedPreferences {
        return context.getSharedPreferences(name, Context.MODE_PRIVATE)
    }
}
