package com.pedrozc90.prototype.ui.screens.settings

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.pedrozc90.prototype.data.local.PreferencesRepository
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

const val TAG = "SettingsViewModel"

class SettingsViewModel(
    private val preferences: PreferencesRepository
) : ViewModel() {

    var uiState by mutableStateOf(SettingsUiState())
        private set

    init {
        viewModelScope.launch {
            uiState = preferences.getSettings()
                .filterNotNull()
                .first()
        }
    }

    private fun isValid(state: SettingsUiState): Boolean {
        return with(state) {
            device.isNotBlank() && value >= 0
        }
    }

    suspend fun update(state: SettingsUiState) {
        uiState = state
    }

    fun onSave() {
        viewModelScope.launch {
            val state = uiState
            if (isValid(state)) {
                preferences.update(state)
            }
        }
    }

}

data class SettingsUiState(
    val device: String = "",
    val value: Int = 0
)
