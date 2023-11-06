package com.rakuten.tech.mobile.sdkutils.eventlogger

import android.content.Context

/**
 * A remote logging utility that sends events to the Event Logger platform.
 * This is intended to be used internally by Rakuten's SDKs.
 */
@SuppressWarnings(
    "EmptyFunctionBlock"
)
object EventLogger {

    /**
     * Initializes the event logging utility. Call this as early as possible in the application lifecycle. The
     * Application's `onCreate` might be the most logical place.
     *
     * @param context Application context.
     */
    fun initialize(context: Context) {
    }

    /**
     * Logs a critical event - an error that may cause the caller to not function properly. This event is sent
     * automatically.
     *
     * @param sourceName Source of the event, e.g. "inappmessaging".
     * @param sourceVersion Source' version, e.g. "1.0.0".
     * @param errorCode Source' error code or HTTP backend response code e.g. "500".
     * @param errorMessage Description of the error. Make it as descriptive as possible, for example, the stacktrace
     * of an exception.
     */
    fun critical(
        sourceName: String,
        sourceVersion: String,
        errorCode: String,
        errorMessage: String
    ) {
    }

    /**
     * Logs a warning event - an unexpected situation that the caller can recover from. This event is sent at a later
     * time.
     *
     * @param sourceName Source of the event, e.g. "inappmessaging".
     * @param sourceVersion Source' version, e.g. "1.0.0".
     * @param errorCode Source' error code or HTTP backend response code e.g. "500".
     * @param errorMessage Description of the error. Make it as descriptive as possible, for example, the stacktrace
     * of an exception.
     */
    fun warning(
        sourceName: String,
        sourceVersion: String,
        errorCode: String,
        errorMessage: String
    ) {
    }
}
