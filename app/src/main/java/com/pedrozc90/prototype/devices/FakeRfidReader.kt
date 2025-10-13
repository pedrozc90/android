package com.pedrozc90.prototype.devices

import android.util.Log
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import kotlinx.coroutines.yield
import kotlin.random.Random

const val TAG = "FakeRfidReader"

class FakeRfidReader(
    private val _delayMs: Long = 500L,
    private val _epcs: MutableList<String> = mutableListOf()
) : BaseRfidReader() {

    override val name: String = "Fake RFID Reader"

    private var job: Job? = null

    init {
        val itemReference = "1010101010"
        val count = 10_000 // Random.nextInt(500, 5_000)
        Log.d(TAG, "Generating $count EPCs")
        repeat(count) { idx ->
            val epc = generateEpc(itemReference, idx.toLong())
            _epcs.add(epc)
        }
    }

    override fun startReading() {
        if (job?.isActive == true) return

        Log.d(TAG, "Starting reading job")

        job = CoroutineScope(Dispatchers.Default).launch {
            val length = _epcs.size
            while (isActive) {
                val n = Random.nextInt(from = 0, until = 50)

                for (x in 0..n) {
                    val index = Random.nextInt(from = 0, until = length)
                    val epc = _epcs[index]
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

    override fun stopReading() {
        Log.d(TAG, "Stopping reading job")
        job?.cancel()
        job = null
    }

    private fun generateEpc(itemReference: String, serialNumber: Long): String {
        return "EPC:${itemReference}:${serialNumber.toString().padStart(7, '0')}"
    }

}
