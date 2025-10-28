package com.pedrozc90.rfid.devices.chainway

import android.content.Context
import android.util.Log
import com.pedrozc90.rfid.core.RfidDevice
import com.pedrozc90.rfid.objects.TagMetadata
import com.rscja.deviceapi.RFIDWithUHFUART
import kotlinx.coroutines.channels.BufferOverflow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.asSharedFlow

private const val TAG = "ChainwayRfidDevice"

class ChainwayRfidDevice(private val context: Context) : RfidDevice {

    private val reader: RFIDWithUHFUART = RFIDWithUHFUART.getInstance()

    private val _flow = MutableSharedFlow<TagMetadata>(
        extraBufferCapacity = 256,
        onBufferOverflow = BufferOverflow.DROP_OLDEST
    )
    override val flow: SharedFlow<TagMetadata> = _flow.asSharedFlow()

    override fun init() {
        Log.d(TAG, "Initializing...")
    }

    override fun close() {
        Log.d(TAG, "Closing...")
    }

    override fun start() {
        Log.d(TAG, "Starting...")
        try {
            reader.init(context)
        } catch (e: Exception) {
            Log.e(TAG, "Error while starting device", e)
        }
    }

    override fun stop() {
        Log.d(TAG, "Stopping...")
    }

}
