package com.pedrozc90.prototype.data.read

import androidx.room.ColumnInfo
import androidx.room.Embedded
import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey
import androidx.room.Relation
import com.pedrozc90.prototype.data.Tag
import java.util.UUID

@Entity(
    tableName = "reads",
    indices = [
        Index(name = "reads_uuid_uk", value = [ "uuid" ], unique = true)
    ]
)
data class Read(
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

data class ReadWithTags(
    @Embedded
    val read: Read,

    @Relation(parentColumn = "id", entityColumn = "read_id")
    val tags: List<Tag>
)

data class ReadSummary(
    @Embedded
    val read: Read,

    @ColumnInfo(name = "tags_count")
    val tagsCount: Int = 0
)
