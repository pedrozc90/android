package com.example.amphibians.data

import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test

class DefaultAppContainerTest {

    private val container = DefaultAppContainer()

    @Test
    fun testGetAmphibians_RealApi() = runTest{
        val results = container.amphibiansRepository.getAmphibians()

        assertNotNull("Results should not be null", results)
        assertTrue("Results should not be empty", results.isEmpty())

        val first = results.first()
        assertNotNull("Amphibian 'name' should not be null", first.name)
        assertNotNull("Amphibian 'type' should not be null", first.type)
        assertNotNull("Amphibian 'description' should not be null", first.description)
        assertNotNull("Amphibian 'imgSrc' should not be null", first.imgSrc)
    }

}