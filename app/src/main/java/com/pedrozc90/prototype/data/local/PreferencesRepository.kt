package com.pedrozc90.prototype.data.local

import android.util.Log
import androidx.datastore.core.DataStore
import androidx.datastore.core.IOException
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.emptyPreferences
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import com.pedrozc90.prototype.core.bluetooth.BluetoothDeviceDto
import com.pedrozc90.prototype.ui.screens.login.LoginUiState
import com.pedrozc90.prototype.ui.screens.settings.SettingsUiState
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map

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
        private val POTENCY = intPreferencesKey("potency")
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
                    value = it[POTENCY] ?: 0
                )
            }
    }

    suspend fun update(state: SettingsUiState) {
        ds.edit { prefs ->
            // prefs[DEVICE] = state.device
            prefs[POTENCY] = state.value
        }
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

}
