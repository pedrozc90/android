package com.pedrozc90.prototype.ui.screens.settings

import android.util.Log
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel

const val TAG = "SettingsViewModel"

class SettingsViewModel : ViewModel() {

    val _uiState = mutableStateOf(SettingsUiState())

    fun setPotency(value: Float) {
        _uiState.value = _uiState.value.copy(potency = value)
    }

    fun persistSettings() {
        Log.d(TAG, "Settings persisted: ${_uiState.value}")
    }

}

data class SettingsUiState(
    val potency: Float = 0f
)
