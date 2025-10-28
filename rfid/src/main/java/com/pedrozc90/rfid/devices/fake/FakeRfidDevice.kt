package com.pedrozc90.rfid.devices.fake

import android.util.Log
import com.pedrozc90.rfid.core.RfidDevice
import com.pedrozc90.rfid.objects.TagMetadata
import com.pedrozc90.rfid.utils.EpcUtils
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.channels.BufferOverflow
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import kotlinx.coroutines.yield
import kotlin.random.Random

private const val TAG = "FakeRfidDevice"

class FakeRfidDevice(
    private val delayMs: Long = 100L
) : RfidDevice {

    private val _dataSource = mutableListOf<String>()

    private val _flow = MutableSharedFlow<TagMetadata>(
        extraBufferCapacity = 256,
        onBufferOverflow = BufferOverflow.DROP_OLDEST
    )
    override val flow: SharedFlow<TagMetadata> = _flow.asSharedFlow()

    private var _job: Job? = null
    private var _index: Int = 0

    init {
        init()
    }

    override fun init() {
        Log.d(TAG, "Initializing ...")
        repeat(10_000) { idx ->
            val epc = EpcUtils.encode(
                filter = 3,
                companyPrefix = "0614141",
                itemReference = "101010",
                serialNumber = idx.toLong()
            )
            _dataSource.add(epc)
        }
    }

    override fun close() {
        Log.d(TAG, "Closing ...")
    }

    override fun start() {
        if (_job?.isActive == true) {
            Log.d(TAG, "Device has already been started")
            return
        }

        Log.d(TAG, "Starting ...")

        _job = CoroutineScope(Dispatchers.Default).launch {
            val length = _dataSource.size
            while (isActive) {
                // pick a random batch size
                val batch = Random.nextInt(from = 0, until = 50)

                for (n in 0 .. batch) {
                    // pick a random index
                    val index = Random.nextInt(from = 0, until = length)

                    val rfid = _dataSource[index]
                    val tag = TagMetadata(rfid = rfid)

                    // Use tryEmit so the emitter never suspends waiting for collectors.
                    // Suspending emit() combined with cancellation can create races where
                    // the emitter coroutine is cancelled while suspended and future
                    // starts don't behave as expected. tryEmit avoids that.
                    val emitted = _flow.tryEmit(tag)
                    if (!emitted) {
                        Log.d(TAG, "Failed to emit EPC (buffer full): $rfid")
                    } else {
                        Log.d(TAG, "Scanned EPC: $rfid")
                    }
                }

                if (delayMs > 0) {
                    delay(delayMs)
                } else {
                    // If delay is 0, yield occasionally to give other coroutines (UI/collector) a chance.
                    // Not strictly necessary with a buffered channel, but polite.
                    yield()
                }

                _index++
            }
        }

    }

    override fun stop() {
        Log.d(TAG, "Stopping ...")
        _job?.cancel()
        _job = null
    }

}
