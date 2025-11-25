package com.pedrozc90.rfid.chainway

import android.bluetooth.BluetoothDevice
import android.content.Context
import android.util.Log
import com.pedrozc90.rfid.chainway.core.ChainwayFrequency
import com.pedrozc90.rfid.chainway.core.of
import com.pedrozc90.rfid.chainway.core.toChainway
import com.pedrozc90.rfid.chainway.core.toDeviceParams
import com.pedrozc90.rfid.chainway.core.toGen2Entity
import com.pedrozc90.rfid.chainway.helpers.FilterHelper
import com.pedrozc90.rfid.chainway.helpers.UHFConfig
import com.pedrozc90.rfid.core.BaseRfidDevice
import com.pedrozc90.rfid.core.DeviceFrequency
import com.pedrozc90.rfid.core.RfidDevice
import com.pedrozc90.rfid.core.clamp
import com.pedrozc90.rfid.exceptions.RfidDeviceException
import com.pedrozc90.rfid.objects.DeviceEvent
import com.pedrozc90.rfid.objects.DeviceParams
import com.pedrozc90.rfid.objects.RfidDeviceStatus
import com.pedrozc90.rfid.objects.TagMetadata
import com.rscja.deviceapi.entity.UHFTAGInfo
import com.rscja.deviceapi.interfaces.ConnectionStatus
import com.rscja.deviceapi.interfaces.ISingleAntenna
import com.rscja.deviceapi.interfaces.IUHF
import com.rscja.deviceapi.interfaces.IUhfReader
import kotlinx.coroutines.Job

abstract class ChainwayBaseRfidDevice(protected val context: Context) : BaseRfidDevice(), RfidDevice {

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

    /**
     * Sets the RF output power of the UHF radio
     * value: Power level (0-30 dBm)
     */
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
    override fun getFrequency(): DeviceFrequency? {
        val mask = reader.getFrequencyMode() // 0x3C = Brazil
        val freq = ChainwayFrequency.of(mask)
        return freq?.toDeviceFrequency()
    }

    /**
     * Selects a pre-configured region frequency plan
     */
    override fun setFrequency(value: DeviceFrequency): Boolean {
        val freq = value.toChainway()
        if (freq == null) throw RfidDeviceException(message = "Frequency not supported.")

        val updated = reader.setFrequencyMode(freq.mask)
        if (updated) {
            Log.d(TAG, "Frequency changed to ${value.label} = $freq")
        }
        return updated
    }

    override fun checkFrequency(value: DeviceFrequency): Boolean {
        return value.toChainway() != null
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
    override fun getInventoryParams(): DeviceParams? {
        val entity = reader.gen2
        val params = entity.toDeviceParams()
        Log.d(TAG, "Gen2Entity: $entity -> $params")
        return params
    }

    override fun setInventoryParams(value: DeviceParams): Boolean {
        val entity = value.toGen2Entity()
        val updated = reader.setGen2(entity)
        Log.d(TAG, "Gen2 Updated $updated to $value -> $entity")
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
