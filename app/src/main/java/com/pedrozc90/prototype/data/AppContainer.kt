package com.pedrozc90.prototype.data

import android.content.Context
import com.jakewharton.retrofit2.converter.kotlinx.serialization.asConverterFactory
import com.pedrozc90.prototype.network.PrototypeApiService
import kotlinx.serialization.json.Json
import okhttp3.MediaType.Companion.toMediaType
import retrofit2.Retrofit

interface AppContainer {

    val prototypeApiRepository: PrototypeApiRepository

}

class DefaultAppContainer(context: Context) : AppContainer {

    private val baseUrl = "http://localhost:4000/android/"

    /**
     * Use the Retrofit builder to build a retrofit object using a kotlinx.serialization converter
     */
    private val retrofit: Retrofit = Retrofit.Builder()
        .addConverterFactory(Json.asConverterFactory("application/json".toMediaType()))
        .baseUrl(baseUrl)
        .build()

    /**
     * Retrofit service object for creating api calls
     */
    private val retrofitService: PrototypeApiService by lazy {
        retrofit.create(PrototypeApiService::class.java)
    }

    override val prototypeApiRepository: PrototypeApiRepository by lazy {
        DefaultPrototypeApiRepository(retrofitService)
    }

}
