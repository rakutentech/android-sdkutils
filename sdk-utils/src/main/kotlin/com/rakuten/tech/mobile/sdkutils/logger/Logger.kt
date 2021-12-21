package com.rakuten.tech.mobile.sdkutils.logger

import android.util.Log
import java.io.PrintWriter
import java.io.StringWriter
import kotlin.math.min

/**
 * Simple logging facility.
 *
 *
 * Logging conventions are:
 *  * Debug: for SDK developers. Will print source file, method and line.
 *  * Info: for SDK consumers. They should know, but is no problem.
 *  * Warn: for SDK consumers. An unexpected situation, that the SDK can recover from.
 *  * Error: for SDK consumers: An error that may cause the SDK to stop working.
 *
 * By default only info, warn and error are logged. Debug is only logged
 * if [Logger.setDebug] is called with `true`.
 *
 * All log calls come in 2 variants:
 *  * log(String template, Object.. args) - will use [String.format] to format the string.
 *  * log(Throwable cause, String template, Object.. args) - same as above
 *  but will add a "Caused by" and stacktrace to the log.
 *
 */
@SuppressWarnings("TooManyFunctions", "SpreadOperator")
class Logger(private val tag: String = "") {

    @SuppressWarnings("LongMethod", "ComplexMethod", "NestedBlockDepth")
    @Synchronized
    private fun log(level: Int, cause: Throwable?, message: String?, vararg args: Any?) {
        // debug level but app is not debuggable -> bail out
        if (level <= Log.DEBUG && !isDebug) {
            return
        }
        val sw = StringWriter(INITIAL_SIZE)
        if (level <= Log.DEBUG) {
            sw.append(invocationLocation())
        }
        val shouldFormat = !message.isNullOrEmpty() && args.isNotEmpty()
        sw.append(if (shouldFormat) message?.let { String.format(it, *args) } else message)

        if (cause != null) {
            sw.append("\nCaused by: ")
            val pw = PrintWriter(sw, false)
            cause.printStackTrace(pw)
            pw.flush()
        }
        val logMessage = sw.toString()

        // Send to log, in chunks if needed.
        when {
            logMessage.length < MAX_LOG_LENGTH && level == Log.ASSERT -> Log.wtf(tag, logMessage)
            logMessage.length < MAX_LOG_LENGTH -> Log.println(level, tag, logMessage)
            else -> logMessage.split("\n").forEach {
                it.chunked(MAX_LOG_LENGTH).forEach { part ->
                    if (level == Log.ASSERT) Log.wtf(tag, part) else Log.println(level, tag, part)
                }
            }
        }
    }

    private fun invocationLocation(): String {
        // Appends the location of the call to #debug, #info, etc…, i.e. 2 levels down the current.
        val st = Thread.currentThread().stackTrace
        var location: StackTraceElement? = null
        for (i in min(2, st.size) until st.size) {
            if (st[i].className != this.javaClass.name) {
                location = st[i]
                break
            }
        }
        return if (location != null) "${location.className}#${location.methodName}:${location.lineNumber} " else ""
    }

    /**
     * Log messages as DEBUG.
     * Will use [String.format] to format the message by substituting the given arguments [args].
     * @param message: The message to log.
     * @param args: Arguments for the format string.
     */
    fun debug(message: String?, vararg args: Any?) {
        log(Log.DEBUG, null, message, *args)
    }

    /**
     * Log messages as DEBUG.
     * Will use [String.format] to format the message by substituting the given arguments [args].
     * @param cause: The given exception to log.
     * @param message: The message to log.
     * @param args: Arguments for the format string.
     */
    fun debug(cause: Throwable? = null, message: String?, vararg args: Any?) {
        log(Log.DEBUG, cause, message, *args)
    }

    /**
     * Log messages as INFO.
     * * Will use [String.format] to format the message by substituting the given arguments [args].
     * @param message: The message to log.
     * @param args: Arguments for the format string.
     */
    fun info(message: String?, vararg args: Any?) {
        log(Log.INFO, null, message, *args)
    }

    /**
     * Log messages as INFO.
     * Will use [String.format] to format the message by substituting the arguments.
     * @param cause: The given exception to log.
     * @param message: The message to log.
     * @param args: Arguments for the format string.
     */
    fun info(cause: Throwable? = null, message: String?, vararg args: Any?) {
        log(Log.INFO, cause, message, *args)
    }

    /**
     * Log messages as WARN.
     * Will use [String.format] to format the message by substituting Arguments.
     * @param message: The message to log.
     * @param args: Arguments for the format string.
     */
    fun warn(message: String?, vararg args: Any?) {
        log(Log.WARN, null, message, *args)
    }

    /**
     * Log messages as WARN.
     * Will use [String.format] to format the message by substituting Arguments [args].
     * @param cause: The given exception to log.
     * @param message: The message to log.
     * @param args: Arguments for the format string.
     */
    fun warn(cause: Throwable? = null, message: String?, vararg args: Any?) {
        log(Log.WARN, cause, message, *args)
    }

    /**
     * Log messages as ERROR.
     * Will use [String.format] to format the message by substituting Arguments [args].
     * @param message: The message to log.
     * @param args: Arguments for the format string.
     */
    fun error(message: String?, vararg args: Any?) {
        log(Log.ERROR, null, message, *args)
    }

    /**
     * Log messages as ERROR.
     * Will use [String.format] to format the message by substituting the given arguments [args].
     * @param cause: The given exception to log.
     * @param message: The message to log.
     * @param args: Arguments for the format string.
     */
    fun error(cause: Throwable? = null, message: String?, vararg args: Any?) {
        log(Log.ERROR, cause, message, *args)
    }

    companion object {
        private const val MAX_LOG_LENGTH = 4000
        private const val INITIAL_SIZE = 256
        private var isDebug = false

        /**
         * This method enables/disables debug logger.
         * By default only info, warn and error are logged. Debug is only logged
         * if [Logger.setDebug] is called with `true`.
         * @param debug true to enable debug, false otherwise
         */
        fun setDebug(debug: Boolean) {
            isDebug = debug
        }
    }
}
