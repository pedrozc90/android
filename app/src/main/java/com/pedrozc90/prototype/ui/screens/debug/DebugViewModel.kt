package com.pedrozc90.prototype.ui.screens.debug

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.pedrozc90.prototype.data.local.PreferencesRepository
import com.pedrozc90.prototype.ui.screens.settings.DeviceSettings
import com.pedrozc90.rfid.core.RfidDevice
import com.pedrozc90.rfid.objects.DeviceEvent
import com.pedrozc90.rfid.objects.TagMetadata
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

private const val TAG = "DebugViewModel"

class DebugViewModel(
    private val device: RfidDevice,
    private val preferences: PreferencesRepository
) : ViewModel() {
    private var _job: Job? = null

    private val _uiState = MutableStateFlow(DebugUiState())
    val uiState: StateFlow<DebugUiState> = _uiState.asStateFlow()

    init {
        _job = viewModelScope.launch {
            launch {
                preferences.getSettings()
                    .collect { value ->
                        _uiState.update { it.copy(device = value) }
                    }
            }

            launch(Dispatchers.IO) {
                device.events.collect { event ->
                    when (event) {
                        is DeviceEvent.TagEvent -> handleTagEvent(event)
                        is DeviceEvent.StatusEvent -> handleStatusEvent(event)
                        is DeviceEvent.BatteryEvent -> handleBatteryEvent(event)
                        is DeviceEvent.ErrorEvent -> handleErrorEvent(event)
                    }
                }
            }
        }
    }

    private fun handleTagEvent(event: DeviceEvent.TagEvent) {
        Log.d(TAG, "Tag received: $event.tag")
        _uiState.update { it.copy(items = it.items + event.tag) }
    }

    private fun handleStatusEvent(event: DeviceEvent.StatusEvent) {
        Log.d(TAG, "Device status changed: ${event.status}")
        _uiState.update {
            it.copy(status = event.status.status)
        }
    }

    private fun handleBatteryEvent(event: DeviceEvent.BatteryEvent) {
        Log.d(TAG, "Battery level: ${event.level}")
    }

    private fun handleErrorEvent(event: DeviceEvent.ErrorEvent) {
        Log.e(TAG, "Device error", event.throwable)
    }

    fun onInit() {
        val x = viewModelScope.launch(Dispatchers.IO) {
            val state = _uiState.value
            val opts = state.device.toRfidOptions()
            device.init(opts = opts)
        }
    }

    fun onDispose() {
        try {
            if (_job?.isActive == true) {
                _job?.cancel()
                _job = null
            }
        } catch (e: Exception) {
            Log.w(TAG, "Failed to clear ViewModel", e)
        }
    }

    fun onStart() {
        device.start()
    }

    fun onStop() {
        device.stop()
    }

    override fun onCleared() {
        super.onCleared()
        this.onDispose()
    }

}

data class DebugUiState(
    val device: DeviceSettings = DeviceSettings(),
    val status: String? = null,
    val items: List<TagMetadata> = emptyList()
)
