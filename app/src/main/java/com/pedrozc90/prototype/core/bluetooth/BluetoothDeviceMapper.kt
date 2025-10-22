package com.pedrozc90.prototype.core.bluetooth

import android.annotation.SuppressLint
import android.bluetooth.BluetoothDevice

@SuppressLint("MissingPermission")
fun BluetoothDevice.toDto(): BluetoothDeviceDto {
    return BluetoothDeviceDto(
        name = name ?: address,
        address = address
    )
}
