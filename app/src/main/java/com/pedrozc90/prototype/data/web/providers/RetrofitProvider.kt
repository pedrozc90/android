package com.pedrozc90.prototype.data.web.providers

import com.jakewharton.retrofit2.converter.kotlinx.serialization.asConverterFactory
import com.pedrozc90.prototype.data.local.PreferencesRepository
import com.pedrozc90.prototype.data.web.interceptors.AuthorizationInterceptor
import kotlinx.serialization.json.Json
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import retrofit2.Retrofit
import java.util.concurrent.TimeUnit

object RetrofitProvider {

    fun create(baseUrl: String, preferences: PreferencesRepository): Retrofit {
        val authorizationInterceptor = AuthorizationInterceptor(preferences = preferences)

        val client = OkHttpClient.Builder()
            .addInterceptor(authorizationInterceptor)
            .connectTimeout(30, TimeUnit.SECONDS)
            .readTimeout(30, TimeUnit.SECONDS)
            .build()

        return Retrofit.Builder()
            .baseUrl(baseUrl)
            .client(client)
            .addConverterFactory(Json.asConverterFactory("application/json".toMediaType()))
            .build()
    }

}
