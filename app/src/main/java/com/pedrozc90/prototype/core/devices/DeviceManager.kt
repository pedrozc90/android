package com.pedrozc90.prototype.core.devices

import android.content.Context
import android.util.Log
import com.pedrozc90.rfid.chafon.ChafonBLE
import com.pedrozc90.rfid.chainway.ChainwayBLE
import com.pedrozc90.rfid.chainway.ChainwayUART
import com.pedrozc90.rfid.core.RfidDevice
import com.pedrozc90.rfid.devices.FakeRfidDevice
import com.pedrozc90.rfid.helpers.DeviceType
import com.pedrozc90.rfid.urovo.UrovoUART

private const val TAG = "DeviceManager"

class DeviceManager(private val context: Context) {

    fun build(type: DeviceType): RfidDevice? {
        return try {
            factory(type = type, context = context)
        } catch (tr: Throwable) {
            Log.e(TAG, "Unable to initialize device ${type.label}", tr)
            null
        }
    }

    companion object {

        fun factory(type: DeviceType = DeviceType.FAKE, context: Context): RfidDevice =
            when (type) {
                DeviceType.CHAINWAY_UART -> ChainwayUART(context = context)
                DeviceType.CHAINWAY_BLE -> ChainwayBLE(context = context)
                DeviceType.UROVO_UART -> UrovoUART(context = context)
                DeviceType.CHAFON_BLE -> ChafonBLE(context = context)
                else -> FakeRfidDevice()
            }

    }

}
