package com.pedrozc90.prototype.domain.repositories

import androidx.test.ext.junit.runners.AndroidJUnit4
import com.pedrozc90.prototype.data.db.DatabaseRuleTest
import com.pedrozc90.prototype.data.db.models.Product
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class ProductRepositoryTest : DatabaseRuleTest() {

    private lateinit var repository: ProductRepository

    @Before
    fun setUp() {
        repository = ProductRepository(db.productDao())
    }

    @Test
    fun fetchAll() = runBlocking {
        val p1 =
            Product(itemReference = "10101010", description = "None", size = "GG", color = "Black")
        val p2 =
            Product(itemReference = "20202020", description = "None", size = "G", color = "Black")
        repository.insert(p1)
        repository.insert(p2)

        val flow = repository.fetch("20")
        val results = flow.first()
        assertNotNull(results)
        assertEquals(1, results.size)
    }

    @Test
    fun fetchFiltered() = runBlocking {
        val p1 =
            Product(itemReference = "10101010", description = "None", size = "GG", color = "Black")
        val p2 =
            Product(itemReference = "20202020", description = "None", size = "G", color = "Black")
        repository.insert(p1)
        repository.insert(p2)

        val flow = repository.fetch()
        val results = flow.first()
        assertNotNull(results)
        assertEquals(2, results.size)
    }

}
