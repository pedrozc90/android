package com.pedrozc90.prototype.data.tags

import android.content.Context
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.pedrozc90.prototype.data.Tag
import com.pedrozc90.prototype.data.TagRepository
import com.pedrozc90.prototype.data.db.PrototypeDatabase
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import kotlin.system.measureTimeMillis

@RunWith(AndroidJUnit4::class)
class TagRepositoryTest {

    private lateinit var database: PrototypeDatabase
    private lateinit var repository: TagRepository

    @Before
    fun setUp() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        database = Room.inMemoryDatabaseBuilder(context, PrototypeDatabase::class.java)
            .allowMainThreadQueries()
            .build()
        repository = TagRepository(database.tagDao())
    }

    @After
    @Throws(Exception::class)
    fun cleanUp() {
        database.close()
    }

    @Test
    fun insertMany_Profiler() = runBlocking {
        val tags = (1..100_000).map { Tag(rfid = "RFID_$it") }
        val time = measureTimeMillis {
            repository.insertMany(tags, size = 10_000)
        }
        println("Inserted ${tags.size} tags in $time ms")
    }

    @Test
    fun insertOne_Profiler() = runBlocking {
        val tags = (1..100_000).map { Tag(rfid = "RFID_$it") }
        val time = measureTimeMillis {
            tags.forEach { tag ->
                repository.insert(tag)
            }
        }
        println("Inserted ${tags.size} tags in $time ms")
    }

    @Test
    fun exists() = runBlocking {
        val tag = Tag(rfid = "RFID_1")
        repository.insert(tag)
        assert(repository.exists("RFID_1"))
        assert(!repository.exists("RFID_2"))
    }

}
