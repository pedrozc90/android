package com.pedrozc90.prototype.ui.screens.scan

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.pedrozc90.rfid.core.RfidDevice
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

const val TAG = "SettingsViewModel"

class ScanViewModel(
    private val reader: RfidDevice
) : ViewModel() {

    private val _uiState = MutableStateFlow(ScanUiState())
    val uiState = _uiState.asStateFlow()

    fun onInit() {
        Log.d(TAG, "onInit called")
        viewModelScope.launch {
            _uiState.update { it.copy(connection = "Connecting") }
            delay(5_000)
            _uiState.update { it.copy(connection = "Connected") }
        }
    }

    fun onDispose() {
        viewModelScope.launch {
            _uiState.update { it.copy(connection = "Disconnecting") }
            delay(5_000)
            _uiState.update { it.copy(connection = "Disconnected") }
        }
        Log.d(TAG, "onDispose called")
    }

    fun startInventory() {
        Log.d(TAG, "startInventory called")
    }

    fun stopInventory() {
        Log.d(TAG, "stopInventory called")
    }

    fun disconnect() {
        Log.d(TAG, "disconnect called")
    }

}

data class ScanUiState(
    val tags: List<String> = emptyList(),
    val connection: String = "Disconnected"
)
