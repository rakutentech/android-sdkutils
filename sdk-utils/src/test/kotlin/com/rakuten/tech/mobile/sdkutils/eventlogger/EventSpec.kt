package com.rakuten.tech.mobile.sdkutils.eventlogger

import com.google.gson.Gson
import org.amshove.kluent.*
import org.junit.Test

class EventSpec {

    private val testEvent = Event(
        "0",
        "sdkutils.sample",
        "sdkutils.sample",
        "1.0.0",
        "Android 10",
        "Galaxy",
        "Samsung",
        "S2",
        "inappmessaging",
        "1.0.0",
        "500",
        "server error",
        "push: 1.0.0",
        2,
        1699254206421
    )

    @Test
    @SuppressWarnings("LongMethod")
    fun `should correctly map mutable and automatic fields`() {
        val testEvent = testEvent.copy(
            occurrenceCount = 5,
            firstOccurrenceMillis = 1699254206421
        )

        testEvent.run {
            eventType shouldBeEqualTo "0"
            appId shouldBeEqualTo "sdkutils.sample"
            appName shouldBeEqualTo "sdkutils.sample"
            appVer shouldBeEqualTo "1.0.0"
            osVer shouldBeEqualTo "Android 10"
            deviceModel shouldBeEqualTo "Galaxy"
            deviceBrand shouldBeEqualTo "Samsung"
            deviceName shouldBeEqualTo "S2"
            sdkName shouldBeEqualTo "inappmessaging"
            sdkVer shouldBeEqualTo "1.0.0"
            errorCode shouldBeEqualTo "500"
            errorMsg shouldBeEqualTo "server error"
            rmcSdks shouldBeEqualTo "push: 1.0.0"
            occurrenceCount shouldBeEqualTo 5
            firstOccurrenceMillis shouldBeEqualTo 1699254206421
            eventVer shouldBeEqualTo "1"
            platform shouldBeEqualTo "Android"
        }
    }

    @Test
    fun `should serialize Event with correct json field names`() {
        Gson().toJson(testEvent) shouldContainAll listOf(
            "eventVer",
            "eventType",
            "appId",
            "appName",
            "appVer",
            "platform",
            "osVer",
            "deviceModel",
            "deviceBrand",
            "deviceName",
            "sdkName",
            "sdkVer",
            "errorCode",
            "errorMsg",
            "rmcSdks"
        )
    }

    @Test
    fun `should not serialize null values`() {
        val testEvent = testEvent.copy(
            rmcSdks = null
        )
        Gson().toJson(testEvent) shouldNotContain "rmcSdks"
    }

    @Test
    fun `should automatically set platform value`() {
        testEvent.platform shouldBe Platform.ANDROID.displayName
    }

    @Test
    fun `should automatically set eventVer value`() {
        testEvent.eventVer shouldNotBe null
    }

    @Test
    fun `Event occurrenceCount should be mutable`() {
        val testEvent = testEvent.copy(
            occurrenceCount = 5
        )
        testEvent.occurrenceCount shouldBeEqualTo 5
    }

    @Test
    fun `Event firstOccurrenceMillis should be mutable`() {
        val currMillis = System.currentTimeMillis()
        val testEvent = testEvent.copy(
            firstOccurrenceMillis = currMillis
        )
        testEvent.firstOccurrenceMillis shouldBeEqualTo currMillis
    }

    @Test
    fun `should set correct critical event type from enum`() {
        EventType.CRITICAL.displayName shouldBeEqualTo "0"
    }

    @Test
    fun `should set correct warning event type from enum`() {
        EventType.WARNING.displayName shouldBeEqualTo "1"
    }

    @Test
    fun `should set correct platform from enum`() {
        Platform.ANDROID.displayName shouldBeEqualTo "Android"
    }
}
