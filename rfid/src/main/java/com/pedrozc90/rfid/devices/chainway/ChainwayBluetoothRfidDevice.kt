package com.pedrozc90.rfid.devices.chainway

import android.bluetooth.BluetoothDevice
import android.content.Context
import android.util.Log
import com.pedrozc90.rfid.core.BaseRfidDevice
import com.pedrozc90.rfid.core.Options
import com.pedrozc90.rfid.core.RfidDevice
import com.pedrozc90.rfid.devices.chainway.objects.Gen2Dto
import com.pedrozc90.rfid.objects.DeviceEvent
import com.pedrozc90.rfid.objects.RfidDeviceStatus
import com.pedrozc90.rfid.objects.TagMetadata
import com.rscja.deviceapi.RFIDWithUHFBLE
import com.rscja.deviceapi.entity.UHFTAGInfo
import com.rscja.deviceapi.interfaces.ConnectionStatus
import com.rscja.deviceapi.interfaces.KeyEventCallback
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch

private const val TAG = "ChainwayBluetoothRfidDevice"

class ChainwayBluetoothRfidDevice(private val context: Context) : BaseRfidDevice(), RfidDevice {

    private val reader: RFIDWithUHFBLE = RFIDWithUHFBLE.getInstance()

    private var _job: Job? = null
    private var _batteryJob: Job? = null

    override fun init(opts: Options) {
        val address = opts.address
            ?: throw IllegalArgumentException("Device MAC Address is required to connect via bluetooth.")

        reader.init(context)

        // set up the inventory callback to handle scanned tags
        reader.setInventoryCallback { onReceiveInfo(it) }

        reader.setConnectionStatusCallback { status, data ->
            onConnectionStatusHandler(status, data)
        }

        reader.setKeyEventCallback(object : KeyEventCallback {
            override fun onKeyDown(p0: Int) {
                Log.d(TAG, "Key down event: $p0")
            }

            override fun onKeyUp(p0: Int) {
                Log.d(TAG, "Key up event: $p0")
            }
        })

        // connect asynchronously to avoid blocking
        scope.launch {
            reader.connect(address)
        }

        // start battery polling
        startBatteryPolling(opts)
    }

    override fun close() {
        // stop polling and inventory first to avoid conflicts
        if (_batteryJob?.isActive == true) _batteryJob?.cancel()
        if (_job?.isActive == true) _job?.cancel()

        try {
            if (reader.connectStatus == ConnectionStatus.CONNECTED) {
                reader.disconnect()
            }

            val free = reader.free()
            if (free) {
                Log.d(TAG, "Resources freed successfully")
            } else {
                Log.e(TAG, "Failed to free resources")
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error closing device.", e)
        } finally {
            super.close()
        }
    }

    override fun start(): Boolean {
        try {
            reader.setEPCAndTIDMode()

            val started = reader.startInventoryTag()
            if (started) {
                Log.d(TAG, "Inventory started successfully")
            } else {
                Log.e(TAG, "Failed to start inventory")
            }
            return started
        } catch (e: Exception) {
            Log.e(TAG, "Error starting device.", e)
            tryEmit(DeviceEvent.ErrorEvent(e))
            return false
        }
    }

    private fun onReceiveInfo(info: UHFTAGInfo) {
        try {
            val tag = TagMetadata.of(info)
            Log.d(TAG, "Info: $info -> Tag: $tag")

            val emitted = tryEmit(DeviceEvent.TagEvent(tag = tag))
            if (!emitted) {
                Log.e(TAG, "Failed to emit tag: $tag")
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error processing received tag info", e)
            tryEmit(DeviceEvent.ErrorEvent(e))
        }
    }

    private fun onConnectionStatusHandler(status: ConnectionStatus, data: Any) {
        try {
            val device = data as? BluetoothDevice
            Log.d(TAG, "Connection status changed: $status, payload: $device")
            updateStatus(status = RfidDeviceStatus.of(status = status, device = device))
        } catch (e: Exception) {
            Log.e(TAG, "Error processing connection status change", e)
            tryEmit(DeviceEvent.ErrorEvent(e))
        }
    }

    override fun stop(): Boolean {
        val stopped = reader.stopInventory()
        if (stopped) {
            Log.d(TAG, "Inventory stopped successfully")
        } else {
            Log.e(TAG, "Failed to stop inventory")
        }
        return stopped
    }

    private fun startBatteryPolling(opts: Options) {
        if (opts.battery && opts.batteryPollingDelay > 0) {
            if (_batteryJob?.isActive == true) {
                _batteryJob?.cancel()
            }

            _batteryJob = scope.launch {
                while (isActive) {
                    try {
                        val level = reader.battery
                        Log.d(TAG, "Battery Level: $level")
                        tryEmit(DeviceEvent.BatteryEvent(level = level))
                    } catch (e: Exception) {
                        Log.e(TAG, "Error reading battery level", e)
                        tryEmit(DeviceEvent.ErrorEvent(e))
                    }
                    delay(maxOf(opts.batteryPollingDelay, 1_000L))
                }
            }
        }
    }

    // API
    fun getInfo(): Gen2Dto {
        val entity = reader.getGen2()
        val dto = Gen2Dto.toDto(entity)
        Log.d(TAG, "GetGen2: $entity -> $dto")
        return dto
    }

    fun setInfo(opts: Gen2Dto): Boolean {
        val entity = opts.toEntity()
        val updated = reader.setGen2(entity)
        Log.d(TAG, "Gen2 Updated $updated to $opts -> $entity")
        return updated
    }

    fun setPower(value: Int) {
        require(value >= 0) { "Power value must be non-negative" }
        val updated = reader.setPower(value)
        if (updated) {
            Log.d(TAG, "Power set to #$value")
        } else {
            Log.e(TAG, "Failed to set power to $value")
        }
    }

    fun setCW(value: Int) {
        val updated = reader.setCW(value)
        if (updated) {
            Log.d(TAG, "CW set to #$value")
        } else {
            Log.e(TAG, "Failed to set CW to $value")
        }
    }

    fun test() {
        reader.setCW(0)
        reader.setPower(0)
        reader.setGen2(null)
        reader.setTagFocus(true)
        reader.setRFLink(0)
        reader.setFastInventoryMode(true)
        reader.setFrequencyMode(0)
        reader.setProtocol(0)
        reader.setDynamicDistance(0)
        reader.setFreHop(0f)
        reader.setVolume(0)
        reader.setBeep(true)
        reader.setFilter(0, 0, 0, "x")
    }

}
