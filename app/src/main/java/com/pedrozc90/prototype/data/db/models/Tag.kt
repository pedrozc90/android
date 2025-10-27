package com.pedrozc90.prototype.data.db.models

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey
import java.util.UUID

@Entity(
    tableName = "tags",
    indices = [
        Index(name = "tags_uuid_uk", value = ["uuid"], unique = true)
    ]
)
data class Tag(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,

    @ColumnInfo(name = "uuid")
    val uuid: UUID = UUID.randomUUID(),

    @ColumnInfo(name = "inserted_at")
    val insertedAt: Long = System.currentTimeMillis(),

    @ColumnInfo(name = "updated_at")
    val updatedAt: Long = System.currentTimeMillis(),

    @ColumnInfo(name = "version")
    val version: Int = 1,

    @ColumnInfo(name = "rfid")
    val rfid: String,

    @ColumnInfo(name = "item_reference")
    val itemReference: String,

    @ColumnInfo(name = "serial_number")
    val serialNumber: Long,
)