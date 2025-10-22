package com.pedrozc90.prototype.data

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey
import com.pedrozc90.prototype.data.read.Read
import java.util.UUID

@Entity(
    tableName = "tags",
    foreignKeys = [
        ForeignKey(
            entity = Read::class,
            parentColumns = ["id"],
            childColumns = ["read_id"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [
        Index(name = "tags_read_id_idx", value = [ "read_id" ]),
        Index(name = "tags_uuid_uk", value = [ "uuid" ], unique = true),
        Index(name = "reads_rfid_read_id_uk", value = [ "rfid", "read_id" ], unique = true)
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

    @ColumnInfo(name = "read_id")
    val readId: Long? = null
)
