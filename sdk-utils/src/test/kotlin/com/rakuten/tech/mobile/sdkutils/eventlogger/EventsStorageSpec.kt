package com.rakuten.tech.mobile.sdkutils.eventlogger

import android.content.Context
import android.content.SharedPreferences
import androidx.test.core.app.ApplicationProvider
import org.amshove.kluent.*
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.Mockito.mock
import org.mockito.Mockito.`when`
import org.robolectric.RobolectricTestRunner

@RunWith(RobolectricTestRunner::class)
class EventsStorageSpec {

    private val context = ApplicationProvider.getApplicationContext<Context>()
    private val sharedPref = context.getSharedPreferences("test-shared-prefs", Context.MODE_PRIVATE)
    private val eventsStorage = SharedPreferencesEventsStorage(sharedPref)

    @Before
    fun setup() {
        clearSharedPrefs()
    }

    @Test
    fun `getAllEvents should return all events`() {
        eventsStorage.insertEvent("id1", EventLoggerTestUtil.generateRandomEvent())
        eventsStorage.insertEvent("id2", EventLoggerTestUtil.generateRandomEvent())

        eventsStorage.getAllEvents() shouldHaveSize 2
    }

    @Test
    fun `getAllEvents should not crash if exception occurs`() {
        val mockSharedPrefs = mock(SharedPreferences::class.java)
        `when`(mockSharedPrefs.all)
            .thenThrow(NullPointerException())

        SharedPreferencesEventsStorage(mockSharedPrefs).getAllEvents().shouldBeEmpty()
    }

    @Test
    fun `getEventById should return correct event`() {
        val event = EventLoggerTestUtil.generateRandomEvent()
        eventsStorage.insertEvent("id1", event)

        eventsStorage.getEventById("id1") shouldBeEqualTo event
    }

    @Test
    fun `getEventById should return null event if not exists`() {
        eventsStorage.getEventById("some-random-id") shouldBeEqualTo null
    }

    @Test
    fun `getEventById should not crash if exception occurs`() {
        val mockSharedPrefs = mock(SharedPreferences::class.java)
        `when`(mockSharedPrefs.getString("test-id", null))
            .thenThrow(ClassCastException())

        SharedPreferencesEventsStorage(mockSharedPrefs).getEventById("test-id") shouldBeEqualTo null
    }

    @Test
    fun `getCount should return correct count`() {
        eventsStorage.insertEvent("id1", EventLoggerTestUtil.generateRandomEvent())
        eventsStorage.insertEvent("id2", EventLoggerTestUtil.generateRandomEvent())

        eventsStorage.getCount() shouldBeEqualTo 2
    }

    @Test
    fun `getCount should not crash if exception occurs`() {
        val mockSharedPrefs = mock(SharedPreferences::class.java)
        `when`(mockSharedPrefs.all)
            .thenThrow(NullPointerException())

        SharedPreferencesEventsStorage(mockSharedPrefs).getCount() shouldBeEqualTo -1
    }

    @Test
    fun `should insert event`() {
        val event = EventLoggerTestUtil.generateRandomEvent()
        eventsStorage.insertEvent("id1", event)
        eventsStorage.getEventById("id1") shouldNotBeEqualTo null
    }

    @Test
    fun `should update event`() {
        val origEvent = EventLoggerTestUtil.generateRandomEvent()
        eventsStorage.insertEvent("id1", origEvent)

        val updatedEvent = origEvent
            .incrementCount()
            .incrementCount()
            .incrementCount()
        eventsStorage.updateEvent("id1", updatedEvent)

        eventsStorage.getEventById("id1")?.occurrenceCount shouldBeEqualTo 3
    }

    @Test
    fun `should delete all events`() {
        val event1 = EventLoggerTestUtil.generateRandomEvent()
        val event2 = EventLoggerTestUtil.generateRandomEvent()
        eventsStorage.insertEvent("id1", event1)
        eventsStorage.insertEvent("id2", event2)

        eventsStorage.deleteAllEvents()

        eventsStorage.getCount() shouldBeEqualTo 0
    }

    @Test
    fun `deleteOldEvents should delete old events based on max capacity`() {
        val event1 = EventLoggerTestUtil.generateRandomEvent()
        val event2 = EventLoggerTestUtil.generateRandomEvent()
        eventsStorage.insertEvent(event1.generateEventIdentifier(), event1)
        eventsStorage.insertEvent(event2.generateEventIdentifier(), event2)

        eventsStorage.deleteOldEvents(1)

        eventsStorage.getCount() shouldBeEqualTo 1
        val events = eventsStorage.getAllEvents()
        events shouldContain event2
        events shouldNotContain event1
    }

    @Test
    fun `deleteOldEvents should not delete any event if within max capacity`() {
        eventsStorage.insertEvent("id1", EventLoggerTestUtil.generateRandomEvent())
        eventsStorage.insertEvent("id2", EventLoggerTestUtil.generateRandomEvent())

        eventsStorage.deleteOldEvents(5)

        eventsStorage.getCount() shouldBeEqualTo 2
    }

    private fun clearSharedPrefs() {
        with(sharedPref.edit()) {
            clear()
            commit()
        }
    }
}
