package com.rakuten.tech.mobile.sdkutils

import android.annotation.SuppressLint
import android.graphics.Color
import android.os.Build
import androidx.annotation.ColorInt
import com.rakuten.tech.mobile.sdkutils.logger.Logger
import java.io.UnsupportedEncodingException
import java.math.BigInteger
import java.net.URLEncoder
import java.nio.charset.Charset
import java.nio.charset.StandardCharsets
import java.security.MessageDigest
import java.security.NoSuchAlgorithmException
import java.text.SimpleDateFormat
import java.util.Date
import java.util.TimeZone

/**
 * String Extensions.
 * */
object StringExtension {
    private val logger = Logger(StringExtension::class.java.simpleName)

    internal var stringForTest: String? = null

    /**
     * Get the encoded byte array of string for charset "UTF-16".
     *
     * @return the ByteArray of the string.
     */
    fun String.getUTF16ByteArray(): ByteArray = this.toByteArray(Charset.forName("UTF-16"))

    /**
     * Get the encoded byte array of string for charset "UTF-8".
     *
     * @return the ByteArray of the string.
     */
    fun String.getUTF8ByteArray(): ByteArray = this.toByteArray(Charset.forName("UTF-8"))

    /**
     * Get the encoded MD5 digest string.
     *
     * @return the MD5 digest string.
     */
    fun String.getMD5HashData(): String? {
        return try {
            String.format(
                "%032x",
                BigInteger(
                    1,
                    MessageDigest.getInstance(stringForTest ?: "MD5").digest(this.getUTF8ByteArray())
                )
            )
        } catch (e: NoSuchAlgorithmException) {
            logger.debug(e, "MD5 not available, aborting hash")
            null
        }
    }

    /**
     * Get the encoded Sha256 digest string.
     *
     * @return the encoded string
     */
    @SuppressWarnings("MagicNumber")
    fun String.getSha256HashData(): String? {
        return try {
            val md = MessageDigest.getInstance(stringForTest ?: "SHA-256")
            val byteData = md.digest(this.getUTF8ByteArray())
            val sb = StringBuilder()
            for (b in byteData) {
                val hex = Integer.toHexString(0xFF and b.toInt())
                if (hex.length == 1) {
                    sb.append('0')
                }
                sb.append(hex)
            }
            sb.toString()
        } catch (e: NoSuchAlgorithmException) {
            logger.debug(e, "SHA-256 not available, aborting hash")
            null
        }
    }

    /**
     * Get the UTC date from a sample date string.
     *
     * @return the date object based on the date string
     */
    @SuppressLint("SimpleDateFormat")
    fun String.getUTCDate(): Date? {
        val dateFormat = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ssX")
        } else {
            SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss")
        }
        dateFormat.timeZone = TimeZone.getTimeZone("UTC")
        return dateFormat.parse(this)
    }

    /**
     * Parse the color string, and return the corresponding color-int.
     *
     * @return the color int value
     */
    @ColorInt
    @SuppressWarnings("TooGenericExceptionCaught")
    fun String.getColorValue(): Int {
        return try {
            Color.parseColor(this)
        } catch (exception: Exception) {
            logger.error(exception, "Failed to parse color", exception)
            -1
        }
    }

    /**
     * Get the URL encoded value of the string for charset "UTF-8".
     *
     * @return UTF-8 URL encoded value, or empty string if an error was encountered
     */
    fun String.getUTF8UrlEncoded(): String {
        return try {
            URLEncoder.encode(this, stringForTest ?: StandardCharsets.UTF_8.displayName())
        } catch (ex: UnsupportedEncodingException) {
            logger.error(ex, "Unsupported encoding.")
            ""
        }
    }

    /**
     * Get the URL encoded value of the string for charset "UTF-16".
     *
     * @return UTF-16 URL encoded value, or empty string if an error was encountered
     */
    fun String.getUTF16UrlEncoded(): String {
        return try {
            URLEncoder.encode(this, stringForTest ?: StandardCharsets.UTF_16.displayName())
        } catch (ex: UnsupportedEncodingException) {
            logger.error(ex, "Unsupported encoding.")
            ""
        }
    }
}
