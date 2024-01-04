package com.rakuten.tech.mobile.sdkutils.eventlogger

import android.os.Handler
import com.nhaarman.mockitokotlin2.mock
import com.nhaarman.mockitokotlin2.never
import com.nhaarman.mockitokotlin2.verify
import okhttp3.mockwebserver.MockResponse
import okhttp3.mockwebserver.MockWebServer
import org.amshove.kluent.shouldBeEqualTo
import org.amshove.kluent.shouldContainAll
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.Mockito.*
import org.robolectric.RobolectricTestRunner
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.net.HttpURLConnection
import java.time.Duration

@RunWith(RobolectricTestRunner::class)
class EventsSenderSpec {

    private val mockHandler: Handler = mock(Handler::class.java)
    private val mockWebServer = MockWebServer()
    private val retrofitApi = Retrofit
        .Builder()
        .baseUrl(mockWebServer.url("").toString())
        .addConverterFactory(GsonConverterFactory.create())
        .build()
        .create(RetrofitEventsSender.Api::class.java)
    private val eventsSender = RetrofitEventsSender(retrofitApi, "mockApiKey", mockHandler)

    @Before
    fun setup() {
        `when`(mockHandler.postDelayed(any(Runnable::class.java), anyLong()))
            .thenAnswer {
                (it.arguments[0] as Runnable).run()
                true
            }
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
    fun `should invoke success callback if server responded with HTTP OK`() {
        mockWebServer.enqueue(
            MockResponse()
                .setResponseCode(HttpURLConnection.HTTP_OK)
        )

        val callback: () -> Unit = mock()
        eventsSender.pushEvents(
            listOf(EventLoggerTestUtil.generateRandomEvent()),
            callback,
            null
        )

        verify(callback, timeout(Duration.ofMillis(500))).invoke()
    }

    @Test
    fun `should invoke failure callback if server responded with HTTP failure`() {
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
}
