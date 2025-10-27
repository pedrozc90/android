package com.pedrozc90.prototype

import android.app.Application
import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.preferencesDataStore
import com.pedrozc90.prototype.core.di.AppContainer
import com.pedrozc90.prototype.core.di.DefaultAppContainer

private const val PREFERENCE_NAME = "prototype_preferences"

private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = PREFERENCE_NAME)

class PrototypeApplication : Application() {

    lateinit var container: AppContainer
    override fun onCreate() {
        super.onCreate()
        container = DefaultAppContainer(this, dataStore)
    }

}
