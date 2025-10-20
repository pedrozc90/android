package com.pedrozc90.rfid.devices.chainway.r6

const val TAG = "R6Reader"

//class R6Reader : RfidReader {
//
//    private val reader: RFIDWithUHFBLE = RFIDWithUHFBLE.getInstance()
//
//    override fun init(context: Context, opts: RfidOptions) {
//        val macAddress = opts.macAddress
//        if (macAddress.isNullOrEmpty()) {
//            throw IllegalArgumentException("MAC address is required in options")
//        }
//
//        try {
//            reader.init(context)
//        } catch (e: Exception) {
//            Log.e(TAG, "SDK init failed", e)
//            throw e
//        }
//
//        try {
//            reader.connect(macAddress) { status, device ->
//                val bd: BluetoothDevice = device as BluetoothDevice
//                when (status) {
//                    ConnectionStatus.CONNECTED -> {
//                        Log.i(TAG, "Connected to device at $macAddress")
//                    }
//                    ConnectionStatus.CONNECTING -> {
//                        Log.i(TAG, "Connecting to device at $macAddress")
//                    }
//                    ConnectionStatus.DISCONNECTED -> {
//                        Log.i(TAG, "Disconnected to device at $macAddress")
//                    }
//                    else -> {
//                        Log.e(TAG, "Failed to connect to device at $macAddress, status: $status")
//                        throw Exception("Connection failed with status: $status")
//                    }
//                }
//            }
//        } catch (e: Exception) {
//            Log.e(TAG, "Failed to connect to device at $macAddress", e)
//            throw e
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
//        TODO("Not yet implemented")
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
//        TODO("Not yet implemented")
//    }
//
//    override fun setPower(value: Int): Boolean =reader.setPower(value)
//
//    override fun getPower(): Int  = reader.power
//
//    override fun getTemperature(): Int {
//        TODO("Not yet implemented")
//    }
//
//    override fun getGen2(): Gen2Dto? {
//        TODO("Not yet implemented")
//    }
//
//    override fun setGen2(entity: Gen2Dto): Boolean {
//        TODO("Not yet implemented")
//    }
//
//    override fun setTxCycle(on: Int, off: Int): Boolean {
//        TODO("Not yet implemented")
//    }
//
//    override fun getTxCycle(): IntArray? {
//        TODO("Not yet implemented")
//    }
//
//    override fun setFocus(enabled: Boolean): Boolean {
//        TODO("Not yet implemented")
//    }
//
//    override fun getBatteryLevel(): Int = reader.battery
//
//    override fun setBeep(value: Boolean): Boolean = reader.setBeep(value)
//
//    override fun setVibration(value: Int): Boolean {
//        TODO("Not yet implemented")
//    }
//
//    override fun getLongitude(): Float {
//        TODO("Not yet implemented")
//    }
//
//    override fun getLatitude(): Float {
//        TODO("Not yet implemented")
//    }
//
//    override fun getVersion(): String? = reader.version
//
//}
