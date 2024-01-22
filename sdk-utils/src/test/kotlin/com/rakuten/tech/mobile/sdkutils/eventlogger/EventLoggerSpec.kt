package com.rakuten.tech.mobile.sdkutils.eventlogger

import android.app.Activity
import android.content.Context
import android.content.SharedPreferences
import androidx.test.core.app.ApplicationProvider
import com.google.common.util.concurrent.MoreExecutors
import com.nhaarman.mockitokotlin2.argumentCaptor
import com.rakuten.tech.mobile.sdkutils.AppLifecycleObserver
import org.junit.Before
import org.junit.FixMethodOrder
import org.junit.Test
import org.junit.runner.RunWith
import org.junit.runners.MethodSorters
import org.mockito.Mockito.*
import org.robolectric.Robolectric
import org.robolectric.RobolectricTestRunner
import java.lang.ref.WeakReference
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
    internal val eventLoggerHelper = EventLoggerHelper(WeakReference(ApplicationProvider.getApplicationContext()))
    internal val lifecycleListener = AppLifecycleObserver(WeakReference(ApplicationProvider.getApplicationContext()))
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
            eventLoggerHelper,
            lifecycleListener,
            tasksQueue
        )
    }
}

class GeneralSpec : EventLoggerSpec() {

    @Before
    fun setup() {
        configureWithMocks()
    }

    @Test
    fun `should not crash when executorService fails to execute command`() {
        val mockTasksQueue = mock(ExecutorService::class.java)
        `when`(mockTasksQueue.execute(any()))
            .thenThrow(RejectedExecutionException(""))

        EventLogger.initialize(
            mockEventsSender,
            mockEventsStorage,
            mockEventLoggerCache,
            eventLoggerHelper,
            lifecycleListener,
            mockTasksQueue
        )
    }

    @Test
    fun `should not process event with missing details`() {
        EventLogger.sendCriticalEvent(sourceName = "", sourceVersion = "test", "test", "test")

        verify(mockEventsStorage, never()).getEventById(anyString())
    }

    @Test
    fun `should send events if storage is full`() {
        val testEvent = EventLoggerTestUtil.generateRandomEvent()
        `when`(mockEventsStorage.getCount())
            .thenReturn(EventLogger.Config.MAX_EVENTS_COUNT) // simulate storage full
        `when`(mockEventsStorage.getAllEvents())
            .thenReturn(mapOf("id1" to testEvent))
        `when`(mockEventsStorage.getEventById(anyString()))
            .thenReturn(testEvent)
        `when`(mockEventLoggerCache.getTtlReferenceTime())
            .thenReturn(System.currentTimeMillis())

        EventLogger.sendCriticalEvent(
            "test",
            "test",
            "test",
            "test"
        )

        verify(mockEventsSender).pushEvents(anyList(), any(), any())
    }

    @SuppressWarnings("LongMethod")
    @Test
    fun `should delete old event if storage is full and sending failed`() {
        `when`(mockEventsStorage.getCount())
            .thenReturn(EventLogger.Config.MAX_EVENTS_COUNT) // simulate storage full
        `when`(mockEventsStorage.getAllEvents())
            .thenReturn(mapOf("id1" to EventLoggerTestUtil.generateRandomEvent()))
        `when`(mockEventsStorage.getEventById(anyString()))
            .thenReturn(null)
        `when`(mockEventLoggerCache.getTtlReferenceTime())
            .thenReturn(System.currentTimeMillis())

        EventLogger.sendWarningEvent(
            "test",
            "test",
            "test",
            "test"
        )

        val captor = argumentCaptor<() -> Unit>()
        verify(mockEventsSender).pushEvents(anyList(), any(), captor.capture())
        captor.firstValue.invoke()
        verify(mockEventsStorage).deleteOldEvents(anyInt())
    }
}

@FixMethodOrder(MethodSorters.NAME_ASCENDING)
@SuppressWarnings("LongMethod")
class ConfigureSpec : EventLoggerSpec() {

    @Test
    fun `0 - should ignore calls if configure not called`() {
        val spy = spy(EventLogger)
        spy.sendWarningEvent(
            "test",
            "test",
            "test",
            "test"
        )

        verify(spy, never()).initialize(
            any() ?: mockEventsSender,
            any() ?: mockEventsStorage,
            any() ?: mockEventLoggerCache,
            any() ?: eventLoggerHelper,
            any() ?: lifecycleListener,
            any() ?: tasksQueue
        )
    }

    @Test
    fun `1 - should configure only once`() {
        EventLogger.configure(mockContext, "", "")
        EventLogger.configure(mockContext, "https://test", "abcd")
        EventLogger.configure(mockContext, "https://test2", "wxyz")

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
    fun `should send events when TTL expired and delete from storage afterwards - configure`() {
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
    fun `should send events when TTL expired and delete from storage afterwards - becameForeground`() {
        `when`(mockEventLoggerCache.getTtlReferenceTime())
            .thenReturn(TimeUnit.MILLISECONDS.toDays(30)) // simulate expiry
        `when`(mockEventsStorage.getAllEvents())
            .thenReturn(mapOf("id1" to EventLoggerTestUtil.generateRandomEvent()))

        val activity = Robolectric.buildActivity(Activity::class.java)
        val observer = AppLifecycleObserver(WeakReference(activity.get()))
        EventLogger.initialize(
            mockEventsSender,
            mockEventsStorage,
            mockEventLoggerCache,
            eventLoggerHelper,
            observer,
            tasksQueue
        )

        activity.create().start().resume().pause().stop().resume() // simulate transition
        val captor = argumentCaptor<() -> Unit>()
        verify(mockEventsSender, atLeastOnce()).pushEvents(
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

@SuppressWarnings("LongMethod")
class SendCriticalSpec : EventLoggerSpec() {

    @Before
    fun setup() {
        configureWithMocks()
    }

    @Test
    @SuppressWarnings("LongMethod")
    fun `should send critical event immediately and update type`() {
        `when`(mockEventsStorage.getCount())
            .thenReturn(1)
        `when`(mockEventsStorage.getAllEvents())
            .thenReturn(mapOf())
        `when`(mockEventsStorage.getEventById(anyString()))
            .thenReturn(null)
        `when`(mockEventLoggerCache.getTtlReferenceTime())
            .thenReturn(System.currentTimeMillis())

        EventLogger.sendCriticalEvent(
            "test",
            "test",
            "test",
            "test"
        )

        val captor = argumentCaptor<() -> Unit>()
        verify(mockEventsSender).pushEvents(anyList(), captor.capture(), any())

        captor.firstValue.invoke()
        verify(mockEventsStorage).updateEvent(anyString(), com.nhaarman.mockitokotlin2.any())
    }
}

class SendWarningSpec : EventLoggerSpec() {

    @Before
    fun setup() {
        configureWithMocks()
    }

    @Test
    fun `should do nothing if warning event is received`() {
        val testEvent = EventLoggerTestUtil.generateRandomEvent()
        `when`(mockEventsStorage.getCount())
            .thenReturn(1)
        `when`(mockEventsStorage.getAllEvents())
            .thenReturn(mapOf("id1" to testEvent))
        `when`(mockEventsStorage.getEventById(anyString()))
            .thenReturn(testEvent)
        `when`(mockEventLoggerCache.getTtlReferenceTime())
            .thenReturn(System.currentTimeMillis())

        EventLogger.sendWarningEvent(
            "inappmessaging",
            "1.0.0",
            "500",
            "server error"
        )

        verify(mockEventsSender, never()).pushEvents(anyList(), any(), any())
    }
}
