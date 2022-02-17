package com.rakuten.tech.mobile.sdkutils.network

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.net.ConnectivityManager
import android.net.Network
import android.net.NetworkCapabilities
import android.os.Build
import android.telephony.SubscriptionInfo
import android.telephony.SubscriptionManager
import android.telephony.TelephonyManager
import com.nhaarman.mockitokotlin2.any
import com.nhaarman.mockitokotlin2.doAnswer
import org.amshove.kluent.*
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.ArgumentMatchers
import org.mockito.Mockito
import org.mockito.Mockito.`when`
import org.mockito.Mockito.doReturn
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import java.util.ArrayList
import com.rakuten.tech.mobile.sdkutils.network.NetworkUtil.NetworkType

@RunWith(RobolectricTestRunner::class)
class NetworkUtilSpec {
    private val context = Mockito.mock(Context::class.java)
    private val fakeTelephony = Mockito.mock(TelephonyManager::class.java)
    private val fakeSubManager = Mockito.mock(SubscriptionManager::class.java)
    private val fakeConnectivity = Mockito.mock(ConnectivityManager::class.java)
    private val fakePackageManager = Mockito.mock(PackageManager::class.java)
    private val fakeCapabilities = Mockito.mock(NetworkCapabilities::class.java)
    private var fakeSubscriptionInfo: SubscriptionInfo = Mockito.mock(SubscriptionInfo::class.java)
    private lateinit var networkUtil: NetworkUtil

    @Before
    fun setup() {
        `when`(context.applicationContext).thenReturn(context)
        `when`(context.getSystemService(Context.TELEPHONY_SERVICE)).thenReturn(fakeTelephony)
        `when`(context.getSystemService(Context.CONNECTIVITY_SERVICE)).thenReturn(fakeConnectivity)
        `when`(context.packageManager).thenReturn(fakePackageManager)
        `when`(fakePackageManager.checkPermission(Manifest.permission.ACCESS_NETWORK_STATE,
            context.packageName)).thenReturn(PackageManager.PERMISSION_GRANTED)
        networkUtil = NetworkUtil(context, fakeCapabilities)
    }

    @Test
    @Config(sdk = [23])
    fun `should read carrier from telephony manager`() {
        `when`(fakeTelephony.networkOperatorName).thenReturn("DOCOMO")
        networkUtil.carrier() shouldBeEqualTo "DOCOMO"
    }

    @Test
    @Config(sdk = [24])
    fun `should read carrier from telephony manager sdk 24`() {
        `when`(fakeTelephony.createForSubscriptionId(any())).thenReturn(fakeTelephony)
        `when`(fakeTelephony.networkOperatorName).thenReturn("DOCOMO")
        networkUtil.carrier() shouldBeEqualTo "DOCOMO"
    }

    @Test
    fun `should read carrier from telephony manager on the second SIM card`() {
        `when`(fakeTelephony.createForSubscriptionId(ArgumentMatchers.anyInt())).thenReturn(fakeTelephony)
        `when`(context.getSystemService(SubscriptionManager::class.java)).thenReturn(fakeSubManager)
        val spyList = Mockito.spy<MutableList<SubscriptionInfo>>(ArrayList())
        `when`(spyList.size).thenReturn(2)
        doReturn(fakeSubscriptionInfo).`when`(spyList)[ArgumentMatchers.anyInt()]
        `when`(fakeSubManager.activeSubscriptionInfoList).thenReturn(spyList)
        `when`(fakeTelephony.networkOperatorName).thenReturn("DOCOMO")
        networkUtil.carrierD() shouldBeEqualTo "DOCOMO"
    }

    @Test
    @Config(sdk = [23])
    fun `should default carrier to empty string on the second SIM card on SDK 23 and lower`() {
        networkUtil.carrierD() shouldBeEqualTo ""
    }

    @Test
    fun `should default carrier to empty string on the second SIM card if READ_PHONE_STATE is not granted`() {
        `when`(context.getSystemService(SubscriptionManager::class.java)).thenReturn(fakeSubManager)
        `when`(fakePackageManager.checkPermission(Manifest.permission.READ_PHONE_STATE,
            context.packageName)).thenReturn(PackageManager.PERMISSION_DENIED)
        val spyList = Mockito.spy<MutableList<SubscriptionInfo>>(ArrayList())
        `when`(spyList.size).thenReturn(2)
        `when`(fakeSubManager.activeSubscriptionInfoList).thenReturn(spyList)
        networkUtil.carrierD() shouldBeEqualTo ""
    }

    @Test
    @Config(sdk = [23])
    fun `should default carrier to empty string if telephony networkOperatorName is null`() {
        `when`(fakeTelephony.networkOperatorName).thenReturn(null)
        networkUtil.carrier() shouldBeEqualTo ""
    }

    @Test
    @Config(sdk = [23, 24])
    fun `should be empty string when telephony manager is null`() {
        `when`(context.getSystemService(Context.TELEPHONY_SERVICE)).thenReturn(null)
        networkUtil.carrier().shouldBeEmpty()
    }

    @Test
    fun `should return no network`() {
        networkUtil.isOnline().shouldBeFalse()
    }

    @Test
    fun `should detect unknown network`() {
        networkUtil = NetworkUtil(context, null)
        networkUtil.networkType() shouldBeEqualTo NetworkType.UNKNOWN
        networkUtil.networkTypeD() shouldBeEqualTo NetworkType.UNKNOWN
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
    fun `should detect unknown network on the second SIM card`() {
        val spyList = Mockito.spy<MutableList<SubscriptionInfo>>(ArrayList())
        `when`(spyList.size).thenReturn(2)
        `when`(fakeSubManager.activeSubscriptionInfoList).thenReturn(spyList)
        networkUtil = NetworkUtil(context, null)
        networkUtil.networkTypeD() shouldBeEqualTo NetworkType.UNKNOWN
    }

    @Test
    fun `should detect unknown network on the second SIM card if ACCESS_NETWORK_STATE is not granted`() {
        `when`(context.getSystemService(SubscriptionManager::class.java)).thenReturn(fakeSubManager)
        `when`(fakePackageManager.checkPermission(Manifest.permission.ACCESS_NETWORK_STATE,
            context.packageName)).thenReturn(PackageManager.PERMISSION_DENIED)
        val spyList = Mockito.spy<MutableList<SubscriptionInfo>>(ArrayList())
        `when`(spyList.size).thenReturn(2)
        `when`(fakeSubManager.activeSubscriptionInfoList).thenReturn(spyList)
        `when`(fakeTelephony.dataNetworkType).thenReturn(TelephonyManager.NETWORK_TYPE_LTE)
        networkUtil.networkTypeD() shouldBeEqualTo NetworkType.UNKNOWN
    }

    @Test
    fun `should detect unknown network on the second SIM card if READ_PHONE_STATE is not granted`() {
        `when`(context.getSystemService(SubscriptionManager::class.java)).thenReturn(fakeSubManager)
        `when`(fakePackageManager.checkPermission(Manifest.permission.READ_PHONE_STATE,
            context.packageName)).thenReturn(PackageManager.PERMISSION_DENIED)
        val spyList = Mockito.spy<MutableList<SubscriptionInfo>>(ArrayList())
        `when`(spyList.size).thenReturn(2)
        `when`(fakeSubManager.activeSubscriptionInfoList).thenReturn(spyList)
        `when`(fakeTelephony.dataNetworkType).thenReturn(TelephonyManager.NETWORK_TYPE_LTE)
        networkUtil.networkTypeD() shouldBeEqualTo NetworkType.UNKNOWN
    }

    @Test
    fun `should detect wifi`() {
        `when`(fakeCapabilities.hasTransport(NetworkCapabilities.TRANSPORT_WIFI)).thenReturn(true)
        networkUtil.networkType() shouldBeEqualTo NetworkType.WIFI
    }

    @Test
    fun `should detect wifi on the second SIM card`() {
        `when`(context.getSystemService(SubscriptionManager::class.java)).thenReturn(fakeSubManager)
        val spyList = Mockito.spy<MutableList<SubscriptionInfo>>(ArrayList())
        `when`(spyList.size).thenReturn(2)
        `when`(fakeSubManager.activeSubscriptionInfoList).thenReturn(spyList)
        `when`(fakeCapabilities.hasTransport(NetworkCapabilities.TRANSPORT_WIFI)).thenReturn(true)
        networkUtil.networkTypeD() shouldBeEqualTo NetworkType.WIFI
    }

    @Test
    fun `should detect wifi on the second SIM card if single sim is used`() {
        `when`(context.getSystemService(SubscriptionManager::class.java)).thenReturn(fakeSubManager)
        val spyList = Mockito.spy<MutableList<SubscriptionInfo>>(ArrayList())
        `when`(spyList.size).thenReturn(1)
        `when`(fakeSubManager.activeSubscriptionInfoList).thenReturn(spyList)
        `when`(fakeCapabilities.hasTransport(NetworkCapabilities.TRANSPORT_WIFI)).thenReturn(true)
        networkUtil.networkTypeD() shouldBeEqualTo NetworkType.WIFI
    }

    @Test
    fun `should detect unknown network on no phone permission`() {
        `when`(context.getSystemService(SubscriptionManager::class.java)).thenReturn(fakeSubManager)
        networkUtil.netCapabilities.set(null)
        `when`(fakePackageManager.checkPermission(Manifest.permission.READ_PHONE_STATE,
            context.packageName)).thenReturn(PackageManager.PERMISSION_DENIED)
        networkUtil.networkTypeD() shouldBeEqualTo NetworkType.UNKNOWN
    }

    @Test
    fun `should detect 3g`() {
        disableWifi()

        // re-initialize for the denied permission
        networkUtil = NetworkUtil(context, fakeCapabilities)
        `when`(fakeCapabilities.linkDownstreamBandwidthKbps).thenReturn(301)
        networkUtil.networkType() shouldBeEqualTo NetworkType.NET_3G
        `when`(fakeCapabilities.linkDownstreamBandwidthKbps).thenReturn(10000)
        networkUtil.networkType() shouldBeEqualTo NetworkType.NET_3G
    }

    @Test
    @Config(sdk = [24])
    fun `should detect 4g network type on the second SIM card`() {
        disableWifi()

        setupFakeTelephony()
        `when`(fakeTelephony.dataNetworkType).thenReturn(TelephonyManager.NETWORK_TYPE_LTE)
        networkUtil.networkTypeD() shouldBeEqualTo NetworkType.NET_4G
    }

    @Test
    @Config(sdk = [24])
    fun `should detect 5g network type on the second SIM card`() {
        disableWifi()

        setupFakeTelephony()
        `when`(fakeTelephony.dataNetworkType).thenReturn(TelephonyManager.NETWORK_TYPE_NR)
        networkUtil.networkTypeD() shouldBeEqualTo NetworkType.NET_5G
    }

    @Test
    @Config(sdk = [24])
    @SuppressWarnings("LongMethod")
    fun `should detect 3g network type on the second SIM card`() {
        disableWifi()

        setupFakeTelephony()
        `when`(fakeTelephony.dataNetworkType).thenReturn(TelephonyManager.NETWORK_TYPE_UMTS)
        networkUtil.networkTypeD() shouldBeEqualTo NetworkType.NET_3G
        `when`(fakeTelephony.dataNetworkType).thenReturn(TelephonyManager.NETWORK_TYPE_HSUPA)
        networkUtil.networkTypeD() shouldBeEqualTo NetworkType.NET_3G
        `when`(fakeTelephony.dataNetworkType).thenReturn(TelephonyManager.NETWORK_TYPE_EVDO_0)
        networkUtil.networkTypeD() shouldBeEqualTo NetworkType.NET_3G
        `when`(fakeTelephony.dataNetworkType).thenReturn(TelephonyManager.NETWORK_TYPE_EVDO_A)
        networkUtil.networkTypeD() shouldBeEqualTo NetworkType.NET_3G
        `when`(fakeTelephony.dataNetworkType).thenReturn(TelephonyManager.NETWORK_TYPE_EVDO_B)
        networkUtil.networkTypeD() shouldBeEqualTo NetworkType.NET_3G
        `when`(fakeTelephony.dataNetworkType).thenReturn(TelephonyManager.NETWORK_TYPE_HSDPA)
        networkUtil.networkTypeD() shouldBeEqualTo NetworkType.NET_3G
        `when`(fakeTelephony.dataNetworkType).thenReturn(TelephonyManager.NETWORK_TYPE_HSPA)
        networkUtil.networkTypeD() shouldBeEqualTo NetworkType.NET_3G
        `when`(fakeTelephony.dataNetworkType).thenReturn(TelephonyManager.NETWORK_TYPE_EHRPD)
        networkUtil.networkTypeD() shouldBeEqualTo NetworkType.NET_3G
        `when`(fakeTelephony.dataNetworkType).thenReturn(TelephonyManager.NETWORK_TYPE_HSPAP)
        networkUtil.networkTypeD() shouldBeEqualTo NetworkType.NET_3G
        `when`(fakeTelephony.dataNetworkType).thenReturn(TelephonyManager.NETWORK_TYPE_TD_SCDMA)
        networkUtil.networkTypeD() shouldBeEqualTo NetworkType.NET_3G
    }

    @Test
    @Config(sdk = [24])
    fun `should detect unknown network type on the second SIM card`() {
        disableWifi()

        setupFakeTelephony()
        `when`(fakeTelephony.dataNetworkType).thenReturn(99)
        networkUtil.networkTypeD() shouldBeEqualTo NetworkType.UNKNOWN
    }

    @Test
    fun `should detect 4g no read phone state permission`() {
        disableWifi()

        // re-initialize for the denied permission
        networkUtil = NetworkUtil(context, fakeCapabilities)
        `when`(fakeCapabilities.linkDownstreamBandwidthKbps).thenReturn(24501)
        networkUtil.networkType() shouldBeEqualTo NetworkType.NET_4G
        `when`(fakeCapabilities.linkDownstreamBandwidthKbps).thenReturn(30000)
        networkUtil.networkType() shouldBeEqualTo NetworkType.NET_4G
    }

    @Test
    fun `should detect 5g no read phone state permission`() {
        disableWifi()

        // re-initialize for the denied permission
        networkUtil = NetworkUtil(context, fakeCapabilities)
        `when`(fakeCapabilities.linkDownstreamBandwidthKbps).thenReturn(50001)
        networkUtil.networkType() shouldBeEqualTo NetworkType.NET_5G
        `when`(fakeCapabilities.linkDownstreamBandwidthKbps).thenReturn(100000)
        networkUtil.networkType() shouldBeEqualTo NetworkType.NET_5G
    }

    @Test
    fun `should have unknown network type if no wifi and data`() {
        disableWifi(false)

        // re-initialize for the denied permission
        networkUtil = NetworkUtil(context, fakeCapabilities)
        networkUtil.networkType() shouldBeEqualTo NetworkType.UNKNOWN
        networkUtil.networkTypeD() shouldBeEqualTo NetworkType.UNKNOWN
    }

    @Test
    fun `should set correct capabilities on network change`() {
        val callback: ConnectivityManager.NetworkCallback? = setupNetworkCapabilities()
        callback?.onCapabilitiesChanged(Mockito.mock(Network::class.java), fakeCapabilities)
        networkUtil.netCapabilities.get().shouldNotBeNull()
    }

    @Test
    fun `should set null capabilities on network lost`() {
        val callback: ConnectivityManager.NetworkCallback? = setupNetworkCapabilities()
        callback?.onLost(Mockito.mock(Network::class.java))
        networkUtil.netCapabilities.get().shouldBeNull()
        networkUtil.isNetworkAvailable.get().shouldBeFalse()
    }

    @Test
    @Config(sdk = [Build.VERSION_CODES.N])
    fun `should set correct capabilities on network available`() {
        val callback: ConnectivityManager.NetworkCallback? = setupNetworkCapabilities()
        `when`(fakeConnectivity.getNetworkCapabilities(any())).thenReturn(fakeCapabilities)
        callback?.onAvailable(Mockito.mock(Network::class.java))
        networkUtil.netCapabilities.get().shouldNotBeNull()
        networkUtil.isNetworkAvailable.get().shouldBeTrue()
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

    private fun disableWifi(enableCellular: Boolean = true) {
        `when`(fakeCapabilities.hasTransport(NetworkCapabilities.TRANSPORT_WIFI)).thenReturn(false)
        `when`(fakeCapabilities.hasTransport(NetworkCapabilities.TRANSPORT_CELLULAR)).thenReturn(enableCellular)
    }

    private fun setupFakeTelephony() {
        // re-initialize for the denied permission
        networkUtil = NetworkUtil(context, fakeCapabilities)
        `when`(context.getSystemService(SubscriptionManager::class.java)).thenReturn(fakeSubManager)
        val spyList = Mockito.spy<MutableList<SubscriptionInfo>>(ArrayList())
        `when`(spyList.size).thenReturn(2)
        `when`(fakeSubManager.activeSubscriptionInfoList).thenReturn(spyList)
        `when`(fakeTelephony.createForSubscriptionId(ArgumentMatchers.anyInt())).thenReturn(fakeTelephony)
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
        networkUtil.networkType() shouldBeEqualTo NetworkType.UNKNOWN
        networkUtil.networkTypeD() shouldBeEqualTo NetworkType.UNKNOWN
    }

    private fun verifyNetCapabilitiesException() {
        val callback: ConnectivityManager.NetworkCallback? = setupNetworkCapabilities()
        `when`(fakeConnectivity.getNetworkCapabilities(any())).thenThrow(IllegalArgumentException("test"))
        callback?.onAvailable(Mockito.mock(Network::class.java))
        networkUtil.netCapabilities.get().shouldBeNull()
        networkUtil.isNetworkAvailable.get().shouldBeTrue()
    }

//    fun verifyCallback(callback: (ex: Exception) -> Unit, mode: VerificationMode = times(1)) {
//        val captor = argumentCaptor<RuntimeException>()
//        Mockito.verify(callback, mode).invoke(captor.capture())
//        captor.firstValue shouldBeInstanceOf RuntimeException::class.java
//    }
}