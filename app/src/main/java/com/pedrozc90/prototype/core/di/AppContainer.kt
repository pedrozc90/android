package com.pedrozc90.prototype.core.di

import android.content.Context
import com.pedrozc90.prototype.data.db.PrototypeDatabase
import com.pedrozc90.prototype.domain.repositories.ProductRepository
import com.pedrozc90.prototype.domain.repositories.TagRepository

interface AppContainer {

    // room
    val database: PrototypeDatabase
    val tagRepository: TagRepository
    val productRepository: ProductRepository

}

class DefaultAppContainer(context: Context) : AppContainer {

    // room
    override val database: PrototypeDatabase by lazy {
        PrototypeDatabase.getInstance(context)
    }

    override val tagRepository: TagRepository by lazy {
        TagRepository(database.tagDao())
    }

    override val productRepository: ProductRepository by lazy {
        ProductRepository(database.productDao())
    }

}
