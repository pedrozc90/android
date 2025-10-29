package com.pedrozc90.prototype.data.db.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import androidx.room.Update
import com.pedrozc90.prototype.data.db.models.Inventory
import com.pedrozc90.prototype.data.db.models.InventorySummary
import com.pedrozc90.prototype.data.db.models.InventoryWithTags
import kotlinx.coroutines.flow.Flow

@Dao
interface InventoryDao {

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insert(entity: Inventory): Long

    @Update
    suspend fun update(entity: Inventory): Int

    @Delete
    suspend fun delete(entity: Inventory)

    @Query("DELETE FROM tags WHERE id > 0")
    suspend fun deleteAll()

    @Transaction
    @Query(
        """
        SELECT i.*, t.* FROM inventories i
        LEFT JOIN tags t ON t.inventory_id = i.id
        WHERE i.id = :id 
        ORDER BY t.item_reference ASC, t.serial_number ASC
        """
    )
    fun getReadWithTags(id: Long): Flow<InventoryWithTags>

    @Query(
        """
        SELECT 
            i.*,
            COUNT(t.id) AS tags
        FROM inventories i
        LEFT JOIN tags t ON t.inventory_id = i.id
        GROUP BY i.id
        ORDER BY i.id DESC
    """
    )
    fun getAllSummary(): Flow<List<InventorySummary>>

}
