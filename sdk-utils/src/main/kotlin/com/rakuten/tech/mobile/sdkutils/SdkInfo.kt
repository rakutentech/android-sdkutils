package com.rakuten.tech.mobile.sdkutils

import android.os.Build
import androidx.annotation.VisibleForTesting

/**
 * Provides information about the Sdk including device info.
 * Should be accessed via [SdkInfo.instance].
 */
abstract class SdkInfo {
    /**
     * Package name of the Sdk.
     */
    abstract val name: String

    /**
     * Version code of the Sdk.
     */
    abstract val versionCode: Int

    /**
     * Version name of the Sdk.
     */
    abstract val versionName: String

    /**
     * Model name.
     */
    abstract val model: String

    /**
     * Device name.
     */
    abstract val device: String

    /**
     * Build type.
     */
    abstract val buildType: String

    companion object {
        @JvmStatic
        lateinit var instance: SdkInfo

        internal fun init() {
            instance = RealSdkInfo()
        }
    }
}

private data class RealSdkInfo @VisibleForTesting constructor(
    override val name: String,
    override val versionCode: Int,
    override val versionName: String,
    override val model: String,
    override val device: String,
    override val buildType: String
) : SdkInfo() {

    constructor() : this(
        name = BuildConfig.LIBRARY_PACKAGE_NAME,
        versionCode = BuildConfig.VERSION_CODE,
        versionName = BuildConfig.VERSION_NAME,
        model = Build.MODEL,
        device = Build.DEVICE,
        buildType = BuildConfig.BUILD_TYPE
    )
}
