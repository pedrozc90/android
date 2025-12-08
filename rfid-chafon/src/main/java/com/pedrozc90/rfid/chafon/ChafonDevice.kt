package com.pedrozc90.rfid.chafon

import android.util.Log
import com.cf.beans.AllParamBean
import com.cf.beans.DeviceInfoBean
import com.cf.zsdk.CfSdk
import com.cf.zsdk.cmd.CmdBuilder
import com.cf.zsdk.cmd.CmdType
import com.cf.zsdk.uitl.FormatUtil
import com.cf.zsdk.uitl.LogUtil
import com.pedrozc90.rfid.core.BaseRfidDevice
import com.pedrozc90.rfid.core.DeviceFrequency
import com.pedrozc90.rfid.core.Options
import com.pedrozc90.rfid.core.RfidDevice
import com.pedrozc90.rfid.exceptions.RfidDeviceException
import com.pedrozc90.rfid.objects.DeviceParams
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.delay
import kotlinx.coroutines.withTimeoutOrNull
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.Executors
import kotlin.math.floor
import kotlin.math.roundToInt

abstract class ChafonDevice : BaseRfidDevice(), RfidDevice {

    override var opts: Options? = null

    override val minPower: Int = 0
    override val maxPower: Int = 30

    @Volatile
    protected var _params: AllParamBean? = null

    @Volatile
    protected var _info: DeviceInfoBean? = null

    protected var _running: Boolean = false

    protected val _listeners = ConcurrentHashMap<Int, CompletableDeferred<Any?>>()

    init {
        // CfSdk.load should be called once in Application.onCreate; don't reload here.
        val executor = Executors.newSingleThreadExecutor()
        CfSdk.load(executor)
    }

    override suspend fun init(opts: Options) {
        // store options
        this.opts = opts

        // enabled/disable sdk logging
        LogUtil.setLogSwitch(opts.verbose)

        this.initReader(opts)
    }

    abstract suspend fun initReader(opts: Options)

    /**
     * NOTE: start() used to perform a sequence of commands synchronously and often assumed
     * that _params was already populated by a prior GET_ALL_PARAM response. When start() gets
     * spammed it can hit NPEs because _params is still null. To avoid blocking callers (start()
     * is not suspend) we now launch a coroutine that executes the sequence, waits briefly for
     * _params to arrive (with timeout) and adds small delays between device commands.
     *
     * The method returns 'true' to indicate the start-sequence has been scheduled. If callers
     * need to know the final success/failure of the startInventory command we should change the
     * API to a suspend function or provide a callback / flow event.
     */
    override suspend fun start(): Boolean {
        // ignore if already running
        if (_running) return false

        val power = opts?.power ?: 30
        val frequency = opts?.frequency ?: DeviceFrequency.BRAZIL

        // request inventory parameters, required to change frequency
        val params = GetAllParamsAsync()
        if (params == null) {
            Log.e(TAG, "Failed to get device parameters; cannot start inventory.")
            return false
        }

        Log.d(TAG, "Params = $params")

        SetWorkMode(params = params, mask = 0x02)
        SetBeep(params = params, enabled = true)
        SetFrequency(params = params, frequency = frequency)
        SetPower(params, power)

        Log.d(TAG, "Params = $params")

        val updated = SetAllParams(params)
        if (updated) {
            delay(100)
        }

        val updatedReadMode = SetReadMode()
        if (updatedReadMode) {
            Log.d(TAG, "Read mode successfully changed to RFID mode.")
            delay(100)
        } else {
            Log.e(TAG, "Read mode failed to change to RFID mode.")
        }

        // finally, start inventory
        val started = StartInventory(intType = 0x00, invParam = 0x00)

        // mark as running
        _running = started

        return started
    }

    override suspend fun stop(): Boolean {
        // ignore if not running
        if (!_running) return false

        // send command to stop device
        val stopped = StopInventory()

        // mask as not running
        _running = false

        return stopped
    }

    override suspend fun getInventoryParams(): DeviceParams? {
        return null
    }

    override suspend fun setInventoryParams(value: DeviceParams): Boolean {
        val params = _params
        if (params != null) {
            return SetAllParams(params)
        }
        Log.e(TAG, "Device parameters not loaded. Call getInventoryParams() first.")
        return false
    }

    override suspend fun getFrequency(): DeviceFrequency? {
        val params = GetAllParamsAsync()
        val region = params?.mRfidFreq?.mREGION?.toInt()
        if (region != null) {
            val freq = ChafonFrequency.of(region)
            if (freq != null) {
                return freq.toDeviceFrequency()
            }
        }
        return null
    }

    override suspend fun setFrequency(value: DeviceFrequency): Boolean {
        val params = GetAllParamsAsync()
        if (params == null) return false

        SetFrequency(params = params, frequency = value)

        return SetAllParams(params)
    }

    override fun checkFrequency(value: DeviceFrequency): Boolean {
        return value.toChafon() != null
    }

    override suspend fun getPower(): Int {
        val params = GetAllParamsAsync()
        return params?.mRfidPower?.toInt() ?: -1
    }

    /**
     * Set the read power of the RFID device
     *
     * H100 - the device power value range is [1, 20] dBm
     * H102 - the device power value range is [1, 26] dBm
     * H103 - the device power value range is [1, 33] dBm
     */
    override suspend fun setPower(value: Int): Boolean {
        val params = GetAllParamsAsync()
        if (params == null) return false

        SetPower(params = params, value = value)

        return SetAllParams(params)
    }

    override suspend fun getBeep(): Boolean {
        val params = GetAllParamsAsync()
        val buzzer = params?.mBuzzerTime
        if (buzzer != null) {
            return buzzer > 0x00
        }
        return false
    }

    override suspend fun setBeep(enabled: Boolean): Boolean {
        val params = _params
        if (params == null) {
            Log.e(TAG, "Device parameters not loaded. Call getInventoryParams() first.")
            return false
        }

        SetBeep(params = params, enabled = enabled)

        val bytes = CmdBuilder.buildSetAllParamCmd(params)
        val result = this.writeData(bytes)
        if (result) {
            Log.d(TAG, "Beep set to '$enabled' successfully.")
        } else {
            Log.e(TAG, "Failed to set beep to '$enabled'.")
        }
        return result
    }

    override suspend fun kill(rfid: String, password: String?): Boolean {
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

    suspend fun getDeviceInfo(): Any? {
        if (_info == null) {
            val bytes = CmdBuilder.buildGetDeviceInfoCmd()
            val result = this.writeData(bytes)
            if (result) {
                Log.d(TAG, "Get device information command successfully send.")
            } else {
                Log.e(TAG, "Get device information command failed to send.")
            }
            return null
        }
        return _info
    }

    // COMMANDS
    /**
     * Write data to the device
     * @param bytes - byte array to write, build by 'CmdBuilder'
     * @return true if the command was sent successfully, false otherwise
     */
    protected abstract suspend fun writeData(bytes: ByteArray): Boolean

    /**
     * Start inventory with parameters
     * @param invType - inventory type: 0x00 = by time, 0x01 = by tag count
     * @param invParam - inventory parameter: if invType is 0x00, then invParam is time in seconds;
     *                   if invType is 0x01, then invParam is number of tags to read
     * @return true if the command was sent successfully, false otherwise
     */
    protected suspend fun StartInventory(intType: Int = 0x00, invParam: Int = 0x00): Boolean {
        val bytes = CmdBuilder.buildInventoryISOContinueCmd(intType.toByte(), invParam)
        // TODO: Não sei pq, mas essa merda sem retorna FALSE
        //       Além disso, o gatilho do leitor le as tags mesmo com o command 'startInventory" retornando FALSE
        val result = this.writeData(bytes)
        if (result) {
            Log.d(TAG, "Start inventory command successfully send.")
        } else {
            Log.e(TAG, "Start inventory command failed to send.")
        }
        return result
    }

    protected suspend fun StopInventory(): Boolean {
        val bytes = CmdBuilder.buildStopInventoryCmd()
        val result = this.writeData(bytes)
        if (result) {
            Log.d(TAG, "Stop inventory command successfully send.")
        } else {
            Log.e(TAG, "Stop inventory command failed to send.")
        }
        return result
    }

    protected suspend fun GetDeviceInfo(): Boolean {
        val bytes = CmdBuilder.buildGetDeviceInfoCmd()
        val result = this.writeData(bytes)
        if (result) {
            delay(100)
            Log.d(TAG, "Get device information command successfully send.")
        } else {
            Log.e(TAG, "Get device information command failed to send.")
        }
        return result
    }

    /**
     * Similar to GetAllParamsAsync but for device info (TYPE_GET_DEVICE_INFO).
     */
    protected suspend fun GetDeviceInfoAsync(timeout: Long = 300): DeviceInfoBean? {
        _info?.let { return it }

        val key = CmdType.TYPE_GET_DEVICE_INFO
        val newDeferred = CompletableDeferred<Any?>()
        val deferred = _listeners.putIfAbsent(key, newDeferred) ?: newDeferred
        val sentByUs = deferred === newDeferred

        if (sentByUs) {
            val ok = try {
                GetDeviceInfo()
            } catch (e: Exception) {
                _listeners.remove(key, deferred)
                throw e
            }

            if (!ok) {
                _listeners.remove(key, deferred)
                return null
            }
        }

        val result = withTimeoutOrNull(timeout) { deferred.await() } as? DeviceInfoBean

        if (sentByUs) {
            _listeners.remove(key, deferred)
        }

        return result ?: _info
    }

    protected suspend fun GetAllParams(): Boolean {
        val bytes = CmdBuilder.buildGetAllParamCmd()
        val result = this.writeData(bytes)
        if (result) {
            Log.d(TAG, "Get inventory parameters command successfully send.")
        } else {
            Log.e(TAG, "Get inventory parameters command failed to send.")
        }
        return result
    }

    protected suspend fun GetAllParamsAsync(timeout: Long = 300): AllParamBean? {
        // If we already have it, return immediately
        _params?.let { return it }

        val key = CmdType.TYPE_GET_ALL_PARAM

        // Try to reuse an existing deferred if someone else already asked
        val newDeferred = CompletableDeferred<Any?>()
        val deferred = _listeners.putIfAbsent(key, newDeferred) ?: newDeferred

        // If we were not the one who inserted the deferred, another waiter will complete it.
        val sentByUs = deferred === newDeferred

        if (sentByUs) {
            // send command to device
            val ok = try {
                GetAllParams()
            } catch (e: Exception) {
                // cleanup and rethrow
                _listeners.remove(key, deferred)
                throw e
            }

            if (!ok) {
                // remove our pending deferred if command didn't send
                _listeners.remove(key, deferred)
                return null
            }
        }

        delay(100)

        // wait for the deferred to complete, with timeout
        val result = withTimeoutOrNull(timeout) { deferred.await() } as? AllParamBean

        // If we were the sender, remove mapping (it should already be removed by the notifier,
        // but guard in case of timeouts/other paths)
        if (sentByUs) {
            _listeners.remove(key, deferred)
        }

        // If notifier already set _params, return it; otherwise return what we got
        return result ?: _params
    }

    protected suspend fun SetAllParams(params: AllParamBean): Boolean {
        val bytes = CmdBuilder.buildSetAllParamCmd(params)
        val result = this.writeData(bytes)
        if (result) {
            Log.d(TAG, "Set inventory params command successfully send.")
            _params = params
        } else {
            Log.e(TAG, "Set inventory params command failed to send.")
        }
        return result
    }

    /**
     * Send command to get battery capacity, this trigger a notification.
     *
     * @return true if the command was sent successfully, false otherwise
     */
    protected suspend fun GetBatteryCapacity(): Boolean {
        val bytes = CmdBuilder.buildGetBatteryCapacityCmd()
        return this.writeData(bytes)
    }

    /**
     * Get read mode
     *
     * @return true if the command was sent successfully, false otherwise
     */
    protected suspend fun GetReadMode(): Boolean {
        val bytes = CmdBuilder.buildGetReadModeCmd()
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
    protected suspend fun SetReadMode(mode: Int = 0x00, recev: ByteArray = ByteArray(7)): Boolean {
        val bytes = CmdBuilder.buildSetReadModeCmd(mode.toByte(), recev)
        return this.writeData(bytes)
    }

    /**
     * Set beep enabled/disable to the params object
     *
     * @param enabled - true to enable beep, false to disable
     */
    private fun SetBeep(params: AllParamBean, enabled: Boolean = false) {
        val buzzer = if (enabled) 1 else 0
        params.mBuzzerTime = buzzer.toByte()
    }

    /**
     * Set protocol
     *
     * @param mask - protocol value
     *               0x00 = ISO 18000-6C
     *               0x01 = GB/T 2976
     *               0x02 = GJB 7377.1
     */
    protected fun SetProtocol(params: AllParamBean, mask: Int = 0x00) {
        params.mRFIDPRO = mask.toByte()
    }

    /**
     * Set work mode
     *
     * @param mask - work mode value
     *               0 = Answer mode
     *               1 = Active mode
     *               2 = Trigger mode
     * @return true if the command was sent successfully, false otherwise
     */
    protected fun SetWorkMode(params: AllParamBean, mask: Int = 0x00) {
        params.mWorkMode = mask.toByte()
    }

    /**
     * Set baud rate
     *
     * @param mask - baud rate value (default: 115200 bps)
     *               0 = 9600 bps
     *               1 = 19200 bps
     *               2 = 38400 bps
     *               3 = 57600 bps
     *               4 = 115200 bps
     * @return true if the command was sent successfully, false otherwise
     */
    protected fun SetBaudRate(params: AllParamBean, mask: Int = 4) {
        params.mBaudrate = mask.toByte()
    }

    /**
     * Set the frequency mode of the RFID device
     *
     * @param band - band (Int)
     * @param fStart - start frequency in MHz (Float)
     * @param fStep - step frequency in MHz (Float)
     * @param minIndex - minimum channel index (Int)
     * @param maxIndex - maximum channel index (Int)
     * @return true if the operation was successful, false otherwise
     */
    protected fun SetFrequency(
        params: AllParamBean,
        band: Int,
        fStart: Float,
        fStep: Float,
        minIndex: Int = 0,
        maxIndex: Int = 63
    ) {
        // Calculate device Fmin: the minimum frequency point (channel at minIndex)
        val fMinMHz = fStart + (fStep * minIndex)

        // integer MHz part
        var intPart = floor(fMinMHz).toInt()

        // fractional part in kHz (0..999). Round to nearest kHz.
        var fracKHz = ((fMinMHz - intPart) * 1_000.0).roundToInt()

        // handle rounding that produces 1000 kHz (carry into integer MHz)
        if (fracKHz >= 1_000) {
            intPart += 1
            fracKHz = 0
        }

        // step in kHz (rounded)
        val stepKHz = (fStep * 1_000.0).roundToInt()

        // CN = number of channels = maxIndex - minIndex + 1
        val cn = (maxIndex - minIndex + 1).coerceIn(1, 0xFF)

        // Vendor docs sometimes limit N to 0..63 (so CN <= 64). If you want enforce:
        if (cn > 64) {
            throw IllegalArgumentException("Computed CN ($cn) is greater than device-typical maximum of 64.")
        }

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
    }

    protected fun SetFrequency(params: AllParamBean, frequency: DeviceFrequency) {
        val freq = frequency.toChafon()
        if (freq != null) {
            SetFrequency(
                params,
                freq.band,
                freq.fStart,
                freq.fStep,
                freq.minIndex,
                freq.maxIndex
            )
        } else {
            throw IllegalArgumentException("Frequency ${frequency.label} not supported.")
        }
    }

    protected fun SetPower(params: AllParamBean, value: Int) {
        require(value >= minPower && value <= maxPower) { "Power value must be between $minPower and $maxPower." }
        params.mRfidPower = value.toByte()
    }

    protected suspend fun SetPwr(value: Int, reserved: Int = 0x00): Boolean {
        require(value >= minPower && value <= maxPower) { "Power value must be between $minPower and $maxPower." }
        val bytes = CmdBuilder.buildSetPwrCmd(value.toByte(), reserved.toByte())
        val result = this.writeData(bytes)
        if (result) {
            Log.d(TAG, "Set power command successfully send.")
        } else {
            Log.e(TAG, "Set power command failed to send.")
        }
        return result
    }

    protected suspend fun Reboot(): Boolean {
        val bytes = CmdBuilder.buildRebootCmd()
        return this.writeData(bytes)
    }

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

    /**
     * Complete any waiter for the given cmdType with payload.
     *
     * This is called by subclasses when they have processed the notification and updated internal objects
     * such as _params or _info. The deferred is removed and completed with the payload.
     */
    protected fun completeListener(cmdType: Int, payload: Any?) {
        val deferred = _listeners.remove(cmdType) ?: return
        val completed = deferred.complete(payload)
        if (!completed) {
            Log.w(TAG, "Listener $cmdType already completed or cancelled.")
        }
    }

    override fun close() {
        try {
            for ((_, deferred) in _listeners) {
                deferred.completeExceptionally(RfidDeviceException(message = "Device closed"))
            }
            _listeners.clear()
        } finally {
            super.close()
        }
    }

}
