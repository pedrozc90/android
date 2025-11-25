package com.pedrozc90.rfid.urovo

import com.pedrozc90.rfid.objects.DeviceParams
import com.pedrozc90.rfid.urovo.core.toDeviceParams
import com.pedrozc90.rfid.urovo.core.toRfidParameter
import com.ubx.usdk.bean.RfidParameter
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertThrows
import org.junit.Test

class DeviceParamsTest {

    @Test
    fun convertRfidParametersToDeviceParams() {
        val entity = RfidParameter()
        val params = entity.toDeviceParams()
        assertNotNull(params)
    }

    @Test
    fun convertDeviceParamsToRfidParameters() {
        val params = DeviceParams(session = 1_000, password = "0")
        val entity = params.toRfidParameter()
        assertNotNull(entity)
        assertEquals(1_000, entity.Session)
        assertEquals("0", entity.Password)
    }

    @Test
    fun convertDeviceParamsToRfidParameters_Invalid() {
        val params = DeviceParams(session = 0, password = null)

        val exception = assertThrows(IllegalArgumentException::class.java) {
            val entity = params.toRfidParameter()
            assertNotNull(entity)
        }

        assertEquals("Password must not be null.", exception.message)
    }

}
