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
import com.pedrozc90.prototype.ui.screens.login.LoginUiState
import com.pedrozc90.prototype.ui.screens.settings.DeviceSettings
import com.pedrozc90.rfid.core.DeviceFrequency
import com.pedrozc90.rfid.helpers.DeviceDetector
import com.pedrozc90.rfid.helpers.DeviceType
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
        private val DEVICE_MAC_ADDRESS = stringPreferencesKey("device_mac_address")
        private val FREQUENCY = stringPreferencesKey("frequency")
        private val POWER = intPreferencesKey("power")

        // BUILT-IN DEVICE INFO
        private val DEVICE_TYPE = stringPreferencesKey("device_type")
        private val DEVICE_BUILT_IN = booleanPreferencesKey("device_built_in")

        private val DEVICE_ID = stringPreferencesKey("device_id")
        private val DEVICE_MODEL = stringPreferencesKey("device_model")
        private val DEVICE_MANUFACTURER = stringPreferencesKey("device_manufacturer")
        private val DEVICE_PRODUCT = stringPreferencesKey("device_product")
        private val DEVICE_DEVICE = stringPreferencesKey("device_device")
        private val DEVICE_BOARD = stringPreferencesKey("device_board")
        private val DEVICE_HARDWARE = stringPreferencesKey("device_hardware")
        private val DEVICE_SERIAL = stringPreferencesKey("device_serial")
    }

    private val _scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

    // Backing flow from DataStore
    private val _persistedFlow: Flow<State> = ds.data
        .catch { emit(emptyPreferences()) }
        .map { prefs ->
            State(
                token = prefs[TOKEN],
                type = DeviceType.of(prefs[DEVICE_TYPE])
            )
        }

    // In-memory state for synchronous access (default null until loaded)
    private val _state = MutableStateFlow<State>(State())
    val state: StateFlow<State> = _state.asStateFlow()

    init {
        this.init()
    }

    private fun init() {
        // Keep the in-memory value in sync with DataStore
        _scope.launch {
            _persistedFlow.collect { value ->
                _state.value = value
            }
        }
    }

    // Login
    val token: String?
        get() = state.value.token

    suspend fun tokenAsync(): String? {
        return ds.data
            .catch {
                Log.e(TAG, "Error while reading 'token' from data store")
                emit(emptyPreferences())
            }
            .map { it[TOKEN] }
            .first()
    }

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
                    token = it[TOKEN]
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
        _state.update { it.copy(token = token) }
    }

    suspend fun clearToken() {
        ds.edit { prefs ->
            prefs.remove(TOKEN)
            prefs.remove(USERNAME)
            prefs.remove(PASSWORD)
        }
        _state.update { it.copy(token = null) }
    }

    // Settings
    fun getSettings(): Flow<DeviceSettings> {
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
                val type = DeviceType.of(it[DEVICE_TYPE])
                val frequency = DeviceFrequency.of(it[FREQUENCY])
                val power = it[POWER] ?: 0
                DeviceSettings(
                    isBuiltIn = it[DEVICE_BUILT_IN] == true,
                    type = type,
                    macAddress = it[DEVICE_MAC_ADDRESS],
                    frequency = frequency,
                    power = power
                )
            }
    }

    suspend fun update(state: DeviceSettings) {
        ds.edit { prefs ->
            prefs[FREQUENCY] = state.frequency.name
            prefs[POWER] = state.power
            prefs[DEVICE_TYPE] = state.type.name
        }
        _state.update { it.copy(type = state.type) }
    }

    // Devices
    val deviceType: DeviceType?
        get() = state.value.type

    suspend fun getDevice(): String {
        return ds.data
            .map { it[DEVICE_MAC_ADDRESS] ?: "None" }
            .first()
    }

    suspend fun update(device: BluetoothDeviceDto) {
        ds.edit { it[DEVICE_MAC_ADDRESS] = device.address }
    }

    suspend fun setBuiltInDevice(detected: DeviceDetector.Result) {
        val isBuiltIn = detected.builtIn
        val type = detected.type
        val device = detected.device
        ds.edit { prefs ->
            prefs[DEVICE_BUILT_IN] = isBuiltIn
            prefs[DEVICE_TYPE] = type?.name ?: ""
            prefs[DEVICE_ID] = device.id ?: ""
            prefs[DEVICE_MODEL] = device.model ?: ""
            prefs[DEVICE_MANUFACTURER] = device.manufacturer ?: ""
            prefs[DEVICE_PRODUCT] = device.product ?: ""
            prefs[DEVICE_DEVICE] = device.device ?: ""
            prefs[DEVICE_BOARD] = device.board ?: ""
            prefs[DEVICE_HARDWARE] = device.hardware ?: ""
            prefs[DEVICE_SERIAL] = device.serial ?: ""
        }
        _state.update { it.copy(type = type) }
    }

    data class State(
        val token: String? = null,
        val type: DeviceType? = null
    )

}
