package com.pedrozc90.prototype.data

import com.pedrozc90.prototype.data.read.Read

interface ReadBaseRepository {

    suspend fun insert(read: Read)

    suspend fun update(read: Read)

    suspend fun delete(read: Read)

}

class ReadRepository(private val dao: ReadDao) : ReadBaseRepository {

    override suspend fun insert(read: Read) = dao.insert(read)

    override suspend fun update(read: Read) = dao.update(read)

    override suspend fun delete(read: Read) = dao.delete(read)

}
