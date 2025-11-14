package com.pedrozc90.prototype.core.devices

import android.content.Context
import com.pedrozc90.rfid.core.RfidDevice
import com.pedrozc90.rfid.devices.chainway.ChainwayBluetoothRfidDevice
import com.pedrozc90.rfid.devices.chainway.ChainwayRfidDevice
import com.pedrozc90.rfid.devices.fake.FakeRfidDevice

class DeviceFactory {

    companion object {

        val options = mapOf(
            "fake" to "Fake Device",
            "chainway_uart" to "Chainway UART (C72)",
            "chainway_ble" to "Chainway Bluetooth (R6)",
        )

        fun build(type: String = "fake", context: Context): RfidDevice = when (type.lowercase()) {
            "chainway_uart" -> ChainwayRfidDevice(context)
            "chainway_ble" -> ChainwayBluetoothRfidDevice(context)
            else -> FakeRfidDevice()
        }

    }

}
