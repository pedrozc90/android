package com.pedrozc90.rfid.core

import android.content.Context
import com.pedrozc90.rfid.devices.chainway.ChainwayBluetoothRfidDevice
import com.pedrozc90.rfid.devices.chainway.ChainwayRfidDevice
import com.pedrozc90.rfid.devices.fake.FakeRfidDevice

private const val TAG = "RfidManager"

class RfidManager {

    companion object {

        fun factory(context: Context, type: RfidDeviceType = RfidDeviceType.MOCK): RfidDevice {
            return when (type) {
                RfidDeviceType.CHAINWAY -> ChainwayRfidDevice(context)
                RfidDeviceType.CHAINWAY_BLUETOOTH -> ChainwayBluetoothRfidDevice(context)
                RfidDeviceType.MOCK -> FakeRfidDevice(context)
            }
        }

        fun identify(): RfidDeviceType {
            // TODO: HOW CAN WE AUTOMATICALLY IDENTIFY THE DEVICE TYPE ???
            return RfidDeviceType.MOCK
        }

    }

}
