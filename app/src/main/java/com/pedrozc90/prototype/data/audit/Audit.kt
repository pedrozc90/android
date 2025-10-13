package com.pedrozc90.prototype.data.audit

import androidx.room.ColumnInfo

abstract class Audit(
    @ColumnInfo(name = "inserted_at") var insertedAt: Long = System.currentTimeMillis(),
    @ColumnInfo(name = "updated_at") var updatedAt: Long = System.currentTimeMillis(),
)
