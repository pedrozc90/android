package com.pedrozc90.prototype.data.web.objects

import com.pedrozc90.prototype.core.utils.UUIDSerializer
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import java.util.UUID

@Serializable
data class ProductDto(
    @SerialName(value = "id")
    val id: Long,

    @Serializable(with = UUIDSerializer::class)
    @SerialName(value = "uuid")
    val uuid: UUID,

    @SerialName(value = "item_reference")
    val itemReference: String,

    @SerialName(value = "description")
    val description: String? = null,

    @SerialName(value = "size")
    val size: String? = null,

    @SerialName(value = "color")
    val color: String? = null,
)

@Serializable
data class ProductsResponse(
    @SerialName(value = "products")
    val products: List<ProductDto>
)
