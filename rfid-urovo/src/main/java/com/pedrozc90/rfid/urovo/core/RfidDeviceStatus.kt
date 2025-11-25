package com.pedrozc90.rfid.urovo.core

import android.bluetooth.BluetoothDevice
import com.pedrozc90.rfid.objects.RfidDeviceStatus
import com.ubx.usdk.USDKManager

fun RfidDeviceStatus.Companion.of(
    status: USDKManager.STATUS,
    device: BluetoothDevice? = null
): RfidDeviceStatus = when (status) {
//    USDKManager.STATUS.SUCCESS -> RfidDeviceStatus(status = "SUCCESS")
//    USDKManager.STATUS.NOT_SUPPORTED -> TODO()
//    USDKManager.STATUS.NOT_READY -> TODO()
//    USDKManager.STATUS.RELEASE -> TODO()
//    USDKManager.STATUS.UNKNOWN -> TODO()
//    USDKManager.STATUS.FAIL -> TODO()
    else -> RfidDeviceStatus(status = status.name)
}
