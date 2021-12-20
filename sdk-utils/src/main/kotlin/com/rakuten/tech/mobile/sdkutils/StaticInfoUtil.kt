package com.rakuten.tech.mobile.sdkutils

import android.content.Context
import android.os.Build
import androidx.annotation.RequiresApi
import org.json.JSONObject

/**
 * This utility class provides application information and sdk information.
 */
object StaticInfoUtil {
    /**
     * Get the application information.
     *
     * @return the application info
     */
    fun getAppInfo(): String {
        val infoMap = HashMap<String, Any?>()

        // Populate the System properties
        infoMap[AppInfoKey.PROPERTIES] = getSystemProperties()

        // Populate all packages
        infoMap[AppInfoKey.PACKAGES] = getPackages()

        // Populate build info
        infoMap[AppInfoKey.BUILD] = getBuildMap()

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
        buildMap[AppInfoKey.RELEASE] = Build.VERSION.RELEASE
        buildMap[AppInfoKey.BOARD] = Build.BOARD
        buildMap[AppInfoKey.BRAND] = Build.BRAND
        buildMap[AppInfoKey.DEVICE] = Build.DEVICE
        buildMap[AppInfoKey.FINGER_PRINT] = Build.FINGERPRINT
        buildMap[AppInfoKey.HARDWARE] = Build.HARDWARE
        buildMap[AppInfoKey.ID] = Build.ID
        buildMap[AppInfoKey.MANUFACTURER] = Build.MANUFACTURER
        buildMap[AppInfoKey.MODEL] = Build.MODEL
        buildMap[AppInfoKey.PRODUCT] = Build.PRODUCT
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            addSecurityPatch(buildMap)
        }

        return buildMap
    }

    @RequiresApi(Build.VERSION_CODES.M)
    private fun addSecurityPatch(map: MutableMap<String, String>) {
        map[AppInfoKey.SECURITY_PATCH] = Build.VERSION.SECURITY_PATCH
    }

    /**
     * Get the SDK info.
     *
     * @param context the application context
     * @return the sdk info
     */
    fun getSdkInfo(context: Context): String {
        val sdkVersion = getModuleVersion(context, SdkInfoKey.MODULE_SDK)
        return if (sdkVersion != null) {
            SdkInfoKey.MODULE_SDK + "/" + sdkVersion
        } else {
            val sdkInfo = StringBuilder()
            for (key in SdkInfoKey.KEYS) {
                appendSdkModuleVersion(context, sdkInfo, key)
            }
            sdkInfo.toString()
        }
    }

    private fun appendSdkModuleVersion(context: Context, sdkInfo: StringBuilder, moduleId: String) {
        val version = getModuleVersion(context, moduleId)
        if (version != null) {
            if (sdkInfo.isNotEmpty()) {
                sdkInfo.append("; ")
            }
            sdkInfo.append(moduleId).append('/').append(version)
        }
    }

    /**
     * Get the SDK info map.
     *
     * @param context the application context
     * @return map of the sdk info
     */
    fun getSdkInfoMap(context: Context): HashMap<String, Any?> {
        val infoMap = HashMap<String, Any?>()
        for (key in SdkInfoKey.KEYS) {
            val version = getModuleVersion(context, key)
            if (version != null) {
                infoMap[key] = getModuleVersion(context, key)
            }
        }
        return infoMap
    }

    @SuppressWarnings("SwallowedException", "TooGenericExceptionCaught")
    private fun getModuleVersion(context: Context, name: String): String? {
        return try {
            context.getString(
                context.resources
                    .getIdentifier(name + "__version", "string", context.packageName)
            )
        } catch (ex: Exception) {
            null
        }
    }

    // sdk info keys
    internal object SdkInfoKey {
        private const val MODULE_ANALYTICS = "analytics"
        private const val MODULE_ANALYTICS_CORE = "analytics_core"
        private const val MODULE_ANALYTICS_RAT = "analytics_rat"
        private const val MODULE_ANALYTICS_IDTOKEN = "analytics_idtoken"
        private const val MODULE_DISCOVER = "discover"
        private const val MODULE_FEEDBACK = "feedback"
        private const val MODULE_PING = "ping"
        private const val MODULE_PUSH = "push"
        private const val MODULE_IAM = "inappmessaging"
        const val MODULE_SDK = "sdk"
        val KEYS = arrayOf(/* Keys of all modules, MODULE_SDK ignored on purpose */
            MODULE_ANALYTICS,
            MODULE_ANALYTICS_CORE,
            MODULE_ANALYTICS_RAT,
            MODULE_ANALYTICS_IDTOKEN,
            MODULE_DISCOVER,
            MODULE_FEEDBACK,
            MODULE_PING,
            MODULE_PUSH,
            MODULE_IAM
        )
    }

    // app info keys
    internal object AppInfoKey {
        const val PROPERTIES = "properties"
        const val PACKAGES = "packages"
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
