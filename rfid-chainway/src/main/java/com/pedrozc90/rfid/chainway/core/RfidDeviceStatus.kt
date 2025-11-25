package com.pedrozc90.rfid.chainway.core

import android.bluetooth.BluetoothDevice
import com.pedrozc90.rfid.objects.RfidDeviceStatus
import com.rscja.deviceapi.interfaces.ConnectionStatus

fun RfidDeviceStatus.Companion.of(
    status: ConnectionStatus,
    device: BluetoothDevice? = null
): RfidDeviceStatus = when (status) {
    ConnectionStatus.CONNECTED -> RfidDeviceStatus(status = "CONNECTED", device = device)
    ConnectionStatus.CONNECTING -> RfidDeviceStatus(status = "CONNECTING", device = device)
    ConnectionStatus.DISCONNECTED -> RfidDeviceStatus(status = "DISCONNECTED", device = device)
    //else -> RfidDeviceStatus(status = "UNKNOWN", device = device)
}
