package com.rakuten.tech.mobile.sdkutils.eventlogger

import com.rakuten.tech.mobile.sdkutils.StringExtension.getMD5HashData

internal data class Event(
    val eventType: String,
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
    var occurrenceCount: Int = 0,
    var firstOccurrenceMillis: Long? = null
) {
    val eventVersion = "1.0"
    val platform = Platform.ANDROID.displayName
}

// ---------------------------- Event enums ----------------------------
internal enum class EventType(val displayName: String) {
    CRITICAL("0"),
    WARNING("1")
}

internal enum class Platform(val displayName: String) {
    ANDROID("Android")
}

// ---------------------------- Event extensions ----------------------------
/**
 * Returns the string hash based on some data from the [Event].
 */
internal fun Event.getIdentifier() = "${this.appVersion}${this.sdkName}${this.errorCode}${this.errorMessage}"
    .getMD5HashData()
    .orEmpty()

internal fun Event.incrementCount() = this.apply { this.occurrenceCount += 1 }

internal fun Event.setFirstOccurrenceTimeToNow() = this.apply {
    this.firstOccurrenceMillis = System.currentTimeMillis()
}
