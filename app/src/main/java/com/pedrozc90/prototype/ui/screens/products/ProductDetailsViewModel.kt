package com.pedrozc90.prototype.ui.screens.products

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.pedrozc90.prototype.data.db.models.Product
import com.pedrozc90.prototype.domain.repositories.ProductRepository
import com.pedrozc90.prototype.ui.navigation.Routes
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

private const val TAG = "ProductDetailsViewModel"

class ProductDetailsViewModel(
    savedStateHandle: SavedStateHandle,
    private val repository: ProductRepository
) : ViewModel() {

    private val productId: Long = checkNotNull(savedStateHandle[Routes.ProductDetails.ARG_ID])

    var uiState by mutableStateOf(ProductUiState())
        private set

    init {
        viewModelScope.launch {
            uiState = repository.get(productId)
                .filterNotNull()
                .first()
                .let { update(it.toProductDetails()) }
        }
    }

    private fun validate(product: Product): Boolean {
        return with(product) {
            itemReference.isNotBlank()
        }
    }

    suspend fun update(details: ProductDetails): ProductUiState {
        return uiState.copy(details = details, isValid = validate(details.toProduct()))
    }

    fun onSave() {
        viewModelScope.launch {
            if (uiState.isValid) {
                val product = uiState.details.toProduct()
                repository.update(product)
            }
        }
    }

}
