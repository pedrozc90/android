package com.pedrozc90.rfid.devices.fake

import android.content.Context
import android.util.Log
import com.pedrozc90.rfid.core.RfidConnectionStatus
import com.pedrozc90.rfid.core.RfidDevice
import com.pedrozc90.rfid.core.RfidOptions
import com.pedrozc90.rfid.core.objects.Gen2Dto
import com.pedrozc90.rfid.core.objects.TagMetadata
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.channels.BufferOverflow
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import kotlinx.coroutines.yield
import kotlin.random.Random

const val TAG = "FakeRfidReader"

/**
 * Simple unit tests with a FakeReader to validate manager behavior.
 */
class FakeRfidDevice(
    private val context: Context,
    private val _delayMs: Long = 50L,
    private val _data: List<TagMetadata> = emptyList()
) : RfidDevice {

    private var _job: Job? = null

    private val _flow = MutableSharedFlow<TagMetadata>(
        extraBufferCapacity = 256,
        onBufferOverflow = BufferOverflow.DROP_OLDEST
    )
    override val flow = _flow.asSharedFlow()

    override val connectionStatus = MutableStateFlow(RfidConnectionStatus.DISCONNECTED)

    private var _power = MutableStateFlow(value = 30)
    private var _gen2 = MutableStateFlow(value = Gen2Dto())

    override suspend fun init(opts: RfidOptions) {
        Log.e(TAG, "Device initialized with options: $opts")
    }

    override suspend fun dispose() {
        Log.d(TAG, "Device disposed")
    }

    override suspend fun startInventory(): Boolean {
        Log.e(TAG, "Starting inventory")
        if (_job?.isActive != true) {

            Log.d(TAG, "Starting reading job")

            _job = CoroutineScope(Dispatchers.Default).launch {
                val length = _data.size
                while (isActive) {
                    val n = Random.nextInt(from = 0, until = 50)

                    for (x in 0..n) {
                        val index = Random.nextInt(from = 0, until = length)
                        val epc = _data[index]
                        // Use tryEmit so the emitter never suspends waiting for collectors.
                        // Suspending emit() combined with cancellation can create races where
                        // the emitter coroutine is cancelled while suspended and future
                        // starts don't behave as expected. tryEmit avoids that.
                        val emitted = _flow.tryEmit(epc)
                        if (!emitted) {
                            Log.d(TAG, "Failed to emit EPC (buffer full): $epc")
                        } else {
                            Log.d(TAG, "Scanned EPC: $epc")
                        }
                    }

                    if (_delayMs > 0) {
                        delay(_delayMs)
                    } else {
                        // If delay is 0, yield occasionally to give other coroutines (UI/collector) a chance.
                        // Not strictly necessary with a buffered channel, but polite.
                        yield()
                    }
                }
            }
        }
        return true
    }

    override suspend fun stopInventory(): Boolean {
        Log.e(TAG, "Stopping inventory")
        return true
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
