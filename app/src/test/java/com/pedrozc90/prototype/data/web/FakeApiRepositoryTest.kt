package com.pedrozc90.prototype.data.web

import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Test

class FakeApiRepositoryTest {

    private val repository = FakeApiRepository()

    @Test
    fun networkMarsPhotosRepository_getMarsPhotos_verifyPhotoList() = runTest {
        assertEquals(FakeDataSource.products, repository.getProducts())
    }

}
