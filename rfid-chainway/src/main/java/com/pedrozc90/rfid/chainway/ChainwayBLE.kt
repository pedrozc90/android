package com.pedrozc90.rfid.chainway

import android.content.Context
import android.util.Log
import com.pedrozc90.rfid.core.Options
import com.pedrozc90.rfid.core.RfidDevice
import com.pedrozc90.rfid.objects.DeviceEvent
import com.rscja.deviceapi.RFIDWithUHFBLE
import com.rscja.deviceapi.interfaces.ConnectionStatus
import com.rscja.deviceapi.interfaces.KeyEventCallback
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch

class ChainwayBLE(context: Context) : ChainwayBaseRfidDevice(context), RfidDevice {

    override val TAG = "ChainwayBluetoothRfidDevice"

    override var opts: Options? = null
    override val reader: RFIDWithUHFBLE = RFIDWithUHFBLE.getInstance()

    private var _batteryJob: Job? = null

    override val name = "ChainwayBluetoothRfidDevice"
    override val minPower: Int = 1
    override val maxPower: Int = 30

    override fun init(opts: Options) {
        this.opts = opts

        val address = opts.macAddress
            ?: throw IllegalArgumentException("Device MAC Address is required to connect via bluetooth.")

        reader.init(context)

        // set up the inventory callback to handle scanned tags
        reader.setInventoryCallback { handleInventory(it) }

        reader.setConnectionStatusCallback { status, data ->
            handleConnectionStatus(status, data)
        }

        // TODO: not working?
        reader.setKeyEventCallback(object : KeyEventCallback {
            override fun onKeyDown(p0: Int) {
                Log.d(TAG, "Key down event: $p0")
            }

            override fun onKeyUp(p0: Int) {
                Log.d(TAG, "Key up event: $p0")
            }
        })

        // connect asynchronously to avoid blocking
        scope.launch {
            reader.connect(address)
        }

        // start battery polling
        startBatteryPolling(opts)
    }

    override fun close() {
        // stop polling and inventory first to avoid conflicts
        if (_batteryJob?.isActive == true) _batteryJob?.cancel()
        if (_job?.isActive == true) _job?.cancel()

        try {
            if (reader.connectStatus == ConnectionStatus.CONNECTED) {
                reader.disconnect()
            }

            val free = reader.free()
            if (free) {
                Log.d(TAG, "Resources freed successfully")
            } else {
                Log.e(TAG, "Failed to free resources")
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error closing device.", e)
        } finally {
            super.close()
        }
    }

    override fun start(): Boolean {
        try {
            if (opts == null) {
                throw IllegalStateException("Device not initialized. Call init() before start().")
            }

            val frequencyUpdated = this.setFrequency(opts!!.frequency)
            if (frequencyUpdated) {
                Log.d(TAG, "Device frequency changed to ${opts!!.frequency}")
            } else {
                Log.e(TAG, "Failed to set device frequency to ${opts!!.frequency}")
            }

            val powerUpdated = this.setPower(opts!!.power)
            if (powerUpdated) {
                Log.d(TAG, "Device power changed to ${opts!!.power}")
            } else {
                Log.e(TAG, "Failed to set device power to ${opts!!.power}")
            }

            setTagFocus(false)
            configUHFInfo()
            disableFilters()

            setBeep(false)          // muito chato
            // reader.setVolume(1)  // não funciona

            val started = reader.startInventoryTag()
            if (started) {
                Log.d(TAG, "Inventory started successfully")
            } else {
                Log.e(TAG, "Failed to start inventory")
            }
            return started
        } catch (e: Exception) {
            Log.e(TAG, "Error starting device.", e)
            tryEmit(DeviceEvent.ErrorEvent(e))
            return false
        }
    }

    override fun stop(): Boolean {
        val stopped = reader.stopInventory()
        if (stopped) {
            Log.d(TAG, "Inventory stopped successfully")
        } else {
            Log.e(TAG, "Failed to stop inventory")
        }
        return stopped
    }

    private fun startBatteryPolling(opts: Options) {
        if (opts.battery && opts.batteryPollingDelay > 0) {
            if (_batteryJob?.isActive == true) {
                _batteryJob?.cancel()
            }

            _batteryJob = scope.launch {
                while (isActive) {
                    try {
                        val level = reader.battery
                        Log.d(TAG, "Battery Level: $level")
                        tryEmit(DeviceEvent.BatteryEvent(level = level))
                    } catch (e: Exception) {
                        Log.e(TAG, "Error reading battery level", e)
                        tryEmit(DeviceEvent.ErrorEvent(e))
                    }
                    delay(maxOf(opts.batteryPollingDelay, 1_000L))
                }
            }
        }
    }

    // API
    override fun getPower(): Int {
        return getPower(reader)
    }

    override fun setPower(value: Int): Boolean {
        return setPower(reader, value)
    }

    override fun getTagFocus(): Int {
        return getTagFocus(reader)
    }

    // BEEP
    override fun getBeep(): Boolean {
        val result = reader.getBeep()
        return result == 1
    }

    /**
     * Enable or disable audible beep on tag read or events.
     */
    override fun setBeep(enabled: Boolean): Boolean {
        return reader.setBeep(enabled)
    }

}
