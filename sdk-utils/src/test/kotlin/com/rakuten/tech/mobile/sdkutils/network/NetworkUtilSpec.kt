package com.rakuten.tech.mobile.sdkutils.network

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.net.ConnectivityManager
import android.net.Network
import android.net.NetworkCapabilities
import android.os.Build
import com.nhaarman.mockitokotlin2.any
import com.nhaarman.mockitokotlin2.doAnswer
import org.amshove.kluent.*
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.ArgumentMatchers
import org.mockito.Mockito
import org.mockito.Mockito.`when`
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
class NetworkUtilSpec {
    private val context = Mockito.mock(Context::class.java)
    private val fakeConnectivity = Mockito.mock(ConnectivityManager::class.java)
    private val fakePackageManager = Mockito.mock(PackageManager::class.java)
    private val fakeCapabilities = Mockito.mock(NetworkCapabilities::class.java)
    private lateinit var networkUtil: NetworkUtil

    @Before
    fun setup() {
        `when`(context.applicationContext).thenReturn(context)
        `when`(context.getSystemService(Context.CONNECTIVITY_SERVICE)).thenReturn(fakeConnectivity)
        `when`(context.packageManager).thenReturn(fakePackageManager)
        `when`(fakePackageManager.checkPermission(Manifest.permission.ACCESS_NETWORK_STATE,
            context.packageName)).thenReturn(PackageManager.PERMISSION_GRANTED)
        networkUtil = NetworkUtil(context, fakeCapabilities)
    }

    @Test
    fun `should detect unknown network when exception encountered in network callback`() {
        verifyRegisterException()
    }

//    @Test
//    fun `should call error callback when exception encountered in network callback`() {
//        AnalyticsManager.setErrorCallback(mockCallback)
//        verifyRegisterException()
//        verifyCallback()
//    }

    @Test
    fun `should set correct capabilities on network change`() {
        val callback: ConnectivityManager.NetworkCallback? = setupNetworkCapabilities()
        callback?.onCapabilitiesChanged(Mockito.mock(Network::class.java), fakeCapabilities)
        networkUtil.networkCapabilities().shouldNotBeNull()
    }

    @Test
    fun `should set null capabilities on network lost`() {
        val callback: ConnectivityManager.NetworkCallback? = setupNetworkCapabilities()
        callback?.onLost(Mockito.mock(Network::class.java))
        networkUtil.networkCapabilities().shouldBeNull()
        networkUtil.isOnline().shouldBeFalse()
    }

    @Test
    @Config(sdk = [Build.VERSION_CODES.N])
    fun `should set correct capabilities on network available`() {
        val callback: ConnectivityManager.NetworkCallback? = setupNetworkCapabilities()
        `when`(fakeConnectivity.getNetworkCapabilities(any())).thenReturn(fakeCapabilities)
        callback?.onAvailable(Mockito.mock(Network::class.java))
        networkUtil.networkCapabilities().shouldNotBeNull()
        networkUtil.isOnline().shouldBeTrue()
    }

    @Test
    @Config(sdk = [Build.VERSION_CODES.N])
    fun `should set null capabilities on network available but encounter exception`() {
        verifyNetCapabilitiesException()
    }

//    @Test
//    @Config(sdk = [Build.VERSION_CODES.N])
//    fun `should call error callback on network available but encounter exception`() {
//        AnalyticsManager.setErrorCallback(mockCallback)
//        verifyNetCapabilitiesException()
//        verifyCallback()
//    }

    private fun setupNetworkCapabilities(): ConnectivityManager.NetworkCallback? {
        var callback: ConnectivityManager.NetworkCallback? = null
        doAnswer {
            callback = it.getArgument(1)
        }.`when`(fakeConnectivity).registerNetworkCallback(
            any(),
            ArgumentMatchers.any(ConnectivityManager.NetworkCallback::class.java)
        )

        networkUtil = NetworkUtil(context)
        return callback
    }

    private fun verifyRegisterException() {
        `when`(
            fakeConnectivity.registerNetworkCallback(
                any(),
                ArgumentMatchers.any(ConnectivityManager.NetworkCallback::class.java)
            )
        ).thenThrow(SecurityException())
        networkUtil = NetworkUtil(context)
        networkUtil.isOnline().shouldBeFalse()
    }

    private fun verifyNetCapabilitiesException() {
        val callback: ConnectivityManager.NetworkCallback? = setupNetworkCapabilities()
        `when`(fakeConnectivity.getNetworkCapabilities(any())).thenThrow(IllegalArgumentException("test"))
        callback?.onAvailable(Mockito.mock(Network::class.java))
        networkUtil.networkCapabilities().shouldBeNull()
        networkUtil.isOnline().shouldBeTrue()
    }

//    fun verifyCallback(callback: (ex: Exception) -> Unit, mode: VerificationMode = times(1)) {
//        val captor = argumentCaptor<RuntimeException>()
//        Mockito.verify(callback, mode).invoke(captor.capture())
//        captor.firstValue shouldBeInstanceOf RuntimeException::class.java
//    }
}