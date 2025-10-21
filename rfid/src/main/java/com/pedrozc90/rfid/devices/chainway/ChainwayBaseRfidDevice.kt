package com.pedrozc90.rfid.devices.chainway

import android.content.Context
import android.util.Log
import com.pedrozc90.rfid.core.RfidConnectionStatus
import com.pedrozc90.rfid.core.RfidDevice
import com.pedrozc90.rfid.core.objects.TagMetadata
import com.rscja.deviceapi.entity.UHFTAGInfo
import com.rscja.deviceapi.interfaces.ConnectionStatus
import com.rscja.deviceapi.interfaces.IUHF
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

    protected fun write(
        reader: IUHF,
        pwd: String?,
        bank: Int,
        data: String,
        ptr: Int,
        length: Int,
        filter: String?
    ): Boolean {
        // normalize password
        val _pwd = pwd?.takeIf { it.isNotBlank() } ?: "00000000"

        // VALIDATIONS
        require(_pwd.length == 8) { "The length of the access password must be 8" }
        require(validateHexadecimal(_pwd)) { "Please enter a hexadecimal password" }

        require(data.isNotEmpty()) { "Write 'data' can not be empty" }
        require(validateHexadecimal(data)) { "Write 'data' must be hexadecimal" }
        require(data.length % 4 == 0) { "Write 'data' length must be a multiple of 4" }

        // cntStr is non-null Int parameter; ensure it's positive (adjust if zero is allowed)
        require(length > 0) { "Write 'length' must be greater than 0" }

        // val writeLen = length
        // val writePtr = ptr.toIntOrNull() ?: throw IllegalArgumentException("strPtr must be a decimal integer")

        // strData has 4 chars per word; availableWords must be >= writeLen
        val availableWords = data.length / 4
        require(availableWords >= length) { "The written content and length do not match!" }

        val hasFilter = filter != null && filter != "null"
        val targetFilter = if (hasFilter) filter else ""
        val startFilter = if (hasFilter) 32 else 0
        val endFilter = if (hasFilter) 96 else 0

        // WRITE
        if (length > 32) {
            val chunkSize = 32
            val count = (length + chunkSize - 1) / chunkSize // ceil(writeLen / 32)
            var currTotal = length
            var currStart = ptr

            for (k in 0 until count) {
                val toWrite = minOf(chunkSize, currTotal)
                val res = reader.writeData(
                    pwd,
                    bank,
                    startFilter,
                    endFilter,
                    targetFilter,
                    bank,
                    currStart,
                    toWrite,
                    data
                )

                if (res) {
                    currStart += toWrite
                    currTotal -= toWrite
                } else {
                    throw RuntimeException("Failed to write ($currStart - ${ptr + length - 1})")
                }
            }
            return true
        } else {
            val res = reader.writeData(
                _pwd,
                bank,
                startFilter,
                endFilter,
                targetFilter,
                bank,
                ptr,
                length,
                data
            )
            return res//reader.errCode
        }
    }

    // Utils
    /**
     * Preserve original name for compatibility.
     * Returns true when str is non-null, non-empty, even-length and contains only hex digits.
     */
    private fun validateHexadecimal(value: String?): Boolean {
        if (value.isNullOrEmpty()) return false
        if (value.length % 2 != 0) return false
        return isHexadecimal(value)
    }

    /**
     * Matches only hexadecimal digits (0-9, a-f, A-F) for the entire string.
     * Anchors are important so partial matches don't pass.
     */
    private fun isHexadecimal(value: String): Boolean = Regex("(?i)^[0-9a-f]+$").matches(value)

    /**
     * Returns true when value is non-null, non-empty and every character is a decimal digit.
     */
    private fun isDecimal(value: String?): Boolean =
        !value.isNullOrEmpty() && value.all { it.isDigit() }

}
