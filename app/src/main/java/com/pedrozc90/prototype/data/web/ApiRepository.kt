package com.pedrozc90.prototype.data.web

import com.pedrozc90.prototype.data.web.objects.LoginRequest
import com.pedrozc90.prototype.data.web.objects.LoginResponse
import com.pedrozc90.prototype.data.web.objects.ProductsResponse

interface ApiRepository {

    suspend fun login(username: String, password: String): LoginResponse

    suspend fun getProducts(): ProductsResponse

}

class RemoteRepository(
    private val service: ApiService
) : ApiRepository {

    override suspend fun login(username: String, password: String): LoginResponse {
        val req = LoginRequest(
            username = username,
            password = password
        )
        return service.login(req)
    }

    override suspend fun getProducts(): ProductsResponse = service.getProducts()

}
