package com.pedrozc90.prototype.core.devices

enum class DeviceType(
    val type: String,
    val label: String,
    val bluetooth: Boolean = false,
) {

    FAKE(type = "fake", label = "Fake Device"),
    CHAINWAY_UART(type = "chainway_uart", label = "Chainway UART (C72)"),
    CHAINWAY_BLE(type = "chainway_ble", label = "Chainway Bluetooth (R6)", bluetooth = true);

    companion object {

        fun of(type: String?): DeviceType {
            return type.let { entries.firstOrNull { it.type == type } } ?: FAKE
        }

    }

}
