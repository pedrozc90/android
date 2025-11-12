package com.pedrozc90.rfid.devices.chainway

import android.content.Context
import android.util.Log
import com.pedrozc90.rfid.core.Options
import com.pedrozc90.rfid.core.RfidDevice
import com.pedrozc90.rfid.objects.DeviceEvent
import com.rscja.deviceapi.RFIDWithUHFUART

private const val TAG = "ChainwayRfidDevice"

class ChainwayRfidDevice(context: Context) : ChainwayBaseRfidDevice(context), RfidDevice {

    override val TAG: String = "ChainwayRfidDevice"

    override val reader: RFIDWithUHFUART = RFIDWithUHFUART.getInstance()

    override val minPower: Int = 0
    override val maxPower: Int = 100;

    override fun init(opts: Options) {
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
            setFrequencyMode("brazil")
            setPower(30)
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

    override fun getPower(): Int {
        return getPower(reader)
    }

    override fun setPower(value: Int): Boolean {
        return setPower(reader, value)
    }

    override fun getTagFocus(): Int {
        return -1
    }

}

