package com.pedrozc90.prototype.ui.screens.products

import android.text.format.DateFormat
import android.util.Log
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
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.pedrozc90.prototype.R
import com.pedrozc90.prototype.core.di.AppViewModelProvider
import com.pedrozc90.prototype.data.db.models.Product
import com.pedrozc90.prototype.ui.theme.PrototypeTheme

@Composable
fun ProductListScreen(
    modifier: Modifier = Modifier,
    onNavigateToItem: (Product) -> Unit,
    onNavigateToNewItem: () -> Unit,
    viewModel: ProductListViewModel = viewModel(factory = AppViewModelProvider.Factory)
) {
    val state by viewModel.uiState.collectAsState()

    ProductListContent(
        state = state,
        onNavigateToItem = onNavigateToItem,
        onNavigateToNewItem = onNavigateToNewItem,
        modifier = modifier
    )
}

@Composable
private fun ProductListContent(
    state: ProductListUiState,
    onNavigateToItem: (Product) -> Unit,
    onNavigateToNewItem: () -> Unit,
    modifier: Modifier = Modifier
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
                items(items = state.list, key = { it.uuid }) { product ->
                    ProductItem(
                        product = product,
                        onClick = { onNavigateToItem(product) }
                    )
                }
            }
        }

        FloatingActionButton(
            onClick = { onNavigateToNewItem() },
            modifier = Modifier.padding(16.dp)
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
private fun ProductItem(
    product: Product,
    onClick: (Product) -> Unit,
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

            Row(verticalAlignment = Alignment.CenterVertically) {
                Spacer(modifier = Modifier.weight(1f))
                Text(
                    text = DateFormat.format("dd/MM/yyyy HH:mm", product.insertedAt).toString(),
                    style = MaterialTheme.typography.labelSmall
                )
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun ProductListScreenPreview() {
    val list = listOf(
        Product(itemReference = "PRD001", description = "Product 1"),
        Product(itemReference = "PRD002", description = "Product 2"),
        Product(itemReference = "PRD003", description = "Product 3"),
        Product(itemReference = "PRD004", description = "Product 4"),
        Product(itemReference = "PRD005", description = "Product 5"),
        Product(itemReference = "PRD006", description = "Product 6"),
        Product(itemReference = "PRD007", description = "Product 7"),
        Product(itemReference = "PRD008", description = "Product 8"),
        Product(itemReference = "PRD009", description = "Product 9"),
        Product(itemReference = "PRD0010", description = "Product 10"),
        Product(itemReference = "PRD0011", description = "Product 11"),
        Product(itemReference = "PRD0012", description = "Product 12"),
        Product(itemReference = "PRD0013", description = "Product 13"),
        Product(itemReference = "PRD0014", description = "Product 14"),
        Product(itemReference = "PRD0015", description = "Product 15"),
    )
    PrototypeTheme {
        ProductListContent(
            state = ProductListUiState(list = list),
            onNavigateToItem = {},
            onNavigateToNewItem = {}
        )
    }
}
