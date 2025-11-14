package com.pedrozc90.prototype.ui.screens.settings

import android.content.Context
import android.util.Log
import android.widget.Toast
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.pedrozc90.prototype.core.devices.DeviceFactory
import com.pedrozc90.prototype.core.devices.DeviceFrequency
import com.pedrozc90.prototype.data.local.PreferencesRepository
import com.pedrozc90.rfid.core.RfidDevice
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.launch

const val TAG = "SettingsViewModel"

class SettingsViewModel(
    private val preferences: PreferencesRepository,
    private val context: Context
) : ViewModel() {

    private var device: RfidDevice? = null

    var uiState by mutableStateOf(SettingsUiState())
        private set

    init {
        viewModelScope.launch {
            preferences.getSettings()
                .filterNotNull()
                .collect { state ->
                    Log.d(TAG, "Loaded settings: $state")
                    device = getDevice(type = state.type)
                    uiState = state.copy(
                        powerMin = device?.minPower ?: 0,
                        powerMax = device?.maxPower ?: 0
                    )
                }
        }
    }

    private fun isValid(state: SettingsUiState): Boolean {
        return with(state) {
            device.isNotBlank() && power >= 0
        }
    }

    fun update(state: SettingsUiState) {
        val changed = state.type != uiState.type
        if (changed) {
            device = getDevice(state.type)
            if (device != null) {
                Log.d(TAG, "Device type changed, new device: ${device!!.name}")
                val powerMin = device!!.minPower
                val powerMax = device!!.maxPower
                val powerValue = if (state.power > powerMax) {
                    powerMax
                } else if (state.power < powerMin) {
                    powerMin
                } else {
                    state.power
                }
                uiState = state.copy(power = powerValue, powerMin = powerMin, powerMax = powerMax)
                return
            }
        }

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

    fun getDevice(type: String): RfidDevice? {
        return try {
            DeviceFactory.build(type = type, context = context)
        } catch (e: Exception) {
            Toast.makeText(context, "Error creating device of type $type", Toast.LENGTH_LONG).show()
            Log.e(TAG, "Error creating device of type $type", e)
            null
        }
    }

}

data class SettingsUiState(
    val device: String = "",        // device MAC address

    val isBuiltIn: Boolean = false,
    val type: String = "",

    val frequency: DeviceFrequency = DeviceFrequency.BRAZIL,

    val power: Int = 0,
    val powerMin: Int = 0,
    val powerMax: Int = 100
)
