package com.pedrozc90.prototype.data.db

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.pedrozc90.prototype.data.ReadDao
import com.pedrozc90.prototype.data.Tag
import com.pedrozc90.prototype.data.TagDao
import com.pedrozc90.prototype.data.read.Read

@Database(entities = [Tag::class, Read::class], version = 1, exportSchema = true)
abstract class PrototypeDatabase : RoomDatabase() {

    abstract fun tagDao(): TagDao

    abstract fun readDao(): ReadDao

    companion object {

        @Volatile
        private var instance: PrototypeDatabase? = null

        fun getInstance(context: Context): PrototypeDatabase {
            return instance ?: synchronized(this) {
                Room.databaseBuilder(context,PrototypeDatabase::class.java,"prototype.db")
                    .build()
                    .also { instance = it }
            }
        }

    }

}
