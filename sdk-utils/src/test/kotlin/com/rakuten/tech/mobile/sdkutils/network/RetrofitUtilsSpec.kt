package com.rakuten.tech.mobile.sdkutils.network

import android.os.Handler
import com.nhaarman.mockitokotlin2.doAnswer
import okhttp3.OkHttpClient
import okhttp3.ResponseBody
import org.amshove.kluent.shouldBeEqualTo
import org.amshove.kluent.shouldBeInstanceOf
import org.amshove.kluent.shouldContain
import org.junit.Test
import org.mockito.Mockito.*
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors
import com.nhaarman.mockitokotlin2.mock
import okhttp3.MediaType

class RetrofitUtilsSpec {
    private val baseUrl = "www.example.com"

    @Test
    fun `should create retrofit instance`() {
        val retrofit = Retrofit.Builder().build("http://$baseUrl")

        retrofit.shouldBeInstanceOf(Retrofit::class.java)
        retrofit.baseUrl().host() shouldBeEqualTo baseUrl
    }

    @Test
    fun `should create retrofit instance with parameters`() {
        val okHttpClient: OkHttpClient = OkHttpClient.Builder().build()
        val gsonConverterFactory: GsonConverterFactory = GsonConverterFactory.create()
        val executor: ExecutorService = Executors.newSingleThreadExecutor()
        val retrofit = Retrofit.Builder().build("http://$baseUrl", okHttpClient, gsonConverterFactory, executor)

        retrofit.shouldBeInstanceOf(Retrofit::class.java)
        retrofit.baseUrl().host() shouldBeEqualTo baseUrl
        retrofit.callFactory() shouldBeEqualTo okHttpClient
        retrofit.converterFactories() shouldContain gsonConverterFactory
        retrofit.callbackExecutor() shouldBeEqualTo executor
    }

    @Test
    fun `should not retry if call is successful`() {
        val call = mock(Call::class.java) as Call<ResponseBody?>
        val cb = mock(Callback::class.java) as Callback<ResponseBody?>

        `when`(call.enqueue(any())).doAnswer {
            cb.onResponse(call, Response.success(null))
            null
        }

//        cb.onResponse(call, Response.success(null))

        val handler = mock(Handler::class.java)
        setupHandler(handler)
        val callback: (Int, Long) -> Unit = mock()
        call.enqueueAndRetryOnNetworkError(
            onRetry = callback,
            handler = handler
        )

        verify(callback, never()).invoke(anyInt(), anyLong())
    }

    @Test
    fun `should not retry if backend responded with error`() {
        val call = mock(Call::class.java) as Call<ResponseBody?>
        val cb = mock(Callback::class.java) as Callback<ResponseBody?>

        `when`(call.enqueue(any()))
            .thenAnswer {
                cb.onResponse(call, Response.error(500,
                    ResponseBody.create(MediaType.get("application/json"), "")
                ))
            }

        val handler = mock(Handler::class.java)
        setupHandler(handler)
        val callback: (Int, Long) -> Unit = mock()
        call.enqueueAndRetryOnNetworkError(
            handler = handler,
            onRetry = callback
        )

        verify(callback, never()).invoke(anyInt(), anyLong())
    }

    @Test
    fun `should not retry if failure is not network error`() {
        val call = mock(Call::class.java) as Call<ResponseBody?>
        val cb = mock(Callback::class.java) as Callback<ResponseBody?>

        `when`(call.enqueue(any()))
            .thenAnswer {
                cb.onFailure(call, Throwable(""))
            }

        val handler = mock(Handler::class.java)
        setupHandler(handler)
        val callback: (Int, Long) -> Unit = mock()
        call.enqueueAndRetryOnNetworkError(
            maxRetries = 1,
            handler = handler,
            onRetry = callback
        )

        verify(callback, never()).invoke(anyInt(), anyLong())
    }

    @Test
    fun `should retry if failure is network error`() {
    }

    private fun setupHandler(mockHandler: Handler) {
        `when`(mockHandler.postDelayed(any(Runnable::class.java), anyLong())).thenAnswer {
            val runnable = it.arguments[0] as Runnable
            runnable.run()
            true
        }
    }
}
