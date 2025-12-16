package com.pedrozc90.rfid.chafon

import android.bluetooth.BluetoothGatt
import android.content.Context
import android.util.Log
import com.cf.beans.AllParamBean
import com.cf.beans.BatteryCapacityBean
import com.cf.beans.CmdData
import com.cf.beans.DeviceInfoBean
import com.cf.beans.DeviceNameBean
import com.cf.beans.GeneralBean
import com.cf.beans.KeyStateBean
import com.cf.beans.OutputModeBean
import com.cf.beans.TagInfoBean
import com.cf.ble.BleUtil
import com.cf.zsdk.BleCore
import com.cf.zsdk.CfSdk
import com.cf.zsdk.SdkC
import com.cf.zsdk.cmd.CmdType
import com.cf.zsdk.uitl.FormatUtil
import com.pedrozc90.rfid.core.Options
import com.pedrozc90.rfid.exceptions.RfidDeviceException
import com.pedrozc90.rfid.objects.DeviceEvent
import com.pedrozc90.rfid.objects.RfidDeviceStatus
import com.pedrozc90.rfid.objects.TagMetadata
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Job
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import java.util.UUID
import kotlin.math.max

/**
 * Chafon BLE RFID Device implementation.
 *
 * Tested with Chafon H103 Bluetooth UHF RFID Sled Reader
 */
class ChafonBLE(private val context: Context) : ChafonDevice() {

    private val COMPANY_ID = "2795"
    private val SERVICE_UUID: UUID = UUID.fromString("0000ffe0-0000-1000-8000-00805f9b34fb")
    private val UUID_1: UUID = UUID.fromString("0000ffe1-0000-1000-8000-00805f9b34fb")
    private val UUID_2: UUID = UUID.fromString("0000ffe2-0000-1000-8000-00805f9b34fb")
    private val WRITE_UUID: UUID = UUID.fromString("0000ffe3-0000-1000-8000-00805f9b34fb")
    private val NOTIFY_UUID: UUID = UUID.fromString("0000ffe4-0000-1000-8000-00805f9b34fb")
    private val UUID_5: UUID = UUID.fromString("0000ffe5-0000-1000-8000-00805f9b34fb")
    private val TYPE_IGNORED: Set<Int> = setOf(83)

    override val TAG: String = "ChafonBle"
    override val minPower = 0 // 4 dbm
    override val maxPower = 33 // 33 dbm

    private val _core: BleCore = CfSdk.get(SdkC.BLE)
    private var _gatt: BluetoothGatt? = null
    private var _batteryJob: Job? = null

    private val _channel = Channel<Pair<Int, CmdData?>>(capacity = 1024)
    private var _processor: Job? = null

    override suspend fun initReader(opts: Options) {
        require(opts.bDevice != null) { "BluetoothDevice must be provided." }

        _processor = scope.launch {
            for ((cmdType, cmdData) in _channel) {
                try {
                    when (cmdType) {
                        CmdType.TYPE_INVENTORY -> handleInventory(cmdData)                      // 1
                        CmdType.TYPE_STOP_INVENTORY -> handleStopInventory(cmdData)             // 2
                        CmdType.TYPE_REBOOT -> handleReboot(cmdData)                            // 82
                        CmdType.TYPE_GET_DEVICE_INFO -> handleGetDeviceInfo(cmdData)            // 112
                        CmdType.TYPE_SET_ALL_PARAM -> handleSetAllParam(cmdData)                // 113
                        CmdType.TYPE_GET_ALL_PARAM -> handleGetAllParam(cmdData)                // 114
                        CmdType.TYPE_GET_BATTERY_CAPACITY -> handleGetBatteryCapacity(cmdData)  // 131
                        CmdType.TYPE_SET_OR_GET_BT_NAME -> handleSetOrGetBTName(cmdData)        // 134
                        CmdType.TYPE_OUT_MODE -> handleOutMode(cmdData)                         // 136
                        CmdType.TYPE_KEY_STATE -> handleKeyState(cmdData)                       // 137
                        else -> handleAny(cmdType, cmdData)
                    }
                } catch (t: Throwable) {
                    if (t is CancellationException) throw t
                    Log.e(TAG, "Error handling notify of type $cmdType", t)
                }
            }
        }

        _core.init(context)

        if (!_core.isSupportBt) {
            throw RfidDeviceException(message = "Bluetooth not supported on this device.")
        }

        if (!_core.isEnabled) {
            throw RfidDeviceException(message = "Bluetooth is not enabled on this device.")
        }

        _core.setOnNotifyCallback { cmdType, cmdData -> onNotify(cmdType, cmdData) }

        _core.setIConnectDoneCallback { b ->
            if (b) {
                Log.d(TAG, "Bluetooth device connected successfully.")

                updateStatus(RfidDeviceStatus(status = "CONNECTED", device = opts.bDevice))

                val updated = _core.setNotifyState(SERVICE_UUID, NOTIFY_UUID, true)
                Log.d(TAG, "Set notify state updated = $updated")

                startBatteryPolling(opts)
            } else {
                Log.e(TAG, "Bluetooth device connection failed.")
                updateStatus(RfidDeviceStatus(status = "DISCONNECTED", device = opts.bDevice))
            }
        }

        _core.setIBleDisConnectCallback { ->
            Log.d(TAG, "Bluetooth device disconnected.")
            updateStatus(RfidDeviceStatus(status = "DISCONNECTED", device = opts.bDevice))
        }

        _gatt = _core.connectDevice(opts.bDevice, context, true)
        Log.d(TAG, "BluetoothGatt connected: $_gatt")

        // looks like this helps
        delay(500)
    }

    private fun startBatteryPolling(opts: Options) {
        if (opts.battery) {
            if (_batteryJob?.isActive == true) {
                _batteryJob?.cancel()
            }

            val delayMs = max(opts.batteryPollingDelay, 1_000L)

            _batteryJob = scope.launch {
                GetBatteryCapacity()
                delay(100)
                while (isActive) {
                    val updated = GetBatteryCapacity()
                    val value = if (updated) delayMs else (2 * delayMs)
                    delay(value)
                }
            }
        }
    }

    private fun onNotify(cmdType: Int, cmdData: CmdData?) {
        // ignore some types, like 83
        if (cmdType in TYPE_IGNORED) return
        val result = _channel.trySend(cmdType to cmdData)
        if (result.isFailure) {
            Log.w(TAG, "Notify channel is full, dropping event $cmdType")
        }
    }

    private val regexNoSpace = Regex("\\s")

    private suspend fun handleInventory(cmdData: CmdData?) {
        val data = cmdData?.data as? TagInfoBean
        if (data == null) return

        Log.d(TAG, "Inventory Tag Handler -> $data")
        val status = data.mStatus
        when (status) {
            0x00 -> {
                // 0x00 = The tag was successfully inventoried, and the tag information is included in PAYLOAD
                val rfid = FormatUtil.bytesToHexStr(data.mEPCNum)
                    .replace(regexNoSpace, "")
                val tag = TagMetadata(
                    rfid = rfid,
                    rssi = data.mRSSI.toString(),
                    antenna = data.mAntenna
                )
                val published = publishTag(tag = tag)
                if (published) {
                    Log.d(TAG, "Published tag: $tag")
                }
            }

            0x01 -> Log.e(
                TAG,
                "Q. The MemBank parameter value is wrong or the Length and Mask data lengths are inconsistent."
            )

            0x02 -> Log.e(TAG, "Command execution failed due to internal module error.")
            0x12 -> Log.e(TAG, "No tags were counted or the entire inventory command was executed.")
            0x17 -> Log.e(
                TAG,
                "The tag data exceeds the maximum transmission length of the serial port."
            )

            else -> Log.e(TAG, "Unknown status '${data.mStatus}")
        }
    }

    private fun handleStopInventory(cmdData: CmdData?) {
        Log.d(TAG, "Stop Inventory Handler: data = $cmdData, type = ${cmdData?.dataType}")
    }

    private suspend fun handleGetDeviceInfo(cmdData: CmdData?) {
        val data = cmdData?.data as DeviceInfoBean?
        val status = data?.mStatus
        when (status) {
            0x00 -> {
                Log.d(TAG, "[Get Device Info Handler] Succeed with data = $data")
                completeListener(CmdType.TYPE_GET_DEVICE_INFO, data)
                _info = data
            }

            else -> Log.e(TAG, "[Get Device Info Handler] status = $status, data = $data")
        }
    }

    private fun handleReboot(cmdData: CmdData?) {
        val data = cmdData?.data as GeneralBean?
        if (data == null) return

        val status = data.mStatus
        when (status) {
            0x00 -> Log.d(TAG, "'Reboot' command executed successful")
            else -> Log.e(TAG, "'Reboot' command returned status $status")
        }
    }

    private suspend fun handleSetAllParam(cmdData: CmdData?) {
        val data = cmdData?.data as GeneralBean?
        if (data == null) return

        val status = data.mStatus
        when (status) {
            0x00 -> Log.d(TAG, "'SetAllParam' command executed successfully.")
            0x01 -> Log.e(TAG, "'SetAllParam' command failed because of parameter error")
            else -> Log.e(TAG, "'SetAllParam' command returned state $status")
        }
    }

    private suspend fun handleGetAllParam(cmdData: CmdData?) {
        val data = cmdData?.data as AllParamBean?
        if (data == null) return

        val status = data.mStatus
        when (status) {
            0x00 -> {
                Log.d(TAG, "'GetAllParams' command executed successful")
                completeListener(CmdType.TYPE_GET_ALL_PARAM, data)
                _params = data
            }

            else -> Log.e(TAG, "'GetAllParam' command returned status $status")
        }
    }

    private fun handleGetBatteryCapacity(cmdData: CmdData?) {
        val data = cmdData?.data as? BatteryCapacityBean
        if (data == null) return

        val status = data.mStatus
        when (status) {
            0x00 -> {
                Log.d(TAG, "'GetBatteryCapacity' command executed successful")

                // Convert battery byte to unsigned int 0..255
                val rawLevel = data.mBatteryCapacity.toInt() and 0xFF

                when {
                    (rawLevel == 0xFF) -> {
                        // sentinel for unknown (if applicable in your protocol)
                        Log.w(TAG, "Battery level unknown (0xFF)")
                    }
                    (rawLevel in 0..100) -> {
                        val emitted = tryEmit(DeviceEvent.BatteryEvent(level = rawLevel))
                        if (emitted) {
                            Log.d(TAG, "Battery event ($rawLevel) emitted")
                        }
                    }
                    else -> {
                        Log.e(TAG, "Battery value out of expected range: $rawLevel")
                    }
                }
            }
            0x01 -> Log.e(TAG, "'GetBatteryCapacity' command parameter error")
            else -> Log.e(TAG, "'GetBatteryCapacity' command returned status $status")
        }
    }

    private fun handleSetOrGetBTName(cmdData: CmdData?) {
        val data = cmdData?.data as DeviceNameBean?
        if (data == null) return

        val options = data.mOption.toInt() and 0xFF // 0..255
        val method = if (options == 1) "SET" else "GET"

        val status = data.mStatus
        when (status) {
            0x00 -> Log.d(TAG, "'SetOrGetBluetoothName' command executed successful")
            0x01 -> Log.d(TAG, "'SetOrGetBluetoothName' command parameter error")
            else -> Log.d(TAG, "'SetOrGetBluetoothName' command returned status $status")
        }
        Log.d(TAG, "Set or Get BT Name Info: $data")
    }

    private fun handleOutMode(cmdData: CmdData?) {
        val data = cmdData?.data as OutputModeBean?
        if (data == null) return

        val status = data.mStatus
        when (status) {
            0x00 -> Log.d(TAG, "'SetOrGetOutputMode' command executed successful")
            0x01 -> Log.d(TAG, "'SetOrGetOutputMode' command parameter error")
            else -> Log.d(TAG, "'SetOrGetOutputMode' command returned status $status")
        }
    }

    private fun handleKeyState(cmdData: CmdData?) {
        val data = cmdData?.data as? KeyStateBean
        val state = data?.mKeyState?.toInt()
        when (state) {
            1 -> Log.d(TAG, "Trigger pressed")
            2 -> Log.d(TAG, "Trigger released")
            else -> Log.d(TAG, "Key State: $state")
        }
    }

    private fun handleAny(cmdType: Int, cmdData: CmdData?) {
        val data = cmdData?.data
        if (data == null) return
        Log.d(TAG, "Unknown type = $cmdType, data = $data")
    }

    fun isConnected(): Boolean {
        return _core.isConnect
    }

    suspend fun startScanDevices(): Boolean {
        if (!_core.isConnect) {
            Log.e(TAG, "Device is not connected")
            return false
        }
        _core.startScan { result ->
            val scanRecord = result.scanRecord
            if (scanRecord != null) {
                val bytes = BleUtil.getCompanyId(scanRecord.bytes)
                val hex = FormatUtil.bytesToHexStr(bytes)
                Log.d(TAG, "ScanRecord CompanyId: $hex")
                if (hex.startsWith(COMPANY_ID)) {
                    Log.d(TAG, "Found Chafon device during scan: ${result.device.address}")
                }
            }
        }
        return true
    }

    suspend fun stopScanDevices(): Boolean {
        _core.stopScan()
        return true
    }

    override suspend fun writeData(bytes: ByteArray): Boolean {
        return _core.writeData(SERVICE_UUID, WRITE_UUID, bytes)
    }

    override fun close() {
        try {
            // stop notify processor job
            _processor?.let {
                if (it.isActive) {
                    it.cancel()
                    Log.d(TAG, "Notify processor job cancelled.")
                }
            }

            // cancel battery job
            _batteryJob?.let {
                if (it.isActive) {
                    it.cancel()
                    Log.d(TAG, "Battery pooling job cancelled.")
                }
            }

            try {
                val closed = _channel.close()
                if (closed) {
                    Log.d(TAG, "Notify channel closed.")
                }
            } catch (e: Exception) {
                Log.e(TAG, "Failed to close notify channel.")
            }

            // unset SDK callbacks and disconnect device
            _core.setNotifyState(SERVICE_UUID, NOTIFY_UUID, false, null)
            _core.setOnNotifyCallback(null)
            _core.setIConnectDoneCallback(null)
            _core.setIBleDisConnectCallback(null)
            _core.disconnectedDevice()
        } finally {
            super.close()
        }
    }

}
