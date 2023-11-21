package com.rakuten.tech.mobile.sdkutils.eventlogger

import android.content.SharedPreferences
import com.google.gson.Gson

internal interface EventsStorage {

    /**
     * Retrieves the events, or empty if the operation fails.
     */
    fun getAllEvents(): List<Event>

    /**
     * Retrieves the event with matching [id] or the [Event.getIdentifier].
     */
    fun getEventById(id: String): Event?

    /**
     * Retrieves the event count based on the number of existing keys in storage, or -1 if the operation fails.
     */
    fun getCount(): Int

    /**
     * Inserts the event which also automatically sets its identifier, count, and occurrence time.
     * It will be inserted with the [Event.getIdentifier] as key and the [Event] in string format as value.
     */
    fun insertEvent(event: Event)

    /**
     * Updates the event with matching [Event.getIdentifier].
     */
    fun updateEvent(event: Event)

    /**
     * Deletes the supplied [events].
     */
    fun deleteEvents(events: List<Event>)

    /**
     * Deletes old events based on [Event.firstOccurrenceMillis] and retains a maximum of [maxCapacity] events.
     */
    fun deleteOldEvents(maxCapacity: Int)
}

@SuppressWarnings(
    "TooGenericExceptionCaught",
    "SwallowedException"
)
internal class SharedPreferencesEventsStorage(private val sharedPref: SharedPreferences) : EventsStorage {

    override fun getAllEvents(): List<Event> {
        val events = mutableListOf<Event>()
        try {
            for (key in sharedPref.all.keys) {
                val eventJson = getEventById(key) ?: continue
                events.add(eventJson)
            }
        } catch (_: Exception) {
        }
        return events
    }

    override fun getEventById(id: String): Event? {
        return try {
            val event = sharedPref.getString(id, null) ?: return null
            Gson().fromJson(event, Event::class.java)
        } catch (e: Exception) {
            null
        }
    }

    override fun getCount(): Int {
        return try {
            sharedPref.all.keys.size
        } catch (e: Exception) {
            -1
        }
    }

    override fun insertEvent(event: Event) {
        with(sharedPref.edit()) {
            event.incrementCount()
            event.setFirstOccurrenceTimeToNow()
            putString(event.getIdentifier(), Gson().toJson(event))
            apply()
        }
    }

    override fun updateEvent(event: Event) {
        with(sharedPref.edit()) {
            putString(event.getIdentifier(), Gson().toJson(event))
            apply()
        }
    }

    override fun deleteEvents(events: List<Event>) {
        with(sharedPref.edit()) {
            events.forEach {
                remove(it.getIdentifier())
            }
            apply()
        }
    }

    override fun deleteOldEvents(maxCapacity: Int) {
        if (getCount() <= maxCapacity) {
            return
        }

        val sortedEvents = getAllEvents().sortedBy { it.firstOccurrenceMillis }
        val oldEvents = sortedEvents.take(0.coerceAtLeast(sortedEvents.size - maxCapacity))
        deleteEvents(oldEvents)
    }
}
