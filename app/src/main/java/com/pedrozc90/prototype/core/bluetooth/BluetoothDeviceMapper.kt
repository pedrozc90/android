package com.pedrozc90.prototype.core.bluetooth

import android.Manifest
import android.bluetooth.BluetoothDevice
import androidx.annotation.RequiresPermission

@RequiresPermission(Manifest.permission.BLUETOOTH_CONNECT)
fun BluetoothDevice.toDto(): BluetoothDeviceDto {
    return BluetoothDeviceDto(
        name = name ?: address,
        address = address
    )
}
