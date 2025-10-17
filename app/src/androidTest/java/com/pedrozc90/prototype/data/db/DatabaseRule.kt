package com.pedrozc90.prototype.data.db

import android.content.Context
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import org.junit.rules.TestRule
import org.junit.runner.Description
import org.junit.runners.model.Statement

/**
 * Test rule that creates an in-memory Room database for the duration of whatever Statement it wraps.
 * - db is nullable internally to avoid lateinit crashes if accessed too early.
 * - use requireDb() to get a non-null database with a clearer error message if accessed incorrectly.
 */
class DatabaseRule : TestRule {

    var db: PrototypeDatabase? = null
        private set

    override fun apply(base: Statement, description: Description): Statement {
        return object : Statement() {
            override fun evaluate() {
                val context = ApplicationProvider.getApplicationContext<Context>()
                db = Room.inMemoryDatabaseBuilder(context, PrototypeDatabase::class.java)
                    .allowMainThreadQueries()
                    .build()

                try {
                    base.evaluate()
                } finally {
                    db?.close()
                    db = null
                }
            }
        }
    }

    /**
     * Safe accessor that throws a helpful exception if the DB isn't initialized yet.
     */
    fun requireDb(): PrototypeDatabase =
        db ?: throw IllegalStateException(
            "Database not initialized. Access the database only in @Before or @Test methods (or use a @ClassRule)."
        )
}
