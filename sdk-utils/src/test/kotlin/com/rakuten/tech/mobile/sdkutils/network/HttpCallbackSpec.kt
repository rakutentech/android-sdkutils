package com.rakuten.tech.mobile.sdkutils.network

import okhttp3.HttpUrl
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.Response
import okhttp3.mockwebserver.MockResponse
import okhttp3.mockwebserver.MockWebServer
import org.amshove.kluent.shouldBeEqualTo
import org.amshove.kluent.shouldNotBeEqualTo
import org.junit.After
import org.junit.Before
import org.junit.Test
import java.util.concurrent.TimeUnit
import java.util.logging.Level
import java.util.logging.LogManager

class HttpCallbackSpec {

    private val server = MockWebServer()
    private lateinit var url: HttpUrl

    init {
        LogManager.getLogManager()
            .getLogger(MockWebServer::class.java.name).level = Level.OFF
    }

    @Before
    fun setup() {
        server.start()
        url = server.url("")
    }

    @After
    fun tearDown() {
        server.shutdown()
    }

    @Test
    fun `should return success response`() {
        var expectedResponse: Response? =  null
        var exception: java.lang.Exception? = null

        getResponse( "https://example.com",
            { expectedResponse = it }, { exception = it })

        server.takeRequest(5, TimeUnit.SECONDS)
        expectedResponse?.code() shouldBeEqualTo 200
        exception shouldBeEqualTo null
    }

    @Test
    fun `should return exception`() {
        var expectedResponse: Response? =  null
        var exception: java.lang.Exception? = null

        getResponse( "https://invalid-url",
            { expectedResponse = it },
            { exception = it }
        )

        server.takeRequest(5, TimeUnit.SECONDS)
        expectedResponse shouldBeEqualTo null
        exception shouldNotBeEqualTo null
    }

    private fun getResponse(url: String, success: (response: Response) -> Unit, failure: (exception: Exception) -> Unit) {
        val request = Request.Builder().url(url).build()
        val client = OkHttpClient.Builder().build()
        client.newCall(request).enqueue(HttpCallback(success, failure))
    }

}