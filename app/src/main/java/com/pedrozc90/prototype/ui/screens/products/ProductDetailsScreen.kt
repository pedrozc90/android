package com.pedrozc90.prototype.ui.screens.products

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.dimensionResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.lifecycle.viewmodel.compose.viewModel
import com.pedrozc90.prototype.R
import com.pedrozc90.prototype.core.di.AppViewModelProvider
import com.pedrozc90.prototype.data.db.models.Product
import com.pedrozc90.prototype.ui.theme.PrototypeTheme
import kotlinx.coroutines.launch

@Composable
fun ProductDetailsScreen(
    modifier: Modifier = Modifier,
    onNavigateBack: () -> Unit,
    onNavigateUp: () -> Unit,
    viewModel: ProductDetailsViewModel = viewModel(factory = AppViewModelProvider.Factory)
) {
    val state = viewModel.uiState
    val coroutineScope = rememberCoroutineScope()

    ProductDetailsContent(
        state = state,
        modifier = modifier,
        onValueChange = {
            coroutineScope.launch {
                viewModel.update(it)
                // onNavigateBack()
            }
        },
        onSaveClick = {
            coroutineScope.launch {
                viewModel.onSave()
                onNavigateUp()
            }
        }
    )
}

@Composable
private fun ProductDetailsContent(
    modifier: Modifier = Modifier,
    state: ProductUiState,
    onValueChange: (ProductDetails) -> Unit,
    onSaveClick: () -> Unit
) {
    Column(
        verticalArrangement = Arrangement.spacedBy(space = dimensionResource(R.dimen.padding_small)),
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = modifier
    ) {
        ProductForm(
            enabled = state.isValid,
            details = state.details,
            onValueChange = onValueChange
        )

        Button(
            enabled = state.isValid,
            onClick = onSaveClick,
            modifier = Modifier.fillMaxWidth()
        ) {
            Text(text = stringResource(R.string.save))
        }
    }
}

@Preview(showBackground = true)
@Composable
fun ProductDetailsScreenPreview() {
    val product = Product(
        id = 1010101010L,
        itemReference = "1010101010",
        description = "Sanity Check",
        size = "None",
        color = "Transparent"
    )
    val state = product.toUiState(isValid = true)
    PrototypeTheme {
        ProductDetailsContent(
            state = state,
            onValueChange = {},
            onSaveClick = {}
        )
    }
}
