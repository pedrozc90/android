package com.pedrozc90.prototype.data.tags

import android.content.Context
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.pedrozc90.prototype.data.Tag
import com.pedrozc90.prototype.data.TagRepository
import com.pedrozc90.prototype.data.db.DatabaseRule
import com.pedrozc90.prototype.data.db.PrototypeDatabase
import com.pedrozc90.prototype.data.db.RepositoryRule
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import kotlin.system.measureTimeMillis

@RunWith(AndroidJUnit4::class)
class TagRepositoryTest {

    @get:Rule
    private var dbRule = DatabaseRule()

    @get:Rule
    private var repoRule = RepositoryRule(TagRepository::class, dbRule.db) { TagRepository(it.tagDao()) }
    private val repository by lazy { repoRule.repo }

    @Test
    fun insertMany_Profiler() = runBlocking {
        val tags = (1..100_000).map { Tag(rfid = "RFID_$it", itemReference = "none", serialNumber = it.toLong()) }
        val time = measureTimeMillis {
            repository.insertMany(tags, size = 10_000)
        }
        println("Inserted ${tags.size} tags in $time ms")
    }

    @Test
    fun insertOne_Profiler() = runBlocking {
        val tags = (1..100_000).map { Tag(rfid = "RFID_$it", itemReference = "none", serialNumber = it.toLong()) }
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
        assert(repository.exists("RFID_1"))
        assert(!repository.exists("RFID_2"))
    }

}
