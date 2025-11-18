package com.pedrozc90.prototype.core.devices

import android.content.Context
import com.pedrozc90.rfid.core.RfidDevice
import com.pedrozc90.rfid.devices.chainway.ChainwayBluetoothRfidDevice
import com.pedrozc90.rfid.devices.chainway.ChainwayRfidDevice
import com.pedrozc90.rfid.devices.fake.FakeRfidDevice

class DeviceFactory {

    companion object {

        fun build(type: String = "fake", context: Context): RfidDevice = when (type.lowercase()) {
            "chainway_uart" -> ChainwayRfidDevice(context)
            "chainway_ble" -> ChainwayBluetoothRfidDevice(context)
            else -> FakeRfidDevice()
        }

    }

}
