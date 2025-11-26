package com.pedrozc90.rfid

import android.content.Context
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import com.pedrozc90.rfid.helpers.DeviceDetector
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Test
import org.junit.runner.RunWith

class Detector : DeviceDetector() {

    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.Main.immediate)

    fun detect(context: Context): Result {
        return detectBuiltIn(context = context)
    }

}

@RunWith(AndroidJUnit4::class)
class DeviceDetectorTest {

    private val detector = Detector()

    @Test
    fun detect() {
        val context = InstrumentationRegistry.getInstrumentation().targetContext
        val result = detector.detect(context = context)
        assertFalse(result.builtIn)
        assertNull(result.key)
        assertNull(result.value)
        assertNotNull(result.device)
        assertEquals("BE2A.250530.026.D1", result.device.id)
        assertEquals("goldfish_x86_64", result.device.board)
        assertEquals("emu64xa", result.device.device)
        assertEquals("ranchu", result.device.hardware)
        assertEquals("Google", result.device.manufacturer)
        assertEquals("sdk_gphone64_x86_64", result.device.model)
        assertEquals("sdk_gphone64_x86_64", result.device.product)
        assertNull(result.device.serial)
    }

}
