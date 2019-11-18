package com.rakuten.tech.mobile.sdkutils

import org.amshove.kluent.shouldBeInstanceOf
import org.junit.Test

class AppInfoSpec {

    @Test
    fun `should be instance`() {
        AppInfo() shouldBeInstanceOf AppInfo::class
    }
}