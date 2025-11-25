package com.pedrozc90.rfid.chainway

import com.pedrozc90.rfid.chainway.core.toDeviceParams
import com.pedrozc90.rfid.chainway.core.toGen2Entity
import com.pedrozc90.rfid.objects.DeviceParams
import com.rscja.deviceapi.entity.Gen2Entity
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertThrows
import org.junit.Assert.assertTrue
import org.junit.Test

class DeviceParamsTest {

    @Test
    fun convertRfidParametersToDeviceParams() {
        val entity = Gen2Entity()
        val params = entity.toDeviceParams()
        assertNotNull(params)
    }

    @Test
    fun convertDeviceParamsToRfidParameters() {
        val params = DeviceParams(
            selectTarget = 0,
            selectAction = 0,
            selectTruncate = 0,
            queryTarget = 0,
            startQ = 0,
            minQ = 0,
            maxQ = 0,
            queryDR = 0,
            queryM = 0,
            queryTRext = 0,
            querySel = 0,
            querySession = 0,
            q = 0,
            linkFrequency = 0
        )
        val entity = params.toGen2Entity()
        assertNotNull(entity)
        assertTrue(entity.checkParameter())
        assertTrue("selectTarget must be 0..3 (s0..s3)", entity.selectTarget in 0..3)
        assertTrue("selectAction must be 0..6", entity.selectAction in 0..6)
        assertTrue("selectTruncate must be 0..1", entity.selectTruncate in 0..1)
        assertTrue("queryTarget must be >= 0", entity.queryTarget in 0..Int.MAX_VALUE)
        assertTrue("startQ must be 0..15", entity.startQ in 0..15)
        assertTrue("minQ must be 0..15", entity.minQ in 0..15)
        assertTrue("maxQ must be 0..15", entity.maxQ in 0..15)
        assertTrue("queryDR (dr) must be 0..1", entity.queryDR in 0..1)
        assertTrue("queryM (coding) must be 0..3", entity.queryM in 0..3)
        assertTrue("queryTRext (p) must be 0..1", entity.queryTRext in 0..1)
        assertTrue("querySel (Sel) must be 0..3", entity.querySel in 0..3)
        assertTrue("querySession (Session) must be 0..3", entity.querySession in 0..3)
        assertTrue("q (g) must be 0..1", entity.q in 0..1)
        assertTrue("linkFrequency must be 0..7", entity.linkFrequency in 0..7)
        assertTrue("minQ must be greater or equal to maxQ", entity.minQ <= entity.maxQ)
    }

    @Test
    fun convertDeviceParamsToRfidParameters_Invalid() {
        val params = DeviceParams(
            selectTarget = 0,
            selectAction = 0,
            selectTruncate = 0,
            queryTarget = 0,
            startQ = 0,
            minQ = 5,
            maxQ = 0,
            queryDR = 0,
            queryM = 0,
            queryTRext = 0,
            querySel = 0,
            querySession = 0,
            q = 0,
            linkFrequency = 0
        )

        val exception = assertThrows(IllegalArgumentException::class.java) {
            val entity = params.toGen2Entity()
            assertNotNull(entity)
            assertTrue(entity.checkParameter())
        }

        assertEquals("minQ must be greater or equal to maxQ", exception.message)
    }

}
