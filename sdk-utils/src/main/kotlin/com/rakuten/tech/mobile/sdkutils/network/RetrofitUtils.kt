package com.rakuten.tech.mobile.sdkutils.network

import android.os.Handler
import okhttp3.OkHttpClient
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.io.IOException
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors

/**
 * This method returns a reference of Retrofit.
 * Retrofit is handling API calls. Adding GsonConverterFactory for parsing returned JSON.
 * Adding OkHttp to handle the network requests.
 *
 * @param baseUrl the API base URL.
 * @param okHttpClient the HTTP client used for requests.
 * @param gsonConverterFactory converter factory for serialization and deserialization of objects.
 * @param executor the executor on which Callback methods are invoked when returning Call
 * from the service method.
 *
 * @return the [Retrofit] instance using the configured values.
 */
fun Retrofit.Builder.build(
    baseUrl: String,
    okHttpClient: OkHttpClient = OkHttpClient.Builder().build(),
    gsonConverterFactory: GsonConverterFactory = GsonConverterFactory.create(),
    executor: ExecutorService = Executors.newSingleThreadExecutor()
): Retrofit = this.baseUrl(baseUrl)
    .addConverterFactory(gsonConverterFactory)
    .client(okHttpClient)
    .callbackExecutor(executor)
    .build()

/**
 * Retrofit [Call.enqueue] extension to perform retries after network failure.
 *
 * @param maxRetries the maximum retries after failure.
 * @param retryDelayMillis the initial retry delay after failure.
 * @param onRetry called when operation will be retried.
 * @param onSuccess called when operation succeeds.
 * @param onFailure called when operation fails even after retries.
 * @param handler handler to use for delaying the retries.
 */
@SuppressWarnings(
    "MagicNumber",
    "LongParameterList"
)
fun <T> Call<T>.enqueueAndRetryOnNetworkError(
    maxRetries: Int = 3,
    retryDelayMillis: Long = 15 * 1000,
    onRetry: (retryCount: Int, delayMillis: Long) -> Unit = { _, _ -> },
    onSuccess: (response: Response<T>) -> Unit = {},
    onFailure: (t: Throwable) -> Unit = {},
    handler: Handler
) {
    fun <T> Call<T>.enqueueWithDelay(delayMillis: Long, callback: Callback<T>) {
        handler.postDelayed(
            { enqueue(callback) },
            delayMillis
        )
    }

    fun calculateExponentialDelay(retryCount: Int, initialDelayMillis: Long): Long {
        return initialDelayMillis * (1L shl (retryCount - 1))
    }

    /**
     * Returns true if within max retries and when [t] is an [IOException] which is typically thrown when there is
     * network issue, such as device being offline.
     */
    fun shouldRetry(retryCount: Int, t: Throwable): Boolean {
        if (retryCount < maxRetries &&
            t is IOException
        ) {
            return true
        }
        return false
    }

    val retryCallback = object : Callback<T> {
        private var retryCount = 0

        override fun onResponse(call: Call<T>, response: Response<T>) {
            if (response.isSuccessful) {
                onSuccess(response)
            } else {
                // Server responded with errors
                onFailure(Throwable("Code: ${response.code()}, Message: ${response.message()}"))
            }
        }

        override fun onFailure(call: Call<T>, t: Throwable) {
            // Check if should retry
            if (shouldRetry(retryCount, t)) {
                retryCount++
                val delayMillis = calculateExponentialDelay(retryCount, retryDelayMillis)
                onRetry(retryCount, delayMillis)
                call.clone().enqueueWithDelay(delayMillis, this)
            } else {
                onFailure(t)
            }
        }
    }

    // Enqueue first call without delay
    enqueueWithDelay(0, retryCallback)
}
