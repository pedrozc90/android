package com.pedrozc90.prototype.domain.repositories

import com.pedrozc90.prototype.data.db.dao.ProductDao
import com.pedrozc90.prototype.data.db.models.Product
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

interface ProductBaseRepository {

    suspend fun insert(product: Product): Long

    suspend fun update(product: Product): Product

    suspend fun delete(product: Product)

    fun get(id: Long): Flow<Product?>

    fun exists(itemReference: String): Flow<Boolean>

    fun fetch(q: String? = null): Flow<List<Product>>

}

class ProductRepository(private val dao: ProductDao) : ProductBaseRepository {

    override suspend fun insert(product: Product) = dao.insert(product)

    override suspend fun update(product: Product): Product {
        val tmp = product.copy(
            updatedAt = System.currentTimeMillis(),
            version = product.version + 1
        )
        val updated = dao.update(tmp)
        return tmp
    }

    override suspend fun delete(product: Product) = dao.delete(product)

    override fun get(id: Long): Flow<Product?> = dao.get(id)

    override fun exists(itemReference: String): Flow<Boolean> =
        dao.exists(itemReference).map { it != 0 }

    override fun fetch(q: String?): Flow<List<Product>> {
        if (q?.isNotBlank() == true) {
            return dao.fetch(q = "%${q}%")
        }
        return dao.fetch()
    }

}
