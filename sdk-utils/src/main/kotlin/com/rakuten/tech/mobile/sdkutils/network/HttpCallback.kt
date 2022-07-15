package com.rakuten.tech.mobile.sdkutils.network

import okhttp3.Call
import okhttp3.Callback
import okhttp3.Response
import java.io.IOException

/**
 * Callback Class that communicate the remote server response.
 *  ```
 * // for example:
 * client.newCall(request).enqueue(HttpCallback(
 *  {
 *      response ->  ...
 *  },{
 *      exception ->  ...
 *  }))
 *
 *  //or by using lambda function:
 *  //1- create your function e.g.
 *  fun get(success: (response: Response) -> Unit, failure: (exception: Exception) -> Unit) {
 *      val request = Request.Builder().url(url).build()
 *      ...
 *      client.newCall(request).enqueue(HttpCallback(success, failure))
 *  }
 *
 *  // 2- get the response
 *  get({ response = it }, { exception = it })
 * ```
 * @property success the HTTP [Response] successfully returned by the remote server.
 * @property failure the [IOException] returned when the request could not be executed.
 */
class HttpCallback(
    private val success: (response: Response) -> Unit,
    private val failure: (exception: Exception) -> Unit
) : Callback {

    override fun onFailure(call: Call, e: IOException) {
        failure(e)
    }

    override fun onResponse(call: Call, response: Response) {
        success(response)
    }
}
