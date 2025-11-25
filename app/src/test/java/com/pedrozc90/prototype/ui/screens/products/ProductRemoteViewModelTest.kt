package com.pedrozc90.prototype.ui.screens.products

import com.pedrozc90.prototype.core.rules.TestDispatcherRule
import com.pedrozc90.prototype.data.web.FakeApiRepository
import com.pedrozc90.prototype.data.web.FakeDataSource
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test

class ProductRemoteViewModelTest {

    @get:Rule
    val testDispatcher = TestDispatcherRule()

    @Test
    fun viewModel_getProducts_verifyUiStateSuccess() = runTest {
        val marsViewModel = ProductRemoteViewModel(
            repository = FakeApiRepository()
        )
        assertEquals(
            ProductRemoteUiState.Success(FakeDataSource.products),
            marsViewModel.uiState
        )
    }

}
