package com.rakuten.tech.mobile.sdkutils

import android.annotation.SuppressLint
import android.graphics.Color
import android.os.Build
import androidx.annotation.ColorInt
import com.rakuten.tech.mobile.sdkutils.StringExtension.getUTF16UrlEncoded
import com.rakuten.tech.mobile.sdkutils.logger.Logger
import java.io.UnsupportedEncodingException
import java.math.BigInteger
import java.net.URLEncoder
import java.nio.charset.Charset
import java.nio.charset.StandardCharsets
import java.security.MessageDigest
import java.security.NoSuchAlgorithmException
import java.text.SimpleDateFormat
import java.util.*

object StringExtension {
    private val logger = Logger(StringExtension::class.java.simpleName)

    /**
     * Get the encoded byte array of string for charset "UTF-16"
     *
     * @return the ByteArray of the string
     */
    fun String.getUTF16ByteArray(): ByteArray {
        return this.toByteArray(Charset.forName("UTF-16"))
    }

    /**
     * Get the encoded byte array of string for charset "UTF-8"
     *
     * @return the ByteArray of the string
     */
    fun String.getUTF8ByteArray(): ByteArray {
        return this.toByteArray(Charset.forName("UTF-8"))
    }

    /**
     * Get the encoded MD5 digest string
     *
     * @return the MD5 digest string
     */
    fun String.getMD5HashData(): String? {
        return try {
            String.format(
                "%032x",
                BigInteger(
                    1,
                    MessageDigest.getInstance("MD5")
                        .digest(this.getUTF8ByteArray())
                )
            )
        } catch (swallowed: NoSuchAlgorithmException) {
            null
        }
    }

    /**
     * Get the encoded Sha256 digest string
     *
     * @return the encoded string
     */
    fun String.getSha256HashData(): String? {
        return try {
            val md = MessageDigest.getInstance("SHA-256")
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
            null
        }
    }

    /**
     * Get the UTC date from a sample date string
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
            URLEncoder.encode(this, StandardCharsets.UTF_8.displayName())
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
            URLEncoder.encode(this, StandardCharsets.UTF_16.displayName())
        } catch (ex: UnsupportedEncodingException) {
            logger.error(ex, "Unsupported encoding.")
            ""
        }
    }
}