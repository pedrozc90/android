package com.pedrozc90.prototype.data

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Update
import com.pedrozc90.prototype.data.read.Read

@Dao
interface ReadDao {

    @Insert(onConflict = OnConflictStrategy.ABORT)
    suspend fun insert(read: Read)

    @Update
    suspend fun update(read: Read)

    @Delete
    suspend fun delete(read: Read)

}
