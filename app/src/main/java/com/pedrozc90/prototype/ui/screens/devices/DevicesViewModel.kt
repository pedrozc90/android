package com.pedrozc90.prototype.ui.screens.devices

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.pedrozc90.prototype.Constants
import com.pedrozc90.prototype.core.bluetooth.BluetoothDeviceDto
import com.pedrozc90.prototype.core.bluetooth.BluetoothRepository
import com.pedrozc90.prototype.data.local.PreferencesRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

private const val TAG = "DevicesViewModel"

class DevicesViewModel(
    private val preferences: PreferencesRepository,
    private val bluetooth: BluetoothRepository
) : ViewModel() {

    private val _scanning = MutableStateFlow(false)
    private val _uiState = MutableStateFlow(DevicesUiState())
    val uiState = combine(
        bluetooth.pairedDevices,
        bluetooth.scannedDevices,
        _scanning,
        _uiState
    ) { paired, scanned, scanning, state ->
        state.copy(paired = paired, scanned = scanned, scanning = scanning)
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(Constants.STATE_TIMEOUT),
        initialValue = DevicesUiState()
    )

    fun startScan() {
        _scanning.update { true }
        bluetooth.start()
    }

    fun stopScan() {
        _scanning.update { false }
        bluetooth.stop()
    }

    fun pairDevice(device: BluetoothDeviceDto) {
        Log.d(TAG, "Pairing device: $device")
        viewModelScope.launch {
            preferences.update(device)
        }
    }

}

data class DevicesUiState(
    val scanning: Boolean = false,
    val paired: List<BluetoothDeviceDto> = emptyList(),
    val scanned: List<BluetoothDeviceDto> = emptyList()
)
