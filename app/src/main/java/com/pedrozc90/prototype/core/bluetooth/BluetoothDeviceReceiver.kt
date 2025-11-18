package com.pedrozc90.prototype.core.bluetooth

import android.Manifest
import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothDevice
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.os.Build
import android.util.Log
import androidx.annotation.RequiresPermission

private const val TAG = "BluetoothDeviceReceiver"

class BluetoothDeviceReceiver(
    private val onFound: (BluetoothDevice) -> Unit
) : BroadcastReceiver() {

    @RequiresPermission(Manifest.permission.BLUETOOTH_CONNECT)
    override fun onReceive(context: Context?, intent: Intent?) {
        val action = intent?.action
        when (action) {
            BluetoothDevice.ACTION_FOUND -> {
                val device = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                    intent.getParcelableExtra(
                        BluetoothDevice.EXTRA_DEVICE,
                        BluetoothDevice::class.java
                    )
                } else {
                    intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE)
                }

                Log.d(TAG, "Bluetooth device found: ${device?.name} - ${device?.address}")

                device?.let { onFound(it) }
            }

            BluetoothAdapter.ACTION_STATE_CHANGED -> {
                val state = intent.getIntExtra(BluetoothAdapter.EXTRA_STATE, BluetoothAdapter.ERROR)
                when (state) {
                    BluetoothAdapter.STATE_OFF -> {
                        Log.d(TAG, "Bluetooth is off")
                    }

                    BluetoothAdapter.STATE_TURNING_OFF -> {
                        Log.d(TAG, "Bluetooth is turning off")
                    }

                    BluetoothAdapter.STATE_ON -> {
                        Log.d(TAG, "Bluetooth is on")
                    }

                    BluetoothAdapter.STATE_TURNING_ON -> {
                        Log.d(TAG, "Bluetooth is turning on")
                    }
                }
            }

            else -> {
                Log.d(TAG, "Unknown action received: $action")
            }
        }
    }

}
