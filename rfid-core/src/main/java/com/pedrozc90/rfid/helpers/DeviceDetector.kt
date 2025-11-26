package com.pedrozc90.rfid.helpers

import android.content.Context
import android.os.Build
import android.util.Log

/**
 * Simple detection helper to decide whether the app runs on a built-in reader
 * or should talk to an external Bluetooth reader. Returns a key string that
 * can be used to select an SDK/implementation.
 *
 * This is intentionally small and focuses on detection signals:
 * - Built-in: Build.MANUFACTURER / MODEL / PRODUCT checks
 * - Remote (example): BluetoothDevice info should be additionally parsed during scanning
 */
abstract class DeviceDetector {

    protected val TAG = "DeviceDetector"

    protected val supported = listOf(
        Device(type = DeviceType.CHAINWAY_UART, matches = setOf("c72", "c72a")),
        Device(
            type = DeviceType.UROVO_UART,
            matches = setOf("dt50", "dt50d", "dt50p", "dt610")
        ), // urovo_uart
        // Device(type = DeviceType.BLUEBIRD_UART, matches = setOf("s10")),                          // bluebird_uart
        // Device(type = DeviceType.ZEBRA_UART, matches = setOf("tc22r"))                            // zebra_uart
    )

    /**
     * Check if running on a known built-in reader by inspecting Build properties.
     * Keep a local mapping of known models -> key.
     */
    protected fun detectBuiltIn(context: Context): Result {
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

        // Property name -> actual value on the current device (we'll use this to produce a readable reason)
        val checks = listOf(
            "model" to model,
            "manufacturer" to manufacturer,
            "product" to product,
            "device" to device,
            "board" to board,
            "hardware" to hardware
        )

        for (row in supported) {
            // Precompute lowercase keys for fast, case-insensitive membership checks.
            for ((key, value) in checks) {
                if (value.isNotBlank()) {
                    val normalized = value.lowercase()
                    if (normalized in row.matches) {
                        return result.copy(
                            builtIn = true,
                            type = row.type,
                            key = key,
                            value = value,
                        )
                    }
                }
            }
        }

        // Optionally check for manufacturer-provided package or file presence (not shown).
        return result
    }

    /**
     * Device metadata
     */
    data class DeviceInfo(
        val id: String? = null,
        val model: String? = null,
        val manufacturer: String? = null,
        val product: String? = null,
        val device: String? = null,
        val board: String? = null,
        val hardware: String? = null,
        val serial: String? = null
    )

    data class Result(
        val builtIn: Boolean = false,   // mark if device is built-in, e.g: C72
        val type: DeviceType? = null,   // detected device type, e.g: "chainway_uart"
        val key: String? = null,        // matched key
        val value: String? = null,      // matched value
        val device: DeviceInfo
    )

    data class Device(
        val type: DeviceType,
        val matches: Set<String> = setOf()
    )
}

enum class DeviceType(
    val label: String,
    val builtIn: Boolean = false,
    val bluetooth: Boolean = false
) {

    CHAINWAY_UART(label = "Chainway UART (C72)", builtIn = true),
    CHAINWAY_BLE(label = "Chainway BLE (R6)", bluetooth = true),
    UROVO_UART(label = "Urovo UART", builtIn = true),
    FAKE(label = "Fake Device");

    companion object {

        fun of(value: String?): DeviceType {
            return value.let { entries.firstOrNull { it.name == value } } ?: FAKE
        }

    }

}
