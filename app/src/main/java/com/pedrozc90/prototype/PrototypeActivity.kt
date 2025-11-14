package com.pedrozc90.prototype

import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import com.pedrozc90.prototype.core.bluetooth.BluetoothRepository
import com.pedrozc90.prototype.core.devices.DeviceDetector
import com.pedrozc90.prototype.ui.theme.PrototypeTheme

private const val TAG = "PrototypeActivity"

class PrototypeActivity : ComponentActivity() {

    private val bluetooth: BluetoothRepository by lazy {
        (application as PrototypeApplication).container.bluetooth
    }

    private val detector: DeviceDetector by lazy {
        (application as PrototypeApplication).container.detector
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        Log.d(TAG, "onCreate called")

        super.onCreate(savedInstanceState)

        bluetooth.onCreate(activity = this)
        detector.onCreate(context = application)

        enableEdgeToEdge()
        setContent {
            PrototypeTheme {
                PrototypeApp()
            }
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        bluetooth.onDestroy()
    }

}
