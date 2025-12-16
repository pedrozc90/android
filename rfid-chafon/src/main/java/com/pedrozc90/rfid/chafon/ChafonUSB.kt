package com.pedrozc90.rfid.chafon

import android.content.Context
import android.util.Log
import com.cf.zsdk.CfSdk
import com.cf.zsdk.SdkC
import com.cf.zsdk.UsbCore
import com.pedrozc90.rfid.core.Options
import com.pedrozc90.rfid.exceptions.RfidDeviceException
import kotlinx.coroutines.launch
import java.util.concurrent.Executors

class ChafonUSB(private val context: Context) : ChafonDevice() {

    override val TAG: String = "ChafonUsb"

    private val _core: UsbCore = CfSdk.get(SdkC.USB)
    private val _timeout: Int = 50

    init {
        // CfSdk.load should be called once in Application.onCreate; don't reload here.
        val executor = Executors.newSingleThreadExecutor()
        CfSdk.load(executor)
    }

    override suspend fun initReader(opts: Options) {
        _core.init(context)

        val usbDevice = _core.findTargetDevice(0, 0)
            ?: throw RfidDeviceException(message = "Usb device not found.")

        _core.connectDevice(context, usbDevice) { onUsbConnectDone(it) }

        _core.setIReadDataCallback { onReadData(it) }
    }

    private fun onUsbConnectDone(p0: Boolean) {
        scope.launch {
            val p1 = if (p0) "ok" else "failed"
            Log.d(TAG, "Device connection '$p1'")
        }
    }

    private fun onReadData(bytes: ByteArray?) {
        scope.launch {
            if (bytes != null) {
                Log.d(TAG, "Data Received: $bytes")
            }
        }
    }

    override suspend fun writeData(bytes: ByteArray): Boolean {
        return _core.writeData(bytes, _timeout)
    }

    override fun close() {
        super.close()
        _core.release()
    }

}
