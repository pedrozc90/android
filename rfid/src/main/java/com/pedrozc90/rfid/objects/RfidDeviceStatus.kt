package com.pedrozc90.rfid.objects

import android.bluetooth.BluetoothDevice
import com.rscja.deviceapi.interfaces.ConnectionStatus

data class RfidDeviceStatus(
    val status: String = "NONE",
    val device: BluetoothDevice? = null
) {

    companion object {

        fun of(status: String): RfidDeviceStatus {
            return RfidDeviceStatus(status = status)
        }

        fun of(status: ConnectionStatus, device: BluetoothDevice? = null): RfidDeviceStatus = when (status) {
            ConnectionStatus.CONNECTED -> RfidDeviceStatus(status = "CONNECTED", device = device)
            ConnectionStatus.CONNECTING -> RfidDeviceStatus(status = "CONNECTING", device = device)
            ConnectionStatus.DISCONNECTED -> RfidDeviceStatus(status = "DISCONNECTED", device = device)
            //else -> RfidDeviceStatus(status = "UNKNOWN", device = device)
        }

    }

}
