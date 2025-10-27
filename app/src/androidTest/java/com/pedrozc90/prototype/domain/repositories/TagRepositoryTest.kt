package com.pedrozc90.prototype.domain.repositories

import androidx.test.ext.junit.runners.AndroidJUnit4
import com.pedrozc90.prototype.data.db.DatabaseRuleTest
import com.pedrozc90.prototype.data.db.models.Tag
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import kotlin.system.measureTimeMillis

@RunWith(AndroidJUnit4::class)
class TagRepositoryTest : DatabaseRuleTest() {

    private lateinit var repository: TagRepository

    @Before
    fun setUp() {
        repository = TagRepository(db.tagDao())
    }

    @Test
    fun insertMany_Profiler() = runBlocking {
        val tags = (1..100_000).map {
            Tag(
                rfid = "RFID_$it",
                itemReference = "none",
                serialNumber = it.toLong()
            )
        }
        val time = measureTimeMillis {
            repository.insertMany(tags, size = 10_000)
        }
        println("Inserted ${tags.size} tags in $time ms")
    }

    @Test
    fun insertOne_Profiler() = runBlocking {
        val tags = (1..100_000).map {
            Tag(
                rfid = "RFID_$it",
                itemReference = "none",
                serialNumber = it.toLong()
            )
        }
        val time = measureTimeMillis {
            tags.forEach { tag ->
                repository.insert(tag)
            }
        }
        println("Inserted ${tags.size} tags in $time ms")
    }

    @Test
    fun exists() = runBlocking {
        val tag = Tag(rfid = "RFID_1", itemReference = "none", serialNumber = 1)
        repository.insert(tag)
        assertTrue(repository.exists("RFID_1"))
        assertTrue(!repository.exists("RFID_2"))
    }

}
