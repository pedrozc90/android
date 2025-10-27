package com.pedrozc90.prototype.data.db.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.pedrozc90.prototype.data.db.models.Product
import kotlinx.coroutines.flow.Flow

@Dao
interface ProductDao {

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insert(entity: Product): Long

    @Update
    suspend fun update(entity: Product): Int

    @Delete
    suspend fun delete(entity: Product)

    @Query("SELECT * FROM products p WHERE p.id = :id")
    fun get(id: Long): Flow<Product?>

    @Query("SELECT 1 FROM products p WHERE p.item_reference = :itemReference LIMIT 1")
    fun exists(itemReference: String): Flow<Int>

    @Query("SELECT * FROM products p ORDER BY p.item_reference ASC")
    fun fetch(): Flow<List<Product>>

    @Query("""
        SELECT * FROM products p
        WHERE p.item_reference LIKE :q
        ORDER BY p.item_reference ASC
    """)
    fun fetch(q: String): Flow<List<Product>>

}
