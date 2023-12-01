package com.rakuten.tech.mobile.sdkutils.eventlogger

import androidx.annotation.WorkerThread
import okhttp3.ResponseBody
import retrofit2.Call
import retrofit2.http.Body
import retrofit2.http.Header
import retrofit2.http.POST
import java.io.IOException

internal interface EventsSender {

    /**
     * Thread-blocking operation that sends the supplied [events] to backend and invokes the optional [onSuccess]
     * callback if succeeded.
     */
    @WorkerThread
    fun pushEvents(events: List<Event>, onSuccess: (() -> Unit)? = null, onFailure: (() -> Unit)? = null)

    companion object {
        const val HEADER_CLIENT_API_KEY = "x-client-apikey"
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

    @SuppressWarnings(
        "TooGenericExceptionCaught",
        "SwallowedException"
    )
    override fun pushEvents(events: List<Event>, onSuccess: (() -> Unit)?, onFailure: (() -> Unit)?) {
        if (events.isEmpty())
            return

        try {
            val request = retrofitApi.sendEvents(apiKey, events).execute()
            if (request.isSuccessful) {
                EventLogger.log.debug("Successfully pushed ${events.size} events")
                onSuccess?.invoke()
                return
            } else {
                EventLogger.log.debug("Backend error: ${request.message()}")
                // ToDo : Retry mechanism if applicable
            }
        } catch (ie: IOException) {
            // Network error
            // ToDo : Retry mechanism if applicable
        } catch (re: RuntimeException) {
            // ToDo : Retry mechanism if applicable
        }
        onFailure?.invoke()
    }
}
