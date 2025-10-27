package com.pedrozc90.prototype.ui.screens.products

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
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
import com.pedrozc90.prototype.ui.theme.PrototypeTheme
import kotlinx.coroutines.launch

@Composable
fun ProductEntryScreen(
    modifier: Modifier = Modifier,
    onNavigateBack: () -> Unit,
    onNavigateUp: () -> Unit,
    viewModel: ProductEntryViewModel = viewModel(factory = AppViewModelProvider.Factory)
) {
    val state = viewModel.uiState
    val coroutineScope = rememberCoroutineScope()

    ProductEntryContent(
        state = state,
        onValueChange = viewModel::update,
        onSaveClick = {
            coroutineScope.launch {
                viewModel.onSave()
                onNavigateBack()
            }
        },
        modifier = modifier
    )
}

@Composable
private fun ProductEntryContent(
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

@Composable
fun ProductForm(
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    details: ProductDetails,
    onValueChange: (ProductDetails) -> Unit
) {
    Column(
        verticalArrangement = Arrangement.spacedBy(space = dimensionResource(R.dimen.padding_small)),
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = modifier
    ) {
        Text(
            text = "Product #${details.id}",
            style = MaterialTheme.typography.titleLarge,
            modifier = Modifier.fillMaxWidth()
        )

        TextField(
            enabled = enabled,
            label = { Text(text = stringResource(R.string.item_reference)) },
            value = details.itemReference,
            onValueChange = { onValueChange(details.copy(itemReference = it)) },
            modifier = Modifier.fillMaxWidth()
        )

        TextField(
            enabled = enabled,
            label = { Text(text = stringResource(R.string.description)) },
            value = details.description ?: "",
            onValueChange = { onValueChange(details.copy(description = it)) },
            modifier = Modifier.fillMaxWidth()
        )

        TextField(
            enabled = enabled,
            label = { Text(text = stringResource(R.string.size)) },
            value = details.size ?: "",
            onValueChange = { onValueChange(details.copy(size = it)) },
            modifier = Modifier.fillMaxWidth()
        )

        TextField(
            enabled = enabled,
            label = { Text(text = stringResource(R.string.color)) },
            value = details.color ?: "",
            onValueChange = { onValueChange(details.copy(color = it)) },
            modifier = Modifier.fillMaxWidth()
        )
    }
}

@Preview(showBackground = true)
@Composable
fun ProductEntryScreenPreview() {
    val state = ProductUiState(isValid = true)
    PrototypeTheme {
        ProductEntryContent(
            state = state,
            onValueChange = {},
            onSaveClick = {}
        )
    }
}
