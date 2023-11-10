package com.rakuten.tech.mobile.sdkutils.eventlogger

import android.content.Context
import android.content.pm.PackageManager
import com.google.gson.Gson
import com.rakuten.tech.mobile.sdkutils.StringExtension.sanitize

@SuppressWarnings(
    "SwallowedException"
)
internal class EventBuilder(private val context: Context) {

    private val metadata: Metadata = buildMetadata()

    /**
     * Event builder that attaches application information to the event.
     */
    fun buildEvent(type: EventType, sourceName: String, sourceVersion: String, code: String, message: String): Event {
        return Event(
            eventType = type.displayName,
            appId = metadata.appId,
            appName = metadata.appName,
            appVer = metadata.appVer,
            osVer = "", // TODO
            deviceModel = "", // TODO
            deviceBrand = "", // TODO
            deviceName = "", // TODO
            sdkName = sourceName.sanitize(MAX_EVENT_PARAM_LENGTH_DEFAULT),
            sdkVer = sourceVersion.sanitize(MAX_EVENT_PARAM_LENGTH_DEFAULT),
            errorCode = code.sanitize(MAX_EVENT_PARAM_LENGTH_DEFAULT),
            errorMsg = message.sanitize(MAX_EVENT_MESSAGE_LENGTH),
            rmcSdks = metadata.rmcSdks
        )
    }

    /**
     * Retrieves information that is typically constant throughout the application lifecycle.
     */
    private fun buildMetadata(): Metadata {
        val packageInfo = try {
            context.packageManager.getPackageInfo(context.packageName, 0)
        } catch (e: PackageManager.NameNotFoundException) {
            null
        }

        return Metadata(
            appId = context.packageName,
            appName = context.applicationInfo.loadLabel(context.packageManager).toString(),
            appVer = packageInfo?.versionName.orEmpty(),
            rmcSdks = getRmcVersions()?.let {
                Gson().toJson(it)
            }
        )
    }

    private fun getRmcVersions(): Map<String, String>? {
        val versionMap = mutableMapOf<String, String>()

        listOf(
            "rmc_inappmessaging"
        ).forEach { sdkName ->
            getVersionFromResource(sdkName)?.let { version ->
                versionMap[sdkName] = version
            }
        }
        return versionMap.ifEmpty { null }
    }

    @SuppressWarnings(
        "TooGenericExceptionCaught"
    )
    private fun getVersionFromResource(sdkName: String): String? {
        return try {
            context.getString(
                context.resources.getIdentifier(sdkName + "__version", "string", context.packageName))
        } catch (e: Exception) {
            null
        }
    }

    private data class Metadata(
        val appId: String,
        val appName: String,
        val appVer: String,
        val rmcSdks: String?
    )

    companion object {
        private const val MAX_EVENT_PARAM_LENGTH_DEFAULT = 100
        private const val MAX_EVENT_MESSAGE_LENGTH = 4000
    }
}
