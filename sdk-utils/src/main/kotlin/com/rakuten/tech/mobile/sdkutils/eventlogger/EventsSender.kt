package com.rakuten.tech.mobile.sdkutils.eventlogger

import com.rakuten.tech.mobile.sdkutils.network.enqueueWithRetriesOnNetworkError
import okhttp3.ResponseBody
import retrofit2.Call
import retrofit2.http.Body
import retrofit2.http.Header
import retrofit2.http.POST
import java.util.concurrent.TimeUnit

internal interface EventsSender {

    /**
     * Asynchronously sends [events] to the server.
     */
    fun pushEvents(events: List<Event>, onSuccess: (() -> Unit)? = null, onFailure: (() -> Unit)? = null)

    companion object {
        const val HEADER_CLIENT_API_KEY = "x-client-apikey"
        const val MAX_REQUEST_RETRIES = 2
        const val INITIAL_RETRY_DELAY_MILLIS = 1000L * 15
    }
}

internal class RetrofitEventsSender(private val retrofitApi: Api, private val apiKey: String) : EventsSender {

    internal interface Api {
        @POST("external/logging/error")
        fun sendEvents(
            @Header(EventsSender.HEADER_CLIENT_API_KEY) apiKey: String,
            @Body events: List<Event>
        ): Call<ResponseBody?>
    }

    override fun pushEvents(events: List<Event>, onSuccess: (() -> Unit)?, onFailure: (() -> Unit)?) {
        if (events.isEmpty())
            return

        val call = retrofitApi.sendEvents(apiKey, events)
        call.enqueueWithRetriesOnNetworkError(
            maxRetries = EventsSender.MAX_REQUEST_RETRIES,
            retryDelayMillis = EventsSender.INITIAL_RETRY_DELAY_MILLIS,
            onRetry = { retryCount, delayMs ->
                EventLogger.log.debug("Retry $retryCount after ${TimeUnit.MILLISECONDS.toSeconds(delayMs)}s")
            },
            onSuccess = {
                EventLogger.log.debug("Successfully pushed ${events.size} events")
                onSuccess?.invoke()
            },
            onFailure = {
                EventLogger.log.warn("Unable to push events")
                onFailure?.invoke()
            }
        )
    }
}