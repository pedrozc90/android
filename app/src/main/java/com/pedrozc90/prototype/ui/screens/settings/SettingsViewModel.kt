package com.pedrozc90.prototype.ui.screens.settings

import android.content.Context
import android.util.Log
import android.widget.Toast
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.pedrozc90.prototype.core.bluetooth.BluetoothDeviceDto
import com.pedrozc90.prototype.core.bluetooth.BluetoothRepository
import com.pedrozc90.prototype.core.devices.DeviceManager
import com.pedrozc90.prototype.data.local.PreferencesRepository
import com.pedrozc90.prototype.ui.screens.devices.DevicesUiState
import com.pedrozc90.rfid.core.DeviceFrequency
import com.pedrozc90.rfid.core.Options
import com.pedrozc90.rfid.core.RfidDevice
import com.pedrozc90.rfid.helpers.DeviceType
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

const val TAG = "SettingsViewModel"

class SettingsViewModel(
    private val preferences: PreferencesRepository,
    private val bluetooth: BluetoothRepository,
    private val factory: DeviceManager,
    private val context: Context
) : ViewModel() {

    private var device: RfidDevice? = null

    private var _uiState = MutableStateFlow(SettingsUiState())
    val uiState = _uiState.asStateFlow()

    init {
        viewModelScope.launch {
            preferences.getSettings()
                .filterNotNull()
                .collect { value ->
                    Log.d(TAG, "Loaded settings: $value")
                    device = buildRfidDevice(type = value.type)
                    Log.d(TAG, "Built device: $device")
                    _uiState.update {
                        it.copy(
                            isBuiltIn = value.isBuiltIn,
                            type = value.type,
                            macAddress = value.macAddress,
                            frequency = value.frequency,
                            power = value.power,
                            powerMin = device?.minPower ?: 0,
                            powerMax = device?.maxPower ?: 0
                        )
                    }
                }
        }

        viewModelScope.launch {
            bluetooth.pairedDevices.collect { paired ->
                _uiState.update { it.copy(devices = it.devices.copy(paired = paired)) }
            }

            bluetooth.scannedDevices.collect { scanned ->
                _uiState.update { it.copy(devices = it.devices.copy(scanned = scanned)) }
            }
        }
    }

    private fun isValid(state: SettingsUiState): Boolean {
        return with(state) {
            ((type.bluetooth && !macAddress.isNullOrBlank()) || !type.bluetooth) && power >= 0
        }
    }

    fun update(state: SettingsUiState) {
        val changed = state.type != _uiState.value.type
        if (changed) {
            device = buildRfidDevice(state.type)
            if (device != null) {
                Log.d(TAG, "Device type changed, new device: ${device!!.TAG}")
                val powerMin = device!!.minPower
                val powerMax = device!!.maxPower
                val powerValue = if (state.power > powerMax) {
                    powerMax
                } else if (state.power < powerMin) {
                    powerMin
                } else {
                    state.power
                }
                _uiState.update {
                    state.copy(
                        power = powerValue,
                        powerMin = powerMin,
                        powerMax = powerMax
                    )
                }
                return
            }
        }

        _uiState.update { state }
    }

    fun onSave() {
        viewModelScope.launch {
            val state = _uiState.value
            val valid = isValid(state)
            if (valid) {
                preferences.update(state.toDeviceSettings())
            }
        }
    }

    fun buildRfidDevice(type: DeviceType): RfidDevice? {
        return try {
            factory.build(type = type)
        } catch (e: Exception) {
            val message = "'${type.label}' not supported."
            Toast.makeText(context, message, Toast.LENGTH_LONG).show()
            Log.e(TAG, message, e)
            null
        }
    }

    fun testConnection() {
        viewModelScope.launch {
            val state = _uiState.value
            val device = device
            if (device != null) {
                val macAddress = state.macAddress

                val bDevice = if (state.type == DeviceType.CHAFON_BLE && macAddress != null)
                    bluetooth.getDevice(macAddress)
                else null

                val opts = Options(
                    macAddress = state.macAddress,
                    frequency = state.frequency,
                    power = state.power,
                    bDevice = bDevice
                )

                device.init(opts)
            }
        }
    }

    fun checkFrequency(value: DeviceFrequency): Boolean {
        return device?.checkFrequency(value) ?: false
    }

    // Bluetooth scanning
    fun startScan() {
        _uiState.update { it.copy(devices = it.devices.copy(scanning = true)) }
        bluetooth.start()
    }

    fun stopScan() {
        _uiState.update { it.copy(devices = it.devices.copy(scanning = false)) }
        bluetooth.stop()
    }

    fun pairDevice(device: BluetoothDeviceDto) {
        Log.d(TAG, "Pairing device: $device")
        viewModelScope.launch {
            _uiState.update { it.copy(macAddress = device.address) }
            preferences.update(device)
        }
    }

}

data class SettingsUiState(
    val isBuiltIn: Boolean = false,                             // rfid reader is built-in the android device
    val type: DeviceType = DeviceType.FAKE,                     // device type, e.g: Chainway UHF, Chainway BLE, etc.
    val macAddress: String? = null,                             // device MAC address, required for bluetooth devices
    val frequency: DeviceFrequency = DeviceFrequency.BRAZIL,    // rfid reader frequency
    val power: Int = 0,
    val powerMin: Int = 0,
    val powerMax: Int = 1,

    val devices: DevicesUiState = DevicesUiState(),

    val errors: Map<String, String> = emptyMap()
) {
    fun toDeviceSettings(): DeviceSettings {
        return DeviceSettings(
            isBuiltIn = isBuiltIn,
            type = type,
            macAddress = macAddress,
            frequency = frequency,
            power = power
        )
    }

    fun isValid(): Boolean {
        return with(this) {
            ((type.bluetooth && !macAddress.isNullOrBlank()) || (!type.bluetooth)) && power > 0
        }
    }

}

data class DeviceSettings(
    val isBuiltIn: Boolean = false,
    val type: DeviceType = DeviceType.FAKE,
    val macAddress: String? = null,
    val frequency: DeviceFrequency = DeviceFrequency.BRAZIL,
    val power: Int = 1
) {

    fun toRfidOptions(): Options {
        return Options(
            macAddress = macAddress,
            frequency = frequency,
            power = power,
            battery = true
        )
    }

}
