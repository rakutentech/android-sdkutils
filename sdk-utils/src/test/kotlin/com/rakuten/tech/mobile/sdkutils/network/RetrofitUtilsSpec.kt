package com.rakuten.tech.mobile.sdkutils.network

import okhttp3.OkHttpClient
import org.amshove.kluent.shouldBeEqualTo
import org.amshove.kluent.shouldBeInstanceOf
import org.amshove.kluent.shouldContain
import org.junit.Test
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors

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
