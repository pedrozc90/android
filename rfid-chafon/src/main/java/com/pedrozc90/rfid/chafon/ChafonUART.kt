package com.pedrozc90.rfid.chafon

import android.content.Context
import android.util.Log
import com.cf.zsdk.CfSdk
import com.cf.zsdk.SdkC
import com.cf.zsdk.UartCore
import com.pedrozc90.rfid.core.Options
import java.util.concurrent.Executors

class ChafonUART(private val context: Context) : ChafonDevice() {

    override val TAG: String = "ChafonUART"

    private val _core: UartCore = CfSdk.get(SdkC.UART)

    init {
        // CfSdk.load should be called once in Application.onCreate; don't reload here.
        val executor = Executors.newSingleThreadExecutor()
        CfSdk.load(executor)
    }

    override fun initReader(opts: Options) {
        val path = "/dev/ttyS7"
        val baudRate = 115_200
        _core.init(path, baudRate)

        _core.receiverData { this.onDataCallback(it) }
    }

    private fun onDataCallback(bytes: ByteArray?) {
        if (bytes != null) {
            Log.d(TAG, "Data Received: $bytes")
        }
    }

    override fun writeData(bytes: ByteArray): Boolean {
        return _core.sendData(bytes)
    }

    override fun close() {
        super.close()
        _core.release()
    }

}
