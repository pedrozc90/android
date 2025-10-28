package com.pedrozc90.prototype

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import com.pedrozc90.prototype.core.bluetooth.BluetoothRepository
import com.pedrozc90.prototype.ui.theme.PrototypeTheme

private const val TAG = "PrototypeActivity"

class PrototypeActivity : ComponentActivity() {

    private val bluetooth: BluetoothRepository by lazy {
        (application as PrototypeApplication).container.bluetooth
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        bluetooth.onCreate(this)
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
