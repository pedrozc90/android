package com.pedrozc90.rfid.core

import com.pedrozc90.rfid.objects.DeviceEvent
import com.pedrozc90.rfid.objects.RfidDeviceStatus
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow

interface RfidDevice {

    /**
     * Flow emitting event from the device
     */
    val events: SharedFlow<DeviceEvent>

    /**
     * state flow to keep last device status
     */
    val status: StateFlow<RfidDeviceStatus>

    /**
     * Initialize the RFID device
     */
    fun init(opts: Options = Options())

    /**
     * Close the RFID device and release resources
     */
    fun close()

    /**
     * Start the RFID device inventory process
     */
    fun start(): Boolean

    /**
     * Stop the RFID device inventory process
     */
    fun stop(): Boolean

}

data class Options(
    val address: String? = null,            // device mac address, for bluetooth devices (e.g.: 'E5:F6:E0:3C:C5:AC')
    val battery: Boolean = false,           // whether to poll battery status
    val batteryPollingDelay: Long = 30_000L // milliseconds between battery status requests
)
