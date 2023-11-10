package com.rakuten.tech.mobile.sdkutils.eventlogger

internal data class Event(
    val eventType: String,
    val appId: String,
    val appName: String,
    val appVer: String,
    val osVer: String,
    val deviceModel: String,
    val deviceBrand: String,
    val deviceName: String,
    val sdkName: String,
    val sdkVer: String,
    val errorCode: String,
    val errorMsg: String,
    val rmcSdks: Map<String, String>? = null,
    var occurrenceCount: Int = 0,
    var firstOccurrenceMillis: Long? = null
) {
    val eventVer = "1"
    val platform = Platform.ANDROID.displayName
}

internal enum class EventType(val displayName: String) {
    CRITICAL("0"),
    WARNING("1")
}

internal enum class Platform(val displayName: String) {
    ANDROID("Android")
}
