package com.pedrozc90.rfid.devices

import android.util.Log
import com.pedrozc90.rfid.core.BaseRfidDevice
import com.pedrozc90.rfid.core.DeviceFrequency
import com.pedrozc90.rfid.core.Options
import com.pedrozc90.rfid.core.RfidDevice
import com.pedrozc90.rfid.objects.DeviceEvent
import com.pedrozc90.rfid.objects.DeviceParams
import com.pedrozc90.rfid.objects.RfidDeviceStatus
import com.pedrozc90.rfid.objects.TagMetadata
import com.pedrozc90.rfid.utils.EpcUtils
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import kotlinx.coroutines.yield
import kotlin.random.Random

private const val TAG = "FakeRfidDevice"

class FakeRfidDevice(
    private val delayMs: Long = 100L
) : BaseRfidDevice(), RfidDevice {

    override val name = "FakeRfidDevice"
    override val minPower: Int = 0
    override val maxPower: Int = 100

    override var opts: Options? = null
    private val _dataSource = mutableListOf<String>()

    private var _job: Job? = null
    private var _index: Int = 0

    private var _params = DeviceParams()
    private var _frequency: DeviceFrequency = DeviceFrequency.BRAZIL
    private var _power: Int = 0
    private var _beep: Boolean = false

    init {
        generateEpcs()
    }

    override fun init(opts: Options) {
        this.opts = opts
        Log.d(TAG, "Device initialized.")
        updateStatus(RfidDeviceStatus.Companion.of(status = "INITIALIZED"))
    }

    override fun close() {
        try {
            this.stop()
        } finally {
            super.close()
        }
    }

    override fun start(): Boolean {
        if (_job?.isActive == true) {
            Log.d(TAG, "Device has already been started")
            return false
        }

        updateStatus(RfidDeviceStatus.Companion.of(status = "RUNNING"))

        _job = CoroutineScope(Dispatchers.Default).launch {
            val length = _dataSource.size
            while (isActive) {
                // pick a random batch size
                val batch = Random.Default.nextInt(from = 0, until = 50)

                for (n in 0..batch) {
                    // pick a random index
                    val index = Random.Default.nextInt(from = 0, until = length)

                    val rfid = _dataSource[index]
                    val tag = TagMetadata(rfid = rfid)

                    // Use tryEmit so the emitter never suspends waiting for collectors.
                    // Suspending emit() combined with cancellation can create races where
                    // the emitter coroutine is cancelled while suspended and future
                    // starts don't behave as expected. tryEmit avoids that.
                    val emitted = tryEmit(DeviceEvent.TagEvent(tag = tag))
                    if (!emitted) {
                        Log.d(TAG, "Failed to emit EPC (buffer full): $rfid")
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

        return true
    }

    override fun stop(): Boolean {
        updateStatus(RfidDeviceStatus.Companion.of(status = "STOPPED"))
        if (_job?.isActive == true) {
            _job?.cancel()
            _job = null
        }
        return true
    }

    private fun generateEpcs() {
        Log.d(TAG, "Generating Epcs ...")
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

    // API
    override fun getInventoryParams(): DeviceParams? {
        return _params
    }

    override fun setInventoryParams(value: DeviceParams): Boolean {
        _params = value
        return true
    }

    override fun getFrequency(): DeviceFrequency {
        return _frequency
    }

    override fun setFrequency(value: DeviceFrequency): Boolean {
        _frequency = value
        return true
    }

    override fun checkFrequency(value: DeviceFrequency): Boolean {
        return true
    }

    override fun getPower(): Int {
        return _power
    }

    override fun setPower(value: Int): Boolean {
        _power = value
        return true
    }

    override fun getBeep(): Boolean {
        return _beep
    }

    override fun setBeep(enabled: Boolean): Boolean {
        _beep = enabled
        return true
    }

    override fun kill(rfid: String, password: String?): Boolean {
        throw UnsupportedOperationException("Kill operation is not supported.")
    }

}
