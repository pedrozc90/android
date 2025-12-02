package com.pedrozc90.rfid.chafon

import org.junit.Assert.assertEquals
import org.junit.Test

class ChafonFrequencyTest {

    @Test
    fun test() {
        val value = ChafonFrequency.All
        assertEquals(0, value.band)
        assertEquals("0x00", value.band.toHexString())
    }

    @Test
    fun test_hex() {
        val v1 = 0x03
        val v2 = 0x61
        val v3 = 0x0361
        assertEquals(3, v1)
        assertEquals(97, v2)
        assertEquals(865, v3)

        val first = (v3 shr 8) and 0xFF
        val second = v3 and 0xFF
        assertEquals(v1, first)
        assertEquals(v2, second)
    }

}
