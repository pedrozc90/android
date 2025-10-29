package com.pedrozc90.prototype.ui.screens.debug

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.pedrozc90.prototype.data.local.PreferencesRepository
import com.pedrozc90.rfid.core.Options
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
            device.events.collect { event ->
                when (event) {
                    is DeviceEvent.TagEvent -> {
                        Log.d(TAG, "Tag received: $event.tag")
                        _uiState.update { it.copy(items = it.items + event.tag) }
                    }

                    is DeviceEvent.StatusEvent -> {
                        Log.d(TAG, "Device status changed: ${event.status}")
                        _uiState.update {
                            it.copy(
                                status = event.status.status,
                                device = event.status.device?.address ?: "No Device"
                            )
                        }
                    }

                    is DeviceEvent.BatteryEvent -> Log.d(TAG, "Battery level: ${event.level}")
                    is DeviceEvent.ErrorEvent -> Log.e(TAG, "Device error", event.throwable)
                }

            }
        }
    }

    fun onInit() {
        val x = viewModelScope.launch(Dispatchers.IO) {
            val opts = Options(address = preferences.getDevice())
            device.init(opts)
        }
    }

    override fun onCleared() {
        super.onCleared()
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

}

data class DebugUiState(
    val items: List<TagMetadata> = emptyList(),
    val status: String? = null,
    val device: String? = null
)
