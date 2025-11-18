package com.pedrozc90.prototype.ui.screens.products

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.pedrozc90.prototype.Constants
import com.pedrozc90.prototype.domain.repositories.ProductRepository
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn

private const val TAG = "ProductListViewModel"

class ProductListViewModel(
    private val repository: ProductRepository
) : ViewModel() {

    val uiState: StateFlow<ProductListUiState> = repository.fetch()
        .map { results ->
            ProductListUiState(list = results)
        }.stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(Constants.STATE_TIMEOUT),
            initialValue = ProductListUiState()
        )

}


