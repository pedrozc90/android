package com.pedrozc90.prototype.data

import com.pedrozc90.prototype.data.db.PrototypeDatabase
import com.pedrozc90.prototype.data.read.Read
import com.pedrozc90.prototype.data.read.ReadWithTags
import kotlinx.coroutines.flow.Flow

interface ReadBaseRepository {

    suspend fun insert(read: Read): Long

    suspend fun update(read: Read): Read

    suspend fun delete(read: Read)

    suspend fun deleteAll()

    suspend fun fetch(): Flow<List<Read>>

    suspend fun getReadWithTags(id: Long): Flow<ReadWithTags>

}

class ReadRepository(private val dao: ReadDao) : ReadBaseRepository {

    constructor(db: PrototypeDatabase) : this(db.readDao())

    override suspend fun insert(read: Read) = dao.insert(read)

    override suspend fun update(read: Read): Read {
        val tmp = read.copy(
            updatedAt = System.currentTimeMillis(),
            version = read.version + 1
        )
        val updated = dao.update(tmp)
        return tmp
    }

    override suspend fun delete(read: Read) = dao.delete(read)

    override suspend fun deleteAll() = dao.deleteAll()

    override suspend fun fetch(): Flow<List<Read>> = dao.fetch()

    override suspend fun getReadWithTags(id: Long): Flow<ReadWithTags> = dao.getReadWithTags(id)

}
