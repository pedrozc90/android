package com.pedrozc90.prototype.data.db.models

import androidx.room.ColumnInfo
import androidx.room.Embedded
import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey
import androidx.room.Relation
import java.util.UUID

@Entity(
    tableName = "inventories",
    indices = [
        Index(name = "inventories_uuid_uk", value = ["uuid"], unique = true)
    ]
)
data class Inventory(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,

    @ColumnInfo(name = "uuid")
    val uuid: UUID = UUID.randomUUID(),

    @ColumnInfo(name = "inserted_at")
    val insertedAt: Long = System.currentTimeMillis(),

    @ColumnInfo(name = "updated_at")
    val updatedAt: Long = System.currentTimeMillis(),

    @ColumnInfo(name = "version")
    val version: Int = 1
)

data class InventoryWithTags(
    @Embedded
    val inventory: Inventory,

    @Relation(parentColumn = "id", entityColumn = "inventory_id")
    val tags: List<Tag>
)

data class InventorySummary(
    @Embedded
    val inventory: Inventory,

    @ColumnInfo(name = "tags")
    val tags: Int
)
