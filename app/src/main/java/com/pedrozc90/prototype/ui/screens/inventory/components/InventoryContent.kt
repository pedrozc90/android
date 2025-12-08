package com.pedrozc90.prototype.ui.screens.inventory.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.HorizontalDivider
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.pedrozc90.prototype.ui.screens.inventory.InventoryUiState
import com.pedrozc90.prototype.ui.theme.PrototypeTheme
import com.pedrozc90.rfid.objects.TagMetadata

@Composable
fun InventoryContent(
    state: InventoryUiState,
    onStart: () -> Unit,
    onStop: () -> Unit,
    onKillTag: (TagMetadata) -> Unit,
    onReset: () -> Unit,
    onSave: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        verticalArrangement = Arrangement.spacedBy(8.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = modifier.fillMaxSize()
    ) {
        // InventoryHeader()

        InventoryCounter(state = state)

        HorizontalDivider()

        InventoryTagList(
            state = state,
            onKillTag = onKillTag,
            modifier = Modifier.weight(1f)
        )

        HorizontalDivider()

        InventoryActions(
            state = state,
            onStart = onStart,
            onStop = onStop,
            onReset = onReset,
            onSave = onSave
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun InventoryContentPreview() {
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
            onStop = {},
            onKillTag = {},
            onReset = {},
            onSave = {}
        )
    }
}
