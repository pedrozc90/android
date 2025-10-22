package com.pedrozc90.prototype.data

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import androidx.room.Update
import com.pedrozc90.prototype.data.db.BaseDao
import com.pedrozc90.prototype.data.read.Read
import com.pedrozc90.prototype.data.read.ReadSummary
import com.pedrozc90.prototype.data.read.ReadWithTags
import kotlinx.coroutines.flow.Flow

@Dao
interface ReadDao : BaseDao<Read, Long> {

    @Insert(onConflict = OnConflictStrategy.ABORT)
    override suspend fun insert(entity: Read): Long

    @Update
    override suspend fun update(entity: Read): Int

    @Delete
    override suspend fun delete(entity: Read)

    @Query("DELETE FROM reads")
    suspend fun deleteAll()

//    @Query("DELETE FROM sqlite_sequence WHERE name = 'reads_id_seq'")
//    suspend fun resetAutoIncrement()

    @Query("SELECT r.* FROM reads r ORDER BY r.id DESC")
    fun fetch(): Flow<List<Read>>

    @Transaction
    @Query(
        """
        SELECT * FROM reads r
        LEFT JOIN tags t ON t.read_id = r.id
        WHERE r.id = :id 
        ORDER BY t.item_reference ASC, t.serial_number ASC
        """
    )
    fun getReadWithTags(id: Long): Flow<ReadWithTags>

    @Query(
        """
        SELECT 
            r.*,
            COUNT(t.id) AS tags_count
        FROM reads r
        LEFT JOIN tags t ON t.read_id = r.id
        GROUP BY r.id
        ORDER BY r.id DESC
    """
    )
    fun getSummary(): Flow<List<ReadSummary>>

}
