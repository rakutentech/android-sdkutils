package com.rakuten.tech.mobile.sdkutils.eventlogger

import com.google.gson.annotations.Expose
import com.rakuten.tech.mobile.sdkutils.StringExtension.getMD5HashData

/**
 * Represents an event.
 *
 * For more details regarding the annotation, see:
 * https://www.javadoc.io/doc/com.google.code.gson/gson/2.9.0/com.google.gson/com/google/gson/annotations/Expose.html
 */
internal data class Event(
    @Expose val eventType: String,
    @Expose val appId: String,
    @Expose val appName: String,
    @Expose val appVer: String,
    @Expose val osVer: String,
    @Expose val deviceModel: String,
    @Expose val deviceBrand: String,
    @Expose val deviceName: String,
    @Expose val sdkName: String,
    @Expose val sdkVer: String,
    @Expose val errorCode: String,
    @Expose val errorMsg: String,
    @Expose val rmcSdks: Map<String, String>? = null,
    @Expose var occurrenceCount: Int = 0,
    @Expose(serialize = false) var firstOccurrenceMillis: Long? = null
) {
    @Expose val eventVer = "1"
    @Expose val platform = Platform.ANDROID.displayName
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
internal fun Event.getIdentifier() = "${this.appVer}${this.sdkName}${this.errorCode}${this.errorMsg}"
    .getMD5HashData()
    .orEmpty()

internal fun Event.incrementCount() = this.apply { this.occurrenceCount += 1 }

internal fun Event.setFirstOccurrenceTimeToNow() = this.apply {
    this.firstOccurrenceMillis = System.currentTimeMillis()
}
