package com.pedrozc90.prototype.data

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update

@Dao
interface TagDao {

    @Insert(onConflict = OnConflictStrategy.ABORT)
    suspend fun insert(tag: Tag)

    @Insert(onConflict = OnConflictStrategy.ABORT)
    suspend fun insertMany(tags: List<Tag>)

    @Update
    suspend fun update(tag: Tag)

    @Delete
    suspend fun delete(tag: Tag)

    @Query("SELECT 1 FROM tags t WHERE t.rfid = :rfid LIMIT 1")
    suspend fun exists(rfid: String): Int

}
