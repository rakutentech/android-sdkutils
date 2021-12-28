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
     * Get the encoded byte array of string from character set
     *
     * @param rawString the raw string.
     * @param charsetName the character set, i.e. "UTF-8", "UTF-16"
     * @return the ByteArray of the string
     */
    fun getEncodedByteArray(rawString: String, charsetName: String): ByteArray {
        return rawString.toByteArray(Charset.forName(charsetName))
    }

    /**
     * Get the encoded MD5 digest string
     *
     * @param rawString the raw string.
     * @return the MD5 digest string
     */
    fun getMD5HashData(rawString: String?): String? {
        return if (rawString == null) {
            null
        } else try {
            String.format(
                "%032x",
                BigInteger(
                    1,
                    MessageDigest.getInstance("MD5")
                        .digest(getEncodedByteArray(rawString, "UTF-8"))
                )
            )
        } catch (swallowed: NoSuchAlgorithmException) {
            null
        }
    }

    /**
     * Check whether the strings are same.
     *
     * @param first the first string
     * @param second the second string
     * @return true if same otherwise false
     */
    fun isEqual(first: String, second: String): Boolean {
        return first == second
    }

    /**
     * Check the string whether null or empty
     *
     * @param rawString the raw string
     * @return true if null or empty otherwise false
     */
    fun isNullOrEmpty(rawString: String?): Boolean {
        return rawString.isNullOrEmpty()
    }

    /**
     * Get the encoded Sha256 digest string
     *
     * @param rawString the raw string.
     * @return the encoded string
     */
    fun getSha256HashData(rawString: String?): String? {
        return if (rawString == null) {
            null
        } else try {
            val md = MessageDigest.getInstance("SHA-256")
            val byteData = md.digest(getEncodedByteArray(rawString, "utf-8"))
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
     *
     * If it doesn't, display a dialog that allows users to download the APK from the Google Play
     * Store or enable it in the device's system settings.
     *
     * @param context non null activity, used to display update dialog if necessary.
     * @return true if play services are available, false otherwise
     */
    fun checkPlayServices(context: Activity): Boolean {
        val googleApi = GoogleApiAvailability.getInstance()
        val result = googleApi.isGooglePlayServicesAvailable(context)
        if (result != ConnectionResult.SUCCESS) {
            if (googleApi.isUserResolvableError(result)) {
                googleApi.getErrorDialog(context, result, PLAY_SERVICES_RESOLUTION_REQUEST).show()
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
        val dateFormat: DateFormat = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ssX")
        } else {
            SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss")
        }
        val timeZone = TimeZone.getTimeZone("UTC")
        dateFormat.timeZone = timeZone
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
        val dateFormat: DateFormat = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ssX")
        } else {
            SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss")
        }
        val timeZone = TimeZone.getTimeZone("UTC")
        dateFormat.timeZone = timeZone
        return dateFormat.parse(dateString ?: "2021-12-01T00:00:00Z")
    }

    /**
     * Get the resource id from drawable folder.
     *
     * @param context the application context.
     * @param resourceName the resource name
     * @return the drawable resource id
     */
    fun getDrawableResourceId(context: Context, resourceName: String?): Int {
        return context.resources.getIdentifier(resourceName, "drawable", context.packageName)
    }

    /**
     * Get the resource id from raw folder.
     *
     * @param context the application context.
     * @param resourceName the resource name
     * @return the raw resource id
     */
    fun getRawResourceId(context: Context, resourceName: String?): Int {
        return context.resources.getIdentifier(resourceName, "raw", context.packageName)
    }

    /**
     * Parse the color string, and return the corresponding color-int.
     *
     * @param colorString the color string.
     * @return the color int value
     */
    @ColorInt
    fun getColorValue(colorString: String?): Int {
        return try {
            Color.parseColor(colorString)
        } catch (exception: Exception) {
            logger.error(exception, "Failed to parse color", exception)
            -1
        }
    }

    /**
     * Get the desired file path exists in assets folder.
     *
     * @param context the application context.
     * @param fileName the file name
     * @return the asset path of the file
     */
    fun getAssetsFilePath(context: Context, fileName: String?): String? {
        if (fileName != null) {
            val cacheFile = File(context.cacheDir, fileName)
            try {
                context.assets.open(fileName).use { input ->
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
     * @param context the application context.
     * @return true if dark mode otherwise false for light mode
     */
    fun isDarkMode(context: Context): Boolean {
        when (context.resources.configuration.uiMode and Configuration.UI_MODE_NIGHT_MASK) {
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