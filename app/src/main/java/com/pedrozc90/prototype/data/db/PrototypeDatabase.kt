package com.pedrozc90.prototype.data.db

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.pedrozc90.prototype.data.db.dao.ProductDao
import com.pedrozc90.prototype.data.db.dao.TagDao
import com.pedrozc90.prototype.data.db.models.Product
import com.pedrozc90.prototype.data.db.models.Tag

@Database(
    entities = [Tag::class, Product::class],
    version = 1,
    exportSchema = true
)
abstract class PrototypeDatabase : RoomDatabase() {

    abstract fun tagDao(): TagDao

    abstract fun productDao(): ProductDao

    companion object {

        @Volatile
        private var instance: PrototypeDatabase? = null

        fun getInstance(context: Context): PrototypeDatabase {
            return instance ?: synchronized(this) {
                Room.databaseBuilder(context, PrototypeDatabase::class.java, "prototype.db")
                    .build()
                    .also { instance = it }
            }
        }

    }

}
