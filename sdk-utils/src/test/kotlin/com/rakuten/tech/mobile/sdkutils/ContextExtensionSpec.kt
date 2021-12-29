package com.rakuten.tech.mobile.sdkutils

import android.app.Activity
import android.content.Context
import android.content.Intent
import androidx.test.core.app.ApplicationProvider
import com.rakuten.tech.mobile.sdkutils.ContextExtension.checkPlayServices
import com.rakuten.tech.mobile.sdkutils.ContextExtension.getAssetsFilePath
import com.rakuten.tech.mobile.sdkutils.ContextExtension.getDrawableResourceId
import com.rakuten.tech.mobile.sdkutils.ContextExtension.getRawResourceId
import com.rakuten.tech.mobile.sdkutils.ContextExtension.isDarkMode
import org.amshove.kluent.shouldBeEqualTo
import org.amshove.kluent.shouldNotBe
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.Robolectric
import org.robolectric.RobolectricTestRunner

@RunWith(RobolectricTestRunner::class)
class ContextExtensionSpec {
    private val context: Context = ApplicationProvider.getApplicationContext<Context>()

    @Test
    fun `should get assets file path`() {
        val assetPath: String? =
            context.getAssetsFilePath(
                "sample_assets_image.jpg"
            )
        assetPath shouldNotBe null
    }

    @Test
    fun `should get resource id for raw folder`() {
        val resourceId = context.getRawResourceId(
            "sample_raw_image"
        )
        resourceId shouldNotBe 0
    }

    @Test
    fun `should get resource id for drawable folder`() {
        val resourceId = context.getDrawableResourceId(
            "sample_drawable_image"
        )
        resourceId shouldNotBe 0
    }

    @Test
    fun `should get dark mode UI`() {
        context.resources.configuration.uiMode = 32
        context.isDarkMode() shouldBeEqualTo true
    }

    @Test
    fun `should get light mode UI`() {
        context.resources.configuration.uiMode = 16
        context.isDarkMode() shouldBeEqualTo false
    }

    @Test
    fun `should get undefined mode UI`() {
        context.resources.configuration.uiMode = 8
        context.isDarkMode() shouldBeEqualTo false
    }

    @Test
    fun `should check play services`() {
        Robolectric.buildActivity(Activity::class.java, Intent()).get()
            .checkPlayServices() shouldBeEqualTo false
    }
}