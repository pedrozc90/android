package com.pedrozc90.prototype.data.db

import android.content.Context
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import org.junit.rules.TestRule
import org.junit.runner.Description
import org.junit.runners.model.Statement
import kotlin.reflect.KClass

class RepositoryRule<T : Any>(private val clazz: KClass<T>,
                              private val db: PrototypeDatabase,
                              private val factory: (PrototypeDatabase) -> T) : TestRule {

    lateinit var repo: T
        private set

    override fun apply(base: Statement?, description: Description?): Statement? {
        return object : Statement() {
            override fun evaluate() {
                repo = factory.invoke(db)
                try {
                    base?.evaluate()
                } finally {
                    db.close()
                }
            }
        }
    }
}
