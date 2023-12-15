package com.rakuten.tech.mobile.sdkutils.sample

import android.app.Activity
import android.content.Context
import android.os.Bundle
import android.widget.RadioButton
import android.widget.Toast
import androidx.databinding.DataBindingUtil
import com.rakuten.tech.mobile.sdkutils.eventlogger.EventLogger
import com.rakuten.tech.mobile.sdkutils.sample.databinding.ActivityEventLoggerBinding
import org.json.JSONObject

class EventLoggerActivity : Activity() {

    private lateinit var binding: ActivityEventLoggerBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        binding = DataBindingUtil.setContentView(this, R.layout.activity_event_logger)
        binding.activity = this

        // EventLogger: use default API Key+URL
        EventLogger.configure(this)
    }

    fun onLogEventButtonClick() {
        val sdkName = binding.sdkNameText.text.toString().ifEmpty { "sdkutils" }
        val sdkVersion = binding.sdkVerText.text.toString().ifEmpty { com.rakuten.tech.mobile.sdkutils.BuildConfig.VERSION_NAME }
        val errorCode = binding.errorCodeText.text.toString()
        val errorMessage = binding.errorMsgText.text.toString()
        val numTimes = binding.numTimesText.text.toString().toIntOrNull() ?: 1
        val eventTypeRadId = binding.eventTypeRadioGrp.checkedRadioButtonId
        val eventType = findViewById<RadioButton>(eventTypeRadId).text.toString().lowercase()
        // TODO: how about the info? should it have a limit

        Toast.makeText(this,
            "sdkName: $sdkName,\n" +
                "sdkVersion: $sdkVersion,\n" +
                "errorCode: $errorCode,\n" +
                "errorMessage: $errorMessage,\n" +
                "sdkVersion: $sdkName,\n" +
                "numTimes: $numTimes,\n" +
                "eventType: $eventType",
            Toast.LENGTH_LONG
        ).show()

        when (eventType) {
            "critical" -> {
                repeat(numTimes) { EventLogger.sendCriticalEvent(sdkName, sdkVersion, errorCode, errorMessage) }
            }
            "warning" -> {
                repeat(numTimes) { EventLogger.sendWarningEvent(sdkName, sdkVersion, errorCode, errorMessage) }
            }
        }
    }

    fun onShowEventsCacheClick() {
        val eventsCache = this.getSharedPreferences("com.rakuten.tech.mobile.sdkutils.eventlogger.events", Context.MODE_PRIVATE)

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
