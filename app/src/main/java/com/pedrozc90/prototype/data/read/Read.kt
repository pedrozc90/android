package com.pedrozc90.prototype.data.read

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import com.pedrozc90.prototype.data.audit.Audit

@Entity(tableName = "reads")
class Read(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    @ColumnInfo(name = "tags") val tags: String
) : Audit()
