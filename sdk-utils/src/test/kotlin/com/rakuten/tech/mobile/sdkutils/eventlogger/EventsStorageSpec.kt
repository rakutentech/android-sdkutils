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
        eventsStorage.insertEvent(EventLoggerTestUtil.generateRandomEvent())
        eventsStorage.insertEvent(EventLoggerTestUtil.generateRandomEvent())

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
        eventsStorage.insertEvent(event)

        eventsStorage.getEventById(event.getIdentifier()) shouldBeEqualTo event
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
        eventsStorage.insertEvent(EventLoggerTestUtil.generateRandomEvent())
        eventsStorage.insertEvent(EventLoggerTestUtil.generateRandomEvent())

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
    fun `should insert event and automatically fill-in some attributes`() {
        val event = EventLoggerTestUtil.generateRandomEvent()
        eventsStorage.insertEvent(event)
        val storedEvent = eventsStorage.getEventById(event.getIdentifier())

        storedEvent!!.getIdentifier() shouldNotBeEqualTo null
        storedEvent.occurrenceCount shouldBeGreaterThan 0
        storedEvent.firstOccurrenceMillis shouldNotBeEqualTo null
    }

    @Test
    fun `should update event`() {
        val origEvent = EventLoggerTestUtil.generateRandomEvent()
        eventsStorage.insertEvent(origEvent)

        val updatedEvent = origEvent.incrementCount()
        eventsStorage.updateEvent(updatedEvent)

        eventsStorage.getEventById(origEvent.getIdentifier())?.occurrenceCount shouldBeEqualTo 2
    }

    @Test
    fun `should delete events`() {
        val event1 = EventLoggerTestUtil.generateRandomEvent()
        val event2 = EventLoggerTestUtil.generateRandomEvent()
        eventsStorage.insertEvent(event1)
        eventsStorage.insertEvent(event2)

        eventsStorage.deleteEvents(listOf(event1))

        eventsStorage.getCount() shouldBeEqualTo 1
        val events = eventsStorage.getAllEvents()
        events shouldContain event2
        events shouldNotContain event1
    }

    @Test
    fun `deleteOldEvents should delete old events based on max capacity`() {
        val event1 = EventLoggerTestUtil.generateRandomEvent()
        val event2 = EventLoggerTestUtil.generateRandomEvent()
        eventsStorage.insertEvent(event1)
        eventsStorage.insertEvent(event2)

        eventsStorage.deleteOldEvents(1)

        eventsStorage.getCount() shouldBeEqualTo 1
        val events = eventsStorage.getAllEvents()
        events shouldContain event2
        events shouldNotContain event1
    }

    @Test
    fun `deleteOldEvents should not delete any event if within max capacity`() {
        eventsStorage.insertEvent(EventLoggerTestUtil.generateRandomEvent())
        eventsStorage.insertEvent(EventLoggerTestUtil.generateRandomEvent())

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
