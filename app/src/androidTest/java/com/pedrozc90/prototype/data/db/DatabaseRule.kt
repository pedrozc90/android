package com.pedrozc90.prototype.data.db

import android.content.Context
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import org.junit.rules.TestRule
import org.junit.runner.Description
import org.junit.runners.model.Statement

class DatabaseRule() : TestRule {

    lateinit var db: PrototypeDatabase
        private set

    override fun apply(base: Statement?, description: Description?): Statement? {
        return object : Statement() {
            override fun evaluate() {
                val context = ApplicationProvider.getApplicationContext<Context>()
                db = Room.inMemoryDatabaseBuilder(context, PrototypeDatabase::class.java)
                    .allowMainThreadQueries()
                    .build()

                try {
                    base?.evaluate()
                } finally {
                    db.close()
                }
            }
        }
    }
}
