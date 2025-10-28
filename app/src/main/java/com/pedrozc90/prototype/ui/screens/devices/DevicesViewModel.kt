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

private const val TAG = "DevicesViewModel"

class DevicesViewModel(
    private val preferences: PreferencesRepository,
    private val bluetooth: BluetoothRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(DevicesUiState())
    val uiState = combine(
        bluetooth.pairedDevices,
        bluetooth.scannedDevices,
        _uiState
    ) { paired, scanned, state ->
        state.copy(paired = paired, scanned = scanned)
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(Constants.STATE_TIMEOUT),
        initialValue = DevicesUiState()
    )

    fun startScan() {
        bluetooth.start()
    }

    fun stopScan() {
        bluetooth.stop()
    }

    fun pairDevice(device: BluetoothDeviceDto) {
        Log.d(TAG, "Pairing device: ${device.name} - ${device.address}")
    }

}

data class DevicesUiState(
    val paired: List<BluetoothDeviceDto> = emptyList(),
    val scanned: List<BluetoothDeviceDto> = emptyList()
)
