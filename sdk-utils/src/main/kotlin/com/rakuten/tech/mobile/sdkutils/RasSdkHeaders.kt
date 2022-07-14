package com.rakuten.tech.mobile.sdkutils

import android.os.Build
import androidx.annotation.VisibleForTesting

/**
 * Standard headers that should be sent with all requests to RAS.
 */
@SuppressWarnings("LongParameterList")
class RasSdkHeaders private constructor(
    private val appId: Pair<String, String>,
    private val subscriptionKey: Pair<String, String>,
    private val sdkName: Pair<String, String>,
    private val sdkVersion: Pair<String, String>,
    private val deviceModel: Pair<String, String>,
    private val deviceOs: Pair<String, String>,
    private val appName: Pair<String, String>,
    private val appVersion: Pair<String, String>
) {

    constructor(
        appId: String,
        subscriptionKey: String,
        sdkName: String,
        sdkVersion: String
    ) : this(
        appId = appId,
        subscriptionKey = subscriptionKey,
        sdkName = sdkName,
        sdkVersion = sdkVersion,
        appInfo = AppInfo.instance
    )

    @VisibleForTesting
    internal constructor(
        appId: String,
        subscriptionKey: String,
        sdkName: String,
        sdkVersion: String,
        appInfo: AppInfo
    ) : this(
        appId = "ras-app-id" to appId,
        subscriptionKey = "apiKey" to "ras-$subscriptionKey",
        sdkName = "ras-sdk-name" to sdkName,
        sdkVersion = "ras-sdk-version" to sdkVersion,
        deviceModel = "ras-device-model" to Build.MODEL,
        deviceOs = "ras-os-version" to Build.VERSION.RELEASE,
        appName = "ras-app-name" to appInfo.name,
        appVersion = "ras-app-version" to (appInfo.version ?: "")
    )

    /**
     * Returns the RAS headers as an array of [Pair]'s.
     *
     * @return array of [Pair] objects with [Pair.first] holding the header name
     * and [Pair.second] holding the header value.
     */
    fun asArray() = arrayOf(
        subscriptionKey,
        appId,
        sdkName,
        sdkVersion,
        deviceModel,
        deviceOs,
        appName,
        appVersion
    )
}
