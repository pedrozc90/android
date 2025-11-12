package com.pedrozc90.rfid.devices.chainway

import android.bluetooth.BluetoothDevice
import android.content.Context
import android.util.Log
import com.pedrozc90.rfid.core.BaseRfidDevice
import com.pedrozc90.rfid.core.clamp
import com.pedrozc90.rfid.devices.chainway.objects.Gen2Dto
import com.pedrozc90.rfid.objects.DeviceEvent
import com.pedrozc90.rfid.objects.RfidDeviceStatus
import com.pedrozc90.rfid.objects.TagMetadata
import com.rscja.deviceapi.entity.UHFTAGInfo
import com.rscja.deviceapi.interfaces.ConnectionStatus
import com.rscja.deviceapi.interfaces.ISingleAntenna
import com.rscja.deviceapi.interfaces.IUHF
import com.rscja.deviceapi.interfaces.IUhfReader
import kotlinx.coroutines.Job

abstract class ChainwayBaseRfidDevice(protected val context: Context) : BaseRfidDevice() {

    protected abstract val TAG: String

    protected abstract val reader: IUHF

    protected var _job: Job? = null

    protected fun handleInventory(info: UHFTAGInfo) {
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

    protected fun handleConnectionStatus(status: ConnectionStatus, data: Any) {
        try {
            val device = data as? BluetoothDevice
            Log.d(TAG, "Connection status changed: $status, payload: $device")
            updateStatus(status = RfidDeviceStatus.of(status = status, device = device))
        } catch (e: Exception) {
            Log.e(TAG, "Error processing connection status change", e)
            tryEmit(DeviceEvent.ErrorEvent(e))
        }
    }

    // API
    fun configUHFInfo(
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

    // POWER
    fun getPower(reader: ISingleAntenna): Int {
        return reader.getPower()
    }

    abstract fun getPower(): Int

    fun setPower(reader: ISingleAntenna, value: Int): Boolean {
        val clamped = value.clamp(0, 30)
        val updated = reader.setPower(clamped)
        if (updated) {
            Log.d(TAG, "Power set to #$clamped")
        } else {
            Log.e(TAG, "Failed to set power to $clamped")
        }
        return updated
    }

    /**
     * Sets the RF output power of the UHF radio
     * value: Power level (0-30 dBm)
     */
    abstract fun setPower(value: Int): Boolean

    // CONTINUOUS WAVE
    fun getContinuousWave(): Int {
        return reader.getCW()
    }

    fun setContinuousWave(enabled: Boolean): Boolean {
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
            "china_1" -> 0x01           // China Standard 1 (840~845 MHz)
            "china_2" -> 0x02           // China Standard 2 (920~925 MHz)
            "europe" -> 0x04            // Europe Standard (865~868 MHz)
            "united_states" -> 0x08     // United States Standard (902~928 MHz)
            "korea" -> 0x16             // Korea (917~923 MHz)
            "japan" -> 0x32             // Japan (916.8~920.8 MHz)
            "south_africa" -> 0x33      // South Africa (915~919 MHz)
            "taiwan" -> 0x34            // Taiwan (920~928 MHz)
            "vietnam" -> 0x35           // Vietnam (918~923 MHz)
            "peru" -> 0x36              // Peru (915~928 MHz)
            "russia" -> 0x37            // Russia (860~867.6 MHz)
            "morocco" -> 0x80           // Morocco (914~921 MHz)
            "malaysia" -> 0x3B          // Malaysia (919~923 MHz)
            "brazil" -> 0x3C            // Brazil (902~907.5 MHz)
            "brazil_2" -> 0x36          // Brazil (915~928 MHz)
            else -> 0x00
        }
        val updated = setFrequencyMode(value)
        if (updated) {
            Log.d(TAG, "Frequency changed to $mode = $value")
        }
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
    fun getTagFocus(reader: IUhfReader): Int {
        return reader.getTagfocus()
    }

    abstract fun getTagFocus(): Int

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

    // FILTER
    fun setFilter(bank: Int, start: Int, length: Int, data: String): Boolean {
        return FilterHelper.setFilter(reader, bank, start, length, data)
    }

    fun setFilterByEpc(epcHex: String): Boolean {
        return FilterHelper.setFilterByEpc(reader, epcHex, startBit = 32)
    }

    fun disableFilters(): Boolean {
        return FilterHelper.disableAllFilters(reader)
    }

}
