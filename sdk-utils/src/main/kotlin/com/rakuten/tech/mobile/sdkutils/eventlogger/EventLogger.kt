package com.rakuten.tech.mobile.sdkutils.eventlogger

import android.content.Context
import androidx.annotation.VisibleForTesting
import com.rakuten.tech.mobile.sdkutils.BuildConfig
import com.rakuten.tech.mobile.sdkutils.logger.Logger
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.lang.ref.WeakReference
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors

/**
 * A remote logging utility that sends events to the Event Logger platform.
 * This is intended to be used internally by Rakuten's SDKs.
 */
@SuppressWarnings(
    "TooManyFunctions",
    "LargeClass"
)
object EventLogger {

    internal val log = Logger(EventLogger::class.java.simpleName).apply { setDebug(BuildConfig.DEBUG) }

    private lateinit var eventsSender: EventsSender
    private lateinit var eventsStorage: EventsStorage
    private lateinit var eventLoggerCache: EventLoggerCache
    private lateinit var eventLoggerHelper: EventLoggerHelper
    private lateinit var tasksQueue: ExecutorService
    @Volatile private var isConfigureCalled = false

    // ------------------------------------Public APIs-----------------------------------------------

    /**
     * Initializes the event logging utility. Call this as early as possible in the application lifecycle, such as
     * `onCreate` or other initialization methods.
     *
     * @param context Application context.
     * @param apiUrl Optional API URL that will override the default configuration.
     * @param apiKey Optional API Key that will override the default configuration.
     */
    @Synchronized
    fun configure(
        context: Context,
        apiUrl: String? = null,
        apiKey: String? = null
    ) {
        if (isConfigureCalled) {
            log.debug("Event Logger has been configured already")
            return
        }

        val (realApiUrl, realApiKey) = if (!apiUrl.isNullOrEmpty() && !apiKey.isNullOrEmpty()) {
            Pair(apiUrl, apiKey)
        } else {
            Pair(Config.EVENT_LOGGER_BASE_URL, Config.EVENT_LOGGER_API_KEY)
        }
        initialize(context, realApiUrl, realApiKey)
    }

    /**
     * Logs a critical event - an error that may cause the caller to not function properly. This event is sent
     * automatically.
     *
     * @param sourceName Source of the event, e.g. "inappmessaging".
     * @param sourceVersion Source' version, e.g. "1.0.0".
     * @param errorCode Source' error code or HTTP backend response code e.g. "500".
     * @param errorMessage Description of the error. Make it as descriptive as possible, for example, the stacktrace
     * of an exception.
     * @param info Optional parameter to attach other useful key-value pair, such as filename, method, line number, etc.
     */
    fun sendCriticalEvent(
        sourceName: String,
        sourceVersion: String,
        errorCode: String,
        errorMessage: String,
        info: Map<String, String>? = null
    ) {
        logEvent(EventType.CRITICAL, sourceName, sourceVersion, errorCode, errorMessage, info)
    }

    /**
     * Logs a warning event - an unexpected situation that the caller can recover from. This event is sent at a later
     * time.
     *
     * @param sourceName Source of the event, e.g. "inappmessaging".
     * @param sourceVersion Source' version, e.g. "1.0.0".
     * @param errorCode Source' error code or HTTP backend response code e.g. "500".
     * @param errorMessage Description of the error. Make it as descriptive as possible, for example, the stacktrace
     * of an exception.
     * @param info Optional parameter to attach other useful key-value pair, such as filename, method, line number, etc.
     */
    fun sendWarningEvent(
        sourceName: String,
        sourceVersion: String,
        errorCode: String,
        errorMessage: String,
        info: Map<String, String>? = null
    ) {
        logEvent(EventType.WARNING, sourceName, sourceVersion, errorCode, errorMessage, info)
    }

    // ------------------------------------Internal APIs-----------------------------------------------

    private fun buildEventLoggerHttpClient(baseUrl: String): Retrofit {
        val realUrl = if (baseUrl.endsWith('/')) baseUrl else "$baseUrl/"
        return Retrofit
            .Builder()
            .baseUrl(realUrl)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
    }

    private fun initialize(
        context: Context,
        apiUrl: String,
        apiKey: String
    ) {
        // Build dependencies
        initialize(
            eventsSender = RetrofitEventsSender(
                buildEventLoggerHttpClient(apiUrl).create(RetrofitEventsSender.Api::class.java),
                apiKey
            ),
            eventsStorage = SharedPreferencesEventsStorage(
                context.getSharedPreferences(Config.EVENTS_STORAGE_FILENAME, Context.MODE_PRIVATE)
            ),
            eventLoggerCache = SharedPreferencesEventLoggerCache(
                context.getSharedPreferences(Config.EVENT_LOGGER_GENERAL_CACHE_FILENAME, Context.MODE_PRIVATE)
            ),
            eventLoggerHelper = EventLoggerHelper(
                WeakReference(context.applicationContext)
            ),
            tasksQueue = Executors.newSingleThreadExecutor()
        )
    }

    /**
     * Registers to the background to foreground app transition and if the TTL expired, will send all events in storage.
     */
    @VisibleForTesting
    @Synchronized
    internal fun initialize(
        eventsSender: EventsSender,
        eventsStorage: EventsStorage,
        eventLoggerCache: EventLoggerCache,
        eventLoggerHelper: EventLoggerHelper,
        tasksQueue: ExecutorService
    ) {
        this.eventsSender = eventsSender
        this.eventsStorage = eventsStorage
        this.eventLoggerCache = eventLoggerCache
        this.eventLoggerHelper = eventLoggerHelper
        this.tasksQueue = tasksQueue
        this.isConfigureCalled = true

        tasksQueue.safeExecute {
            registerToAppTransitions()
            if (isTtlExpired()) {
                sendAllEventsInStorage()
            }
        }
    }

    @SuppressWarnings(
        "LongParameterList"
    )
    private fun logEvent(
        eventType: EventType,
        sourceName: String,
        sourceVersion: String,
        errorCode: String,
        errorMessage: String,
        info: Map<String, String>?
    ) {
        if (!canProcessEvent(sourceName, sourceVersion, errorCode, errorMessage))
            return

        tasksQueue.safeExecute {
            val eventId = generateEventIdentifier(eventType.displayName, eventLoggerHelper.getMetadata().appVer,
                sourceName, errorCode, errorMessage)
            val storedEvent = eventsStorage.getEventById(eventId)
            val eventToProcess = (storedEvent ?: eventLoggerHelper.buildEvent(eventType, sourceName, sourceVersion,
                errorCode, errorMessage, info))
                .incrementCount()

            val isNewEvent = storedEvent == null
            insertOrUpdateEvent(eventId, eventToProcess, isNewEvent)
            sendEventIfNeeded(eventType, eventId, eventToProcess, isNewEvent)
        }
    }

    /**
     * Validates the incoming event.
     *
     * @return false if [configure] wasn't called yet, or any of the supplied required [sendWarningEvent] or
     * [sendCriticalEvent] parameter is null or empty, otherwise true.
     */
    @SuppressWarnings(
        "ReturnCount"
    )
    private fun canProcessEvent(
        sourceName: String,
        sourceVersion: String,
        errorCode: String,
        errorMessage: String
    ): Boolean {
        if (!isConfigureCalled) {
            log.debug("Event Logger is not configured, skip")
            return false
        }

        val isValidSourceInfo = sourceName.isNotEmpty() && sourceVersion.isNotEmpty()
        val isValidErrorInfo = errorCode.isNotEmpty() && errorMessage.isNotEmpty()
        if (!isValidSourceInfo || !isValidErrorInfo) {
            log.warn("Event contains an empty parameter, skip")
            return false
        }

        return true
    }

    private fun insertOrUpdateEvent(eventId: String, event: Event, isNewEvent: Boolean) {
        if (isNewEvent) {
            eventsStorage.insertEvent(eventId, event)
        } else {
            eventsStorage.updateEvent(eventId, event)
        }
    }

    private fun sendEventIfNeeded(
        eventType: EventType,
        eventId: String,
        event: Event,
        isNewEvent: Boolean
    ) {
        // If full, send all of the events
        if (eventsStorage.getCount() >= Config.MAX_EVENTS_COUNT) {
            sendAllEventsInStorage(isNewEvent)
            return
        }

        if (!isNewEvent) {
            return
        }

        when (eventType) {
            // Send the unique critical event immediately, and convert it to a warning type to prevent multiple
            // alerts in server
            EventType.CRITICAL -> sendEvents(
                events = listOf(event),
                onSuccess = {
                    eventsStorage.updateEvent(eventId, event.setType(EventType.WARNING.displayName))
                }
            )
            // do nothing
            else -> {}
        }
    }

    private fun registerToAppTransitions() {
        // ToDo
    }

    private fun sendAllEventsInStorage(deleteOldEventsOnFailure: Boolean = false) {
        val allEvents = eventsStorage.getAllEvents()
        sendEvents(
            events = allEvents.values.toList(),
            onSuccess = {
                eventLoggerCache.setTtlReferenceTime(System.currentTimeMillis())
                this.eventsStorage.deleteEvents(allEvents.keys.toList())
            },
            onFailure = {
                if (deleteOldEventsOnFailure) {
                    eventsStorage.deleteOldEvents(Config.MAX_EVENTS_COUNT)
                }
            }
        )
    }

    private fun sendEvents(events: List<Event>, onSuccess: (() -> Unit)? = null, onFailure: (() -> Unit)? = null) {
        if (events.isEmpty()) return

        eventsSender.pushEvents(
            events = events,
            onSuccess = onSuccess,
            onFailure = onFailure
        )
    }

    private fun isTtlExpired(): Boolean {
        val currentTime = System.currentTimeMillis()
        val referenceTime = eventLoggerCache.getTtlReferenceTime()

        if (referenceTime == -1L) { // never pushed before
            eventLoggerCache.setTtlReferenceTime(currentTime)
            return false
        }
        return currentTime - referenceTime >= Config.TTL_EXPIRY_MILLIS
    }

    @SuppressWarnings(
        "SwallowedException",
        "TooGenericExceptionCaught"
    )
    /**
     * ExecutorService extension for fail-safe execution of [command].
     */
    private fun <T> ExecutorService.safeExecute(command: () -> T?) {
        try {
            this.execute { command() }
        } catch (e: Exception) {
            log.debug("Failed to execute command")
        }
    }

    internal object Config {
        const val MAX_EVENTS_COUNT = 50
        const val TTL_EXPIRY_MILLIS = 3600 * 1000 * 12 // 12 hours
        const val EVENT_LOGGER_BASE_URL = BuildConfig.EVENT_LOGGER_API_URL
        const val EVENT_LOGGER_API_KEY = BuildConfig.EVENT_LOGGER_API_KEY
        const val EVENTS_STORAGE_FILENAME = "${BuildConfig.LIBRARY_PACKAGE_NAME}.eventlogger.events"
        const val EVENT_LOGGER_GENERAL_CACHE_FILENAME = "${BuildConfig.LIBRARY_PACKAGE_NAME}.eventlogger.cache"
    }
}
