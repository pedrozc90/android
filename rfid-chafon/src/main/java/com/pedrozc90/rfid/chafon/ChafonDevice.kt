package com.pedrozc90.rfid.chafon

import android.annotation.SuppressLint
import android.bluetooth.BluetoothGatt
import android.content.Context
import android.util.Log
import com.cf.beans.AllParamBean
import com.cf.beans.BatteryCapacityBean
import com.cf.beans.CmdData
import com.cf.beans.DeviceInfoBean
import com.cf.beans.DeviceNameBean
import com.cf.beans.GeneralBean
import com.cf.beans.OutputModeBean
import com.cf.beans.TagInfoBean
import com.cf.ble.BleUtil
import com.cf.ble.interfaces.IOnNotifyCallback
import com.cf.zsdk.BleCore
import com.cf.zsdk.CfSdk
import com.cf.zsdk.SdkC
import com.cf.zsdk.cmd.CmdBuilder
import com.cf.zsdk.cmd.CmdType
import com.cf.zsdk.uitl.FormatUtil
import com.cf.zsdk.uitl.LogUtil
import com.pedrozc90.rfid.core.BaseRfidDevice
import com.pedrozc90.rfid.core.DeviceFrequency
import com.pedrozc90.rfid.core.Options
import com.pedrozc90.rfid.core.RfidDevice
import com.pedrozc90.rfid.exceptions.RfidDeviceException
import com.pedrozc90.rfid.objects.DeviceEvent
import com.pedrozc90.rfid.objects.DeviceParams
import com.pedrozc90.rfid.objects.RfidDeviceStatus
import com.pedrozc90.rfid.objects.TagMetadata
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import java.util.UUID
import java.util.concurrent.Executors
import kotlin.math.floor
import kotlin.math.max
import kotlin.math.roundToInt

class ChafonDevice(private val context: Context) : BaseRfidDevice(), RfidDevice, IOnNotifyCallback {

    private val COMPANY_ID = "2795"
    private val SERVICE_UUID: UUID = UUID.fromString("0000ffe0-0000-1000-8000-00805f9b34fb")
    private val NOTIFY_UUID: UUID = UUID.fromString("0000ffe4-0000-1000-8000-00805f9b34fb")
    private val WRITE_UUID: UUID = UUID.fromString("0000ffe3-0000-1000-8000-00805f9b34fb")

    override var opts: Options? = null
    private var _batteryJob: Job? = null

    override val name: String = "ChafonDevice"
    override val minPower: Int = 1
    override val maxPower: Int = 33

    private val mBleCore: BleCore = CfSdk.get(SdkC.BLE)
    private var mGatt: BluetoothGatt? = null
    private var _params: AllParamBean? = null
    private var _info: DeviceInfoBean? = null

    init {
        // CfSdk.load should be called once in Application.onCreate; don't reload here.
        val executor = Executors.newSingleThreadExecutor()
        CfSdk.load(executor)
        LogUtil.setLogSwitch(true)

        mBleCore.init(context)
    }

    @SuppressLint("MissingPermission")
    override fun init(opts: Options) {
        require(opts.bDevice != null) { "BluetoothDevice must be provided." }

        // store options
        this.opts = opts

        val self = this

        scope.launch {
            if (!mBleCore.isSupportBt) {
                throw RfidDeviceException(message = "Bluetooth not supported on this device.")
            }

            if (!mBleCore.isEnabled) {
                throw RfidDeviceException(message = "Bluetooth is not enabled on this device.")
            }

            mBleCore.setOnNotifyCallback(self)

            mBleCore.setIConnectDoneCallback { b ->
                if (b) {
                    Log.d(name, "Bluetooth device connected successfully.")

                    updateStatus(RfidDeviceStatus(status = "CONNECTED", device = opts.bDevice))

                    val updated = mBleCore.setNotifyState(SERVICE_UUID, NOTIFY_UUID, true)
                    Log.d(name, "Set notify state updated = $updated")

                    self.getInventoryParams()
                    self.getBatteryCapacity()
                } else {
                    Log.e(name, "Bluetooth device connection failed.")
                    updateStatus(RfidDeviceStatus(status = "DISCONNECTED", device = opts.bDevice))
                }
            }

            mBleCore.setIBleDisConnectCallback { ->
                Log.d(name, "Bluetooth device disconnected.")
                updateStatus(RfidDeviceStatus(status = "DISCONNECTED", device = opts.bDevice))
            }

            mGatt = mBleCore.connectDevice(opts.bDevice, context, true)
            Log.d(name, "BluetoothGatt connected: $mGatt")

            startBatteryPolling(opts)
        }
    }

    private fun startBatteryPolling(opts: Options) {
        if (opts.battery) {
            if (_batteryJob?.isActive == true) {
                _batteryJob?.cancel()
            }

            val delayMs = max(opts.batteryPollingDelay, 1_000L)

            _batteryJob = scope.launch {
                while (isActive) {
                    val updated = getBatteryCapacity()
                    val value = if (updated) delayMs else (2 * delayMs)
                    delay(value)
                }
            }
        }
    }

    override fun onNotify(cmdType: Int, cmdData: CmdData?) {
        when (cmdType) {
            CmdType.TYPE_GET_DEVICE_INFO -> handleGetDeviceInfo(cmdData)
            CmdType.TYPE_REBOOT -> handleReboot(cmdData)
            CmdType.TYPE_SET_ALL_PARAM -> handleSetParam(cmdData)
            CmdType.TYPE_GET_ALL_PARAM -> handleGetAllParam(cmdData)
            CmdType.TYPE_GET_BATTERY_CAPACITY -> handleGetBatteryCapacity(cmdData)
            CmdType.TYPE_SET_OR_GET_BT_NAME -> handleSetOrGetBTName(cmdData)
            CmdType.TYPE_OUT_MODE -> handleOutMode(cmdData)
            CmdType.TYPE_INVENTORY -> handleInventory(cmdData)
            else -> handleAny(cmdType, cmdData)
        }
    }

    private fun handleGetDeviceInfo(cmdData: CmdData?) {
        val info = cmdData?.data as DeviceInfoBean
        Log.d(name, "Device Info Handler -> $info")
        _info = info
    }

    private fun handleReboot(cmdData: CmdData?) {
        val info = cmdData?.data as GeneralBean
        Log.d(name, "Reboot Info: $info")
    }

    private fun handleSetParam(cmdData: CmdData?) {
        val info = cmdData?.data as GeneralBean
        Log.d(name, "Set All Param Info: $info")
    }

    private fun handleGetAllParam(cmdData: CmdData?) {
        val info = cmdData?.data as AllParamBean
        Log.d(name, "Get All Param Handler -> $info")
        _params = info
    }

    private fun handleGetBatteryCapacity(cmdData: CmdData?) {
        val info = cmdData?.data as BatteryCapacityBean
        Log.d(name, "Get Battery Capacity Info: $info")
        val emitted = tryEmit(DeviceEvent.BatteryEvent(level = 0))
        Log.d(name, "Battery event emitted: $emitted")
    }

    private fun handleSetOrGetBTName(cmdData: CmdData?) {
        val info = cmdData?.data as DeviceNameBean
        Log.d(name, "Set or Get BT Name Info: $info")
    }

    private fun handleOutMode(cmdData: CmdData?) {
        val info = cmdData?.data as OutputModeBean
        Log.d(name, "Out Mode Info: $info")
    }

    private fun handleInventory(cmdData: CmdData?) {
        val info = cmdData?.data as? TagInfoBean
        Log.d(name, "Inventory Tag Handler -> $info")
        if (info != null) {
            val rfid = FormatUtil.bytesToHexStr(info.mEPCNum).replace("\\s", "")
            val tag = TagMetadata(
                rfid = rfid,
                rssi = info.mRSSI.toString(),
                antenna = info.mAntenna
            )
            val published = publishTag(tag = tag)
            if (published) {
                Log.d(name, "Published tag: $tag")
            }
        }
    }

    private fun handleAny(cmdType: Int, cmdData: CmdData?) {
        Log.d(name, "Unknown type = $cmdType, data = $cmdData")
    }

    fun isConnected(): Boolean {
        return mBleCore.isConnect
    }

    /**
     * Start inventory with parameters
     * @param invType - inventory type: 0x00 = by time, 0x01 = by tag count
     * @param invParam - inventory parameter: if invType is 0x00, then invParam is time in seconds;
     *                   if invType is 0x01, then invParam is number of tags to read
     * @return true if the command was sent successfully, false otherwise
     */
    private fun startInventory(intType: Int = 0x00, invParam: Int = 0): Boolean {
        val bytes = CmdBuilder.buildInventoryISOContinueCmd(intType.toByte(), invParam)
        val result = this.writeData(pCmd = bytes)
        Log.d(name, "Start inventory result = $result")
        return result
    }

    override fun start(): Boolean {
        this.getDeviceInfo()
        this.getInventoryParams()
        this.setPower(15)
        this.setFrequency(DeviceFrequency.UNITED_STATES)
        return this.startInventory(intType = 0x00, invParam = 0)
    }

    private fun stopInventory(): Boolean {
        val bytes = CmdBuilder.buildStopInventoryCmd()
        val result = this.writeData(pCmd = bytes)
        Log.d(name, "Stop inventory result = $result")
        return result
    }

    override fun stop(): Boolean {
        return this.stopInventory()
    }

    override fun getInventoryParams(): DeviceParams? {
        val bytes = CmdBuilder.buildGetAllParamCmd()
        val result = this.writeData(pCmd = bytes)
        Log.d(name, "Get AllParamCmd result: $result")
        return null
    }

    override fun setInventoryParams(value: DeviceParams): Boolean {
        val params = _params
        if (params == null) {
            Log.e(name, "Device parameters not loaded. Call getInventoryParams() first.")
            return false
        }
        val bytes = CmdBuilder.buildSetAllParamCmd(params)
        val result = this.writeData(bytes)
        Log.d(name, "Set inventory params result = $result")
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
            Log.e(name, "Device parameters not loaded. Call getInventoryParams() first.")
            return false
        }

        val freq = value.toChafon()
        val band = freq.band      // band (Int)
        val fStart = freq.fStart  // in MHz (Double)
        val fStep = freq.fStep    // in MHz (Double)
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

        params.mRfidFreq.mREGION = freq.band.toByte()

        params.mRfidFreq.mSTRATFREI[0] = ((intPart shr 8) and 0xFF).toByte()
        params.mRfidFreq.mSTRATFREI[1] = (intPart and 0xFF).toByte()

        params.mRfidFreq.mSTRATFRED[0] = ((fracKHz shr 8) and 0xFF).toByte()
        params.mRfidFreq.mSTRATFRED[1] = (fracKHz and 0xFF).toByte()

        params.mRfidFreq.mSTEPFRE[0] = ((stepKHz shr 8) and 0xFF).toByte()
        params.mRfidFreq.mSTEPFRE[1] = (stepKHz and 0xFF).toByte()

        params.mRfidFreq.mCN = cn.toByte()

        val bytes = CmdBuilder.buildSetAllParamCmd(params)
        val result = this.writeData(pCmd = bytes)
        Log.d(name, "Set frequency result = $result")
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
            return this.writeData(pCmd = bytes)
        }

        val reserved = 0x00.toByte()
        val bytes = CmdBuilder.buildSetPwrCmd(value.toByte(), reserved)
        val result = this.writeData(pCmd = bytes)
        Log.d(name, "Set power result = $result")
        return result
    }

    override fun getBeep(): Boolean {
        return false
    }

    override fun setBeep(enabled: Boolean): Boolean {
        return false
    }

    override fun kill(rfid: String, password: String?): Boolean {
        TODO("Not yet implemented")
    }

    fun startScanDevices(): Boolean {
        if (!mBleCore.isConnect) {
            Log.e(name, "Device is not connected")
            return false
        }
        mBleCore.startScan { result ->
            val scanRecord = result.scanRecord
            if (scanRecord != null) {
                val bytes = BleUtil.getCompanyId(scanRecord.bytes)
                val hex = FormatUtil.bytesToHexStr(bytes)
                Log.d(name, "ScanRecord CompanyId: $hex")
                if (hex.startsWith(COMPANY_ID)) {
                    Log.d(name, "Found Chafon device during scan: ${result.device.address}")
                }
            }
        }
        return true
    }

    fun stopScanDevices(): Boolean {
        mBleCore.stopScan()
        return true
    }

    private fun getDeviceInfo(): Any? {
        val bytes = CmdBuilder.buildGetDeviceInfoCmd()
        val result = this.writeData(pCmd = bytes)
        Log.d(name, "GetDeviceInfoCmd result: $result")
        return null
    }

    private fun getBatteryCapacity(): Boolean {
        val bytes = CmdBuilder.buildGetBatteryCapacityCmd()
        val result = this.writeData(pCmd = bytes)
        Log.d(name, "GetBatteryCapacityCmd result = $result")
        return result
    }

    private fun writeData(pCmd: ByteArray): Boolean {
        return mBleCore.writeData(SERVICE_UUID, WRITE_UUID, pCmd)
    }

    override fun close() {
        super.close()
        mBleCore.setNotifyState(SERVICE_UUID, NOTIFY_UUID, false, null)
        mBleCore.setOnNotifyCallback(null)
        mBleCore.setIConnectDoneCallback(null)
        mBleCore.setIBleDisConnectCallback(null)
        mBleCore.disconnectedDevice()
    }

}
