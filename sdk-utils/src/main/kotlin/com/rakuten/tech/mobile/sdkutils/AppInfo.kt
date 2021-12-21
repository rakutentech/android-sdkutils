package com.rakuten.tech.mobile.sdkutils

import android.content.Context
import androidx.annotation.VisibleForTesting

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
    abstract val version: String

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
    override val version: String
) : AppInfo() {

    constructor(context: Context) : this (
        name = context.packageName,
        version = context.packageManager
            .getPackageInfo(context.packageName, 0).versionName
    )
}
