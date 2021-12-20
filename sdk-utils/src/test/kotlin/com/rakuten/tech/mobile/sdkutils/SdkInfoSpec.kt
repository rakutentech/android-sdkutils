package com.rakuten.tech.mobile.sdkutils

import org.amshove.kluent.*
import org.junit.Before
import org.junit.Test
import org.robolectric.util.ReflectionHelpers

class SdkInfoSpec {
    @Before
    fun setup() {
        ReflectionHelpers.setStaticField(android.os.Build::class.java, "MODEL", "TestModel")
        ReflectionHelpers.setStaticField(android.os.Build::class.java, "DEVICE", "TestDevice")

        SdkInfo.init()
    }

    @Test
    fun `should return sdk name`() {
        SdkInfo.instance.name shouldBeEqualTo "com.rakuten.tech.mobile.sdkutils"
    }

    @Test
    fun `should return sdk version name`() {
        SdkInfo.instance.versionName shouldBeEqualTo "0.2.0"
    }

    @Test
    fun `should return model name`() {
        SdkInfo.instance.model shouldBeEqualTo "TestModel"
    }

    @Test
    fun `should return device name`() {
        SdkInfo.instance.device shouldBeEqualTo "TestDevice"
    }
}
