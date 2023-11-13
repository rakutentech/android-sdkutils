package com.rakuten.tech.mobile.sdkutils.eventlogger

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import org.amshove.kluent.*
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import kotlin.random.Random

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
        eventsStorage.insertEvent(generateRandomEvent())
        eventsStorage.insertEvent(generateRandomEvent())

        eventsStorage.getAllEvents() shouldHaveSize 2
    }

    @Test
    fun `getEventById should return correct event`() {
        val event = generateRandomEvent()
        eventsStorage.insertEvent(event)

        eventsStorage.getEventById(event.getIdentifier()) shouldBeEqualTo event
    }

    @Test
    fun `getEventById should return null event if not exists`() {
        eventsStorage.getEventById("some-random-id") shouldBeEqualTo null
    }

    @Test
    fun `getCount should return correct count`() {
        eventsStorage.insertEvent(generateRandomEvent())
        eventsStorage.insertEvent(generateRandomEvent())

        eventsStorage.getCount() shouldBeEqualTo 2
    }

    @Test
    fun `should insert event and automatically fill-in some attributes`() {
        val event = generateRandomEvent()
        eventsStorage.insertEvent(event)
        val storedEvent = eventsStorage.getEventById(event.getIdentifier())

        storedEvent!!.getIdentifier() shouldNotBeEqualTo null
        storedEvent.occurrenceCount shouldBeGreaterThan 0
        storedEvent.firstOccurrenceMillis shouldNotBeEqualTo null
    }

    @Test
    fun `should update event`() {
        val origEvent = generateRandomEvent()
        eventsStorage.insertEvent(origEvent)

        val updatedEvent = origEvent.incrementCount()
        eventsStorage.updateEvent(updatedEvent)

        eventsStorage.getEventById(origEvent.getIdentifier())?.occurrenceCount shouldBeEqualTo 2
    }

    @Test
    fun `should delete events`() {
        val event1 = generateRandomEvent()
        val event2 = generateRandomEvent()
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
        val event1 = generateRandomEvent()
        val event2 = generateRandomEvent()
        eventsStorage.insertEvent(event1)
        eventsStorage.insertEvent(event2)

        eventsStorage.deleteOldEvents(1)

        val events = eventsStorage.getAllEvents()
        events shouldContain event2
        events shouldNotContain event1
    }

    @Test
    fun `deleteOldEvents should not delete any event if within max capacity`() {
        eventsStorage.insertEvent(generateRandomEvent())
        eventsStorage.insertEvent(generateRandomEvent())

        eventsStorage.deleteOldEvents(5)

        eventsStorage.getCount() shouldBeEqualTo 2
    }

    private fun clearSharedPrefs() {
        with(sharedPref.edit()) {
            clear()
            commit()
        }
    }

    private fun generateRandomEvent(): Event {
        val randomText = System.currentTimeMillis().toString()
        return EventBuilder(context)
            .buildEvent(
                EventType.values()[Random.nextInt(EventType.values().size)],
                randomText,
                randomText,
                randomText,
                randomText
            )
    }
}
