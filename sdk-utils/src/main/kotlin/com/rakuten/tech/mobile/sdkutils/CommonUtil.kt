package com.rakuten.tech.mobile.sdkutils

import android.annotation.SuppressLint
import android.app.Activity
import android.content.Context
import android.content.res.Configuration
import android.graphics.Color
import android.os.Build
import androidx.annotation.ColorInt
import com.google.android.gms.common.ConnectionResult
import com.google.android.gms.common.GoogleApiAvailability
import com.rakuten.tech.mobile.sdkutils.logger.Logger
import java.io.File
import java.io.FileOutputStream
import java.io.IOException
import java.math.BigInteger
import java.nio.charset.Charset
import java.security.MessageDigest
import java.security.NoSuchAlgorithmException
import java.text.DateFormat
import java.text.SimpleDateFormat
import java.util.*

object CommonUtil {
    private const val PLAY_SERVICES_RESOLUTION_REQUEST = 9000
    private val logger = Logger(CommonUtil::class.java.simpleName)

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
                        .digest(getUTF8ByteArray())
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
            val byteData = md.digest(getUTF8ByteArray())
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
     * Check the device to make sure it has the Google Play Services APK.
     *
     * If it doesn't, display a dialog that allows users to download the APK from the Google Play
     * Store or enable it in the device's system settings.
     *
     * @return true if play services are available, false otherwise
     */
    fun Activity.checkPlayServices(): Boolean {
        val googleApi = GoogleApiAvailability.getInstance()
        val result = googleApi.isGooglePlayServicesAvailable(this)
        if (result != ConnectionResult.SUCCESS) {
            if (googleApi.isUserResolvableError(result)) {
                googleApi.getErrorDialog(this, result, PLAY_SERVICES_RESOLUTION_REQUEST)?.show()
            }
            return false
        }
        return true
    }

    /**
     * Get the UTC date format from the simple date format, i.e. "yyyy-MM-dd'T'HH:mm:ssX"
     *
     * @return the date format
     */
    @SuppressLint("SimpleDateFormat")
    fun getUTCDateFormat(): DateFormat {
        val dateFormat = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ssX")
        } else {
            SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss")
        }
        dateFormat.timeZone = TimeZone.getTimeZone("UTC")
        return dateFormat
    }

    /**
     * Get the UTC date from a sample date string
     *
     * @param dateString the date string.
     * @return the date object based on the date string
     */
    @SuppressLint("SimpleDateFormat")
    fun getUTCDate(dateString: String?): Date? {
        val dateFormat = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ssX")
        } else {
            SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss")
        }
        dateFormat.timeZone = TimeZone.getTimeZone("UTC")
        return dateFormat.parse(dateString ?: "2021-12-01T00:00:00Z")
    }

    /**
     * Get the resource id from drawable folder.
     *
     * @param resourceName the resource name
     * @return the drawable resource id
     */
    fun Context.getDrawableResourceId(resourceName: String?): Int {
        return this.resources.getIdentifier(resourceName, "drawable", this.packageName)
    }

    /**
     * Get the resource id from raw folder.
     *
     * @param resourceName the resource name
     * @return the raw resource id
     */
    fun Context.getRawResourceId(resourceName: String?): Int {
        return this.resources.getIdentifier(resourceName, "raw", this.packageName)
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
     * Get the desired file path exists in assets folder.
     *
     * @param fileName the file name
     * @return the asset path of the file
     */
    fun Context.getAssetsFilePath(fileName: String?): String? {
        if (fileName != null) {
            val cacheFile = File(this.cacheDir, fileName)
            try {
                this.assets.open(fileName).use { input ->
                    FileOutputStream(cacheFile).use { output ->
                        input.copyTo(output)
                    }
                }
            } catch (ex: IOException) {
                logger.error(ex, "Failed to fetch media file from assets folder")
                return null
            }
            return cacheFile.path
        }
        return null
    }

    /**
     * Get the device mode (Dark or Light)
     *
     * @return true if dark mode otherwise false for light mode
     */
    fun Context.isDarkMode(): Boolean {
        when (this.resources.configuration.uiMode and Configuration.UI_MODE_NIGHT_MASK) {
            Configuration.UI_MODE_NIGHT_NO -> {
                return false
            }
            Configuration.UI_MODE_NIGHT_YES -> {
                return true
            }
        }
        return false
    }
}