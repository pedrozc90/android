package com.pedrozc90.prototype.data.db

import android.content.Context
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.pedrozc90.prototype.data.Tag
import com.pedrozc90.prototype.data.TagRepository
import com.pedrozc90.prototype.data.db.PrototypeDatabase
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Assert.assertNotNull
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import kotlin.system.measureTimeMillis

@RunWith(AndroidJUnit4::class)
class PrototypeDatabaseTest {

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
    fun validateDaos() = runBlocking {
        assertNotNull(database)
        assertNotNull(database.tagDao())
        assertNotNull(database.readDao())
    }

}
