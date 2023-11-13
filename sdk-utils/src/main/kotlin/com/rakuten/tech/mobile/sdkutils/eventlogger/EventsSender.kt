package com.rakuten.tech.mobile.sdkutils.eventlogger

import android.util.Log
import okhttp3.ResponseBody
import retrofit2.Call
import retrofit2.http.Body
import retrofit2.http.POST
import java.io.IOException

internal interface EventsSender {
    fun pushEvents(events: List<Event>, onSuccess: (() -> Unit)?)
}

internal class RetrofitEventsSender(private val retrofitApi: Api) : EventsSender {

    internal interface Api {
        @POST("events")
        fun sendEvents(@Body events: List<Event>): Call<ResponseBody?>
    }

    /**
     * Thread-blocking operation that sends the supplied [events] to backend and invokes the optional [onSuccess]
     * callback if succeeded.
     */
    @SuppressWarnings(
        "TooGenericExceptionCaught",
        "SwallowedException"
    )
    override fun pushEvents(events: List<Event>, onSuccess: (() -> Unit)?) {
        if (events.isEmpty())
            return

        try {
            val request = retrofitApi.sendEvents(events).execute()
            if (request.isSuccessful) {
                EventLogger.log.debug("Successfully pushed ${events.size} events")
                onSuccess?.invoke()
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
    }
}
