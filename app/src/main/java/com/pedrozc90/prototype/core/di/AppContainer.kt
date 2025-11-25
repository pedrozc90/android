package com.pedrozc90.prototype.core.di

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import com.jakewharton.retrofit2.converter.kotlinx.serialization.asConverterFactory
import com.pedrozc90.prototype.core.bluetooth.BluetoothRepository
import com.pedrozc90.prototype.core.bluetooth.DefaultBluetoothRepository
import com.pedrozc90.prototype.core.devices.DeviceDetector
import com.pedrozc90.prototype.core.devices.DeviceManager
import com.pedrozc90.prototype.data.db.PrototypeDatabase
import com.pedrozc90.prototype.data.local.PreferencesRepository
import com.pedrozc90.prototype.data.web.ApiRepository
import com.pedrozc90.prototype.data.web.ApiService
import com.pedrozc90.prototype.data.web.RemoteRepository
import com.pedrozc90.prototype.data.web.providers.RetrofitProvider
import com.pedrozc90.prototype.domain.repositories.InventoryRepository
import com.pedrozc90.prototype.domain.repositories.ProductRepository
import com.pedrozc90.prototype.domain.repositories.TagRepository
import kotlinx.serialization.json.Json
import okhttp3.MediaType.Companion.toMediaType
import retrofit2.Retrofit

interface AppContainer {

    // data store
    val preferences: PreferencesRepository

    // room
    val database: PrototypeDatabase
    val tagRepository: TagRepository
    val productRepository: ProductRepository
    val inventoryRepository: InventoryRepository

    // retrofit
    val remote: ApiRepository

    // bluetooth
    val bluetooth: BluetoothRepository

    // detector
    val detector: DeviceDetector

    val manager: DeviceManager
}

class DefaultAppContainer(context: Context, dataStore: DataStore<Preferences>) : AppContainer {

    // data store
    override val preferences: PreferencesRepository by lazy {
        PreferencesRepository(ds = dataStore)
    }

    // room
    override val database: PrototypeDatabase by lazy {
        PrototypeDatabase.getInstance(context)
    }

    override val tagRepository: TagRepository by lazy {
        TagRepository(dao = database.tagDao())
    }

    override val productRepository: ProductRepository by lazy {
        ProductRepository(dao = database.productDao())
    }

    override val inventoryRepository: InventoryRepository by lazy {
        InventoryRepository(dao = database.inventoryDao())
    }

    // retrofit
    // Use 10.0.2.2 on the Android emulator to reach the machine hosting the server
    // Android do not like 'localhost' or '127.0.0.1'
    private val baseUrl: String = "http://10.0.2.2:4100/"

    private val retrofit: Retrofit = RetrofitProvider.create(baseUrl, preferences)

    private val service: ApiService by lazy {
        retrofit.create(ApiService::class.java)
    }

    override val remote: ApiRepository by lazy {
        RemoteRepository(service = service)
    }

    // bluetooth
    override val bluetooth: BluetoothRepository by lazy {
        DefaultBluetoothRepository(context)
    }

    // detector
    override val detector: DeviceDetector by lazy {
        DeviceDetector(preferences = preferences)
    }

    // devices
    override val manager: DeviceManager by lazy {
        DeviceManager(context = context)
    }

}
