package com.pedrozc90.prototype.core.devices

import android.content.Context
import android.util.Log
import com.pedrozc90.prototype.data.local.PreferencesRepository
import com.pedrozc90.rfid.helpers.DeviceDetector
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch

/**
 * Simple detection helper to decide whether the app runs on a built-in reader
 * or should talk to an external Bluetooth reader. Returns a key string that
 * can be used to select an SDK/implementation.
 *
 * This is intentionally small and focuses on detection signals:
 * - Built-in: Build.MANUFACTURER / MODEL / PRODUCT checks
 * - Remote (example): BluetoothDevice info should be additionally parsed during scanning
 */
class DeviceDetector(private val preferences: PreferencesRepository) : DeviceDetector() {

    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.Main.immediate)

    fun onCreate(context: Context) {
        // Run built-in detection on startup (non-blocking).
        // If a built-in device is found, persist it in the DeviceSelectionRepository.
        scope.launch {
            try {
                val detected = detectBuiltIn(context = context)
                Log.i(TAG, "Detector result: $detected")

                if (detected.builtIn && detected.type != null) {
                    // Persist the detected API name (eg "chainway_uart")
                    preferences.setBuiltInDevice(detected)
                    Log.i(TAG, "Persisted detected device type: ${detected.type}")
                } else {
                    Log.i(TAG, "No built-in device detected")
                }
            } catch (t: Throwable) {
                Log.w(TAG, "Device detection failed: ${t.message}")
            }
        }
    }

}
