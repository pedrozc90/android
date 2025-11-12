package com.pedrozc90.prototype.ui.screens.settings

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.pedrozc90.prototype.data.local.PreferencesRepository
import com.pedrozc90.rfid.core.RfidDevice
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.launch

const val TAG = "SettingsViewModel"

class SettingsViewModel(
    private val preferences: PreferencesRepository,
    private val device: RfidDevice
) : ViewModel() {

    var uiState by mutableStateOf(SettingsUiState())
        private set

    init {
        viewModelScope.launch {
            preferences.getSettings()
                .filterNotNull()
                .collect { uiState = it.copy(minPower = device.minPower, maxPower = device.maxPower) }
        }
    }

    private fun isValid(state: SettingsUiState): Boolean {
        return with(state) {
            device.isNotBlank() && power >= 0
        }
    }

    fun update(state: SettingsUiState) {
        uiState = state
    }

    fun onSave() {
        viewModelScope.launch {
            val state = uiState
            val valid = isValid(state)
            if (valid) {
                preferences.update(state)
            }
        }
    }

}

data class SettingsUiState(
    val device: String = "",
    val frequency: String = "",
    val power: Int = 0,
    val minPower: Int = 0,
    val maxPower: Int = 100
)
