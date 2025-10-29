package com.pedrozc90.rfid.devices.chainway

import android.bluetooth.BluetoothDevice
import android.content.Context
import android.util.Log
import com.pedrozc90.rfid.core.BaseRfidDevice
import com.pedrozc90.rfid.core.Options
import com.pedrozc90.rfid.core.RfidDevice
import com.pedrozc90.rfid.objects.DeviceEvent
import com.pedrozc90.rfid.objects.RfidDeviceStatus
import com.pedrozc90.rfid.objects.TagMetadata
import com.rscja.deviceapi.RFIDWithUHFUART
import com.rscja.deviceapi.entity.UHFTAGInfo
import com.rscja.deviceapi.interfaces.ConnectionStatus
import kotlinx.coroutines.Job

private const val TAG = "ChainwayRfidDevice"

class ChainwayRfidDevice(private val context: Context) : BaseRfidDevice(), RfidDevice {

    private val reader: RFIDWithUHFUART = RFIDWithUHFUART.getInstance()

    private var _job: Job? = null

    override fun init(opts: Options) {
        reader.init(context)

        // set up the inventory callback to handle scanned tags
        reader.setInventoryCallback { onReceiveHandler(it) }

        reader.setConnectionStatusCallback { connStatus, data ->
            onConnectionStatusHandler(status = connStatus, data = data)
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
            reader.setEPCAndTIDMode()
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

    private fun onReceiveHandler(info: UHFTAGInfo) {
        try {
            val tag = TagMetadata.of(info)
            Log.d(TAG, "Info: $info -> Tag: $tag")

            val emitted = tryEmit(DeviceEvent.TagEvent(tag = tag))
            if (!emitted) {
                Log.e(TAG, "Failed to emit tag: $tag")
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error processing received tag info", e)
            tryEmit(DeviceEvent.ErrorEvent(e))
        }
    }

    private fun onConnectionStatusHandler(status: ConnectionStatus, data: Any) {
        try {
            val device = data as? BluetoothDevice
            Log.d(TAG, "Connection status changed: $status, payload: $device")
            val payload = RfidDeviceStatus.of(status = status, device = device)
            updateStatus(status = payload)
        } catch (e: Exception) {
            Log.e(TAG, "Error processing connection status change", e)
            tryEmit(DeviceEvent.ErrorEvent(e))
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

}

