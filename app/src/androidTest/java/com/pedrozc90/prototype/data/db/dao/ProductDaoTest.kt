package com.pedrozc90.prototype.data.db.dao

import com.pedrozc90.prototype.data.db.PrototypeDatabaseTest
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertNull
import org.junit.Before
import org.junit.Test

class ProductDaoTest : PrototypeDatabaseTest() {

    private lateinit var dao: ProductDao;

    @Before
    override fun setUp() {
        super.setUp()
        dao = database.productDao()
    }

    @Test
    fun test_SelectNonExistentEntry() = runTest {
        val result = dao.get(0)
        val product = result.first()
        assertNull(product)
    }

}