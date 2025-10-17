package com.pedrozc90.prototype.ui.screens.readings

import android.text.format.DateFormat
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.pedrozc90.prototype.data.read.Read
import com.pedrozc90.prototype.data.read.ReadSummary
import com.pedrozc90.prototype.ui.AppViewModelProvider
import com.pedrozc90.prototype.ui.theme.PrototypeTheme

@Composable
fun ReadListScreen(
    onNavigateToItem: (Long) -> Unit,
    modifier: Modifier = Modifier,
    model: ReadListViewModel = viewModel(factory = AppViewModelProvider.Factory)
) {
    val state by model.uiState.collectAsState()

    LazyColumn(
        contentPadding = PaddingValues(8.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp),
        modifier = modifier
    ) {
        itemsIndexed(items = state.items, key = { idx, row -> row.read.id }) { index, item ->
            ReadSummaryItem(
                summary = item,
                onNavigateToItem = onNavigateToItem,
                modifier = Modifier
                    .fillMaxWidth()
            )
        }
    }
}

@Composable
private fun ReadSummaryItem(
    summary: ReadSummary,
    onNavigateToItem: (Long) -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 3.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant
        ),
        onClick = { onNavigateToItem(summary.read.id) }
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = summary.read.id.toString(),
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.weight(1f))
                Text(
                    text = "Tags: ${summary.tagsCount}",
                    style = MaterialTheme.typography.bodyMedium
                )
            }

            Spacer(Modifier.height(4.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Spacer(modifier = Modifier.weight(1f))
                Text(
                    text = "Created At:",
                    style = MaterialTheme.typography.bodySmall
                )
                Spacer(modifier = Modifier.width(12.dp))
                Text(
                    text = DateFormat.format("dd/MM/yyyy HH:mm", summary.read.insertedAt)
                        .toString(),
                    style = MaterialTheme.typography.bodySmall
                )
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun ReadSummaryItemPreview() {
    PrototypeTheme {
        ReadSummaryItem(
            summary = ReadSummary(read = Read(id = 1), tagsCount = 12),
            onNavigateToItem = {}
        )
    }
}
