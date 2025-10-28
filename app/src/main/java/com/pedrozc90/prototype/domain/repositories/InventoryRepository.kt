package com.pedrozc90.prototype.domain.repositories

import com.pedrozc90.prototype.data.db.dao.InventoryDao
import com.pedrozc90.prototype.data.db.models.Inventory
import com.pedrozc90.prototype.data.db.models.InventorySummary
import com.pedrozc90.prototype.data.db.models.InventoryWithTags
import kotlinx.coroutines.flow.Flow

interface InventoryBaseRepository {

    suspend fun insert(entity: Inventory): Long

    suspend fun update(entity: Inventory): Inventory

    suspend fun delete(entity: Inventory)

    suspend fun deleteAll()

    fun getReadWithTags(id: Long): Flow<InventoryWithTags>

    fun getAllSummary(): Flow<List<InventorySummary>>

}

class InventoryRepository(private val dao: InventoryDao) : InventoryBaseRepository {

    override suspend fun insert(entity: Inventory) = dao.insert(entity)

    override suspend fun update(entity: Inventory): Inventory {
        val tmp = entity.copy(
            updatedAt = System.currentTimeMillis(),
            version = entity.version + 1
        )
        val updated = dao.update(tmp)
        return tmp
    }

    override suspend fun delete(entity: Inventory) = dao.delete(entity)

    override suspend fun deleteAll() = dao.deleteAll()

    override fun getReadWithTags(id: Long): Flow<InventoryWithTags> = dao.getReadWithTags(id)

    override fun getAllSummary(): Flow<List<InventorySummary>> = dao.getAllSummary()

}
