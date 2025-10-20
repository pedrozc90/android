package com.pedrozc90.rfid.devices.chainway

import android.content.Context
import android.util.Log
import com.pedrozc90.rfid.core.RfidConnectionStatus
import com.pedrozc90.rfid.core.RfidDevice
import com.pedrozc90.rfid.core.objects.TagMetadata
import com.rscja.deviceapi.entity.UHFTAGInfo
import com.rscja.deviceapi.interfaces.ConnectionStatus
import kotlinx.coroutines.channels.BufferOverflow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.asSharedFlow

private const val TAG = "ChainwayBaseRfidDevice"

abstract class ChainwayBaseRfidDevice(
    protected val context: Context
) : RfidDevice {

    private val _flow = MutableSharedFlow<TagMetadata>(
        extraBufferCapacity = 256,
        onBufferOverflow = BufferOverflow.DROP_OLDEST
    )
    override val flow = _flow.asSharedFlow()

    private val _connectionStatus = MutableSharedFlow<RfidConnectionStatus>()
    override val connectionStatus = _connectionStatus.asSharedFlow()

    protected val callback: (UHFTAGInfo) -> Unit = { info ->
        try {
            val tag = TagMetadata.from(info)
            _flow.tryEmit(tag)
        } catch (e: Exception) {
            Log.e(TAG, "Error processing tag info", e)
        }
    }

    protected val connectionCallback: (ConnectionStatus, Any) -> Unit = { status, payload ->
        Log.d(TAG, "Battery callback - Status: $status, Payload: $payload")
        _connectionStatus.tryEmit(RfidConnectionStatus.of(status))
        // val device: BluetoothDevice = payload as BluetoothDevice
        when (status) {
            ConnectionStatus.CONNECTED -> Log.i(TAG, "Connected to device")
            ConnectionStatus.CONNECTING -> Log.i(TAG, "Connecting to device")
            ConnectionStatus.DISCONNECTED -> Log.i(TAG, "Disconnected to device")
        }
    }

}
