package com.rakuten.tech.mobile.sdkutils.sample

import android.app.Activity
import android.os.Bundle
import android.widget.Toast
import androidx.databinding.DataBindingUtil
import com.rakuten.tech.mobile.sdkutils.RasSdkHeaders
import com.rakuten.tech.mobile.sdkutils.okhttp.addHeaderInterceptor
import com.rakuten.tech.mobile.sdkutils.sample.databinding.ActivityMainBinding
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import okhttp3.Request

class MainActivity: Activity() {

    private lateinit var binding: ActivityMainBinding

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
                }

                showToast("Request sent successfully. Returned resposne: ${response.body()!!.string()}")
            }
        }

    }

    private fun showToast(message: String) =
        Toast.makeText(this, message, Toast.LENGTH_SHORT)
            .show()
}
