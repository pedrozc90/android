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
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import java.util.UUID
import kotlin.math.max

class ChafonBle(private val context: Context) : ChafonDevice() {

    private val COMPANY_ID = "2795"
    private val SERVICE_UUID: UUID = UUID.fromString("0000ffe0-0000-1000-8000-00805f9b34fb")
    private val UUID_1: UUID = UUID.fromString("0000ffe1-0000-1000-8000-00805f9b34fb")
    private val UUID_2: UUID = UUID.fromString("0000ffe2-0000-1000-8000-00805f9b34fb")
    private val WRITE_UUID: UUID = UUID.fromString("0000ffe3-0000-1000-8000-00805f9b34fb")
    private val NOTIFY_UUID: UUID = UUID.fromString("0000ffe4-0000-1000-8000-00805f9b34fb")
    private val UUID_5: UUID = UUID.fromString("0000ffe5-0000-1000-8000-00805f9b34fb")

    override val TAG: String = "ChafonBle"

    private val _core: BleCore = CfSdk.get(SdkC.BLE)
    private var _gatt: BluetoothGatt? = null
    private var _batteryJob: Job? = null

    override fun initReader(opts: Options) {
        require(opts.bDevice != null) { "BluetoothDevice must be provided." }

        _core.init(context)

        scope.launch {
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

                    // update inventory params
                    getInventoryParams()

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
            delay(1_000)
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

    private fun onNotify(cmdType: Int, cmdData: CmdData?) {
        when (cmdType) {
            CmdType.TYPE_INVENTORY -> handleInventory(cmdData)                      // 1
            CmdType.TYPE_STOP_INVENTORY -> handleStopInventory(cmdData)                 // 2
            CmdType.TYPE_REBOOT -> handleReboot(cmdData)                            // 82
            CmdType.TYPE_GET_DEVICE_INFO -> handleGetDeviceInfo(cmdData)            // 112
            CmdType.TYPE_SET_ALL_PARAM -> handleSetParam(cmdData)                   // 113
            CmdType.TYPE_GET_ALL_PARAM -> handleGetAllParam(cmdData)                // 114
            CmdType.TYPE_GET_BATTERY_CAPACITY -> handleGetBatteryCapacity(cmdData)  // 131
            CmdType.TYPE_SET_OR_GET_BT_NAME -> handleSetOrGetBTName(cmdData)        // 134
            CmdType.TYPE_OUT_MODE -> handleOutMode(cmdData)                         // 136
            CmdType.TYPE_KEY_STATE -> handleKeyState(cmdData)                       // 137
            else -> handleAny(cmdType, cmdData)
        }
    }

    private val regexNoSpace = Regex("\\s")

    private fun handleInventory(cmdData: CmdData?) {
        val info = cmdData?.data as? TagInfoBean
        if (info == null) return

        Log.d(TAG, "Inventory Tag Handler -> $info")

        // 0x00 = The tag was successfully inventoried, and the tag information is included in PAYLOAD
        if (info.mStatus == 0x00) {
            val rfid = FormatUtil.bytesToHexStr(info.mEPCNum)
                .replace(regexNoSpace, "")
            val tag = TagMetadata(
                rfid = rfid,
                rssi = info.mRSSI.toString(),
                antenna = info.mAntenna
            )
            val published = publishTag(tag = tag)
            if (published) {
                Log.d(TAG, "Published tag: $tag")
            }
        }
        // 0x01 = Q. The MemBank parameter value is wrong or the Length and Mask data lengths are inconsistent;
        else if (info.mStatus == 0x01) {
            Log.e(
                TAG,
                "Q. The MemBank parameter value is wrong or the Length and Mask data lengths are inconsistent."
            )
        }
        // 0x02 = Command execution failed due to internal module error;
        else if (info.mStatus == 0x02) {
            Log.e(TAG, "Command execution failed due to internal module error.")
        }
        // 0x12 = No tags were counted or the entire inventory command was executed;
        else if (info.mStatus == 0x12) {
            Log.e(TAG, "No tags were counted or the entire inventory command was executed.")
        }
        // 0x17 = The tag data exceeds the maximum transmission length of the serial port;
        else if (info.mStatus == 0x17) {
            Log.e(TAG, "The tag data exceeds the maximum transmission length of the serial port.")
        } else {
            Log.e(TAG, "Unknown status '${info.mStatus}")
        }
    }

    private fun handleStopInventory(cmdData: CmdData?) {
        Log.d(TAG, "Stop Inventory Handler: data = $cmdData, type = ${cmdData?.dataType}")
    }

    private fun handleGetDeviceInfo(cmdData: CmdData?) {
        val info = cmdData?.data as DeviceInfoBean
        Log.d(TAG, "Device Info Handler -> $info")
        _info = info
    }

    private fun handleReboot(cmdData: CmdData?) {
        val info = cmdData?.data as GeneralBean
        Log.d(TAG, "Reboot Info: $info")
    }

    private fun handleSetParam(cmdData: CmdData?) {
        val info = cmdData?.data as GeneralBean
        Log.d(TAG, "Set All Param Info: $info")
    }

    private fun handleGetAllParam(cmdData: CmdData?) {
        val info = cmdData?.data as AllParamBean
        Log.d(TAG, "Get All Param Handler -> $info")
        _params = info
    }

    private fun handleGetBatteryCapacity(cmdData: CmdData?) {
        val info = cmdData?.data as? BatteryCapacityBean
        if (info != null) {
            val level = info.mBatteryCapacity.toInt()
            val emitted = tryEmit(DeviceEvent.BatteryEvent(level = level))
            if (emitted) {
                Log.d(TAG, "Battery event ($level) emitted")
            }
        }
    }

    private fun handleSetOrGetBTName(cmdData: CmdData?) {
        val info = cmdData?.data as DeviceNameBean
        Log.d(TAG, "Set or Get BT Name Info: $info")
    }

    private fun handleOutMode(cmdData: CmdData?) {
        val info = cmdData?.data as OutputModeBean
        Log.d(TAG, "Out Mode Info: $info")
    }

    private fun handleKeyState(cmdData: CmdData?) {
        val state = cmdData?.data as? KeyStateBean
        Log.d(TAG, "Key State: $state")
    }

    private fun handleAny(cmdType: Int, cmdData: CmdData?) {
        Log.d(TAG, "Unknown type = $cmdType, data = $cmdData")
    }

    fun isConnected(): Boolean {
        return _core.isConnect
    }

    fun startScanDevices(): Boolean {
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

    fun stopScanDevices(): Boolean {
        _core.stopScan()
        return true
    }

    override fun writeData(bytes: ByteArray): Boolean {
        return _core.writeData(SERVICE_UUID, WRITE_UUID, bytes)
    }

    override fun close() {
        super.close()
        _core.setNotifyState(SERVICE_UUID, NOTIFY_UUID, false, null)
        _core.setOnNotifyCallback(null)
        _core.setIConnectDoneCallback(null)
        _core.setIBleDisConnectCallback(null)
        _core.disconnectedDevice()
    }

}
