package com.rakuten.tech.mobile.sdkutils.network

import android.Manifest
import android.annotation.SuppressLint
import android.content.Context
import android.net.ConnectivityManager
import android.net.Network
import android.net.NetworkCapabilities
import android.net.NetworkRequest
import android.os.Build
import android.telephony.SubscriptionInfo
import android.telephony.SubscriptionManager
import android.telephony.TelephonyManager
import androidx.annotation.RequiresApi
import com.rakuten.tech.mobile.sdkutils.ContextExtension.hasPermission
import com.rakuten.tech.mobile.sdkutils.logger.Logger
import java.util.concurrent.atomic.AtomicBoolean
import java.util.concurrent.atomic.AtomicReference

@SuppressLint("MissingPermission")
@SuppressWarnings("TooManyFunctions", "TooGenericExceptionCaught", "LargeClass")
class NetworkUtil internal constructor(context: Context,
                                       /*private val errorCallback: ((ex: Exception) -> Unit)?,*/
                                       capabilities: NetworkCapabilities?) {
    private val logger = Logger(NetworkUtil::class.java.simpleName)
    private val appContext: Context = context.applicationContext
    private val defTelephony =
        context.getSystemService(Context.TELEPHONY_SERVICE) as TelephonyManager?

    internal val netCapabilities = AtomicReference(capabilities)
    internal val isNetworkAvailable = AtomicBoolean(false)

    constructor(context: Context/*, errorCallback: ((ex: Exception) -> Unit)? = null*/):
            this(context, /*errorCallback,*/ null) {
        if (appContext.hasPermission(Manifest.permission.ACCESS_NETWORK_STATE)) {
            try {
                registerNetworkCallback()
            } catch (ex: Exception) {
//                errorCallback?.let {
//                    it(RuntimeException("Network callback registration failed", ex))
//                }
                logger.debug("Network callback registration failed", ex)
                isNetworkAvailable.set(false)
                netCapabilities.set(null)
            }
        }
    }

    /**
     * Returns whether the device has network connectivity.
     *
     * @return true if online, false otherwise.
     */
    fun isOnline() = isNetworkAvailable.get()

    /**
     * Returns the alphabetic name of the default registered operator.
     *
     * @return the network operator name.
     */
    fun carrier(): String {
        val defaultSubscriptionId = getDefaultDataSubscriptionID()
        return when {
            Build.VERSION.SDK_INT >= Build.VERSION_CODES.N ->
                getCarrierBySubscriptionId(defaultSubscriptionId)
            else -> defTelephony?.networkOperatorName ?: ""
        }
    }

    /**
     * Returns the alphabetic name of the secondary registered operator,
     * if multi Sim Usage is supported.
     *
     * @return the network operator name.
     */
    @RequiresApi(Build.VERSION_CODES.M)
    fun carrierD(): String {
        return when {
            !isMultiSimConfig() -> ""
            !appContext.hasPermission(Manifest.permission.READ_PHONE_STATE) -> {
                logger.debug(LOG_MSG_READ_PHONE_STATE_PERMISSION)
                ""
            }
            Build.VERSION.SDK_INT >= Build.VERSION_CODES.N -> {
                val subscriptionId = getSecondaryDataSubscriptionID()
                getCarrierBySubscriptionId(subscriptionId)
            }
            else -> ""
        }
    }

    /**
     * This method is used to get the network type. 1 - Wifi 2 - 2G (no longer supported), 3 - 3G, 4 - 4G
     *
     * @return networkType returns the network type 1, 2, 3, or 4
     * @see NetworkType
     */
    @SuppressWarnings("LongMethod", "ComplexMethod")
    fun networkType(): Int {
        val capabilities = netCapabilities.get()
        return if (!appContext.hasPermission(Manifest.permission.ACCESS_NETWORK_STATE) ||
            capabilities == null) {
            logger.debug("Application does not have ACCESS_NETWORK_STATE Permission")
            NetworkType.UNKNOWN
        } else if (capabilities.hasTransport(NetworkCapabilities.TRANSPORT_WIFI)) {
            NetworkType.WIFI
        } else if (capabilities.hasTransport(NetworkCapabilities.TRANSPORT_CELLULAR)) {
            val bandwidth = capabilities.linkDownstreamBandwidthKbps
            when {
                bandwidth <= NET_3G_THRESHOLD -> NetworkType.NET_3G
                bandwidth <= NET_4G_THRESHOLD -> NetworkType.NET_4G
                else -> NetworkType.NET_5G
            }
        } else {
            NetworkType.UNKNOWN
        }
    }

    /**
     * This method is used to get the network type of the secondary subscription if
     * multi Sim Usage is supported.
     *
     * @return networkType returns the network type 1, 2, 3, or 4
     * @see NetworkType
     */
    @RequiresApi(Build.VERSION_CODES.M)
    @SuppressWarnings("LongMethod")
    fun networkTypeD(): Int {
        return when {
            !appContext.hasPermission(Manifest.permission.ACCESS_NETWORK_STATE) -> {
                logger.debug("Application does not have ACCESS_NETWORK_STATE Permission")
                NetworkType.UNKNOWN
            }
            netCapabilities.get()?.hasTransport(NetworkCapabilities.TRANSPORT_WIFI) == true -> NetworkType.WIFI
            !isMultiSimConfig() -> NetworkType.UNKNOWN
            !appContext.hasPermission(Manifest.permission.READ_PHONE_STATE) -> {
                logger.debug(LOG_MSG_READ_PHONE_STATE_PERMISSION)
                NetworkType.UNKNOWN
            }
            Build.VERSION.SDK_INT >= Build.VERSION_CODES.N -> {
                val subId = getSecondaryDataSubscriptionID()
                getDataNetworkTypeBySubId(subId)
            }
            else -> NetworkType.UNKNOWN
        }
    }

    /**
     * Return whether the device uses multiple SIM cards.
     *
     * @return true if multiple SIM cards are active, false otherwise.
     */
    @RequiresApi(Build.VERSION_CODES.M)
    private fun isMultiSimConfig(): Boolean = getSubscriptionInfoList().size > 1

    /**
     * Returns the alphabetic name of the default registered operator
     * for the given subId.
     *
     * @param subId the subscription ID.
     *
     * @return the network operator name.
     */
    @RequiresApi(Build.VERSION_CODES.N)
    private fun getCarrierBySubscriptionId(subId: Int): String =
        defTelephony?.createForSubscriptionId(subId)?.networkOperatorName ?: ""

    /**
     * Returns a constant indicating the the subscription ID for the the default
     * subscription currently chosen to provide cellular internet connection to
     * the user or the system's default data subscription if found, [INVALID_SUBSCRIPTION_ID]
     * otherwise.
     *
     * @return the subscription ID.
     */
    private fun getDefaultDataSubscriptionID(): Int {
        return when {
            Build.VERSION.SDK_INT >= Build.VERSION_CODES.R ->
                SubscriptionManager.getActiveDataSubscriptionId()
            Build.VERSION.SDK_INT >= Build.VERSION_CODES.N ->
                SubscriptionManager.getDefaultDataSubscriptionId()
            else -> INVALID_SUBSCRIPTION_ID
        }
    }

    /**
     * Returns a constant indicating the the subscription ID for the secondary subscription
     * providing cellular internet connection to the user.
     *
     * @return the subscription ID.
     */
    @RequiresApi(Build.VERSION_CODES.M)
    private fun getSecondaryDataSubscriptionID(): Int {
        val primaryDataSubscriptionId = getDefaultDataSubscriptionID()
        val subscriptionList = getSubscriptionInfoList()
        for (subscription in subscriptionList) {
            if (subscription.subscriptionId != primaryDataSubscriptionId) {
                return subscription.subscriptionId
            }
        }
        return INVALID_SUBSCRIPTION_ID
    }

    /**
     * Returns the available subscriptions in the current device,
     * If there are no {@link SubscriptionInfo} records currently available the list will be empty,
     * If READ_PHONE_STATE permission is not granted the list will be empty.
     *
     * @return a list of available subscriptions.
     */
    @RequiresApi(Build.VERSION_CODES.M)
    private fun getSubscriptionInfoList(): List<SubscriptionInfo> {
        if (!appContext.hasPermission(Manifest.permission.READ_PHONE_STATE)) {
            logger.debug(LOG_MSG_READ_PHONE_STATE_PERMISSION)
            return listOf()
        }
        val subscriptionManager: SubscriptionManager? = appContext
            .getSystemService(SubscriptionManager::class.java)
        return subscriptionManager?.activeSubscriptionInfoList ?: listOf()
    }

    /**
     * Returns a constant indicating the radio technology (network type)
     * currently in use on the device for data transmission that applies to the given
     * subId.
     *
     * @return the network type associated with the given phone account if
     * {@link android.Manifest.permission#READ_PHONE_STATE READ_PHONE_STATE} is granted,
     * [NetworkType.UNKNOWN] otherwise.
     *
     * @see #NetworkType.UNKNOWN
     */
    @RequiresApi(Build.VERSION_CODES.N)
    private fun getDataNetworkTypeBySubId(subId: Int): Int {
        if (!appContext.hasPermission(Manifest.permission.READ_PHONE_STATE)) {
            logger.debug(LOG_MSG_READ_PHONE_STATE_PERMISSION)
            return NetworkType.UNKNOWN
        }
        // create a new TelephonyManager object pinned to the given subscription ID.
        val tm: TelephonyManager? = defTelephony?.createForSubscriptionId(subId)
        return when (tm?.dataNetworkType) {
            TelephonyManager.NETWORK_TYPE_UMTS, TelephonyManager.NETWORK_TYPE_HSUPA,
            TelephonyManager.NETWORK_TYPE_EVDO_0, TelephonyManager.NETWORK_TYPE_EVDO_A,
            TelephonyManager.NETWORK_TYPE_EVDO_B, TelephonyManager.NETWORK_TYPE_HSDPA,
            TelephonyManager.NETWORK_TYPE_HSPA, TelephonyManager.NETWORK_TYPE_EHRPD,
            TelephonyManager.NETWORK_TYPE_HSPAP, TelephonyManager.NETWORK_TYPE_TD_SCDMA -> NetworkType.NET_3G
            TelephonyManager.NETWORK_TYPE_LTE -> NetworkType.NET_4G
            TelephonyManager.NETWORK_TYPE_NR -> NetworkType.NET_5G
            else -> NetworkType.UNKNOWN
        }
    }

    private fun registerNetworkCallback() {
        val request = NetworkRequest.Builder()
            .addTransportType(NetworkCapabilities.TRANSPORT_WIFI)
            .addTransportType(NetworkCapabilities.TRANSPORT_CELLULAR)
            .build()
        val connectivityManager =
            appContext.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        connectivityManager.registerNetworkCallback(
            request,
            object : ConnectivityManager.NetworkCallback() {
                override fun onCapabilitiesChanged(
                    network: Network,
                    networkCapabilities: NetworkCapabilities
                ) {
                    super.onCapabilitiesChanged(network, networkCapabilities)
                    netCapabilities.set(networkCapabilities)
                }

                override fun onLost(network: Network) {
                    super.onLost(network)
                    isNetworkAvailable.set(false)
                    netCapabilities.set(null)
                }

                override fun onAvailable(network: Network) {
                    super.onAvailable(network)
                    isNetworkAvailable.set(true)

                    // workaround: onCapabilitiesChanged is not guaranteed to be called immediately after
                    // onAvailable in versions below Android 8
                    if (Build.VERSION.SDK_INT < Build.VERSION_CODES.O) {
                        try {
                            netCapabilities.set(connectivityManager.getNetworkCapabilities(network))
                        } catch (ex: Exception) {
//                            errorCallback?.let {
//                                it(RuntimeException("Failed to load factory names", ex))
//                            }
                            logger.debug("Network capabilities retrieval failed", ex)
                            netCapabilities.set(null)
                        }
                    }
                }
            }
        )
    }

    companion object {
        private const val NET_3G_THRESHOLD = 24500 // in kbps
        private const val NET_4G_THRESHOLD = 50000 // in kbps
        private const val INVALID_SUBSCRIPTION_ID = -1
        private const val LOG_MSG_READ_PHONE_STATE_PERMISSION = "Application does not have READ_PHONE_STATE Permission"
    }

    object NetworkType {
        const val UNKNOWN = -1
        const val WIFI = 1
        const val NET_3G = 3
        const val NET_4G = 4
        const val NET_5G = 5
    }
}