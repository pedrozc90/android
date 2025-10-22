package com.pedrozc90.prototype.core.bluetooth

import androidx.activity.ComponentActivity
import kotlinx.coroutines.flow.StateFlow

interface BluetoothController {

    val scannedDevices: StateFlow<List<BluetoothDeviceDto>>
    val pairedDevices: StateFlow<List<BluetoothDeviceDto>>

    fun setup(activity: ComponentActivity)

    fun start()

    fun stop()

    fun release()

}
