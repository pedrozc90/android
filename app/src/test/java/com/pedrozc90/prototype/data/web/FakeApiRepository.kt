package com.pedrozc90.prototype.data.web

import com.pedrozc90.prototype.data.web.objects.LoginRequest
import com.pedrozc90.prototype.data.web.objects.ProductsResponse

class FakeApiRepository : ApiRepository {

    private val service: FakeApiService = FakeApiService()

    override suspend fun login(username: String, password: String) = service.login(
        LoginRequest(
            username = username,
            password = password
        )
    )

    override suspend fun getProducts(): ProductsResponse = service.getProducts()

}
