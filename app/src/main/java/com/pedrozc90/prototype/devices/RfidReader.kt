package com.pedrozc90.prototype.devices

import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.asSharedFlow

interface RfidReader {

    val name: String


    fun startReading()

    fun stopReading()

}

abstract class BaseRfidReader : RfidReader {

    // Give the shared flow a small buffer so emitters won't suspend if the collector
    // isn't scheduled immediately. This avoids races where emit() suspends and the
    // emitting coroutine is cancelled, which can make subsequent starts behave
    // unexpectedly.
    protected val _flow = MutableSharedFlow<String>(extraBufferCapacity = 64)
    val flow = _flow.asSharedFlow()

}
