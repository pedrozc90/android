package com.pedrozc90.prototype.ui.screens.debug

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.pedrozc90.rfid.devices.chainway.ChainwayBluetoothRfidDevice
import com.pedrozc90.rfid.devices.chainway.ChainwayRfidDevice
import com.pedrozc90.rfid.objects.TagMetadata
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

private const val TAG = "DebugViewModel"

class DebugViewModel(
    private val device: ChainwayBluetoothRfidDevice
) : ViewModel() {

    private val _uiState = MutableStateFlow(DebugUiState())
    val uiState: StateFlow<DebugUiState> = _uiState.asStateFlow()

    fun onStart() {
        Log.d(TAG, "Debug button clicked")
        device.start()
    }

    fun onStop() {
        device.stop()
    }

}

data class DebugUiState(
    val items: List<TagMetadata> = emptyList()
)
