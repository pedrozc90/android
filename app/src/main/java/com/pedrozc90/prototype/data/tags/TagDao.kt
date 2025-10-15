package com.pedrozc90.prototype.data

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.pedrozc90.prototype.data.db.BaseDao

@Dao
interface TagDao : BaseDao<Tag, Long> {

    @Insert(onConflict = OnConflictStrategy.ABORT)
    override suspend fun insert(entity: Tag): Long

    @Insert(onConflict = OnConflictStrategy.ABORT)
    suspend fun insertMany(tags: List<Tag>)

    @Update
    override suspend fun update(entity: Tag): Int

    @Delete
    override suspend fun delete(entity: Tag)

    @Query("DELETE FROM reads")
    suspend fun deleteAll()

    @Query("SELECT 1 FROM tags t WHERE t.rfid = :rfid LIMIT 1")
    suspend fun exists(rfid: String): Int

}
