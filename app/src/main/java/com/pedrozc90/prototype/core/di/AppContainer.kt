package com.pedrozc90.prototype.core.di

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import com.pedrozc90.prototype.data.db.PrototypeDatabase
import com.pedrozc90.prototype.data.local.PreferencesRepository
import com.pedrozc90.prototype.domain.repositories.ProductRepository
import com.pedrozc90.prototype.domain.repositories.TagRepository

interface AppContainer {

    // data store
    val preferences: PreferencesRepository

    // room
    val database: PrototypeDatabase
    val tagRepository: TagRepository
    val productRepository: ProductRepository

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

}
