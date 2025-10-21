package com.pedrozc90.rfid.devices.chainway

import android.content.Context
import android.util.Log
import com.pedrozc90.rfid.core.RfidOptions
import com.pedrozc90.rfid.core.objects.Gen2Dto
import com.rscja.deviceapi.RFIDWithUHFBLE

private const val TAG = "ChainwayBluetoothRfidDevice"

class ChainwayBluetoothRfidDevice(context: Context) : ChainwayBaseRfidDevice(context) {

    private var reader: RFIDWithUHFBLE = RFIDWithUHFBLE.getInstance()

    override suspend fun init(opts: RfidOptions) {
        try {
            val macAddress = opts.macAddress
            if (macAddress.isNullOrEmpty()) {
                throw IllegalArgumentException("MAC address is required in options")
            }

            reader.init(context)
            reader.connect(macAddress)
        } catch (e: Exception) {
            throw e
        }
    }

    override suspend fun dispose() {
        try {
            val free = reader.free()
            if (free) {
                Log.d(TAG, "Reader freed successfully")
            } else {
                Log.e(TAG, "Failed to free reader")
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error disposing reader", e)
        }
    }

    override suspend fun startInventory(): Boolean {
        return try {
            val started = reader.startInventoryTag()
            if (started) {
                reader.setInventoryCallback(callback)
            }
            started
        } catch (e: Exception) {
            Log.e(TAG, "Fail to start inventory", e)
            false
        }
    }

    override suspend fun stopInventory(): Boolean {
        return try {
            reader.stopInventory()
        } catch (e: Exception) {
            Log.e(TAG, "Fail to stop inventory", e)
            false
        }
    }

    override suspend fun write(
        pwd: String?,
        bank: Int,
        data: String,
        ptr: Int,
        length: Int,
        filter: String?
    ): Boolean {
        return super.write(reader, pwd, bank, data, ptr, length, filter)
    }

    override suspend fun getVersion(): String? = reader.getVersion()

    override suspend fun getPower(): Int = reader.getPower()

    override suspend fun setPower(value: Int): Boolean = reader.setPower(value)

    override suspend fun getGen2Settings(): Gen2Dto = Gen2Dto.toDto(reader.getGen2())

    override suspend fun setGen2Settings(value: Gen2Dto): Boolean = reader.setGen2(value.toEntity())

    override suspend fun getBatteryLevel(): Int = reader.getBattery()

}
