package com.pedrozc90.prototype.core.devices

import android.content.Context
import android.os.Build
import android.util.Log
import com.pedrozc90.prototype.data.local.PreferencesRepository
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
class DeviceDetector(
    private val preferences: PreferencesRepository
) {
    private val TAG = "DeviceDetector";

    private val _built_in = listOf(
        Device(name = "chainway_uart", keys = arrayOf("c72", "c72a")),
        Device(
            name = "urovo_uart",
            keys = arrayOf("dt50", "dt50d", "dt50p", "dt610")
        ), // urovo_uart
        Device(
            name = "bluebird_uart",
            keys = arrayOf("s10")
        ),                          // bluebird_uart
        Device(
            name = "zebra_uart",
            keys = arrayOf("tc22r")
        )                            // zebra_uart
    )

    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.Main.immediate)

    fun onCreate(context: Context) {
        // Run built-in detection on startup (non-blocking).
        // If a built-in device is found, persist it in the DeviceSelectionRepository.
        scope.launch {
            try {
                val detected = detectBuiltIn(context = context)
                Log.i(TAG, "Detector result: $detected")

                if (detected.isBuiltInDevice && !detected.detectedType.isNullOrBlank()) {
                    // Persist the detected API name (eg "chainway_uart")
                    preferences.setBuiltInDevice(detected)
                    Log.i(TAG, "Persisted detected device type: ${detected.detectedType}")
                } else {
                    Log.i(TAG, "No built-in device detected")
                }
            } catch (t: Throwable) {
                Log.w(TAG, "Device detection failed: ${t.message}")
            }
        }
    }

    /**
     * Check if running on a known built-in reader by inspecting Build properties.
     * Keep a local mapping of known models -> key.
     */
    fun detectBuiltIn(context: Context): Result {
        // Build.ID - build id string meant for internal use (e.g. "MRA58K").
        // Often contains build tags / incremental build identifier.
        val id = Build.ID ?: "" // "MRA58K"

        // Build.MODEL - end-user-visible model name (e.g. "C72", "Pixel 6").
        // Commonly used to identify device model variants.
        val model = Build.MODEL ?: "" // "C72"

        // Build.MANUFACTURER - manufacturer/brand (e.g. "Samsung", "Xiaomi", or vendor OEM).
        val manufacturer = Build.MANUFACTURER ?: "" // "wtk"

        // Build.PRODUCT - product name (build-time product identifier produced by device's build system).
        // Often more consistent for embedded devices (e.g. "c72a").
        val product = Build.PRODUCT ?: "" // "c72a"

        // Build.DEVICE - device codename (kernel/device tree name), often used by vendors internally.
        val device = Build.DEVICE ?: "" // "c72"

        // Build.BOARD - board name (name of the underlying board/hardware platform).
        val board = Build.BOARD ?: "" // "c72a"

        // Build.HARDWARE - name of the hardware (e.g. SoC/platform identifier like "mt6735").
        // Useful when distinguishing different chips (CPU/GPU) used by the same model.
        val hardware = Build.HARDWARE ?: "" // "mt6735"

        // Build.SERIAL - device serial number (deprecated on newer Android versions).
        // Historically unique but now restricted; may be "unknown" or require READ_PHONE_STATE/system permission.
        val serial =
            if (Build.VERSION.SDK_INT < Build.VERSION_CODES.O) Build.SERIAL else null // "HC720A180800024"

        val deviceInfo = DeviceInfo(
            id = id,
            model = model,
            manufacturer = manufacturer,
            product = product,
            device = device,
            board = board,
            hardware = hardware,
            serial = serial
        )

        Log.i(TAG, "BuiltIn detection $deviceInfo")

        val result = Result(device = deviceInfo)

        for (row in _built_in) {
            if (row.keys.contains(model)) {
                return result.copy(
                    isBuiltInDevice = true,
                    detectedType = row.name,
                    detectedKey = model,
                    reason = "build model '$model' matched"
                )
            } else if (row.keys.contains(manufacturer)) {
                return result.copy(
                    isBuiltInDevice = true,
                    detectedType = row.name,
                    detectedKey = manufacturer,
                    reason = "build manufacturer '$manufacturer' matched"
                )
            } else if (row.keys.contains(product)) {
                return result.copy(
                    isBuiltInDevice = true,
                    detectedType = row.name,
                    detectedKey = product,
                    reason = "build product '$product' matched"
                )
            } else if (row.keys.contains(device)) {
                return result.copy(
                    isBuiltInDevice = true,
                    detectedType = row.name,
                    detectedKey = device,
                    reason = "build device '$device' matched"
                )
            } else if (row.keys.contains(board)) {
                return result.copy(
                    isBuiltInDevice = true,
                    detectedType = row.name,
                    detectedKey = board,
                    reason = "build board '$board' matched"
                )
            } else if (row.keys.contains(hardware)) {
                return result.copy(
                    isBuiltInDevice = true,
                    detectedType = row.name,
                    detectedKey = hardware,
                    reason = "build hardware '$hardware' matched"
                )
            }
        }

        // Optionally check for manufacturer-provided package or file presence (not shown).
        return result
    }

    data class Result(
        val isBuiltInDevice: Boolean = false,   // mark if device is built-in, e.g: C72
        val detectedType: String? = null,       // detected device type, e.g: "chainway_uart"
        val detectedKey: String? = null,        // parameter that matched
        val reason: String? = null,
        // device params
        val device: DeviceInfo
    )

    data class Device(
        val name: String,
        val keys: Array<String>
    ) {
        override fun equals(other: Any?): Boolean {
            if (this === other) return true
            if (javaClass != other?.javaClass) return false

            other as Device

            if (name != other.name) return false
            if (!keys.contentEquals(other.keys)) return false

            return true
        }

        override fun hashCode(): Int {
            var result = name.hashCode()
            result = 31 * result + keys.contentHashCode()
            return result
        }
    }

}
