package com.pedrozc90.prototype.data.web

import com.pedrozc90.prototype.data.web.objects.LoginRequest
import com.pedrozc90.prototype.data.web.objects.LoginResponse
import com.pedrozc90.prototype.data.web.objects.ProductsResponse
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST

interface ApiService {

    @POST("login")
    suspend fun login(@Body req: LoginRequest): LoginResponse

    @GET("products")
    suspend fun getProducts(): ProductsResponse

}

