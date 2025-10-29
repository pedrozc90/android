package com.pedrozc90.rfid.core

import android.content.Context
import com.pedrozc90.rfid.devices.chainway.ChainwayBluetoothRfidDevice
import com.pedrozc90.rfid.devices.chainway.ChainwayRfidDevice
import com.pedrozc90.rfid.devices.fake.FakeRfidDevice

class RfidDeviceFactory {

    companion object {

        fun build(type: String = "fake", context: Context): RfidDevice = when (type.lowercase()) {
            "chainway_uart" -> ChainwayRfidDevice(context)
            "chainway_bluetooth" -> ChainwayBluetoothRfidDevice(context)
            else -> FakeRfidDevice()
        }

    }

}
