package com.rakuten.tech.mobile.sdkutils.sample

import android.app.Activity
import android.os.Bundle
import android.widget.Toast
import androidx.databinding.DataBindingUtil
import com.rakuten.tech.mobile.sdkutils.logger.Logger
import com.rakuten.tech.mobile.sdkutils.RasSdkHeaders
import com.rakuten.tech.mobile.sdkutils.network.addHeaderInterceptor
import com.rakuten.tech.mobile.sdkutils.sample.databinding.ActivityMainBinding
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import okhttp3.Request
import java.util.Date

@Suppress("UndocumentedPublicClass", "UndocumentedPublicFunction", "SpreadOperator")
class MainActivity : Activity() {

    private lateinit var binding: ActivityMainBinding
    private val log = Logger(MainActivity::class.java.simpleName)

    private val rasHeadersClient = OkHttpClient.Builder()
        .addHeaderInterceptor(
            *RasSdkHeaders(
                appId = "test-app-name",
                subscriptionKey = "test-subscription-key",
                sdkName = "Test Sdk Name",
                sdkVersion = "2.0.0"
            ).asArray()
        ).build()

    override fun onCreate(savedInstanceState: Bundle?) {

        // enable debug logs (by default only info, warn and error are logged)
        Logger.setDebug(true)
        log.debug("simple debug log at %s", listOf(Date()))

        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        binding = DataBindingUtil.setContentView(this, R.layout.activity_main)
        binding.activity = this
    }

    fun onSendRasHeadersRequestClick() {
        CoroutineScope(Dispatchers.Default).launch {
            val response = rasHeadersClient.newCall(
                Request.Builder()
                    .url("https://www.example.com")
                    .build()
            ).execute()

            withContext(Dispatchers.Main) {
                if (!response.isSuccessful) {
                    showToast("Error: Failed to send request. Server returned: $response")
                    return@withContext
                }

                showToast("Request sent successfully. Returned response: ${response.body()?.toString()}")
            }
        }
    }

    private fun showToast(message: String) =
        Toast.makeText(this, message, Toast.LENGTH_SHORT)
            .show()
}
