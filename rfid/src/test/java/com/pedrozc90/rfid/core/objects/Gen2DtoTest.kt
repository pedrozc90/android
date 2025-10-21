package com.pedrozc90.rfid.core.objects

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertThrows
import org.junit.Test

class Gen2DtoTest {

    @Test
    fun test() {
        val obj = Gen2Dto()
        assertNotNull(obj)
    }

    @Test
    fun validate() {
        val obj = Gen2Dto(
            selectTarget = 1,
            selectAction = 2,
            selectTruncate = 0,
            queryTarget = 0,
            startQ = 4,
            minQ = 2,
            maxQ = 8,
            queryDR = 1,
            queryM = 2,
            queryTRext = 0,
            querySel = 3,
            querySession = 1,
            q = 0,
            linkFrequency = 5
        )
        assertNotNull(obj)
    }

    @Test
    fun invalidParameters() {
        val exception = assertThrows(IllegalArgumentException::class.java) {
            val obj = Gen2Dto(minQ = 5, maxQ = 0)
        }
        assertEquals("minQ must be greater or equal to maxQ", exception.message)
    }

}
