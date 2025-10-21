package com.pedrozc90.rfid.devices.urovo

import android.content.Context
import android.util.Log
import com.pedrozc90.rfid.core.RfidConnectionStatus
import com.pedrozc90.rfid.core.RfidDevice
import com.pedrozc90.rfid.core.RfidOptions
import com.pedrozc90.rfid.core.objects.Gen2Dto
import com.pedrozc90.rfid.core.objects.TagMetadata
import com.ubx.usdk.USDKManager
import com.ubx.usdk.rfid.RfidManager
import com.ubx.usdk.rfid.aidl.IRfidCallback
import kotlinx.coroutines.channels.BufferOverflow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.asSharedFlow

private const val TAG = "UrovoRfidDevice"

class UrovoRfidDevice(private val context: Context) : RfidDevice {

    private val manager: USDKManager = USDKManager.getInstance()
    private val reader: RfidManager = manager.rfidManager

    private val _flow = MutableSharedFlow<TagMetadata>(
        extraBufferCapacity = 256,
        onBufferOverflow = BufferOverflow.DROP_OLDEST
    )
    override val flow = _flow.asSharedFlow()

    private val _connectionStatus = MutableSharedFlow<RfidConnectionStatus>()
    override val connectionStatus = _connectionStatus.asSharedFlow()

    override suspend fun init(opts: RfidOptions) {
        manager.init(context) { status ->
            Log.d(TAG, "$status")
            when (status) {
                USDKManager.STATUS.SUCCESS -> {
                    _connectionStatus.tryEmit(RfidConnectionStatus.CONNECTED)
                }
                else -> {
                    _connectionStatus.tryEmit(RfidConnectionStatus.DISCONNECTED)
                }
            }
        }

        val callback = TagCallback(_flow)
        reader.registerCallback(callback)
    }

    override suspend fun dispose() {
        reader.disConnect()
    }

    suspend fun isConnected(): Boolean {
        return reader.isConnected()
    }

    override suspend fun startInventory(): Boolean {
        val session = Byte.MIN_VALUE
        val res = reader.startInventory(session)
        if (res != 0) {
            return false
        }
        return true
    }

    override suspend fun stopInventory(): Boolean {
        val res = reader.stopInventory()
        return (res != 0)
    }

    override suspend fun write(
        pwd: String?,
        bank: Int,
        data: String,
        ptr: Int,
        length: Int,
        filter: String?
    ): Boolean {
        throw UnsupportedOperationException("Method not supported")
    }

    override suspend fun getVersion(): String? {
        val deviceId = reader.getDeviceId()
        val readerType = reader.getReaderType()
        return reader.getFirmwareVersion()
    }

    override suspend fun getPower(): Int = reader.getOutputPower()

    override suspend fun setPower(value: Int): Boolean {
        val res = reader.setOutputPower(value.toByte())
        if (res < 0 || res > 33) {
            Log.e(TAG, "Failed to set power to $value, result: $res")
            return false
        }
        return (res == 0)
    }

    override suspend fun getGen2Settings(): Gen2Dto {
        throw UnsupportedOperationException("Urovo RFID reader does not support retrieving Gen2 parameters")
    }

    override suspend fun setGen2Settings(value: Gen2Dto): Boolean {
        throw UnsupportedOperationException("Urovo RFID reader does not support setting Gen2 parameters")
    }

    override suspend fun getBatteryLevel(): Int {
        throw UnsupportedOperationException("Urovo RFID reader does not support battery level retrieval")
    }

}

class TagCallback(private val _flow: MutableSharedFlow<TagMetadata>) : IRfidCallback {

    override fun onInventoryTag(p0: String?, p1: String?, p2: String?) {
        Log.d(TAG, "onInventoryTag called with p0: $p0, p1: $p1, p2: $p2")
        val tag = TagMetadata(epc = p0 ?: "", tid = p1, rssi = p2)
        _flow.tryEmit(tag)
    }

    override fun onInventoryTagEnd() {
        Log.d(TAG, "onInventoryTagEnd called")
    }

}
