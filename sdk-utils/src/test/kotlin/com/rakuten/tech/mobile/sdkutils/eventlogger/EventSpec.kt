package com.rakuten.tech.mobile.sdkutils.eventlogger

import com.google.gson.Gson
import com.rakuten.tech.mobile.sdkutils.StringExtension
import com.rakuten.tech.mobile.sdkutils.StringExtension.getMD5HashData
import org.amshove.kluent.*
import org.junit.After
import org.junit.Test

class EventSpec {

    @After
    fun tearDown() {
        StringExtension.stringForTest = null
    }

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
        mapOf("rmc_push" to "1.0.0"),
        mapOf("file_name" to "MyFile.kt"),
        2
    )

    @Test
    @SuppressWarnings("LongMethod")
    fun `should correctly map mutable and automatic fields`() {
        val testEvent = testEvent.copy(
            occurrenceCount = 5
        )

        testEvent.run {
            eventType shouldBeEqualTo "0"
            appId shouldBeEqualTo "sdkutils.sample"
            appName shouldBeEqualTo "sdkutils.sample"
            appVersion shouldBeEqualTo "1.0.0"
            osVersion shouldBeEqualTo "Android 10"
            deviceModel shouldBeEqualTo "Galaxy"
            deviceBrand shouldBeEqualTo "Samsung"
            deviceName shouldBeEqualTo "S2"
            sdkName shouldBeEqualTo "inappmessaging"
            sdkVersion shouldBeEqualTo "1.0.0"
            errorCode shouldBeEqualTo "500"
            errorMessage shouldBeEqualTo "server error"
            rmcSdks shouldBeEqualTo mapOf("rmc_push" to "1.0.0")
            info shouldBeEqualTo mapOf("file_name" to "MyFile.kt")
            occurrenceCount shouldBeEqualTo 5
            eventVersion shouldBeEqualTo "1.0"
            platform shouldBeEqualTo "Android"
        }
    }

    @Test
    @SuppressWarnings("LongMethod")
    fun `should serialize Event with correct json field names`() {
        Gson().toJson(testEvent) shouldContainAll listOf(
            "eventVersion",
            "eventType",
            "appId",
            "appName",
            "appVersion",
            "platform",
            "osVersion",
            "deviceModel",
            "deviceBrand",
            "deviceName",
            "sdkName",
            "sdkVersion",
            "errorCode",
            "errorMessage",
            "rmcSdks",
            "info",
            "occurrenceCount",
            "firstOccurrenceOn"
        )
    }

    @Test
    fun `should not serialize null values`() {
        val testEvent = testEvent.copy(
            rmcSdks = null,
            info = null
        )
        Gson().toJson(testEvent) shouldNotContainAll listOf(
            "rmcSdks",
            "info"
        )
    }

    @Test
    fun `should automatically set platform value`() {
        testEvent.platform shouldBe "Android"
    }

    @Test
    fun `should automatically set eventVer value`() {
        testEvent.eventVersion shouldNotBe null
    }

    @Test
    fun `Event occurrenceCount should be mutable`() {
        val testEvent = testEvent.copy(
            occurrenceCount = 5
        )
        testEvent.occurrenceCount shouldBeEqualTo 5
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
    fun `should update type`() {
        val testEvent = testEvent.copy(
            eventType = EventType.CRITICAL.displayName
        )

        testEvent.setType(EventType.WARNING.displayName)
        testEvent.eventType shouldBeEqualTo EventType.WARNING.displayName
    }

    @Test
    fun `should return hash data`() {
        generateEventIdentifier(
            "a",
            "b",
            "c",
            "d",
            "e"
        ) shouldBeEqualTo "abcde".getMD5HashData()
    }

    @Test
    fun `should return original data when hashing fails`() {
        StringExtension.stringForTest = "anyAlgorithm"
        generateEventIdentifier(
            "a",
            "b",
            "c",
            "d",
            "e"
        ) shouldBeEqualTo "abcde"
    }
}
