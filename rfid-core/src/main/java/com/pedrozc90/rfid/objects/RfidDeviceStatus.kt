package com.pedrozc90.rfid.objects

import android.bluetooth.BluetoothDevice

data class RfidDeviceStatus(
    val status: String = "NONE",
    val device: BluetoothDevice? = null
) {

    companion object {

        fun of(status: String): RfidDeviceStatus {
            return RfidDeviceStatus(status = status)
        }

    }

}
