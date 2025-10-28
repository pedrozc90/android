package com.pedrozc90.prototype.core.bluetooth

data class BluetoothDevice(
    val name: String,
    val address: String
)

typealias BluetoothDeviceDto = BluetoothDevice
