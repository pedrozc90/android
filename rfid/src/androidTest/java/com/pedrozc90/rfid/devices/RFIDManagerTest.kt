package com.pedrozc90.rfid.devices

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import com.pedrozc90.rfid.core.RfidDevice
import com.pedrozc90.rfid.core.RfidManager
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import org.junit.Assert
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class RFIDManagerTest {

    private lateinit var context: Context
    private lateinit var device: RfidDevice

    @Before
    fun setUp() {
        context = ApplicationProvider.getApplicationContext()
        device = RfidManager.factory(context = context)
    }

    @Test
    fun startInventory_emitsTag() = runBlocking {
        val started = device.startInventory()
        Assert.assertTrue(started)

        // collect first tag emitted
        val tag = device.flow.first()
        Assert.assertEquals("E2000017221101441890ABCD", tag.epc)
        Assert.assertEquals(-55, tag.rssi)
    }

//    @Test
//    fun writeTagSuspend_success() = runBlocking {
//        // should not throw
//        device.writeTagSuspend(
//            "00000000",
//            bank = 1,
//            ptr = 2,
//            cnt = 1,
//            dataHex = "ABCD",
//            filter = null
//        )
////        Assert.assertNotNull(reader.lastWriteCall)
////        Assert.assertEquals("00000000", reader.lastWriteCall?.first)
//    }

    @Test
    fun getTxPowerSuspend_returnsExpected() = runBlocking {
        val power = device.getPower()
        Assert.assertEquals(30, power)
    }

}
