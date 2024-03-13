package com.rakuten.tech.mobile.sdkutils.network

import android.os.Handler
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
import org.junit.Before
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import java.io.IOException
import java.time.Duration

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
}

@RunWith(RobolectricTestRunner::class)
class RetrofitUtilsEnqueueAndRetrySpec {

    private val mockHandler: Handler = mock(Handler::class.java)
    private val call = mock(Call::class.java) as Call<ResponseBody?>

    @Before
    fun setup() {
        `when`(mockHandler.postDelayed(any(Runnable::class.java), anyLong()))
            .thenAnswer {
                (it.arguments[0] as Runnable).run()
                true
            }
    }

    @Test
    fun `should invoke success callback if successful`() {
        `when`(call.enqueue(any()))
            .thenAnswer {
                (it.arguments[0] as Callback<ResponseBody?>)
                    .onResponse(call, Response.success(null))
                null
            }

        val callback: (Response<ResponseBody?>) -> Unit = mock()
        call.enqueueAndRetryOnNetworkError(
            onSuccess = callback,
            handler = mockHandler
        )

        verify(callback, timeout(Duration.ofMillis(500))).invoke(
            com.nhaarman.mockitokotlin2.any()
        )
    }

    @Test
    fun `should invoke failure callback`() {
        `when`(call.enqueue(any()))
            .thenAnswer {
                (it.arguments[0] as Callback<ResponseBody?>)
                    .onFailure(call, Throwable(""))
                null
            }

        val callback: (Throwable) -> Unit = mock()
        call.enqueueAndRetryOnNetworkError(
            onFailure = callback,
            handler = mockHandler
        )
        verify(callback, timeout(Duration.ofMillis(500)))
            .invoke(any(Throwable::class.java) ?: Throwable())
    }

    @SuppressWarnings("LongMethod")
    @Test
    fun `should retry on IOException and invoke failure callback`() {
        `when`(call.clone())
            .thenReturn(call)
        `when`(call.enqueue(any()))
            .thenAnswer {
                (it.arguments[0] as Callback<ResponseBody?>)
                    .onFailure(call, IOException(""))
                null
            }
            .thenAnswer {
                (it.arguments[0] as Callback<ResponseBody?>)
                    .onFailure(call, Throwable(""))
                null
            }

        val callback: (Throwable) -> Unit = mock()
        call.enqueueAndRetryOnNetworkError(
            onFailure = callback,
            handler = mockHandler
        )
        verify(callback, timeout(Duration.ofMillis(500)))
            .invoke(any(Throwable::class.java) ?: Throwable())
    }
}
