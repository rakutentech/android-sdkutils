package com.rakuten.tech.mobile.sdkutils.eventlogger

import com.google.gson.annotations.SerializedName
import com.rakuten.tech.mobile.sdkutils.StringExtension.getMD5HashData

internal data class Event(
    var eventType: String,
    val appId: String,
    val appName: String,
    val appVersion: String,
    val osVersion: String,
    val deviceModel: String,
    val deviceBrand: String,
    val deviceName: String,
    val sdkName: String,
    val sdkVersion: String,
    val errorCode: String,
    val errorMessage: String,
    val rmcSdks: Map<String, String>? = null,
    val info: Map<String, String>? = null,
    var occurrenceCount: Int = 0,
) {
    val eventVersion = "1.0"
    val platform = "Android"
    @SerializedName("firstOccurrenceOn") val createdOn = System.currentTimeMillis()
}

// ---------------------------- Event enums ----------------------------
internal enum class EventType(val displayName: String) {
    CRITICAL("0"),
    WARNING("1")
}

// ---------------------------- Event extensions ----------------------------
/**
 * Returns the string hash based on some data from the [Event].
 */
internal fun Event.generateEventIdentifier() = generateEventIdentifier(
    eventType,
    appVersion,
    sdkName,
    errorCode,
    errorMessage
)

internal fun generateEventIdentifier(
    eventType: String,
    appVersion: String,
    sdkName: String,
    errorCode: String,
    errorMessage: String
) = "$eventType$appVersion$sdkName$errorCode$errorMessage"
    .getMD5HashData()
    .orEmpty()

internal fun Event.incrementCount() = apply { occurrenceCount += 1 }

internal fun Event.setType(newType: String) = apply { eventType = newType }
