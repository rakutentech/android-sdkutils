package com.rakuten.tech.mobile.sdkutils.eventlogger

import android.content.Context
import android.content.SharedPreferences
import com.google.common.util.concurrent.MoreExecutors
import com.nhaarman.mockitokotlin2.argumentCaptor
import org.junit.FixMethodOrder
import org.junit.Test
import org.junit.runner.RunWith
import org.junit.runners.MethodSorters
import org.mockito.ArgumentMatchers.anyInt
import org.mockito.ArgumentMatchers.anyString
import org.mockito.Mockito.*
import org.robolectric.RobolectricTestRunner
import java.util.concurrent.ExecutorService
import java.util.concurrent.RejectedExecutionException
import java.util.concurrent.TimeUnit

@RunWith(RobolectricTestRunner::class)
@SuppressWarnings("UnnecessaryAbstractClass")
abstract class EventLoggerSpec {

    internal val mockContext = mock(Context::class.java)
    internal val mockEventsSender = mock(EventsSender::class.java)
    internal val mockEventsStorage = mock(EventsStorage::class.java)
    internal val mockEventLoggerCache = mock(EventLoggerCache::class.java)
    internal val mockEventLoggerHelper = mock(EventLoggerHelper::class.java)
    internal val tasksQueue = MoreExecutors.newDirectExecutorService()

    init {
        `when`(mockContext.getSharedPreferences(anyString(), anyInt()))
            .thenReturn(mock(SharedPreferences::class.java))
        `when`(mockContext.applicationContext)
            .thenReturn(mockContext)
    }

    fun configureWithMocks() {
        EventLogger.initialize(
            mockEventsSender,
            mockEventsStorage,
            mockEventLoggerCache,
            mockEventLoggerHelper,
            tasksQueue
        )
    }
}

class GeneralSpec : EventLoggerSpec() {
    @Test
    fun `should not crash when executorService fails to execute command`() {
        val mockTasksQueue = mock(ExecutorService::class.java)
        `when`(mockTasksQueue.execute(any()))
            .thenThrow(RejectedExecutionException(""))

        EventLogger.initialize(
            mockEventsSender,
            mockEventsStorage,
            mockEventLoggerCache,
            mockEventLoggerHelper,
            mockTasksQueue
        )
    }
}

@FixMethodOrder(MethodSorters.NAME_ASCENDING)
class ConfigureSpec : EventLoggerSpec() {
    @Test
    fun `0 - should configure only once`() {
        EventLogger.configure(mockContext, "https://test", "abcd")
        EventLogger.configure(mockContext)

        verify(mockContext, times(1)).getSharedPreferences(
            EventLogger.Config.EVENTS_STORAGE_FILENAME,
            Context.MODE_PRIVATE
        )
        verify(mockContext, times(1)).getSharedPreferences(
            EventLogger.Config.EVENT_LOGGER_GENERAL_CACHE_FILENAME,
            Context.MODE_PRIVATE
        )
    }

    @Test
    fun `should send events when TTL expired and delete from storage afterwards`() {
        `when`(mockEventLoggerCache.getTtlReferenceTime())
            .thenReturn(TimeUnit.MILLISECONDS.toDays(30)) // simulate expiry
        `when`(mockEventsStorage.getAllEvents())
            .thenReturn(mapOf("id1" to EventLoggerTestUtil.generateRandomEvent()))

        configureWithMocks()

        val captor = argumentCaptor<() -> Unit>()
        verify(mockEventsSender).pushEvents(
            anyList(),
            captor.capture(),
            any()
        )
        captor.firstValue.invoke()
        verify(mockEventsStorage).deleteEvents(anyList())
    }

    @Test
    fun `should not process if storage is empty`() {
        `when`(mockEventsStorage.getAllEvents())
            .thenReturn(mapOf())

        configureWithMocks()

        verify(mockEventsSender, never()).pushEvents(anyList(), any(), any())
    }

    @Test
    fun `should not process if TTL not yet expired`() {
        `when`(mockEventLoggerCache.getTtlReferenceTime())
            .thenReturn(-1)

        configureWithMocks()

        verify(mockEventsSender, never()).pushEvents(anyList(), any(), any())
    }
}

class SendCriticalSpec : EventLoggerSpec() {
    @Test
    fun `should log critical event`() {
        EventLogger.critical(
            "inappmessaging",
            "1.0.0",
            "500",
            "server error"
        ) // do nothing as of now
    }
}

class SendWarningSpec : EventLoggerSpec() {
    @Test
    fun `should log warning event`() {
        EventLogger.warning(
            "inappmessaging",
            "1.0.0",
            "500",
            "server error"
        ) // do nothing as of now
    }
}
