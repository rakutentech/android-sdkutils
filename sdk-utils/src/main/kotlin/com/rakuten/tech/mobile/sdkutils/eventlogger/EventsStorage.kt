package com.rakuten.tech.mobile.sdkutils.eventlogger

import android.content.SharedPreferences
import com.google.gson.Gson

/**
 * Interface to access events storage.
 */
internal interface EventsStorage {

    /**
     * Retrieves the events, or empty if the operation fails.
     */
    fun getAllEvents(): Map<String, Event>

    /**
     * Retrieves the event with matching [id].
     */
    fun getEventById(id: String): Event?

    /**
     * Retrieves the event count based on the number of existing keys in storage, or -1 if the operation fails.
     */
    fun getCount(): Int

    /**
     * Inserts the event with [id] as its key and [Event] in string format.
     */
    fun insertEvent(id: String, event: Event)

    /**
     * Updates the event with matching [id] key.
     */
    fun updateEvent(id: String, event: Event)

    /**
     * Deletes the events with the supplied [ids].
     */
    fun deleteEvents(ids: List<String>)

    /**
     * Deletes old events based on [Event.createdOn] and retains a maximum of [maxCapacity] events.
     */
    fun deleteOldEvents(maxCapacity: Int)
}

@SuppressWarnings(
    "TooGenericExceptionCaught",
    "SwallowedException"
)
internal class SharedPreferencesEventsStorage(private val sharedPref: SharedPreferences) : EventsStorage {

    override fun getAllEvents(): Map<String, Event> {
        val events = mutableMapOf<String, Event>()
        try {
            for (key in sharedPref.all.keys) {
                val eventJson = getEventById(key) ?: continue
                events[key] = eventJson
            }
        } catch (_: Exception) {
            // return empty
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

    override fun insertEvent(id: String, event: Event) {
        insertOrUpdateEvent(id, event)
    }

    override fun updateEvent(id: String, event: Event) {
        insertOrUpdateEvent(id, event)
    }

    override fun deleteEvents(ids: List<String>) {
        with(sharedPref.edit()) {
            ids.forEach {
                remove(it)
            }
            apply()
        }
    }

    override fun deleteOldEvents(maxCapacity: Int) {
        if (getCount() <= maxCapacity) {
            return
        }

        val sortedKeys = getAllEvents().entries.toList().sortedBy { (_, event) -> event.createdOn }.map { it.key }
        val oldKeys = sortedKeys.take(0.coerceAtLeast(sortedKeys.size - maxCapacity))
        deleteEvents(oldKeys)
    }

    private fun insertOrUpdateEvent(id: String, event: Event) {
        with(sharedPref.edit()) {
            putString(id, Gson().toJson(event))
            apply()
        }
    }
}
