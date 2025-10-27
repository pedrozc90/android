package com.pedrozc90.prototype.ui.screens.products

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.core.text.isDigitsOnly
import androidx.lifecycle.ViewModel
import com.pedrozc90.prototype.domain.repositories.ProductRepository

private const val TAG = "ProductDetailsViewModel"

class ProductEntryViewModel(
    private val repository: ProductRepository
) : ViewModel() {

    var uiState by mutableStateOf(ProductUiState())
        private set

    private fun validate(details: ProductDetails = uiState.details): Boolean {
        return with(details) {
            itemReference.isNotBlank() && itemReference.isDigitsOnly()
        }
    }

    fun update(details: ProductDetails) {
        uiState = uiState.copy(details = details, isValid = validate(details))
    }

    suspend fun onSave() {
        val isValid = validate()
        if (isValid) {
            repository.insert(uiState.details.toProduct())
        }
    }

}
