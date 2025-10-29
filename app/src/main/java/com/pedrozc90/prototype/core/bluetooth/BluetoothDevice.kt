package com.pedrozc90.prototype.core.bluetooth

data class BluetoothDevice(
    val name: String,
    val address: String,
    val paired: Boolean = false
)

typealias BluetoothDeviceDto = BluetoothDevice
