package com.rakuten.tech.mobile.sdkutils

import android.os.Build
import com.nhaarman.mockitokotlin2.mock
import org.amshove.kluent.When
import org.amshove.kluent.calling
import org.amshove.kluent.itReturns
import org.amshove.kluent.shouldContain
import org.junit.Test
import org.junit.runner.RunWith
import org.junit.runners.Parameterized
import org.robolectric.util.ReflectionHelpers

@RunWith(Parameterized::class)
class RasSdkHeadersSpec(
    private val description: String,
    private val name: String,
    private val value: String
) {

    private val appInfo: AppInfo = mock()

    @Test
    fun `should have the header`() {
        When calling appInfo.name itReturns "test.app.name"
        When calling appInfo.version itReturns "1.0.0"
        ReflectionHelpers.setStaticField(Build::class.java, "MODEL", "test model name")
        ReflectionHelpers.setStaticField(Build.VERSION::class.java, "RELEASE", "9")

        val headers = RasSdkHeaders(
            appId = "test-app-id",
            subscriptionKey = "test-subscription-key",
            sdkName = "SDK Name",
            sdkVersion = "3.0.0",
            appInfo = appInfo
        )

        headers.asArray() shouldContain (name to value)
    }

    companion object {
        @JvmStatic
        @Parameterized.Parameters(
            name = "\"{0}\""
        )
        fun data(): Collection<Array<String>> {
            return listOf(
                arrayOf("App ID", "ras-app-id", "test-app-id"),
                arrayOf("Subscription Key prefixed with 'ras-'", "apiKey", "ras-test-subscription-key"),
                arrayOf("SDK Name", "ras-sdk-name", "SDK Name"),
                arrayOf("SDK Version", "ras-sdk-version", "3.0.0"),
                arrayOf("App Name", "ras-app-name", "test.app.name"),
                arrayOf("App Version", "ras-app-version", "1.0.0"),
                arrayOf("Device Model", "ras-device-model", "test model name"),
                arrayOf("Device Version", "ras-os-version", "9")
            )
        }
    }
}
