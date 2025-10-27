package com.pedrozc90.prototype.ui.screens.products

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.Card
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.pedrozc90.prototype.R
import com.pedrozc90.prototype.core.di.AppViewModelProvider
import com.pedrozc90.prototype.data.db.models.Product
import com.pedrozc90.prototype.data.web.objects.ProductDto
import com.pedrozc90.prototype.ui.theme.PrototypeTheme
import java.util.UUID

@Composable
fun ProductRemoteScreen(
    modifier: Modifier = Modifier,
    onNavigateToItem: (ProductDto) -> Unit,
    onNavigateToNewItem: () -> Unit,
    viewModel: ProductRemoteViewModel = viewModel(factory = AppViewModelProvider.Factory)
) {
    val state = viewModel.uiState

    when (state) {
        is ProductRemoteUiState.Loading -> {
            Text(text = "Loading products...")
        }

        is ProductRemoteUiState.Error -> {
            Text(text = "Error loading products.")
        }

        is ProductRemoteUiState.Success -> {
            ProductRemoteContent(
                items = state.products,
                onNavigateToItem = onNavigateToItem,
                onNavigateToNewItem = onNavigateToNewItem,
                modifier = modifier
            )
        }
    }
}

@Composable
private fun ProductRemoteContent(
    modifier: Modifier = Modifier,
    items: List<ProductDto> = listOf(),
    onNavigateToItem: (ProductDto) -> Unit,
    onNavigateToNewItem: () -> Unit
) {
    Box(modifier = modifier.fillMaxSize()) {
        Column(
            verticalArrangement = Arrangement.Top,
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = modifier
                .fillMaxSize()
        ) {
            Text(
                text = stringResource(R.string.products),
                style = MaterialTheme.typography.titleLarge,
                modifier = Modifier.padding(16.dp)
            )

            LazyColumn(
                verticalArrangement = Arrangement.spacedBy(8.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = modifier
                    .fillMaxSize()
            ) {
                items(items = items, key = { it.uuid }) { product ->
                    ProductDtoItem(
                        product = product,
                        onClick = { onNavigateToItem(product) }
                    )
                }
            }
        }

        FloatingActionButton(
            onClick = { onNavigateToNewItem() },
            modifier = Modifier
                .padding(16.dp)
                .align(Alignment.BottomEnd)
        ) {
            Icon(
                imageVector = Icons.Default.Add,
                contentDescription = "Add Product"
            )
        }
    }
}

@Composable
private fun ProductDtoItem(
    product: ProductDto,
    onClick: (ProductDto) -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        onClick = { onClick(product) },
        modifier = modifier
            .fillMaxWidth()
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(4.dp),
            modifier = Modifier
                .padding(4.dp)
                .fillMaxWidth()
        ) {
            Row(
                horizontalArrangement = Arrangement.Center,
                modifier = Modifier
                    .fillMaxWidth()
            ) {
                Spacer(modifier = Modifier.weight(1f))
                Text(
                    text = "Item Reference: ${product.itemReference}",
                    style = MaterialTheme.typography.bodyMedium
                )
                Spacer(modifier = Modifier.weight(1f))
                Text(
                    text = product.description ?: "No Description",
                    style = MaterialTheme.typography.bodyMedium
                )
                Spacer(modifier = Modifier.weight(1f))
            }

//            Row(verticalAlignment = Alignment.CenterVertically) {
//                Spacer(modifier = Modifier.weight(1f))
//                Text(
//                    text = DateFormat.format("dd/MM/yyyy HH:mm", product.insertedAt).toString(),
//                    style = MaterialTheme.typography.labelSmall
//                )
//            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun ProductRemoteScreenPreview() {
    val items = listOf(
        ProductDto(id = 0, uuid = UUID.randomUUID(), itemReference = "PRD001", description = "Product 1")
    )
    PrototypeTheme {
        ProductRemoteContent(
            items = items,
            onNavigateToItem = {},
            onNavigateToNewItem = {}
        )
    }
}

