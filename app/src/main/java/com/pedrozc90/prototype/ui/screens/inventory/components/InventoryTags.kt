package com.pedrozc90.prototype.ui.screens.inventory.components

import android.text.format.DateFormat
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.grid.rememberLazyGridState
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ExpandLess
import androidx.compose.material.icons.filled.ExpandMore
import androidx.compose.material3.Card
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.pedrozc90.prototype.ui.screens.inventory.InventoryUiState
import com.pedrozc90.prototype.ui.theme.PrototypeTheme
import com.pedrozc90.rfid.objects.TagMetadata

@Composable
fun InventoryTagList(
    state: InventoryUiState,
    modifier: Modifier = Modifier
) {
    val gridState = rememberLazyGridState()

    // automatically scroll to the bottom when a new item is added
    LaunchedEffect(state.lastIndex) {
        val lastVisible = gridState.layoutInfo.visibleItemsInfo.lastOrNull()?.index
        if (state.lastIndex >= 0 && lastVisible != state.lastIndex) {
            gridState.scrollToItem(state.lastIndex)
        }
    }

    LazyColumn(
        verticalArrangement = Arrangement.spacedBy(8.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = modifier.fillMaxSize()
    ) {
        itemsIndexed(items = state.items, key = { idx, row -> row.rfid }) { idx, item ->
            ScannerTagItem(index = idx, item = item)
        }
    }
}

@Composable
private fun ScannerTagItem(
    index: Int = 0,
    item: TagMetadata,
    modifier: Modifier = Modifier
) {
    var expanded by remember { mutableStateOf(false) }

    Card(
        modifier = modifier.fillMaxWidth()
    ) {
        Column(
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.fillMaxWidth()
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.fillMaxWidth().padding(start = 8.dp)
            ) {
                Text(
                    text = item.rfid,
                    style = MaterialTheme.typography.bodyLarge
                )

                Spacer(modifier = Modifier.weight(1f))

                InventoryItemButton(
                    expanded = expanded,
                    onClick = { expanded = !expanded },
                )
            }

            if (expanded) {
                Column (
                    verticalArrangement = Arrangement.Center,
                    horizontalAlignment = Alignment.Start,
                    modifier = Modifier.fillMaxWidth()
                        .padding(4.dp)
                ) {
                    if (item.tid != null) {
                        Text(
                            text = "TID: ${item.tid}",
                            style = MaterialTheme.typography.bodySmall
                        )
                    }

                    if (item.rssi != null) {
                        Text(
                            text = "RSSI: ${item.rssi}",
                            style = MaterialTheme.typography.bodySmall
                        )
                    }

                    val timestamp = DateFormat.format("yyyy-MM-dd HH:mm:ss", item.timestamp).toString()
                    Text(
                        text = "Received At: $timestamp",
                        style = MaterialTheme.typography.bodySmall
                    )
                }
            }
        }
    }
}

@Composable
private fun InventoryItemButton(
    expanded: Boolean = false,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    IconButton(
        onClick = onClick,
        modifier = modifier
    ) {
        Icon(
            imageVector = if (expanded) Icons.Default.ExpandLess else Icons.Default.ExpandMore,
            contentDescription = "Expand"
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun InventoryTagsPreview() {
    val state = InventoryUiState(
        items = listOf(
            TagMetadata(rfid = "E2000016591702080740B3D4", tid = "0", rssi = "-65"),
            TagMetadata(rfid = "E2000016591702080740B3D5", tid = "1", rssi = "-45"),
            TagMetadata(rfid = "E2000016591702080740B3D6", tid = "2"),
            TagMetadata(rfid = "E2000016591702080740B3D7", tid = "3"),
            TagMetadata(rfid = "E2000016591702080740B3D8"),
        )
    )
    PrototypeTheme {
        InventoryTagList(state = state)
    }
}
