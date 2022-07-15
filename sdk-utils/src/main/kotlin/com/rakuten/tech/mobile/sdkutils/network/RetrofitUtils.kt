package com.rakuten.tech.mobile.sdkutils.network

import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
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
