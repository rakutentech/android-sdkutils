@file:JvmName("OkHttpUtils")

package com.rakuten.tech.mobile.sdkutils.okhttp

import okhttp3.Interceptor
import okhttp3.OkHttpClient
import okhttp3.Response

/**
 * Adds an [Interceptor] to OkHttp's [okhttp3.OkHttpClient] which will add the provided headers
 * to all requests.
 * @param headers the headers to be added to all requests.
 */
fun OkHttpClient.Builder.addHeaderInterceptor(
    vararg headers: Pair<String, String>
): OkHttpClient.Builder = this.addNetworkInterceptor(HeaderInterceptor(headers))

private class HeaderInterceptor constructor(
    private val headers: Array<out Pair<String, String>>
) : Interceptor {

    override fun intercept(chain: Interceptor.Chain): Response {
        val requestBuilder = chain.request().newBuilder()
        headers.forEach { header ->
            requestBuilder.addHeader(header.first, header.second)
        }

        return chain.proceed(
            requestBuilder.build()
        )
    }
}
