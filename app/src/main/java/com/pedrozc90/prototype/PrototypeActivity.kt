package com.pedrozc90.prototype

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import com.pedrozc90.prototype.ui.theme.PrototypeTheme

class PrototypeActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            PrototypeTheme {
                PrototypeApp()
            }
        }
    }

}
