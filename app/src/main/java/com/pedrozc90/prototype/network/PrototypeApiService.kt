package com.pedrozc90.prototype.network

import retrofit2.http.GET

interface PrototypeApiService {

    @GET("prototype")
    suspend fun get(): List<String>

}
