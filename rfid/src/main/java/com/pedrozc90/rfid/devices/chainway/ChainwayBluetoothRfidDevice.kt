package com.pedrozc90.rfid.devices.chainway

import android.content.Context
import android.util.Log
import com.pedrozc90.rfid.core.RfidDevice
import com.pedrozc90.rfid.objects.TagMetadata
import com.rscja.deviceapi.RFIDWithUHFBLE
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.channels.BufferOverflow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.launch

private const val TAG = "ChainwayBluetoothRfidDevice"

class ChainwayBluetoothRfidDevice(private val context: Context) : RfidDevice {

    val reader: RFIDWithUHFBLE = RFIDWithUHFBLE.getInstance()

    private var _job: Job? = null

    private val _flow = MutableSharedFlow<TagMetadata>(
        extraBufferCapacity = 256,
        onBufferOverflow = BufferOverflow.DROP_OLDEST
    )
    override val flow: SharedFlow<TagMetadata> = _flow.asSharedFlow()

    override fun init() {
        Log.d(TAG, "Initializing ...")
    }

    override fun close() {
        Log.d(TAG, "Closing ...")
    }

    override fun start() {
        Log.d(TAG, "Starting...")

        try {
            reader.init(context)

            reader.setInventoryCallback { info ->
                val tag = TagMetadata.of(info)
                Log.d(TAG, "Info: $info -> Tag scanned: $tag")
            }

            // MAC Address of the Chainway device
            reader.connect("E5:F6:E0:3C:C5:AC") { status, payload ->
                Log.d(TAG, "Connection status changed: $status, payload: $payload")
            }

            reader.setEPCAndTIDMode()

            val started = reader.startInventoryTag()
            if (started) {
                Log.d(TAG, "Inventory started successfully")
            } else {
                Log.e(TAG, "Failed to start inventory")
            }
        } catch (e: Exception) {
            e.printStackTrace()
            Log.e(TAG, "Error starting Chainway Bluetooth RFID Device", e)
        }
    }

    override fun stop() {
        val stopped = reader.stopInventory()
        if (stopped) {
            Log.d(TAG, "Inventory stopped successfully")
        } else {
            Log.e(TAG, "Failed to stop inventory")
        }

        val free = reader.free()
        if (free) {
            Log.d(TAG, "Resources freed successfully")
        } else {
            Log.e(TAG, "Failed to free resources")
        }

        reader.disconnect()
        Log.d(TAG, "Disconnected from device")

        if (_job?.isActive == true) {
            Log.d(TAG, "Device has already been stopped")
            _job?.cancel()
            _job = null
        }
    }

    fun onStart() {
        CoroutineScope(Dispatchers.IO).launch {
            start()
        }
    }

    fun onStop() {
        CoroutineScope(Dispatchers.IO).launch {
            stop()
        }
    }

}
