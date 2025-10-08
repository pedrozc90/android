package com.pedrozc90.prototype.data

interface TagBaseRepository {

    suspend fun insert(tag: Tag)

    suspend fun insertMany(tags: List<Tag>, size: Int = 1_000)

    suspend fun update(tag: Tag)

    suspend fun delete(tag: Tag)

    suspend fun exists(rfid: String): Boolean

}

class TagRepository(private val dao: TagDao) : TagBaseRepository {

    override suspend fun insert(tag: Tag) = dao.insert(tag)

    override suspend fun insertMany(tags: List<Tag>, size: Int) {
        tags.chunked(size = size).forEach { chunk ->
            dao.insertMany(chunk)
        }
    }

    override suspend fun update(tag: Tag) = dao.update(tag)

    override suspend fun delete(tag: Tag)  = dao.delete(tag)

    override suspend fun exists(rfid: String): Boolean {
        return dao.exists(rfid) != 0
    }

}
