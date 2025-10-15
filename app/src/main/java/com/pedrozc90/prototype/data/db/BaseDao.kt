package com.pedrozc90.prototype.data.db

interface BaseDao<T, ID> {

    suspend fun insert(entity: T): ID

    suspend fun update(entity: T): Int

    suspend fun delete(entity: T)

}
