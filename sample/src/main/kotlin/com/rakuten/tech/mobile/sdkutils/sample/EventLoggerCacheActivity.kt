package com.rakuten.tech.mobile.sdkutils.sample

import android.content.Context
import android.content.SharedPreferences
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.databinding.DataBindingUtil
import com.rakuten.tech.mobile.sdkutils.sample.databinding.ActivityEventLoggerCacheBinding
import org.json.JSONObject

@Suppress(
    "UndocumentedPublicClass",
    "MagicNumber"
)
class EventLoggerCacheActivity : AppCompatActivity() {

    private lateinit var binding: ActivityEventLoggerCacheBinding
    private lateinit var eventsCache: SharedPreferences

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = DataBindingUtil.setContentView(this, R.layout.activity_event_logger_cache)
        eventsCache = this
            .getSharedPreferences("com.rakuten.tech.mobile.sdkutils.eventlogger.events", Context.MODE_PRIVATE)
        setCacheText()
    }

    private fun setCacheText() {

        if (eventsCache.all.isEmpty()) {
            binding.eventsStorageText.text = "<empty>"
            return
        }

        val textBuilder = StringBuilder(0)
        for (event in eventsCache.all) {
            textBuilder.append(event.key)
            textBuilder.append("\n")
            textBuilder.append(
                JSONObject(event.value.toString()).toString(4)
            )
            textBuilder.append("\n\n\n")
        }

        binding.eventsStorageText.text = textBuilder
    }
}
