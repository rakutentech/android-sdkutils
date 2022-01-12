package com.rakuten.tech.mobile.sdkutils.network

import okhttp3.HttpUrl
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.mockwebserver.MockResponse
import okhttp3.mockwebserver.MockWebServer
import org.amshove.kluent.shouldBeEqualTo
import org.junit.After
import org.junit.Before
import org.junit.Test
import java.util.logging.Level
import java.util.logging.LogManager

class HeaderInterceptorSpec {

    private val server = MockWebServer()
    private lateinit var url: HttpUrl

    init {
        LogManager.getLogManager()
            .getLogger(MockWebServer::class.java.name).level = Level.OFF
    }

    @Before
    fun setup() {
        server.start()
        server.enqueue(MockResponse())
        url = server.url("")
    }

    @After
    fun tearDown() {
        server.shutdown()
    }

    @Test
    fun `should attach the provided header`() {
        val client = OkHttpClient.Builder().addHeaderInterceptor(
            "test_header_name" to "test_header_value"
        ).build()

        client.newCall(
            Request.Builder().url(url).build()
        ).execute()

        server.takeRequest().headers["test_header_name"] shouldBeEqualTo "test_header_value"
    }

    @Test
    fun `should attach all of the provided headers`() {
        val client = OkHttpClient.Builder().addHeaderInterceptor(
                "test_header_name" to "test_header_value",
                "test_header_name_2" to "test_header_value_2"
        ).build()

        client.newCall(
            Request.Builder().url(url).build()
        ).execute()
        val headers = server.takeRequest().headers

        headers["test_header_name"] shouldBeEqualTo "test_header_value"
        headers["test_header_name_2"] shouldBeEqualTo "test_header_value_2"
    }

    @Test
    fun `should not modify other headers`() {
        val client = OkHttpClient.Builder().addHeaderInterceptor(
            "test_header_name" to "test_header_value"
        ).build()

        client.newCall(
            Request.Builder()
                .url(url)
                .addHeader("another_header_name", "another_header_value")
                .build()
        ).execute()

        server.takeRequest().headers["another_header_name"] shouldBeEqualTo "another_header_value"
    }
}
