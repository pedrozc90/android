package com.pedrozc90.rfid.urovo.core

import com.pedrozc90.rfid.objects.DeviceParams
import com.ubx.usdk.bean.RfidParameter

fun RfidParameter.toDeviceParams(): DeviceParams {
    return DeviceParams(
        comAddr = ComAddr,
        ivtType = IvtType,
        memory = Memory,
        password = Password,
        wordPtr = WordPtr,
        length = Length,
        session = Session,
        qValue = QValue,
        scanTime = ScanTime,
        antenna = Antenna,
        interval = Interval,
        maskMem = MaskMem,
        maskAdr = MaskAdr,
        maskLen = MaskLen,
        maskData = MaskData
    )
}

fun DeviceParams.toRfidParameter(): RfidParameter {
    val obj = RfidParameter()
    comAddr?.let { obj.ComAddr = it }
    ivtType?.let { obj.IvtType = it }
    memory?.let { obj.Memory = it }
    obj.Password = password
    wordPtr?.let { obj.WordPtr = it }
    length?.let { obj.Length = it }
    session?.let { obj.Session = it }
    qValue?.let { obj.QValue = it }
    scanTime?.let { obj.ScanTime = it }
    antenna?.let { obj.Antenna = it }
    interval?.let { obj.Interval = it }
    maskMem?.let { obj.MaskMem = it }
    obj.MaskAdr = maskAdr
    maskLen?.let { obj.MaskLen = it }
    obj.MaskData = maskData

    require(obj.Password != null) { "Password must not be null." }
    require(obj.MaskLen >= 0) { "MaskLen must be greater or equal than 0." }

    return obj
}
