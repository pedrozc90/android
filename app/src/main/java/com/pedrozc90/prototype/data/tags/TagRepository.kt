package com.pedrozc90.prototype.data

import com.pedrozc90.prototype.data.ReadRepository
import com.pedrozc90.prototype.data.db.PrototypeDatabase

interface TagBaseRepository {

    suspend fun insert(tag: Tag): Long

    suspend fun insertMany(tags: List<Tag>, size: Int = 1_000)

    suspend fun update(tag: Tag): Tag

    suspend fun delete(tag: Tag)

    suspend fun deleteAll()

    suspend fun exists(rfid: String): Boolean

}

class TagRepository(private val dao: TagDao) : TagBaseRepository {

    constructor(db: PrototypeDatabase) : this(db.tagDao())

    override suspend fun insert(tag: Tag) = dao.insert(tag)

    override suspend fun insertMany(tags: List<Tag>, size: Int) {
        tags.chunked(size = size).forEach { chunk ->
            dao.insertMany(chunk)
        }
    }

    override suspend fun update(tag: Tag): Tag {
        val tmp = tag.copy(
            updatedAt = System.currentTimeMillis(),
            version = tag.version + 1
        )
        val updated = dao.update(tmp)
        return tmp
    }

    override suspend fun delete(tag: Tag)  = dao.delete(tag)

    override suspend fun deleteAll() = dao.deleteAll()

    override suspend fun exists(rfid: String): Boolean {
        return dao.exists(rfid) != 0
    }

}
