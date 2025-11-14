package com.pedrozc90.prototype.data.local

import android.util.Log
import androidx.datastore.core.DataStore
import androidx.datastore.core.IOException
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.emptyPreferences
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import com.pedrozc90.prototype.core.bluetooth.BluetoothDeviceDto
import com.pedrozc90.prototype.core.devices.DeviceDetector
import com.pedrozc90.prototype.core.devices.DeviceFrequency
import com.pedrozc90.prototype.ui.screens.login.LoginUiState
import com.pedrozc90.prototype.ui.screens.settings.SettingsUiState
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

private const val TAG = "PreferencesRepository"

class PreferencesRepository(
    private val ds: DataStore<Preferences>
) {

    companion object {
        // Login
        private val TOKEN = stringPreferencesKey("token")
        private val USERNAME = stringPreferencesKey("username")
        private val PASSWORD = stringPreferencesKey("password")

        // Settings
        private val DEVICE = stringPreferencesKey("device")
        private val FREQUENCY = stringPreferencesKey("frequency")
        private val POWER = intPreferencesKey("power")

        // BUILT-IN DEVICE INFO
        private val DEVICE_BUILT_IN = booleanPreferencesKey("device_built_in")
        private val DEVICE_TYPE = stringPreferencesKey("device_type")

        private val DEVICE_ID = stringPreferencesKey("device_id")
        private val DEVICE_MODEL = stringPreferencesKey("device_model")
        private val DEVICE_MANUFACTURER = stringPreferencesKey("device_manufacturer")
        private val DEVICE_PRODUCT = stringPreferencesKey("device_product")
        private val DEVICE_DEVICE = stringPreferencesKey("device_device")
        private val DEVICE_BOARD = stringPreferencesKey("device_board")
        private val DEVICE_HARDWARE = stringPreferencesKey("device_hardware")
        private val DEVICE_SERIAL = stringPreferencesKey("device_serial")
    }

    private val _scope = CoroutineScope(SupervisorJob() + Dispatchers.Main.immediate)

    // Backing flow from DataStore
    private val _persistedFlow: Flow<String> = ds.data
        .catch { emit(emptyPreferences()) }
        .map { prefs -> prefs[DEVICE_TYPE] ?: "none" }

    // In-memory state for synchronous access (default null until loaded)
    private val _state = MutableStateFlow("")
    val state: StateFlow<String> = _state.asStateFlow()

    init {
        // Keep the in-memory value in sync with DataStore
        _scope.launch {
            _persistedFlow.collect { value ->
                _state.value = value
            }
        }
    }

    // Login
    fun getLoginUiState(): Flow<LoginUiState> {
        return ds.data
            .catch {
                if (it is IOException) {
                    Log.d(TAG, "Error reading login preferences", it)
                    emit(emptyPreferences())
                } else {
                    throw it
                }
            }
            .map {
                LoginUiState(
                    username = it[USERNAME] ?: "",
                    password = it[PASSWORD] ?: "",
                    token = it[TOKEN],
                    isValid = true
                )
            }
    }

    suspend fun update(state: LoginUiState) {
        ds.edit { prefs ->
            prefs[USERNAME] = state.username
            prefs[PASSWORD] = state.password
            state.token?.let { token ->
                prefs[TOKEN] = token
            }
        }
    }

    // Settings
    fun getSettings(): Flow<SettingsUiState> {
        return ds.data
            .catch {
                if (it is IOException) {
                    Log.d(TAG, "Error reading settings preferences", it)
                    emit(emptyPreferences())
                } else {
                    throw it
                }
            }
            .map {
                SettingsUiState(
                    device = it[DEVICE] ?: "None",
                    isBuiltIn = it[DEVICE_BUILT_IN] == true,
                    type = it[DEVICE_TYPE] ?: "None",
                    frequency = DeviceFrequency.fromName(it[FREQUENCY]),
                    power = it[POWER] ?: 0
                )
            }
    }

    suspend fun update(state: SettingsUiState) {
        ds.edit { prefs ->
            // prefs[DEVICE] = state.device
            prefs[FREQUENCY] = state.frequency.key
            prefs[POWER] = state.power
            prefs[DEVICE_TYPE] = state.type
        }
        // TODO: REVIEW
        // _state.value = state.type
    }

    // Devices
    suspend fun update(device: BluetoothDeviceDto) {
        ds.edit { it[DEVICE] = device.address }
    }

    suspend fun getDevice(): String {
        return ds.data
            .map { it[DEVICE] ?: "None" }
            .first()
    }

    suspend fun setBuiltInDevice(detected: DeviceDetector.Result) {
        val isBuiltIn = detected.isBuiltInDevice
        val api = detected.detectedApi ?: "none"
        val device = detected.device
        ds.edit { prefs ->
            prefs[DEVICE_BUILT_IN] = isBuiltIn
            prefs[DEVICE_TYPE] = api
            prefs[DEVICE_ID] = device.id ?: ""
            prefs[DEVICE_MODEL] = device.model ?: ""
            prefs[DEVICE_MANUFACTURER] = device.manufacturer ?: ""
            prefs[DEVICE_PRODUCT] = device.product ?: ""
            prefs[DEVICE_DEVICE] = device.device ?: ""
            prefs[DEVICE_BOARD] = device.board ?: ""
            prefs[DEVICE_HARDWARE] = device.hardware ?: ""
            prefs[DEVICE_SERIAL] = device.serial ?: ""
        }
        _state.update { api }
    }

    fun getDeviceType(): String {
        return _state.value
    }

}
