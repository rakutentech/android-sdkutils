package com.rakuten.tech.mobile.sdkutils

import android.app.Activity
import android.content.Context
import android.content.res.Configuration
import com.google.android.gms.common.ConnectionResult
import com.google.android.gms.common.GoogleApiAvailability
import com.rakuten.tech.mobile.sdkutils.logger.Logger
import java.io.File
import java.io.FileOutputStream
import java.io.IOException

object ContextExtension {
    private const val PLAY_SERVICES_RESOLUTION_REQUEST = 9000
    private val logger = Logger(ContextExtension::class.java.simpleName)

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
}