package com.pedrozc90.prototype.data.web

import com.pedrozc90.prototype.data.web.objects.ProductDto
import java.util.UUID

object FakeDataSource {

    const val token: String = "Sanity Check"

    val products: List<ProductDto> = listOf(
        ProductDto(
            id = 1,
            uuid = UUID.randomUUID(),
            itemReference = "1010101010",
            description = "Product 1",
            size = "GG",
            color = "Red"
        ),
        ProductDto(
            id = 2,
            uuid = UUID.randomUUID(),
            itemReference = "1010101011",
            description = "Product 2",
            size = "G",
            color = "Red"
        ),
        ProductDto(
            id = 3,
            uuid = UUID.randomUUID(),
            itemReference = "1010101012",
            description = "Product 3",
            size = "M",
            color = "Red"
        )
    )

}
