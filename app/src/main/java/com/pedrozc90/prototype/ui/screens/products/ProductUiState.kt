package com.pedrozc90.prototype.ui.screens.products

import com.pedrozc90.prototype.data.db.models.Product

data class ProductDetails(
    val id: Long = 0,
    val itemReference: String = "",
    val description: String? = null,
    val size: String? = null,
    val color: String? = null
)

/**
 * Extension functions to map between ProductDetails and Product entity
 */
fun ProductDetails.toProduct(): Product = Product(
    id = id,
    itemReference = itemReference,
    description = description,
    size = size,
    color = color
)

/**
 * Extension function to map Product entity to ProductDetails
 */
fun Product.toProductDetails(): ProductDetails = ProductDetails(
    id = id,
    itemReference = itemReference,
    description = description,
    size = size,
    color = color
)

/**
 * Extension function to map Product entity to ProductUiState
 */
fun Product.toUiState(isValid: Boolean = false): ProductUiState = ProductUiState(
    isNew = id == 0L,
    isValid = isValid,
    product = this,
    details = this.toProductDetails()
)

data class ProductUiState(
    val isNew: Boolean = true,
    val isValid: Boolean = true,
    val product: Product = Product(),
    val details: ProductDetails = ProductDetails()
)

data class ProductListUiState(
    val list: List<Product> = emptyList()
)
