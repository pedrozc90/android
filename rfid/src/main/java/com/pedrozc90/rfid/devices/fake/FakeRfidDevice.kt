package com.pedrozc90.rfid.devices.fake

import android.content.Context
import android.util.Log
import com.pedrozc90.rfid.core.RfidConnectionStatus
import com.pedrozc90.rfid.core.RfidDevice
import com.pedrozc90.rfid.core.RfidOptions
import com.pedrozc90.rfid.core.objects.Gen2Dto
import com.pedrozc90.rfid.core.objects.TagMetadata
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.update

const val TAG = "FakeRfidReader"

/**
 * Simple unit tests with a FakeReader to validate manager behavior.
 */
class FakeRfidDevice(
    private val context: Context
) : RfidDevice {

    private val _flow = MutableSharedFlow<TagMetadata>()
    override val flow = _flow.asSharedFlow()

    override val connectionStatus = MutableStateFlow(RfidConnectionStatus.DISCONNECTED)

    private var _power = MutableStateFlow(30)
    private var _gen2 = MutableStateFlow(
        Gen2Dto(
            selectTarget = 0,
            selectAction = 0,
            selectTruncate = 0,
            queryTarget = 0,
            startQ = 4,
            minQ = 0,
            maxQ = 15,
            queryDR = 0,
            queryM = 0,
            queryTRext = 0,
            querySel = 0,
            querySession = 1,
            q = 4,
            linkFrequency = 0
        )
    )

    override suspend fun init(opts: RfidOptions) {
        Log.e(TAG, "Device initialized with options: $opts")
    }

    override suspend fun dispose() {
        Log.d(TAG, "Device disposed")
    }

    override suspend fun startInventory(): Boolean {
        Log.e(TAG, "Starting inventory")
        return true
    }

    override suspend fun stopInventory(): Boolean {
        Log.e(TAG, "Stopping inventory")
        return true
    }

    override suspend fun getVersion(): String? = "v0.0-fake"

    override suspend fun getPower(): Int = _power.value

    override suspend fun setPower(value: Int): Boolean {
        _power.update { value }
        return true
    }

    override suspend fun getGen2Settings(): Gen2Dto = _gen2.value

    override suspend fun setGen2Settings(value: Gen2Dto): Boolean {
        _gen2.update { value }
        return false
    }

    override suspend fun getBatteryLevel(): Int {
        return 100
    }

}
