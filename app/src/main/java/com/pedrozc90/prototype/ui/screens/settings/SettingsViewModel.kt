package com.pedrozc90.prototype.ui.screens.settings

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.pedrozc90.prototype.Constants
import com.pedrozc90.prototype.data.ReadRepository
import com.pedrozc90.prototype.data.TagRepository
import com.pedrozc90.prototype.data.local.PreferencesRepository
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking

const val TAG = "SettingsViewModel"

class SettingsViewModel(
    private val tagRepository: TagRepository,
    private val readRepository: ReadRepository,
    private val preferencesRepository: PreferencesRepository
) : ViewModel() {

    // private val _uiState = MutableStateFlow(SettingsUiState())
    val uiState = preferencesRepository.getPotency()
        .map { potency -> SettingsUiState(potency = potency) }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(Constants.STATE_TIMEOUT),
            initialValue = runBlocking {
                SettingsUiState(
                    potency = preferencesRepository.getPotency().first()
                )
            }
        )

    fun setPotency(value: Float) {
        viewModelScope.launch {
            preferencesRepository.setPotency(value)
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
