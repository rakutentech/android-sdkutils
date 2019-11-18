package com.rakuten.tech.mobile.sdkutils.sample

import android.app.Activity
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.databinding.DataBindingUtil
import com.rakuten.tech.mobile.sdkutils.sample.databinding.ActivityMainBinding

class MainActivity: Activity() {

    private lateinit var binding: ActivityMainBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        binding = DataBindingUtil.setContentView(this, R.layout.activity_main)
        binding.activity = this
    }

    private fun <T> showToast(title: String, getter: () -> T) {
        val message = try {
            "$title = ${getter.invoke()}"
        } catch (e: Exception) {
            Log.e("SDK Utils Sample", "Error", e)

            "Error: ${e.message}"
        }

        Toast.makeText(this, message, Toast.LENGTH_SHORT)
            .show()
    }
}
