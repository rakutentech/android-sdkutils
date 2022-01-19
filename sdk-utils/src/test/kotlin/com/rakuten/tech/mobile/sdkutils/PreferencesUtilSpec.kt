package com.rakuten.tech.mobile.sdkutils

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import org.amshove.kluent.shouldBeEqualTo
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner

@RunWith(RobolectricTestRunner::class)
class PreferencesUtilSpec {
    private val context: Context = ApplicationProvider.getApplicationContext<Context>()
    private val sharedName = "com.test.application.name.shared"

    @Test
    fun `should put int value to shared preferences`() {
        PreferencesUtil.putInt(context, sharedName, "INT", 100)
        PreferencesUtil.getInt(context, sharedName, "INT", 3) shouldBeEqualTo 100
    }

    @Test
    fun `should get default int value from shared preferences`() {
        PreferencesUtil.getInt(context, sharedName, "INT", 3) shouldBeEqualTo 3
    }

    @Test
    fun `should put long value to shared preferences`() {
        PreferencesUtil.putLong(context, sharedName, "LONG", 100000000)
        PreferencesUtil.getLong(context, sharedName, "LONG") shouldBeEqualTo 100000000
    }

    @Test
    fun `should get default long value from shared preferences`() {
        PreferencesUtil.getLong(context, sharedName, "LONG", 2000000) shouldBeEqualTo 2000000
    }

    @Test
    fun `should put boolean value to shared preferences`() {
        PreferencesUtil.putBoolean(context, sharedName, "BOOLEAN", true)
        PreferencesUtil.getBoolean(context, sharedName, "BOOLEAN") shouldBeEqualTo true
    }

    @Test
    fun `should get default boolean value from shared preferences`() {
        PreferencesUtil.getBoolean(context, sharedName, "BOOLEAN", true) shouldBeEqualTo true
    }

    @Test
    fun `should put floating value to shared preferences`() {
        PreferencesUtil.putFloat(context, sharedName, "FLOAT", 100.0f)
        PreferencesUtil.getFloat(context, sharedName, "FLOAT") shouldBeEqualTo 100.0f
    }

    @Test
    fun `should get default floating value from shared preferences`() {
        PreferencesUtil.getFloat(context, sharedName, "FLOAT", 500.0f) shouldBeEqualTo 500.0f
    }

    @Test
    fun `should put string value to shared preferences`() {
        PreferencesUtil.putString(context, sharedName, "STRING", "TestString")
        PreferencesUtil.getString(context, sharedName, "STRING", null) shouldBeEqualTo "TestString"
    }

    @Test
    fun `should get default string value from shared preferences`() {
        PreferencesUtil.getString(
            context,
            sharedName,
            "STRING",
            "Default"
        ) shouldBeEqualTo "Default"
    }

    @Test
    fun `should get null as default string value from shared preferences`() {
        PreferencesUtil.getString(context, sharedName, "STRING", null) shouldBeEqualTo null
    }

    @Test
    fun `should put string set value to shared preferences`() {
        val stringSet: MutableSet<String> = HashSet()
        stringSet.add("TestString")
        PreferencesUtil.putStringSet(context, sharedName, "STRING_SET", stringSet)
        PreferencesUtil.getStringSet(context, sharedName, "STRING_SET", null)
            .toString() shouldBeEqualTo "[TestString]"
    }

    @Test
    fun `should get default string set value from shared preferences`() {
        val stringSet: MutableSet<String> = HashSet()
        stringSet.add("TestString")
        PreferencesUtil.getStringSet(context, sharedName, "STRING_SET", stringSet)
            .toString() shouldBeEqualTo "[TestString]"
    }

    @Test
    fun `should get null as default string set value from shared preferences`() {
        PreferencesUtil.getStringSet(context, sharedName, "STRING_SET", null)
            .toString() shouldBeEqualTo "null"
    }

    @Test
    fun `should remove value from shared preferences`() {
        PreferencesUtil.putString(context, sharedName, "STRING", "TestString")
        PreferencesUtil.remove(context, sharedName, "STRING")
        PreferencesUtil.getString(context, sharedName, "STRING", null) shouldBeEqualTo null
    }

    @Test
    fun `should clear value from shared preferences`() {
        PreferencesUtil.putInt(context, sharedName, "INT", 100)
        PreferencesUtil.putLong(context, sharedName, "LONG", 100000000)
        PreferencesUtil.putBoolean(context, sharedName, "BOOLEAN", true)
        PreferencesUtil.putFloat(context, sharedName, "FLOAT", 100.0f)
        PreferencesUtil.putString(context, sharedName, "STRING", "TestString")
        PreferencesUtil.clear(context, sharedName)
        PreferencesUtil.getString(context, sharedName, "STRING", null) shouldBeEqualTo null
    }

    @Test
    fun `should check the key used in shared preferences`() {
        PreferencesUtil.putInt(context, sharedName, "INT", 100)
        PreferencesUtil.contains(context, sharedName, "INT") shouldBeEqualTo true
    }

    @Test
    fun `should check the key if not used in shared preferences`() {
        PreferencesUtil.putInt(context, sharedName, "INT", 100)
        PreferencesUtil.contains(context, sharedName, "STRING") shouldBeEqualTo false
    }
}
