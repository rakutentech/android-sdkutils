package com.rakuten.tech.mobile.sdkutils.sample

import android.content.Intent
import android.os.Bundle
import android.widget.RadioButton
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.databinding.DataBindingUtil
import com.google.gson.Gson
import com.google.gson.JsonSyntaxException
import com.google.gson.reflect.TypeToken
import com.rakuten.tech.mobile.sdkutils.eventlogger.EventLogger
import com.rakuten.tech.mobile.sdkutils.sample.databinding.ActivityEventLoggerBinding
import kotlin.random.Random

@Suppress(
    "UndocumentedPublicClass",
    "UndocumentedPublicFunction",
    "MagicNumber",
    "TooManyFunctions"
)
class EventLoggerActivity : AppCompatActivity() {

    private lateinit var binding: ActivityEventLoggerBinding
    private val sdkName
        get() = binding.sdkNameText.text.toString().ifEmpty { "sdkutils" }
    private val sdkVersion
        get() = binding.sdkVerText.text.toString().ifEmpty { com.rakuten.tech.mobile.sdkutils.BuildConfig.VERSION_NAME }
    private val errorCode
        get() = binding.errorCodeText.text.toString()
    private val errorMessage
        get() = binding.errorMsgText.text.toString()
    private val numTimes
        get() = binding.numTimesText.text.toString().toIntOrNull() ?: 1
    private val eventTypeRadId
        get() = binding.eventTypeRadioGrp.checkedRadioButtonId
    private val eventType
        get() = findViewById<RadioButton>(eventTypeRadId).text.toString().lowercase()
    private val infoString
        get() = binding.addtnlInfoText.text.toString()
    private val info: Map<String, String>?
        get() = if (infoString.isEmpty()) null else jsonStringToMap(infoString)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = DataBindingUtil.setContentView(this, R.layout.activity_event_logger)
        binding.activity = this
        setDefaultsOrHints()

        EventLogger.configure(this, BuildConfig.EVENT_LOGGER_API_URL, BuildConfig.EVENT_LOGGER_API_KEY)
    }

    fun onLogEventButtonClick() {
        logEvent()
    }

    fun onLogUniqueEventButtonClick() {
        logEvent(true)
    }

    fun onCustomButtonClick() {
        binding.errorMsgText.setText("")
    }

    fun onException1ButtonClick() {
        binding.errorMsgText.setText(
            ArithmeticException().stackTraceToString().take(1000)
        )
    }

    fun onException2ButtonClick() {
        binding.errorMsgText.setText(
            IllegalArgumentException("Testing").stackTraceToString().take(1000)
        )
    }

    fun onShowEventsCacheClick() {
        val intent = Intent(this, EventLoggerCacheActivity::class.java)
        this.startActivity(intent)
    }

    private fun setDefaultsOrHints() {
        binding.apply {
            sdkNameText.setText("sdkutils")
            sdkVerText.setText(com.rakuten.tech.mobile.sdkutils.BuildConfig.VERSION_NAME)
            numTimesText.setText("1")
            addtnlInfoText.hint = """{ "key": "value" }"""
        }
    }

    @SuppressWarnings("LongMethod")
    private fun logEvent(randomizeMessage: Boolean = false) {
        when (eventType) {
            "critical" -> repeat(numTimes) {
                EventLogger.sendCriticalEvent(
                    sourceName = sdkName,
                    sourceVersion = sdkVersion,
                    errorCode = errorCode,
                    errorMessage = if (randomizeMessage) randomizeString() else errorMessage,
                    info = info
                )
            }
            "warning" -> repeat(numTimes) {
                EventLogger.sendWarningEvent(
                    sourceName = sdkName,
                    sourceVersion = sdkVersion,
                    errorCode = errorCode,
                    errorMessage = if (randomizeMessage) randomizeString() else errorMessage,
                    info = info
                )
            }
        }

        Toast.makeText(this, "Processed!", Toast.LENGTH_SHORT).show()
    }

    @SuppressWarnings("SwallowedException")
    private fun jsonStringToMap(jsonString: String): Map<String, String>? {
        val type = object : TypeToken<Map<String, String>>() {}.type
        return try {
            Gson().fromJson(jsonString, type)
        } catch (e: JsonSyntaxException) {
            Toast.makeText(this, "Not a valid Json representation!", Toast.LENGTH_SHORT).show()
            return null
        }
    }

    private fun randomizeString(length: Int = 20) = (1..length)
        .map { Random.nextInt(33, 127).toChar() } // Ascii alphanumeric + some special characters range
        .joinToString("")
}
