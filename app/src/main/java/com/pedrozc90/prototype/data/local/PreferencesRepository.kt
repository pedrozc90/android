package com.pedrozc90.prototype.data.local

import android.util.Log
import androidx.datastore.core.DataStore
import androidx.datastore.core.IOException
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.emptyPreferences
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import com.pedrozc90.prototype.ui.screens.settings.SettingsUiState
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.map

private const val TAG = "PreferencesRepository"

class PreferencesRepository(
    private val ds: DataStore<Preferences>
) {

    companion object {
        private val DEVICE = stringPreferencesKey("device")
        private val POTENCY = intPreferencesKey("potency")
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
                    device = it[DEVICE] ?: "",
                    value = it[POTENCY] ?: 0
                )
            }
    }

    suspend fun update(state: SettingsUiState) {
        ds.edit { prefs ->
            prefs[DEVICE] = state.device
            prefs[POTENCY] = state.value
        }
    }

}
