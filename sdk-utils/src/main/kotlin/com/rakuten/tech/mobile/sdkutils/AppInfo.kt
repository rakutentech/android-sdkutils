package com.rakuten.tech.mobile.sdkutils

import android.content.Context
import android.content.pm.PackageManager
import androidx.annotation.VisibleForTesting
import com.rakuten.tech.mobile.sdkutils.logger.Logger

/**
 * Provides information about the App including App name, and version name.
 * Should be accessed via [AppInfo.instance].
 */
@Suppress("UnnecessaryAbstractClass")
abstract class AppInfo {

    /**
     * Package name of the App.
     */
    abstract val name: String

    /**
     * Version name of the App.
     */
    abstract val version: String?

    companion object {
        @JvmStatic
        lateinit var instance: AppInfo

        internal fun init(context: Context) {
            instance = RealAppInfo(context)
        }
    }
}

private data class RealAppInfo @VisibleForTesting constructor(
    override val name: String,
    override val version: String?
) : AppInfo() {

    constructor(context: Context) : this (
        name = context.packageName,
        version = try {
                context.packageManager.getPackageInfo(context.packageName, 0).versionName
            } catch (ex: PackageManager.NameNotFoundException) {
                Logger(TAG).warn(ex, "Failed to load current app version")
                null
            }

    )

    companion object {
        val TAG: String = AppInfo::class.java.name
    }
}
