package com.rakuten.tech.mobile.sdkutils.eventlogger

import android.content.Context
import android.content.pm.PackageInfo
import android.content.pm.PackageManager
import android.os.Build
import android.provider.Settings
import androidx.core.content.pm.PackageInfoCompat
import com.rakuten.tech.mobile.sdkutils.StringExtension.sanitize
import java.lang.ref.WeakReference

/**
 * Utility class for the event logger feature.
 */
@SuppressWarnings(
    "SwallowedException"
)
internal class EventLoggerHelper(private val context: WeakReference<Context>) {

    /**
     * Information that is typically constant throughout the application lifecycle such as application and
     * device information.
     */
    val metadata: Metadata = buildMetadata(context.get())

    /**
     * Returns true if all event parameters are non-empty, otherwise false.
     */
    fun isEventValid(
        sourceName: String,
        sourceVersion: String,
        errorCode: String,
        errorMessage: String
    ): Boolean {
        val isValidSourceInfo = sourceName.isNotEmpty() && sourceVersion.isNotEmpty()
        val isValidErrorInfo = errorCode.isNotEmpty() && errorMessage.isNotEmpty()

        return isValidSourceInfo && isValidErrorInfo
    }

    /**
     * Attaches metadata to the event.
     */
    @SuppressWarnings("LongParameterList")
    fun buildEvent(
        type: EventType,
        sourceName: String,
        sourceVersion: String,
        code: String,
        message: String,
        info: Map<String, String>? = null
    ): Event {
        return Event(
            eventType = type.displayName,
            appId = metadata.appId,
            appName = metadata.appName,
            appVersion = metadata.appVer,
            osVersion = metadata.osVer,
            deviceModel = metadata.deviceModel,
            deviceBrand = metadata.deviceBrand,
            deviceName = metadata.deviceName,
            sdkName = sourceName.sanitize(MAX_EVENT_PARAM_LENGTH_DEFAULT),
            sdkVersion = sourceVersion.sanitize(MAX_EVENT_PARAM_LENGTH_DEFAULT),
            errorCode = code.sanitize(MAX_EVENT_PARAM_LENGTH_DEFAULT),
            errorMessage = message.sanitize(MAX_EVENT_MESSAGE_LENGTH),
            rmcSdks = metadata.rmcSdks,
            info = info
        )
    }

    @SuppressWarnings("LongMethod")
    private fun buildMetadata(context: Context?): Metadata {
        val packageInfo = if (context != null) {
            try {
                context.packageManager?.getPackageInfo(context.packageName, 0)
            } catch (e: PackageManager.NameNotFoundException) {
                null
            }
        } else {
            null
        }

        return Metadata(
            appId = context?.packageName.orEmpty(),
            appName = context?.applicationInfo?.loadLabel(context.packageManager)?.toString().orEmpty(),
            appVer = getAppVersion(packageInfo),
            osVer = "Android ${Build.VERSION.RELEASE}",
            deviceModel = Build.MODEL,
            deviceBrand = Build.MANUFACTURER,
            deviceName = Settings.Global.getString(context?.contentResolver, "device_name").orEmpty(),
            rmcSdks = getRmcVersions()
        )
    }

    private fun getAppVersion(packageInfo: PackageInfo?): String {
        if (packageInfo != null) {
            return "${packageInfo.versionName}.${PackageInfoCompat.getLongVersionCode(packageInfo)}"
        }
        return ""
    }

    private fun getRmcVersions(): Map<String, String>? {
        val versionMap = mutableMapOf<String, String>()

        listOf(
            "rmc_inappmessaging"
        ).forEach { sdkName ->
            getVersionFromResource(context.get(), sdkName)?.let { version ->
                versionMap[sdkName] = version
            }
        }
        return versionMap.ifEmpty { null }
    }

    @SuppressWarnings(
        "TooGenericExceptionCaught"
    )
    private fun getVersionFromResource(context: Context?, sdkName: String): String? {
        return if (context != null) {
            try {
                context.getString(
                    context.resources.getIdentifier(sdkName + "__version", "string", context.packageName))
            } catch (e: Exception) {
                null
            }
        } else {
            null
        }
    }

    internal data class Metadata(
        val appId: String,
        val appName: String,
        val appVer: String,
        val osVer: String,
        val deviceModel: String,
        val deviceBrand: String,
        val deviceName: String,
        val rmcSdks: Map<String, String>?
    )

    companion object {
        private const val MAX_EVENT_PARAM_LENGTH_DEFAULT = 100
        private const val MAX_EVENT_MESSAGE_LENGTH = 4000
    }
}
