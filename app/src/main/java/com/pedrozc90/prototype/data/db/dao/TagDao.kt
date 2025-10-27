package com.pedrozc90.prototype.data.db.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.pedrozc90.prototype.data.db.models.Tag

@Dao
interface TagDao {

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insert(entity: Tag): Long

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertMany(tags: List<Tag>)

    @Update
    suspend fun update(entity: Tag): Int

    @Delete
    suspend fun delete(entity: Tag)

    @Query("DELETE FROM tags WHERE id > 0")
    suspend fun deleteAll()

    @Query("SELECT 1 FROM tags t WHERE t.rfid = :rfid LIMIT 1")
    suspend fun exists(rfid: String): Int

}
