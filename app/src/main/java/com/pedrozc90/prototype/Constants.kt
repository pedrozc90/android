package com.pedrozc90.prototype

import com.pedrozc90.rfid.helpers.DeviceType


class Constants {
    companion object {
        const val BATCH_SIZE: Int = 500
        const val BATCH_TIMEOUT: Long = 250L  // ms
        const val STATE_TIMEOUT: Long = 5_000L // ms

        val SUPPORTED_DEVICES = listOf(
            DeviceType.FAKE,
            DeviceType.CHAINWAY_UART,
            DeviceType.CHAINWAY_BLE,
            DeviceType.UROVO_UART,
            DeviceType.CHAFON_BLE,
        )
    }
}
