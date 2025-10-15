package com.pedrozc90.prototype.ui.screens.settings

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.pedrozc90.prototype.data.ReadRepository
import com.pedrozc90.prototype.data.TagRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

const val TAG = "SettingsViewModel"

class SettingsViewModel(
    private val tagRepository: TagRepository,
    private val readRepository: ReadRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(SettingsUiState())
    val uiState = _uiState.asStateFlow()

    fun setPotency(value: Float) {
        _uiState.update { it.copy(potency = value) }
    }

    fun persistSettings() {
        viewModelScope.launch {
            try {
                val state = _uiState.value
                Log.d(TAG, "Settings persisted: $state")
            } catch (e: Exception) {
                Log.e(TAG, "Error persisting settings", e)
            }
        }
    }

    fun resetDatabase() {
        viewModelScope.launch {
            try {
                tagRepository.deleteAll()
                readRepository.deleteAll()
                Log.d(TAG, "Database reset")
            } catch (e: Exception) {
                Log.e(TAG, "Error resetting database", e)
            }
        }
    }

}

data class SettingsUiState(
    val potency: Float = 0f
)
