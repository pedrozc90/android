package com.pedrozc90.rfid.chainway

import android.content.Context
import android.util.Log
import com.pedrozc90.rfid.core.Options
import com.pedrozc90.rfid.core.RfidDevice
import com.pedrozc90.rfid.objects.DeviceEvent
import com.rscja.deviceapi.RFIDWithUHFUART

class ChainwayUART(context: Context) : ChainwayBaseRfidDevice(context), RfidDevice {

    override val TAG: String = "ChainwayRfidDevice"

    override var opts: Options? = null
    override val reader: RFIDWithUHFUART = RFIDWithUHFUART.getInstance()

    override val name = "ChainwayRfidDevice"
    override val minPower: Int = 1
    override val maxPower: Int = 30

    override fun init(opts: Options) {
        this.opts = opts

        reader.init(context)

        // set up the inventory callback to handle scanned tags
        reader.setInventoryCallback { this.handleInventory(it) }

        reader.setConnectionStatusCallback { connStatus, data ->
            this.handleConnectionStatus(status = connStatus, data = data)
        }
    }

    override fun close() {
        // stop polling and inventory first to avoid conflicts
        if (_job?.isActive == true) _job?.cancel()

        try {
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

    // API
    override fun getPower(): Int {
        return super.getPower(reader)
    }

    override fun setPower(value: Int): Boolean {
        return super.setPower(reader, value)
    }

    override fun getBeep(): Boolean {
        throw UnsupportedOperationException("Beep setting is not supported on $name")
    }

    override fun setBeep(enabled: Boolean): Boolean {
        throw UnsupportedOperationException("Beep setting is not supported on $name")
    }

    override fun getTagFocus(): Int {
        return -1
    }

}

