package com.pedrozc90.prototype.data.local

import android.util.Log
import androidx.datastore.core.DataStore
import androidx.datastore.core.IOException
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.emptyPreferences
import androidx.datastore.preferences.core.floatPreferencesKey
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.map

private const val TAG = "PreferencesRepository"

class PreferencesRepository(
    private val ds: DataStore<Preferences>
) {

    companion object {
        private val POTENCY = floatPreferencesKey("potency")
    }

    fun getPotency(): Flow<Float> {
        return ds.data
            .catch {
                if (it is IOException) {
                    Log.d(TAG, "Error reading potency preference", it)
                    emit(emptyPreferences())
                } else {
                    throw it
                }
            }
            .map { it[POTENCY] ?: 0f }
    }

    suspend fun setPotency(value: Float) = ds.edit { it[POTENCY] = value }

}
