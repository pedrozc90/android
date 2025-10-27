package com.pedrozc90.prototype.data.db

import android.content.Context
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import org.junit.Before
import org.junit.Rule

/**
 * Base test class that provides a database rule for subclasses.
 * Use this when each test class (instance lifecycle) should get a fresh DB per-test-method.
 */
abstract class DatabaseRuleTest {

    // Instance rule; the rule runs around each test method.
    @get:Rule
    val dbRule = DatabaseRule()

    // Protected convenience property so subclasses can use `db` directly.
    protected val db: PrototypeDatabase
        get() = dbRule.requireDb()
}

open class PrototypeDatabaseTest {

    protected lateinit var database: PrototypeDatabase
    // private lateinit var repository: TagRepository

    @Before
    open fun setUp() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        database = Room.inMemoryDatabaseBuilder(context, PrototypeDatabase::class.java)
            .allowMainThreadQueries()
            .build()
        // repository = TagRepository(database.tagDao())
    }

}

