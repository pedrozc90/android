package com.pedrozc90.prototype

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import com.pedrozc90.prototype.core.bluetooth.BluetoothController
import com.pedrozc90.prototype.ui.theme.PrototypeTheme

private const val TAG = "PrototypeActivity"

class PrototypeActivity : ComponentActivity() {

    private val bluetoothController: BluetoothController by lazy {
        val container = (application as PrototypeApplication).container
        container.bluetoothController
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        bluetoothController.setup(this)
        enableEdgeToEdge()
        setContent {
            PrototypeTheme {
                PrototypeApp()
            }
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        bluetoothController.release()
    }

}
