package com.pedrozc90.prototype.core.devices

import android.content.Context
import com.pedrozc90.rfid.core.RfidDevice
import com.pedrozc90.rfid.devices.chainway.ChainwayBluetoothRfidDevice
import com.pedrozc90.rfid.devices.chainway.ChainwayRfidDevice
import com.pedrozc90.rfid.devices.fake.FakeRfidDevice

class DeviceManager(
    private val context: Context
) {

    fun build(type: String = "fake"): RfidDevice {
        return factory(type = type, context = context)
    }

    companion object {

        fun factory(type: String = "fake", context: Context): RfidDevice = when (type.lowercase()) {
            "chainway_uart" -> ChainwayRfidDevice(context = context)
            "chainway_ble" -> ChainwayBluetoothRfidDevice(context = context)
            else -> FakeRfidDevice()
        }

    }

}
