package com.pedrozc90.prototype.data.db

import com.pedrozc90.prototype.data.db.DatabaseRule
import org.junit.Rule

/**
 * Base test class that provides a database rule for subclasses.
 * Use this when each test class (instance lifecycle) should get a fresh DB per-test-method.
 */
abstract class BaseDatabaseTest {

    // Instance rule; the rule runs around each test method.
    @get:Rule
    val dbRule = DatabaseRule()

    // Protected convenience property so subclasses can use `db` directly.
    protected val db: PrototypeDatabase
        get() = dbRule.requireDb()
}
