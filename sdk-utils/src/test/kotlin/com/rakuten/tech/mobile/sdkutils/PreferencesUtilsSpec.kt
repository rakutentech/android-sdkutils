package com.rakuten.tech.mobile.sdkutils

import androidx.test.core.app.ApplicationProvider
import org.amshove.kluent.shouldBeEqualTo
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner

@RunWith(RobolectricTestRunner::class)
class PreferencesUtilsSpec {
    private val prefs = PreferencesUtils.getInstance(
        ApplicationProvider.getApplicationContext(),
        "com.test.application.name.shared"
    )

    @Test
    fun `should put int value to shared preferences`() {
        prefs.putInt("INT", 100)

        prefs.getInt("INT") shouldBeEqualTo 100
    }

    @Test
    fun `should put long value to shared preferences`() {
        prefs.putLong("LONG", 100000000)

        prefs.getLong("LONG") shouldBeEqualTo 100000000
    }

    @Test
    fun `should put boolean value to shared preferences`() {
        prefs.putBoolean("BOOLEAN", true)

        prefs.getBoolean("BOOLEAN") shouldBeEqualTo true
    }

    @Test
    fun `should put floating value to shared preferences`() {
        prefs.putFloat("FLOAT", 100.0f)

        prefs.getFloat("FLOAT") shouldBeEqualTo 100.0f
    }

    @Test
    fun `should put string value to shared preferences`() {
        prefs.putString("STRING", "TestString")

        prefs.getString("STRING") shouldBeEqualTo "TestString"
    }

    @Test
    fun `should put string set value to shared preferences`() {
        val stringSet: MutableSet<String> = HashSet()
        stringSet.add("TestString")
        prefs.putStringSet("STRING_SET", stringSet)

        prefs.getStringSet("STRING_SET").toString() shouldBeEqualTo "[TestString]"
    }

    @Test
    fun `should clear value from shared preferences`() {
        prefs.putString("STRING", "TestString")
        prefs.clear("STRING")

        prefs.getString("STRING") shouldBeEqualTo null
    }
}