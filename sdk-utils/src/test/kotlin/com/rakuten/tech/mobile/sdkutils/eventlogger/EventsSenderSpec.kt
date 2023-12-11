package com.rakuten.tech.mobile.sdkutils.eventlogger

import android.os.Handler
import com.nhaarman.mockitokotlin2.doAnswer
import com.nhaarman.mockitokotlin2.mock
import com.nhaarman.mockitokotlin2.never
import com.nhaarman.mockitokotlin2.verify
import okhttp3.ResponseBody
import okhttp3.mockwebserver.MockResponse
import okhttp3.mockwebserver.MockWebServer
import okhttp3.mockwebserver.SocketPolicy
import org.amshove.kluent.shouldBeEqualTo
import org.amshove.kluent.shouldContainAll
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.Mockito.*
import org.robolectric.RobolectricTestRunner
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.net.HttpURLConnection
import java.time.Duration

@RunWith(RobolectricTestRunner::class)
class EventsSenderSpec {

    private val mockWebServer = MockWebServer()
    private val retrofitApi = Retrofit
        .Builder()
        .baseUrl(mockWebServer.url("").toString())
        .addConverterFactory(GsonConverterFactory.create())
        .build()
        .create(RetrofitEventsSender.Api::class.java)
    private val mockHandler: Handler = mock(Handler::class.java)
    private val eventsSender = RetrofitEventsSender(retrofitApi, "mockApiKey", mockHandler)

    @Before
    fun setup() {
        setupHandler()
    }

    @Test
    fun `should do nothing if events list is empty`() {
        val callback: () -> Unit = mock()
        eventsSender.pushEvents(
            listOf(),
            callback
        )

        verify(callback, never()).invoke()
    }

    @Test
    @SuppressWarnings("LongMethod")
    fun `should send json request based on backend contract`() {
        mockWebServer.enqueue(MockResponse()
            .setResponseCode(HttpURLConnection.HTTP_OK))

        eventsSender.pushEvents(
            listOf(EventLoggerTestUtil.generateRandomEvent())
        )

        val request = mockWebServer.takeRequest()
        request.getHeader(EventsSender.HEADER_CLIENT_API_KEY) shouldBeEqualTo "mockApiKey"
        val requestBody = request.body.readUtf8()
        requestBody shouldContainAll listOf(
            "eventVersion",
            "eventType",
            "appId",
            "appName",
            "appVersion",
            "platform",
            "osVersion",
            "deviceModel",
            "deviceBrand",
            "deviceName",
            "sdkName",
            "sdkVersion",
            "errorCode",
            "errorMessage",
            "occurrenceCount"
        )
    }

    @Test
    fun `should invoke success callback if successful`() {
        mockWebServer.enqueue(MockResponse()
            .setResponseCode(HttpURLConnection.HTTP_OK))

        val callback: () -> Unit = mock()
        eventsSender.pushEvents(
            listOf(EventLoggerTestUtil.generateRandomEvent()),
            callback,
            null
        )

        verify(callback, timeout(Duration.ofMillis(500))).invoke()
    }

    @Test
    fun `should invoke failure callback if unsuccessful`() {
        mockWebServer.enqueue(MockResponse()
            .setResponseCode(HttpURLConnection.HTTP_INTERNAL_ERROR))

        val callback: () -> Unit = mock()
        eventsSender.pushEvents(
            listOf(EventLoggerTestUtil.generateRandomEvent()),
            null,
            callback
        )

        verify(callback, timeout(Duration.ofMillis(500))).invoke()
    }

    @Test
    fun `should retry and invoke failure callback`() {
        mockWebServer.enqueue(MockResponse()
            .setSocketPolicy(SocketPolicy.NO_RESPONSE))

        val callback: () -> Unit = mock()
        eventsSender.pushEvents(
            listOf(EventLoggerTestUtil.generateRandomEvent()),
            null,
            callback
        )

        verify(callback, timeout(Duration.ofMillis(500))).invoke()
    }

    @Test
    fun a() {
        val mockApi = mock(RetrofitEventsSender.Api::class.java)
        val mockCall = mock(Call::class.java) as Call<ResponseBody?>
        val sender = RetrofitEventsSender(mockApi, "mockApiKey", mockHandler)

        `when`(mockApi.sendEvents(anyString(), anyList()))
            .thenReturn(mockCall)
        `when`(mockCall.enqueue(any()))
            .thenAnswer {
                val cb = it.arguments[0] as Callback<ResponseBody?>
                cb.onFailure(mockCall, Throwable(""))
                null
            }

        val callback: () -> Unit = mock()
        sender.pushEvents(
            listOf(EventLoggerTestUtil.generateRandomEvent()),
            null,
            callback
        )

        verify(callback, timeout(Duration.ofMillis(500))).invoke()
    }

    private fun setupHandler() {
        `when`(mockHandler.postDelayed(any(Runnable::class.java), anyLong())).thenAnswer {
            val runnable = it.arguments[0] as Runnable
            runnable.run()
            true
        }
    }
}
