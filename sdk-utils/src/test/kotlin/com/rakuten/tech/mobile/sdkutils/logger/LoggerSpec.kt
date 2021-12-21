package com.rakuten.tech.mobile.sdkutils.logger

import android.os.Build
import android.util.Log
import org.amshove.kluent.shouldBeEqualTo
import org.amshove.kluent.shouldContainAll
import org.amshove.kluent.shouldHaveSize
import org.amshove.kluent.shouldNotContainAll
import org.junit.Assert
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import org.robolectric.shadows.ShadowLog
import org.robolectric.shadows.ShadowLog.LogItem

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [Build.VERSION_CODES.O_MR1])
class LoggerSpec {

    private lateinit var logger: Logger

    @Before
    fun setup() {
        logger = Logger(TAG)
    }

    @Test
    fun `should disable debug and verbose logging in set debug`() {
        Logger.setDebug(false)
        logger.debug("some debug log")
        assertThatLog().hasNoMoreMessages()
    }

    @Test
    fun `should enable debug and verbose logging in set debug`() {
        Logger.setDebug(true)
        logger.debug("some debug log")
        assertThatLog().hasSequentialMessage(Log.DEBUG, TAG, listOf("some debug log"))
    }

    @Test
    fun `should not affect info and above in set debug`() {
        Logger.setDebug(false)
        logger.info("info")
        logger.warn("warn")
        logger.error("error")
        assertThatLog().hasSequentialMessage(Log.INFO, TAG, listOf("info"))
            .hasSequentialMessage(Log.WARN, TAG, listOf("warn"))
            .hasSequentialMessage(Log.ERROR, TAG, listOf("error"))
            .hasNoMoreMessages()
    }

    @Test
    fun `should only print invocation location on debug logs`() {
        Logger.setDebug(true)
        val className = this.javaClass.name
        logger.debug("debug")
        logger.info("info")
        logger.warn("warn")
        logger.error("error")
        assertThatLog()
            .hasSequentialMessage(Log.DEBUG, TAG, listOf("debug", className))
            .hasSequentialMessage(Log.INFO, TAG, listOf("info"), listOf(className))
            .hasSequentialMessage(Log.WARN, TAG, listOf("warn"), listOf(className))
            .hasSequentialMessage(Log.ERROR, TAG, listOf("error"), listOf(className))
            .hasNoMoreMessages()
    }

    @Test
    fun `should print stacktrace debug`() {
        Logger.setDebug(true)
        logger.debug(Throwable(), "debug")
        assertThatLog().hasMessage(Log.DEBUG, TAG, "debug")
            .hasMessage(Log.DEBUG, TAG, "Caused by:")
    }

    @Test
    fun `should print stacktrace info`() {
        logger.info(Throwable(), "info")
        assertThatLog().hasMessage(Log.INFO, TAG, "info")
            .hasMessage(Log.INFO, TAG, "Caused by:")
    }

    @Test
    fun `should print stacktrace warn`() {
        logger.warn(Throwable(), "warn")
        assertThatLog().hasMessage(Log.WARN, TAG, "warn")
            .hasMessage(Log.WARN, TAG, "Caused by:")
    }

    @Test
    fun `should print stacktrace error`() {
        logger.error(Throwable(), "error")
        assertThatLog().hasMessage(Log.ERROR, TAG, "error")
            .hasMessage(Log.ERROR, TAG, "Caused by:")
    }

    @Test
    fun `should print max log`() {
        Logger.setDebug(true)

        logger.debug(Throwable(), "test".repeat(1000))
        assertThatLog().hasMessage(Log.DEBUG, TAG, "test")
    }

    private fun assertThatLog() = LogAssert(ShadowLog.getLogs())

    private class LogAssert(loggedItems: List<LogItem>) {
        private val items: List<LogItem> = loggedItems.filter { it.tag == TAG }
        private var index: Int = 0

        fun hasSequentialMessage(
            priority: Int,
            tag: String,
            containList: List<String>,
            notContainList: List<String> = listOf()
        ): LogAssert {
            val item = items[index++]
            item.type shouldBeEqualTo priority
            item.tag shouldBeEqualTo tag
            item.msg shouldContainAll containList
            if (notContainList.isNotEmpty()) {
                item.msg shouldNotContainAll notContainList
            }
            return this
        }

        /**
         * When run in Robolectric from gradle stack traces with one [LogItem] per stack trace
         * line, whereas when run in AS it will be one [LogItem] with a long message. This
         * hasMassage will find the first line that matches the condition and verify the tage and
         * priority. No sequence is enforced, so this can only be used in very small, isolated tests.
         */
        fun hasMessage(priority: Int, tag: String, condition: String): LogAssert {
            for (item in items) {
                if (item.msg.contains(condition)) {
                    item.type shouldBeEqualTo priority
                    item.tag shouldBeEqualTo tag
                    return this
                }
            }
            Assert.fail()
            return this
        }

        fun hasNoMoreMessages() {
            items.shouldHaveSize(index)
        }
    }

    companion object {
        private const val TAG = "test"
    }
}
