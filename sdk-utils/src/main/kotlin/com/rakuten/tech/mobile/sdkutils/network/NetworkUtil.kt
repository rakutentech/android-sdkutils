package com.rakuten.tech.mobile.sdkutils.network

import android.Manifest
import android.annotation.SuppressLint
import android.content.Context
import android.net.ConnectivityManager
import android.net.Network
import android.net.NetworkCapabilities
import android.net.NetworkRequest
import android.os.Build
import com.rakuten.tech.mobile.sdkutils.ContextExtension.hasPermission
import com.rakuten.tech.mobile.sdkutils.logger.Logger
import java.util.concurrent.atomic.AtomicBoolean
import java.util.concurrent.atomic.AtomicReference
import kotlin.jvm.Throws

@SuppressLint("MissingPermission")
class NetworkUtil internal constructor(context: Context, capabilities: NetworkCapabilities?) {
    private val logger = Logger(NetworkUtil::class.java.simpleName)
    private val appContext: Context = context.applicationContext

    private val netCapabilities = AtomicReference(capabilities)
    private val isNetworkAvailable = AtomicBoolean(false)

    @Throws(Exception::class)
    constructor(context: Context): this(context,null) {
        if (appContext.hasPermission(Manifest.permission.ACCESS_NETWORK_STATE)) {
            try {
                registerNetworkCallback()
            } catch (ex: Exception) {
                logger.debug("Network callback registration failed", ex)
                isNetworkAvailable.set(false)
                netCapabilities.set(null)
                throw ex
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
     * Returns the capabilities of the active network.
     *
     * @return NetworkCapabilities object
     */
    fun networkCapabilities() = netCapabilities.get()

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
                            logger.debug("Network capabilities retrieval failed", ex)
                            netCapabilities.set(null)
                            throw ex
                        }
                    }
                }
            }
        )
    }
}