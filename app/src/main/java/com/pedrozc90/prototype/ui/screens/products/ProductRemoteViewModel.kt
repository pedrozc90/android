package com.pedrozc90.prototype.ui.screens.products

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.pedrozc90.prototype.data.web.ApiRepository
import com.pedrozc90.prototype.data.web.objects.ProductDto
import kotlinx.coroutines.launch
import retrofit2.HttpException
import java.io.IOException

class ProductRemoteViewModel(
    private val repository: ApiRepository
) : ViewModel() {

    /** The mutable State that stores the status of the most recent request */
    var uiState: ProductRemoteUiState by mutableStateOf(ProductRemoteUiState.Loading)
        private set

    /**
     * Call getProducts() on init so we can display status immediately.
     */
    init {
        getProducts()
    }

    /**
     * Gets products information from the Mars API Retrofit service and updates the
     * [ProductDto] [List] [MutableList].
     */
    fun getProducts() {
        viewModelScope.launch {
            uiState = try {
                val results = repository.getProducts()
                ProductRemoteUiState.Success(results.products)
            } catch (e: IOException) {
                e.printStackTrace()
                ProductRemoteUiState.Error
            } catch (e: HttpException) {
                e.printStackTrace()
                ProductRemoteUiState.Error
            }
        }
    }

}

sealed interface ProductRemoteUiState {
    data class Success(val products: List<ProductDto>) : ProductRemoteUiState
    object Loading : ProductRemoteUiState
    object Error : ProductRemoteUiState
}

