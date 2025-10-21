package com.pedrozc90.rfid.devices.chainway

import android.content.Context
import android.os.BatteryManager
import android.util.Log
import com.pedrozc90.rfid.core.RfidOptions
import com.pedrozc90.rfid.core.objects.Gen2Dto
import com.rscja.deviceapi.RFIDWithUHFUART

private const val TAG = "ChainwayRfidDevice"

class ChainwayRfidDevice(context: Context) : ChainwayBaseRfidDevice(context) {

    // SDK instance
    private var reader: RFIDWithUHFUART = RFIDWithUHFUART.getInstance()

    override suspend fun init(opts: RfidOptions) {
        try {
            reader.init(context)
            reader.setInventoryCallback(callback)
            reader.setConnectionStatusCallback(connectionCallback)

            // reader.setFreHop(0f)            // frequency hopping
            // reader.setFastID(false)         // fast ID mode, increase throughput / decrease data (less memory used)
            // reader.setProtocol(0)           // RFID protocol or GEN2
            // reader.setCW(0)                 // continuous wave mode (disable during inventory)
            // reader.setDynamicDistance(0)    // dynamic power adjustment
            // reader.setUart("none")          // configures UART transport option/name for multiple UARTs devices
            // reader.setFrequencyMode(0)      // set frequency/channel plan or regulatory region mode
            // reader.setFastInventoryMode(false)
            // reader.setRFLink(0)
            // reader.setTagFocus(false)       // enable/disable tag focus mode, where the reader focuses on a single tag
            // reader.setEPCMode()             // sets the return object to return only EPC for each tag
            // reader.setEPCAndTIDMode()                       // sets the return object to return EPC + TID for each tag
            // reader.setEPCAndTIDUserMode(0, 0)   // sets the return object to return EPC + TID + User for each tag
            // val mode = InventoryModeEntity.Builder()
            //     .setMode(InventoryModeEntity.MODE_EPC_TID_USER)
            //     .setUserLength(0)
            //     .setUserOffset(0)
            //     .setReservedLength(0)
            //     .setReservedOffset(0)
            //     .build()
            // reader.setEPCAndTIDUserMode(mode)
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

    /**
     * Return array length 14 means return data, return other length means read failed.
     * <p>
     * params.target              - (s0:0 s1:1 s2:2 s3:3)
     * params.action              - (0,1,2,3,4,5,6)
     * params.t                   - enables truncate (0 = disable, 1 = enable)
     * params.q                   - set Q algorithm (0 = fixed, 1 = dynamic)
     * params.startQ              - (0,1,2,3.....15 note: in fixed algorithm, Q will be fixed as StartQ, neglect MinQ and MaxQ)
     * params.minQ                - (0,1,2,3.....15)
     * params.maxQ                - (0,1,2,3.....15)
     * params.dr                  - (0,1)
     * params.coding              - (0,1,2,3)
     * params.p                   - (0,1)
     * params.Sel                 - (0,1,2,3)
     * params.Session             - (0,1,2,3)
     * params.g                   - (0,1)
     * params.linkFrequency       - (0,1,2,3,4,5,6,7)
     */
    override suspend fun getGen2Settings(): Gen2Dto = Gen2Dto.toDto(reader.gen2)

    override suspend fun setGen2Settings(value: Gen2Dto): Boolean = reader.setGen2(value.toEntity())

    override suspend fun getBatteryLevel(): Int {
        val bm: BatteryManager? =
            context.getSystemService(Context.BATTERY_SERVICE) as BatteryManager?
        return bm?.getIntProperty(BatteryManager.BATTERY_PROPERTY_CAPACITY) ?: -1
    }

}
