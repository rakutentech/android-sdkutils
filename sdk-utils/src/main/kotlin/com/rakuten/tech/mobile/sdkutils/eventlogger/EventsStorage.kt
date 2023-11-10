package com.rakuten.tech.mobile.sdkutils.eventlogger

import android.content.SharedPreferences
import com.google.gson.Gson
import com.google.gson.JsonSyntaxException

internal interface EventsStorage {
    fun getAllEvents(): List<Event>
    fun getEventById(id: String): Event?
    fun getCount(): Int
    fun insertEvent(event: Event)
    fun updateEvent(event: Event)
    fun deleteEvents(events: List<Event>)
    fun deleteOldEvents(maxCapacity: Int)
}

internal class SharedPreferencesEventsStorage(private val sharedPref: SharedPreferences) : EventsStorage {

    override fun getAllEvents(): List<Event> {
        val events = mutableListOf<Event>()

        for (key in sharedPref.all.keys) {
            val eventJson = getEventById(key) ?: continue
            events.add(eventJson)
        }
        return events
    }

    @SuppressWarnings("SwallowedException")
    override fun getEventById(id: String): Event? {
        val event = sharedPref.getString(id, null) ?: return null

        return try {
            Gson().fromJson(event, Event::class.java)
        } catch (e: JsonSyntaxException) {
            null
        }
    }

    override fun getCount(): Int = sharedPref.all.keys.size

    /**
     * Inserts the event to SharedPreferences and automatically setting its identifier, count, and occurrence time.
     */
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

    /**
     * Deletes old events based on [Event.firstOccurrenceMillis] and retains a maximum of [maxCapacity] events.
     */
    override fun deleteOldEvents(maxCapacity: Int) {
        if (getCount() <= maxCapacity) {
            return
        }

        val sortedEvents = getAllEvents().sortedBy { it.firstOccurrenceMillis }
        val oldEvents = sortedEvents.take(0.coerceAtLeast(sortedEvents.size - maxCapacity))
        deleteEvents(oldEvents)
    }
}
