package com.rakuten.tech.mobile.sdkutils.json

import android.os.Build
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import org.amshove.kluent.shouldBeEqualTo
import org.amshove.kluent.shouldBeNull
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import java.io.InputStreamReader

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [Build.VERSION_CODES.O_MR1])
class JsonUtilsSpec {
    private val gson = Gson()

    @Before
    fun before() {

    }

    @Test
    fun `should convert payload from file for generic type`() {
        val path = LIST_PAYLOAD
        val inputAsString = InputStreamReader(javaClass.classLoader?.getResourceAsStream(path))
            .use { it.readText() }
        val devices: List<Device>? = gson.fromResources(path, object: TypeToken<List<Device>>(){}.type)
        gson.toJson(devices) shouldBeEqualTo inputAsString
    }

    @Test
    fun `should convert payload from file for class type`() {
        val path = OBJECT_PAYLOAD
        val inputAsString = InputStreamReader(javaClass.classLoader?.getResourceAsStream(path))
            .use { it.readText() }
        val device: Device? = gson.fromResources(path, Device::class.java)
        gson.toJson(device) shouldBeEqualTo inputAsString
    }

    @Test
    fun `should return null if empty payload for generic type`() {
        val path = EMPTY_PAYLOAD
        val devices: List<Device>? = gson.fromResources(path, object: TypeToken<List<Device>>(){}.type)
        devices.shouldBeNull()
    }

    @Test
    fun `should return null if empty payload for class type`() {
        val path = EMPTY_PAYLOAD
        val device: Device? = gson.fromResources(path, Device::class.java)
        device.shouldBeNull()
    }

    @Test
    fun `should return null if not found file for generic type`() {
        val path = INVALID_PATH
        val devices: List<Device>? = gson.fromResources(path, object: TypeToken<List<Device>>(){}.type)
        devices.shouldBeNull()
    }

    @Test
    fun `should return null if not found file for class type`() {
        val path = INVALID_PATH
        val device: Device? = gson.fromResources(path, Device::class.java)
        device.shouldBeNull()
    }



    data class Device(val name:String, val os: String)

    companion object {
        private const val LIST_PAYLOAD = "json-utils-list-payload.json"
        private const val OBJECT_PAYLOAD = "json-utils-OBJECT-payload.json"
        private const val EMPTY_PAYLOAD = "json-utils-empty-payload.json"
        private const val INVALID_PATH = "invalid-path.json"
    }
}