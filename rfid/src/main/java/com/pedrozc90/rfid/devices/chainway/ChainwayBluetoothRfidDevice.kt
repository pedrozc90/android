package com.pedrozc90.rfid.devices.chainway

import android.bluetooth.BluetoothDevice
import android.content.Context
import android.util.Log
import com.pedrozc90.rfid.core.BaseRfidDevice
import com.pedrozc90.rfid.core.Options
import com.pedrozc90.rfid.core.RfidDevice
import com.pedrozc90.rfid.core.clamp
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

    private fun configUHFInfo(
        epc: Boolean = true,
        tid: Boolean = true,
        rssi: Boolean = true,
        user: Boolean = false
    ): Boolean {
        return UHFConfig.configureInventory(
            reader = reader,
            epc = epc,
            tid = tid,
            rssi = rssi,
            user = user
        )
    }

    override fun start(): Boolean {
        try {
            setFrequencyMode("brazil")
            setPower(30)
            setBeep(true)
            setTagFocus(false)
            configUHFInfo()
            disableFilters()

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


    // POWER
    fun getPower(): Int {
        return reader.power
    }

    /**
     * Sets the RF output power of the UHF radio
     * value: Power level (0-30 dBm)
     */
    fun setPower(value: Int): Boolean {
        val clamped = value.clamp(0, 30)
        val updated = reader.setPower(clamped)
        if (updated) {
            Log.d(TAG, "Power set to #$clamped")
        } else {
            Log.e(TAG, "Failed to set power to $clamped")
        }
        return updated
    }

    // CONTINUOUS WAVE
    fun getCW(): Int {
        return reader.getCW()
    }

    fun setCW(enabled: Boolean): Boolean {
        val value = when (enabled) {
            true -> 1
            false -> 0
        }
        val updated = reader.setCW(value)
        if (updated) {
            Log.d(TAG, "CW set to #$value")
        } else {
            Log.e(TAG, "Failed to set CW to $value")
        }
        return updated
    }

    // FREQUENCY MODE
    fun getFrequencyMode(): Int {
        return reader.getFrequencyMode()
    }

    /**
     * Selects a pre-configured region frequency plan
     */
    fun setFrequencyMode(value: Int): Boolean {
        return reader.setFrequencyMode(value)
    }

    fun setFrequencyMode(mode: String) {
        val value = when (mode) {
            "china-standard-1" -> 0x01       // China Standard 1 (840~845 MHz)
            "china-standard-2" -> 0x02       // China Standard 2 (920~925 MHz)
            "europe-standard" -> 0x04        // Europe Standard (865~868 MHz)
            "united-states-standard" -> 0x08 // United States Standard (902~928 MHz)
            "korea" -> 0x16                  // Korea (917~923 MHz)
            "japan" -> 0x32                  // Japan (916.8~920.8 MHz)
            "south_africa" -> 0x33           // South Africa (915~919 MHz)
            "taiwan" -> 0x34                 // Taiwan (920~928 MHz)
            "vietnam" -> 0x35                // Vietnam (918~923 MHz)
            "peru" -> 0x36                   // Peru (915~928 MHz)
            "russia" -> 0x37                 // Russia (860~867.6 MHz)
            "morocco" -> 0x80                // Morocco (914~921 MHz)
            "malaysia" -> 0x3B               // Malaysia (919~923 MHz)
            "brazil" -> 0x3C                 // Brazil (902~907.5 MHz)
            "brazil_2" -> 0x36               // Brazil (915~928 MHz)
            else -> 0x00
        }
        setFrequencyMode(value)
    }

    // FREQUENCY HOPPING
    /**
     * Configure frequency hopping behavior (channel list or hopping enable/disable).
     */
    fun setFreHop(value: Float): Boolean {
        return reader.setFreHop(value)
    }

    // RFLINK
    fun getRFLink(): Int {
        return reader.getRFLink()
    }

    /**
     * Set RF link parameters/profile (modulation, link optimization).
     */
    fun setRFLink(value: Int): Boolean {
        return reader.setRFLink(value)
    }

    // PROTOCOL
    fun getProtocol(): Int {
        return reader.getProtocol()
    }

    /**
     * Set/read supported tag protocol (usually Gen2 / EPCglobal UHF Class 1 Gen2).
     *
     * 0 = ISO 18000-6C
     * 1 = GB/T 29768
     * 2 = GJB 7377.1
     */
    fun setProtocol(value: Int): Boolean {
        return reader.setProtocol(value)
    }

    // GEN 2 SETTINGS
    fun getGen2(): Gen2Dto {
        val entity = reader.getGen2()
        val dto = Gen2Dto.toDto(entity)
        Log.d(TAG, "GetGen2: $entity -> $dto")
        return dto
    }

    /**
     * Configure Gen2-specific behavior (Q, inventory flags)
     */
    fun setGen2(opts: Gen2Dto): Boolean {
        val entity = opts.toEntity()
        val updated = reader.setGen2(entity)
        Log.d(TAG, "Gen2 Updated $updated to $opts -> $entity")
        return updated
    }

    // FOCUS
    fun getTagFocus(): Int {
        return reader.getTagfocus()
    }

    fun setTagFocus(enabled: Boolean): Boolean {
        return reader.setTagFocus(enabled)
    }

    // FAST INVENTORY MODE
    fun getFastInventoryMode(): Int {
        return reader.getFastInventoryMode()
    }

    /**
     * Toggle a faster inventory algorithm/mode (may sacrifice some accuracy for speed)
     */
    fun setFastInventoryMode(enabled: Boolean): Boolean {
        return reader.setFastInventoryMode(enabled)
    }

    // BEEP
    fun getBeep(): Int {
        return reader.getBeep()
    }

    /**
     * Enable or disable audible beep on tag read or events.
     */
    fun setBeep(enabled: Boolean): Boolean {
        return reader.setBeep(enabled)
    }

    // FILTER
    fun setFilter(bank: Int, startAddr: Int, length: Int, data: String): Boolean {
        return FilterHelper.setFilter(reader, bank, startAddr, length, data)
    }

    fun setFilterByEpc(epcHex: String): Boolean {
        return FilterHelper.setFilterByEpc(reader, epcHex, startBit = 32)
    }

    fun disableFilters(): Boolean {
        return FilterHelper.disableAllFilters(reader)
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
