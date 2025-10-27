package com.pedrozc90.prototype.data.web

import com.pedrozc90.prototype.data.web.objects.LoginRequest
import com.pedrozc90.prototype.data.web.objects.LoginResponse
import com.pedrozc90.prototype.data.web.objects.ProductsResponse
import java.time.Instant
import java.time.format.DateTimeFormatter

class FakeApiService : ApiService {

    private val formatter = DateTimeFormatter.ISO_INSTANT

    override suspend fun login(req: LoginRequest): LoginResponse {
        return LoginResponse(
            token = FakeDataSource.token,
            expiresAt = formatter.format(Instant.now())
        )
    }

    override suspend fun getProducts(): ProductsResponse {
        return ProductsResponse(products = FakeDataSource.products)
    }

}
