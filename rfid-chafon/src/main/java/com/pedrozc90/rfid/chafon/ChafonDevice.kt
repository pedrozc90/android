package com.pedrozc90.rfid.chafon

import android.util.Log
import com.cf.beans.AllParamBean
import com.cf.beans.DeviceInfoBean
import com.cf.zsdk.CfSdk
import com.cf.zsdk.cmd.CmdBuilder
import com.cf.zsdk.uitl.FormatUtil
import com.cf.zsdk.uitl.LogUtil
import com.pedrozc90.rfid.core.BaseRfidDevice
import com.pedrozc90.rfid.core.DeviceFrequency
import com.pedrozc90.rfid.core.Options
import com.pedrozc90.rfid.core.RfidDevice
import com.pedrozc90.rfid.exceptions.RfidDeviceException
import com.pedrozc90.rfid.objects.DeviceParams
import java.util.concurrent.Executors
import kotlin.math.floor
import kotlin.math.roundToInt

abstract class ChafonDevice : BaseRfidDevice(), RfidDevice {

    override var opts: Options? = null

    override val minPower: Int = 1
    override val maxPower: Int = 33

    protected var _params: AllParamBean? = null
    protected var _info: DeviceInfoBean? = null

    init {
        // CfSdk.load should be called once in Application.onCreate; don't reload here.
        val executor = Executors.newSingleThreadExecutor()
        CfSdk.load(executor)
    }

    override fun init(opts: Options) {
        // store options
        this.opts = opts

        LogUtil.setLogSwitch(opts.verbose)

        this.initReader(opts)
    }

    abstract fun initReader(opts: Options)

    /**
     * Start inventory with parameters
     * @param invType - inventory type: 0x00 = by time, 0x01 = by tag count
     * @param invParam - inventory parameter: if invType is 0x00, then invParam is time in seconds;
     *                   if invType is 0x01, then invParam is number of tags to read
     * @return true if the command was sent successfully, false otherwise
     */
    protected open fun startInventory(intType: Int = 0x00, invParam: Int = 0x00): Boolean {
        val bytes = CmdBuilder.buildInventoryISOContinueCmd(intType.toByte(), invParam)
        // TODO: Não sei pq, mas essa merda sem retorna FALSE
        //       Além disso, o gatilho do leitor le as tags mesmo com o command 'startInventory" retornando FALSE
        val result = this.writeData(bytes)
        Log.d(TAG, "Start inventory result = $result")
        return result
    }

    override fun start(): Boolean {
        getDeviceInfo()
        getInventoryParams()
        setBeep(true)
        setReadMode(mode = 0x00)
        setPower(15)
        setFrequency(DeviceFrequency.UNITED_STATES)
        return startInventory(intType = 0x00, invParam = 0x00)
    }

    protected fun stopInventory(): Boolean {
        val bytes = CmdBuilder.buildStopInventoryCmd()
        val result = this.writeData(bytes)
        Log.d(TAG, "Stop inventory result = $result")
        return result
    }

    override fun stop(): Boolean {
        return this.stopInventory()
    }

    override fun getInventoryParams(): DeviceParams? {
        val bytes = CmdBuilder.buildGetAllParamCmd()
        val result = this.writeData(bytes)
        Log.d(TAG, "Get AllParamCmd result: $result")
        return null
    }

    override fun setInventoryParams(value: DeviceParams): Boolean {
        val params = _params
        if (params == null) {
            Log.e(TAG, "Device parameters not loaded. Call getInventoryParams() first.")
            return false
        }
        val bytes = CmdBuilder.buildSetAllParamCmd(params)
        val result = this.writeData(bytes)
        Log.d(TAG, "Set inventory params result = $result")
        return result
    }

    override fun getFrequency(): DeviceFrequency? {
        val region = _params?.mRfidFreq?.mREGION?.toInt()
        if (region == null) return null
        val freq = ChafonFrequency.of(region)
        return freq?.toDeviceFrequency()
    }

    override fun setFrequency(value: DeviceFrequency): Boolean {
        val params = _params
        if (params == null) {
            Log.e(TAG, "Device parameters not loaded. Call getInventoryParams() first.")
            return false
        }

        val freq = value.toChafon()
        if (freq == null) {
            Log.e(TAG, "Frequency ${value.label} not supported.")
            return false
        }

        val band = freq.band            // band (Int)
        val fStart = freq.fStart        // in MHz (Double)
        val fStep = freq.fStep          // in MHz (Double)
        val minIndex = freq.minIndex    // Int
        val maxIndex = freq.maxIndex    // Int

        // Calculate device Fmin: the minimum frequency point (channel at minIndex)
        val fMinMHz = freq.minFrequency

        // integer MHz part
        var intPart = floor(fMinMHz).toInt()

        // fractional part in kHz (0..999). Round to nearest kHz.
        var fracKHz = ((fMinMHz - intPart) * 1000.0).roundToInt()

        // handle rounding that produces 1000 kHz (carry into integer MHz)
        if (fracKHz >= 1000) {
            intPart += 1
            fracKHz = 0
        }

        // step in kHz (rounded)
        val stepKHz = (freq.fStep * 1000.0).roundToInt()

        // CN = number of channels = maxIndex - minIndex + 1
        val cn = (maxIndex - minIndex + 1).coerceIn(1, 0xFF)

        // Vendor docs sometimes limit N to 0..63 (so CN <= 64). If you want enforce:
        if (cn > 64) {
            throw IllegalArgumentException("Computed CN ($cn) is greater than device-typical maximum of 64.")
        }

        // Build the command bytes while synchronized on params to avoid races
        val bytes = synchronized(params) {
            // Ensure we have an RfidFreq instance and keep a stable reference
            val freqStruct = params.mRfidFreq ?: AllParamBean.RfidFreq().also { params.mRfidFreq = it }

            // Ensure internal arrays are initialized (adjust to actual types if non-nullable)
            if (freqStruct.mSTRATFREI == null) freqStruct.mSTRATFREI = ByteArray(2)
            if (freqStruct.mSTRATFRED == null) freqStruct.mSTRATFRED = ByteArray(2)
            if (freqStruct.mSTEPFRE == null) freqStruct.mSTEPFRE = ByteArray(2)

            // Write all fields
            freqStruct.mREGION = band.toByte()

            freqStruct.mSTRATFREI[0] = highByteOf(intPart)
            freqStruct.mSTRATFREI[1] = lowByteOf(intPart)

            freqStruct.mSTRATFRED[0] = highByteOf(fracKHz)
            freqStruct.mSTRATFRED[1] = lowByteOf(fracKHz)

            freqStruct.mSTEPFRE[0] = highByteOf(stepKHz)
            freqStruct.mSTEPFRE[1] = lowByteOf(stepKHz)

            freqStruct.mCN = cn.toByte()

            // Build the command using the params (done while synchronized to keep consistency)
            CmdBuilder.buildSetAllParamCmd(params)
        }

        val result = this.writeData(bytes)
        Log.d(TAG, "Set frequency result = $result")
        return result
    }

    override fun checkFrequency(value: DeviceFrequency): Boolean {
        return value.toChafon() != null
    }

    override fun getPower(): Int {
        return _params?.mRfidPower?.toInt() ?: -1
    }

    /**
     * Set the read power of the RFID device
     *
     * H100 - the device power value range is [1, 20] dBm
     * H102 - the device power value range is [1, 26] dBm
     * H103 - the device power value range is [1, 33] dBm
     */
    override fun setPower(value: Int): Boolean {
        require(value >= minPower && value <= maxPower) { "Power value must be between $minPower and $maxPower." }

        if (false) {
            val params = _params
            if (params == null) {
                throw RfidDeviceException(message = "Device parameters not loaded. Call getInventoryParams() first.")
            }

            params.mRfidPower = value.toByte()

            val bytes = CmdBuilder.buildSetAllParamCmd(params)
            return this.writeData(bytes)
        }

        val reserved = 0x00.toByte()
        val bytes = CmdBuilder.buildSetPwrCmd(value.toByte(), reserved)
        val result = this.writeData(bytes)
        Log.d(TAG, "Set power result = $result")
        return result
    }

    override fun getBeep(): Boolean {
        val params = _params
        if (params == null) {
            Log.e(TAG, "Device parameters not loaded. Call getInventoryParams() first.")
            return false
        }

        val buzzer = params.mBuzzerTime
        return buzzer > 0x00
    }

    override fun setBeep(enabled: Boolean): Boolean {
        val params = _params
        if (params == null) {
            Log.e(TAG, "Device parameters not loaded. Call getInventoryParams() first.")
            return false
        }

        val buzzer = if (enabled) 1 else 0
        params.mBuzzerTime = buzzer.toByte()

        val bytes = CmdBuilder.buildSetAllParamCmd(params)
        val result = this.writeData(bytes)
        Log.d(TAG, "Change beep to '$enabled' ('$buzzer') returned '$result'")
        return result
    }

    override fun kill(rfid: String, password: String?): Boolean {
        // select tag first
        val mask: ByteArray = FormatUtil.hexStrToByteArray(rfid)
        val sBytes = CmdBuilder.buildSelectMaskCmd(mask)
        val sResult = this.writeData(sBytes)
        if (!sResult) return false

        // then we kill tag
        val pwd = password ?: "0000000"
        val pwdBytes = FormatUtil.hexStrToByteArray(pwd)
        val kBytes = CmdBuilder.buildKlenlISOTagCmd(pwdBytes)
        val kResult = this.writeData(kBytes)
        return kResult
    }

    fun getDeviceInfo(): Any? {
        if (_info == null) {
            val bytes = CmdBuilder.buildGetDeviceInfoCmd()
            val result = this.writeData(bytes)
            Log.d(TAG, "GetDeviceInfoCmd result: $result")
            return null
        }
        return _info
    }

    /**
     * Send command to get battery capacity, this trigger a notification.
     *
     * @return true if the command was sent successfully, false otherwise
     */
    protected fun getBatteryCapacity(): Boolean {
        val bytes = CmdBuilder.buildGetBatteryCapacityCmd()
        return this.writeData(bytes)
    }

    /**
     * Set read mode
     *
     * @param mode - scanning mode
     *               0x00 = turn-on RFID mode and turn-off QR code mode
     *               0x01 = turn-on QR code mode and turn-off RFID mode
     * @param recev - reserve byte array (7 bytes)
     * @return true if the command was sent successfully, false otherwise
     */
    protected fun setReadMode(mode: Int = 0x00, recev: ByteArray = ByteArray(7)): Boolean {
        val bytes = CmdBuilder.buildSetReadModeCmd(mode.toByte(), recev)
        return this.writeData(bytes)
    }

    protected abstract fun writeData(bytes: ByteArray): Boolean

    // HELPERS
    /**
     * Return the high-order (most significant) byte of a 16-bit value.
     *
     * The input `value` is treated as an unsigned 16-bit integer (only bits 0..15 are used).
     * This function extracts bits 8..15 and returns them as a Byte suitable for big-endian encoding.
     *
     * @param value Int containing the value to extract (only the lower 16 bits are considered)
     * @return Byte representing the high-order 8 bits (0x00..0xFF)
     */
    private fun highByteOf(value: Int): Byte {
        return ((value shr 8) and 0xFF).toByte()
    }

    /**
     * Return the low-order (least significant) byte of a 16-bit value.
     *
     * The input `value` is treated as an unsigned 16-bit integer (only bits 0..15 are used).
     * This function extracts bits 0..7 and returns them as a Byte suitable for big-endian encoding.
     *
     * @param value Int containing the value to extract (only the lower 16 bits are considered)
     * @return Byte representing the low-order 8 bits (0x00..0xFF)
     */
    private fun lowByteOf(value: Int): Byte {
        return (value and 0xFF).toByte()
    }

}
