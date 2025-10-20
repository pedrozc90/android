package com.pedrozc90.rfid.devices.chainway.c72

const val TAG = "C72Reader"

/**
 * C72Reader - Kotlin replacement for the old Cordova plugin.
 *
 * Goals:
 * - Remove Cordova dependencies.
 * - Use SDK's inventory callback (setInventoryCallback) instead of readTagFromBuffer.
 * - Expose a SharedFlow<TagInfo> for tag events.
 * - Provide typed DTOs instead of JSONObjects.
 *
 * Usage:
 * val rfid = C72Reader(context, '00:11:22:33:44:55')
 * lifecycleScope.launchWhenStarted { rfid.tagFlow.collect { ... } }
 * rfid.startInventory()
 * rfid.stopInventory()
 */
//class C72Reader : RfidReader {
//
//    // SDK reader instance
//    private var context: Context? = null
//    private val reader: RFIDWithUHFUART = RFIDWithUHFUART.getInstance()
//    private var callback: ((TagMetadata) -> Unit)? = null
//
//    override fun init(context: Context, opts: RfidOptions) {
//        try {
//            // initialize SDK (keep try/catch; init() may throw)
//            this.context = context
//            reader.init(context)
//        } catch (ex: Exception) {
//            Log.e(TAG, "SDK init failed", ex)
//        }
//    }
//
//    override fun start(): Boolean {
//        return reader.startInventoryTag()
//    }
//
//    override fun stop(): Boolean {
//        return try {
//            reader.stopInventory()
//            reader.free()
//            true
//        } catch (ex: Exception) {
//            Log.e(TAG, "stopInventory error", ex)
//            false
//        }
//    }
//
//    override fun setInventoryCallback(callback: ((TagMetadata) -> Unit)?) {
//        this.callback = callback
//        try {
//            if (callback == null) {
//                reader.setInventoryCallback(null)
//            } else {
//                reader.setInventoryCallback { info: UHFTAGInfo ->
//                    try {
//                        val tag = TagMetadata.from(info)
//                        if (tag != null) {
//                            callback.invoke(tag)
//                        }
//                    } catch (e: Exception) {
//                        Log.e(TAG, "Inventory callback error", e)
//                    }
//                }
//            }
//        } catch (ex: Throwable) {
//            // Some SDK versions may throw; fallback to wrapping UHFTAGInfo read from buffer is possible,
//            // but we're using setInventoryCallback when available.
//            Log.w(TAG, "setInventoryCallback failed on SDK: ${ex.message}")
//        }
//    }
//
//    override fun writeData(
//        accessPwd: String,
//        bank: Int,
//        startFilter: Int,
//        endFilter: Int,
//        targetFilter: String,
//        writeBank: Int,
//        writePtr: Int,
//        writeLen: Int,
//        dataHex: String
//    ): Boolean {
//        return reader.writeData(
//            accessPwd,
//            bank,
//            startFilter,
//            endFilter,
//            targetFilter,
//            writeBank,
//            writePtr,
//            writeLen,
//            dataHex
//        )
//    }
//
//    override fun getPower(): Int = reader.power
//    override fun setPower(value: Int): Boolean = reader.setPower(value)
//
//    override fun getTemperature(): Int = reader.temperature
//
//    override fun getGen2(): Gen2Dto? {
//        return reader.gen2.let {
//            Gen2Dto.toDto(it)
//        }
//    }
//
//    override fun setGen2(entity: Gen2Dto): Boolean {
//        return reader.setGen2(entity.toEntity())
//    }
//
//    override fun setTxCycle(on: Int, off: Int): Boolean {
//        // TODO: verify SDK method
//        // reader.setEPCAndTIDUserMode(on, off)
//        // reader.setPwm(on, off)
//        TODO("Not yet implemented")
//    }
//
//    override fun getTxCycle(): IntArray? {
//        // TODO: verify SDK method
//        // reader.getPwm()
//        TODO("Not yet implemented")
//    }
//
//    override fun setFocus(enabled: Boolean): Boolean = reader.setTagFocus(enabled)
//
//    override fun getBatteryLevel(): Int {
//        val bm: BatteryManager? =
//            context?.getSystemService(Context.BATTERY_SERVICE) as BatteryManager
//        return bm?.getIntProperty(BatteryManager.BATTERY_PROPERTY_CAPACITY) ?: -1
//    }
//
//    override fun setBeep(value: Boolean): Boolean {
//        TODO("Not yet implemented")
//    }
//
//    override fun setVibration(value: Int): Boolean {
//        TODO("Not yet implemented")
//    }
//
//    override fun getLongitude(): Float {
//        // return 0f
//        TODO("Not yet implemented")
//    }
//
//    override fun getLatitude(): Float {
//        TODO("Not yet implemented")
//    }
//
//    override fun getVersion(): String? = reader.getVersion()
//
//}
