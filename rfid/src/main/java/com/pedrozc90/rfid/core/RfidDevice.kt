package com.pedrozc90.rfid.core

import com.pedrozc90.rfid.objects.DeviceEvent
import com.pedrozc90.rfid.objects.RfidDeviceStatus
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow

interface RfidDevice {

    var opts: Options?

    val name: String
    val minPower: Int
    val maxPower: Int

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

    /**
     * Get the current inventory parameters
     */
    fun getInventoryParams(): Any?

    /**
     * Set the inventory parameters
     * @param value parameters to set
     * @return true if the operation was successful, false otherwise
     */
    fun setInventoryParams(value: Any): Boolean

    /**
     * Get the current frequency mode of the RFID device
     * @return current frequency mode
     */
    fun getFrequency(): DeviceFrequency

    /**
     * Set the frequency mode of the RFID device
     * @param value frequency mode to set
     * @return true if the operation was successful, false otherwise
     */
    fun setFrequency(value: DeviceFrequency): Boolean

    /**
     * Get the current read power of the RFID device
     * @return current power value
     */
    fun getPower(): Int

    /**
     * Set the read power of the RFID device
     * @param value power value to set
     * @return true if the operation was successful, false otherwise
     */
    fun setPower(value: Int): Boolean

    /**
     * Get the current beep setting
     * @return true if beep is enabled, false otherwise
     */
    fun getBeep(): Boolean

    /**
     * Enable or disable the beep sound on tag read
     * @param enabled true to enable, false to disable
     * @return true if the operation was successful, false otherwise
     */
    fun setBeep(enabled: Boolean): Boolean

}

data class Options(
    val macAddress: String? = null,                          // device mac address, for bluetooth devices (e.g.: 'E5:F6:E0:3C:C5:AC')
    val frequency: DeviceFrequency = DeviceFrequency.BRAZIL, // device frequency
    val power: Int = 1,                                      // device power
    val battery: Boolean = true,                             // whether to poll battery status
    val batteryPollingDelay: Long = 60_000L                  // milliseconds between battery status requests
)
