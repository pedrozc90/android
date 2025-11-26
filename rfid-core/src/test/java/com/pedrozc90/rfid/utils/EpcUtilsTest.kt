package com.pedrozc90.rfid.utils

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Test

class EpcUtilsTest {

    @Test
    fun decode() {
        val value = "3074257BF7194E4000001A85"
        val result = EpcUtils.decode(value)
        assertNotNull(result)
        assertEquals(value, result.rfid)
        assertEquals("sgtin", result.schema)
        assertEquals("urn:epc:tag:sgtin-96:3.0614141.812345.6789", result.uri)
        assertEquals("urn:epc:id:sgtin:0614141.812345.6789", result.urn)
        assertEquals(48, result.header)
        assertEquals(3, result.filter)
        assertEquals(5, result.partition)
        assertEquals("0614141", result.companyPrefix)
        assertEquals("812345", result.itemReference)
        assertEquals(6789L, result.serialNumber)
    }

    @Test
    fun encode() {
        val result = EpcUtils.encode(
            filter = 3,
            companyPrefix = "0614141",
            itemReference = "812345",
            serialNumber = 6789L
        )
        assertNotNull(result)
        assertEquals("3074257BF7194E4000001A85", result)
    }

}
