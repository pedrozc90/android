package com.pedrozc90.rfid.chainway

import android.bluetooth.BluetoothDevice
import android.content.Context
import android.util.Log
import com.pedrozc90.rfid.chainway.core.of
import com.pedrozc90.rfid.chainway.core.toDeviceParams
import com.pedrozc90.rfid.chainway.core.toGen2Entity
import com.pedrozc90.rfid.chainway.helpers.FilterHelper
import com.pedrozc90.rfid.chainway.helpers.UHFConfig
import com.pedrozc90.rfid.core.BaseRfidDevice
import com.pedrozc90.rfid.core.DeviceFrequency
import com.pedrozc90.rfid.core.RfidDevice
import com.pedrozc90.rfid.exceptions.RfidDeviceException
import com.pedrozc90.rfid.objects.DeviceEvent
import com.pedrozc90.rfid.objects.DeviceParams
import com.pedrozc90.rfid.objects.RfidDeviceStatus
import com.pedrozc90.rfid.objects.TagMetadata
import com.pedrozc90.rfid.utils.clamp
import com.rscja.deviceapi.entity.UHFTAGInfo
import com.rscja.deviceapi.interfaces.ConnectionStatus
import com.rscja.deviceapi.interfaces.ISingleAntenna
import com.rscja.deviceapi.interfaces.IUHF
import com.rscja.deviceapi.interfaces.IUhfReader
import com.rscja.utility.StringUtility
import kotlinx.coroutines.Job


abstract class ChainwayBaseRfidDevice(protected val context: Context) : BaseRfidDevice(),
    RfidDevice {

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

    /**
     * Kill a tag using a password plus a filter to target a specific tag.
     *
     * @param p0 password string (8 hex characters), default "00000000".
     *           This is the kill/access password required by the tag. Expected format:
     *           - Exactly 8 hexadecimal characters (0-9, A-F/a-f).
     *           - Some UI code treats spaces/other formats differently; pass the raw hex string without "0x".
     *
     * @param p1 memory bank constant. One of:
     *           - IUHF.Bank_EPC  (EPC memory bank)
     *           - IUHF.Bank_TID  (TID memory bank)
     *           - IUHF.Bank_USER (USER memory bank)
     *           This selects which memory bank the filter will be applied to.
     *
     * @param p2 ptr (start offset). Non-negative integer indicating where the filter starts in the selected memory bank.
     *           The demo SDK treats the pointer as a bit offset into the bank. Confirm with your SDK docs if your
     *           environment expects a word/byte offset instead.
     *
     * @param p3 cnt (length in bits). Non-negative integer specifying how many bits to compare starting at `ptr`.
     *           If cnt > 0, the `data` parameter must contain at least ceil(cnt / 8) bytes of hex data.
     *
     * @param p4 data filter as a hex string. Spaces are allowed and ignored when measuring length.
     *           - Example valid formats: "3000A1" or "30 00 A1"
     *           - The number of bytes provided (hex digits / 2) must be >= ceil(cnt / 8) when cnt > 0.
     *
     * Returns Boolean indicating whether the kill operation succeeded (true) or failed (false).
     */
    private fun killTag(
        accessPwd: String,
        filterBank: Int = 0,
        filterPtr: Int = 0,
        filterLen: Int = 0,
        filterData: String
    ): Boolean {
        if (accessPwd.length != 8) {
            throw IllegalArgumentException("'pwd' must have 8 characters")
        } else if (!this.isHexadecimal(accessPwd)) {
            throw IllegalArgumentException("'pwd' must be a hexadecimal string")
        }

        // If no bank/filter selected, call simple killTag(pwd)
        if (filterLen == 0) {
            return reader.killTag(accessPwd)
        }
        // Validate ptr and cnt
        require(filterPtr >= 0) { "'ptr' must be greater or equal to 0" }
        require(filterLen > 0) { "'cnt' must be greater or equal to 0" }

        // Number of bytes required by cnt bits (round up)
        val flag = (filterLen + 7) / 8
        val dataLen = filterData.replace(" ", "").length / 2

        // dataLen must be at least flag
        require(dataLen >= flag) { "'data' length (in bytes) must be at least the number of bytes required by 'cnt' ($flag)" }

        return reader.killTag(accessPwd, filterBank, filterPtr, filterLen, filterData)
    }

    override fun kill(rfid: String, password: String?): Boolean {
        return killTag(
            accessPwd = password ?: "00000000",
            filterBank = IUHF.Bank_EPC,
            filterPtr = 16,
            filterLen = rfid.length * 4,
            filterData = rfid            // epc hexadecimal string)
        )
    }

    /**
     * Write data into tag.
     *
     * @param accessPwd  - access password (4 bytes)
     * @param filterBank - filtered banks (IUHF.Bank_EPC, IUHF.Bank_TID or IUHF.Bank_USER)
     * @param filterPtr  - filter starting address
     * @param filterCnt  - filter data length, when filtered data length is 0, it means not filtering.
     * @param filterData - filter data
     * @param bank       - write banks (IUHF.Bank_RESERVED, IUHF.Bank_EPC, IUHF.Bank_TID or IUHF.Bank_USER)
     * @param ptr        - write starting address
     * @param cnt        - write data length, can not be 0
     * @param writeData  - write data, must be hexadecimal value
     * @return true if operation succeeded, false otherwise.
     */
    fun writeTag(
        accessPwd: String? = "00000000", // 4 bytes = 8 characters
        filterBank: Int = -1,
        filterPtr: Int = 0,
        filterLen: Int = 0,
        filterData: String? = null,
        bank: Int = 0,
        ptr: Int = 0,
        len: Int = 0,
        writeData: String
    ): Boolean {
        require(!accessPwd.isNullOrEmpty()) { "Access password is required." }
        require(accessPwd.length != 8) { "Access password must have 4 bytes or 8 characters." }
        require(len <= 0) { "Length must be greater than zero." }
        require(!writeData.isBlank()) { "Write data is required." }

        if (filterLen > 0) {
            require(!filterData.isNullOrEmpty()) { "Filter data is required when filter length is greater than zero." }
            require(filterData.length * 4 >= filterLen) { "Filter data must be greater than or equal to filter bit count." }
            return reader.writeData(
                accessPwd,
                filterBank,
                filterPtr,
                filterLen,
                filterData,
                bank,
                ptr,
                len,
                writeData
            )
        }

        return reader.writeData(
            accessPwd,
            bank,
            ptr,
            len,
            writeData
        )
    }

    // HELPERS
    fun isHexadecimal(value: String?): Boolean {
        if (value == null || value.length == 0 || value.length % 2 != 0) return false
        return StringUtility.isHexNumberRex(value)
    }

    // OBJECTS
    sealed class Command {
        data class KillTag(
            val pwd: String = "00000000",
            val epc: Boolean = false,
            val tid: Boolean = false,
            val user: Boolean = false,
            val ptr: Int = 0,
            val cnt: Int = 0,
            val data: String = ""
        )
    }

}
