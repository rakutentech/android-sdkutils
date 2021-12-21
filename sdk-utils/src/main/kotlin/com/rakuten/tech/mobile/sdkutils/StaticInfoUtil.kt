package com.rakuten.tech.mobile.sdkutils

import android.os.Build
import androidx.annotation.RequiresApi
import org.json.JSONObject

/**
 * This utility class provides application information.
 */
object StaticInfoUtil {
    /**
     * Get the application information.
     *
     * @return return the app info map data based on AppInfo and BuildInfo keys
     */
    fun getAppInfo(): String {
        val infoMap = HashMap<String, Any?>()

        // Populate the System properties
        infoMap[AppInfoKey.PROPERTIES] = getSystemProperties()

        // Populate all packages
        infoMap[AppInfoKey.PACKAGES] = getPackages()

        // Populate build info
        infoMap[BuildInfoKey.BUILD] = getBuildMap()

        return JSONObject(infoMap).toString()
    }

    /**
     * Get the system properties.
     *
     * @return the system properties map
     */
    fun getSystemProperties(): HashMap<String, String> {
        val propertiesHashMap = HashMap<String, String>()
        val properties = System.getProperties()
        for (name in properties.stringPropertyNames()) {
            propertiesHashMap[name] = properties.getProperty(name)
        }

        return propertiesHashMap
    }

    /**
     * Get all the packages.
     *
     * @return the packages list
     */
    fun getPackages(): Array<String?> {
        val packages = Package.getPackages()
        val packageNames = arrayOfNulls<String>(packages.size)
        for (i in packageNames.indices) {
            packageNames[i] = packages[i].toString()
        }

        return packageNames
    }

    /**
     * Get the build map.
     *
     * @return the build map
     */
    fun getBuildMap(): HashMap<String, String> {
        val buildMap = HashMap<String, String>()
        buildMap[BuildInfoKey.RELEASE] = Build.VERSION.RELEASE
        buildMap[BuildInfoKey.BOARD] = Build.BOARD
        buildMap[BuildInfoKey.BRAND] = Build.BRAND
        buildMap[BuildInfoKey.DEVICE] = Build.DEVICE
        buildMap[BuildInfoKey.FINGER_PRINT] = Build.FINGERPRINT
        buildMap[BuildInfoKey.HARDWARE] = Build.HARDWARE
        buildMap[BuildInfoKey.ID] = Build.ID
        buildMap[BuildInfoKey.MANUFACTURER] = Build.MANUFACTURER
        buildMap[BuildInfoKey.MODEL] = Build.MODEL
        buildMap[BuildInfoKey.PRODUCT] = Build.PRODUCT
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            addSecurityPatch(buildMap)
        }

        return buildMap
    }

    @RequiresApi(Build.VERSION_CODES.M)
    private fun addSecurityPatch(map: MutableMap<String, String>) {
        map[BuildInfoKey.SECURITY_PATCH] = Build.VERSION.SECURITY_PATCH
    }

    // App info keys
    internal object AppInfoKey {
        const val PROPERTIES = "properties"
        const val PACKAGES = "packages"
    }

    // Build info keys
    internal object BuildInfoKey {
        const val RELEASE = "release"
        const val SECURITY_PATCH = "security_patch"
        const val BOARD = "board"
        const val BRAND = "brand"
        const val DEVICE = "device"
        const val FINGER_PRINT = "fingerprint"
        const val HARDWARE = "hardware"
        const val ID = "id"
        const val MANUFACTURER = "manufacturer"
        const val MODEL = "model"
        const val PRODUCT = "product"
        const val BUILD = "build"
    }
}
