package com.pedrozc90.rfid.urovo

import android.content.Context
import android.util.Log
import com.pedrozc90.rfid.core.BaseRfidDevice
import com.pedrozc90.rfid.core.DeviceFrequency
import com.pedrozc90.rfid.core.Options
import com.pedrozc90.rfid.core.RfidDevice
import com.pedrozc90.rfid.exceptions.RfidDeviceException
import com.pedrozc90.rfid.objects.DeviceEvent
import com.pedrozc90.rfid.objects.DeviceParams
import com.pedrozc90.rfid.urovo.core.UrovoFrequency
import com.pedrozc90.rfid.urovo.core.toDeviceParams
import com.pedrozc90.rfid.urovo.core.toRfidParameter
import com.pedrozc90.rfid.urovo.core.toUrovo
import com.ubx.usdk.USDKManager
import com.ubx.usdk.rfid.RfidManager

class UrovoUART(private val context: Context) : BaseRfidDevice(), RfidDevice {

    override var opts: Options? = null

    override val name: String = "UrovoUART"
    override val minPower: Int = 1
    override val maxPower: Int = 33

    private val manager: USDKManager = USDKManager.getInstance()
    private var initializing: Boolean = false

    private val reader: RfidManager
        get() = manager.rfidManager
            ?: throw RfidDeviceException(message = "Reader not initialized.")

    override fun init(opts: Options) {
        if (initializing) {
            Log.d(name, "init() called while already initializing; ignoring.")
            return
        }

        this.opts = opts

        manager.init(context) { status: USDKManager.STATUS ->
            Log.d(name, "InitListener status = $status")
            if (!initializing) {
                initializing = true
                when (status) {
                    USDKManager.STATUS.SUCCESS -> {
                        val reader = manager.rfidManager
                        reader.setOutputPower(opts.power.toByte())

                        val deviceId = reader.deviceId
                        val readerType = reader.readerType
                        val firmware = reader.firmwareVersion
                        Log.d(
                            name,
                            "Reader: deviceId = $deviceId, readerType = $readerType, firmware = $firmware"
                        )
                    }

                    USDKManager.STATUS.NOT_SUPPORTED,
                    USDKManager.STATUS.NOT_READY,
                    USDKManager.STATUS.RELEASE,
                    USDKManager.STATUS.UNKNOWN,
                    USDKManager.STATUS.FAIL -> {
                        Log.d(name, "Device Initialization returned status $status")
                    }
                }
            }
        }
    }

    fun disconnect() {
        try {
            val stopped = stop()
            if (stopped) {
                Log.d(name, "Device stopped")
            }

            reader.disConnect()
        } finally {
            initializing = false
        }
    }

    fun isConnected(): Boolean {
        return try {
            reader.isConnected
        } catch (e: Exception) {
            false
        }
    }

    override fun start(): Boolean {
        val session: Byte = 0.toByte()

        reader.registerCallback(InventoryCallback { tag ->
            Log.d(name, "Tag Received: $tag")
            tryEmit(DeviceEvent.TagEvent(tag))
        })

        val result = reader.startInventory(session)
        return result == 0
    }

    override fun stop(): Boolean {
        val result = reader.stopInventory()
        reader.registerCallback(null)
        return result == 0
    }

    override fun getInventoryParams(): DeviceParams? {
        return reader.inventoryParameter?.toDeviceParams()
    }

    override fun setInventoryParams(value: DeviceParams): Boolean {
        reader.inventoryParameter = value.toRfidParameter()
        return true
    }

    override fun getFrequency(): DeviceFrequency? {
        val result = reader.frequencyRegion
        val freq = UrovoFrequency.of(
            result.btRegion,
            result.btFrequencyStart,
            result.btFrequencyEnd
        )
        return freq?.map()
    }

    override fun setFrequency(value: DeviceFrequency): Boolean {
        val freq = value.toUrovo()
        val result = when (freq) {
            is UrovoFrequency.Range -> reader.setFrequencyRegion(
                freq.region.toByte(),
                freq.start.toByte(),
                freq.end.toByte()
            )

            is UrovoFrequency.Band -> reader.setCustomRegion(
                freq.flags.toByte(),
                freq.band,
                freq.space,
                freq.num,
                freq.start
            )

            null -> throw RfidDeviceException(message = "Invalid frequency region.")
        }
        return result == 0
    }

    override fun checkFrequency(value: DeviceFrequency): Boolean {
        return value.toUrovo() != null
    }

    override fun getPower(): Int {
        return try {
            reader.outputPower
        } catch (e: Exception) {
            -1
        }
    }

    override fun setPower(value: Int): Boolean {
        if (value < minPower) {
            throw RfidDeviceException(message = "Power value must be greater than $minPower")
        } else if (value > maxPower) {
            throw RfidDeviceException(message = "Power value must be smaller than $maxPower")
        }
        val result = reader.setOutputPower(value.toByte())
        return result == 0
    }

    override fun getBeep(): Boolean = false

    override fun setBeep(enabled: Boolean): Boolean = false

}
