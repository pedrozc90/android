package com.pedrozc90.rfid.objects

sealed class DeviceEvent {
    data class TagEvent(val tag: TagMetadata) : DeviceEvent()
    data class StatusEvent(val status: RfidDeviceStatus) : DeviceEvent()
    data class BatteryEvent(val level: Int) : DeviceEvent()
    data class ErrorEvent(val throwable: Throwable) : DeviceEvent()
}
