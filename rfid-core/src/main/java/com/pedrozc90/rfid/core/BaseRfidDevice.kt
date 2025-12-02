package com.pedrozc90.rfid.core

import com.pedrozc90.rfid.objects.DeviceEvent
import com.pedrozc90.rfid.objects.RfidDeviceStatus
import com.pedrozc90.rfid.objects.TagMetadata
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.channels.BufferOverflow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

/**
 * A small base class that provides a scope and common flows.
 * Concrete implementations should emit events via emitEvent(...) and update statusFlow.
 */
abstract class BaseRfidDevice {

    protected val scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

    // events: Tag, Battery, Error, Status-change as events
    protected val _events = MutableSharedFlow<DeviceEvent>(
        extraBufferCapacity = 512,
        onBufferOverflow = BufferOverflow.DROP_OLDEST
    )
    val events: SharedFlow<DeviceEvent> = _events

    // status: last-known status snapshot (immediate access)
    protected val _status = MutableStateFlow(RfidDeviceStatus.of("None"))
    val status: StateFlow<RfidDeviceStatus> = _status

    protected suspend fun emitEvent(event: DeviceEvent) {
        // use tryEmit on hot path if you cannot suspend, else use emit
        _events.emit(event)
    }

    protected fun tryEmit(event: DeviceEvent): Boolean {
        return _events.tryEmit(event)
    }

    protected fun publishTag(tag: TagMetadata): Boolean {
        return tryEmit(DeviceEvent.TagEvent(tag))
    }

    protected fun updateStatus(status: RfidDeviceStatus) {
        _status.value = status
        // optionally also emit as an event if you want ordering visible to consumers:
        scope.launch { _events.emit(DeviceEvent.StatusEvent(status)) }
    }

    open fun close() {
        // cancel coroutines
        scope.cancel()
    }

}
