package com.pedrozc90.prototype.ui.screens.inventory

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
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.pedrozc90.prototype.R
import com.pedrozc90.prototype.core.di.AppViewModelProvider
import com.pedrozc90.prototype.ui.theme.PrototypeTheme
import com.pedrozc90.rfid.objects.TagMetadata

@Composable
fun InventoryBasicScreen(
    modifier: Modifier = Modifier,
    viewModel: InventoryViewModel = viewModel(factory = AppViewModelProvider.Factory)
) {
    val state by viewModel.uiState.collectAsState()

    InventoryContent(
        state = state,
        onStart = { viewModel.start() },
        onStop = { viewModel.stop() },
        modifier = modifier
    )
}

@Composable
private fun InventoryContent(
    state: InventoryUiState,
    onStart: () -> Unit,
    onStop: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        verticalArrangement = Arrangement.spacedBy(8.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = modifier.fillMaxSize()
    ) {
        InventoryHeader()

        InventoryCounter(state = state)

        HorizontalDivider()

        InventoryTagList(
            state = state,
            modifier = Modifier.weight(1f)
        )

        HorizontalDivider()

        InventoryActions(
            state = state,
            onStart = onStart,
            onStop = onStop
        )
    }
}

@Composable
private fun InventoryHeader() {
    Text(
        text = stringResource(R.string.inventory),
        style = MaterialTheme.typography.titleLarge
    )
}

@Composable
private fun InventoryCounter(
    state: InventoryUiState,
    modifier: Modifier = Modifier
) {
    Text(
        text = "Tags: ${state.counter}",
        fontSize = 42.sp,
        modifier = modifier
    )
}


@Composable
private fun InventoryTagList(
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
    Card(
        modifier = modifier.fillMaxWidth()
    ) {
        Column(
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier
                .fillMaxWidth()
                .padding(4.dp)
        ) {
            Text(
                text = "RFID: ${item.rfid}",
                style = MaterialTheme.typography.bodyLarge
            )

            Row {
                if (item.tid != null) {
                    Text(
                        text = "TID: ${item.tid}",
                        style = MaterialTheme.typography.bodySmall
                    )
                }

                Spacer(modifier = Modifier.weight(1f))

                Text(
                    text = DateFormat.format("yyyy-MM-dd HH:mm:ss", item.timestamp).toString(),
                    style = MaterialTheme.typography.bodySmall
                )
            }
        }
    }
}

@Composable
private fun InventoryActions(
    state: InventoryUiState,
    onStart: () -> Unit,
    onStop: () -> Unit,
    modifier: Modifier = Modifier
) {
    Button(
        enabled = !state.isRunning,
        onClick = onStart,
        modifier = Modifier.fillMaxWidth()
    ) {
        Text(text = stringResource(R.string.start))
    }

    Button(
        enabled = state.isRunning,
        onClick = onStop,
        modifier = Modifier.fillMaxWidth()
    ) {
        Text(text = stringResource(R.string.stop))
    }
}

@Preview(showBackground = true)
@Composable
private fun InventoryScreenPreview() {
    val state = InventoryUiState(
        items = listOf(
            TagMetadata(rfid = "E2000016591702080740B3D4", tid = "0"),
            TagMetadata(rfid = "E2000016591702080740B3D5", tid = "1"),
            TagMetadata(rfid = "E2000016591702080740B3D6", tid = "2"),
            TagMetadata(rfid = "E2000016591702080740B3D7", tid = "3"),
            TagMetadata(rfid = "E2000016591702080740B3D8"),
        )
    )
    PrototypeTheme {
        InventoryContent(
            state = state,
            onStart = {},
            onStop = {}
        )
    }
}
