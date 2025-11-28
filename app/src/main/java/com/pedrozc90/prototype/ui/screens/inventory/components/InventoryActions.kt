package com.pedrozc90.prototype.ui.screens.inventory.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.dimensionResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.pedrozc90.prototype.R
import com.pedrozc90.prototype.ui.screens.inventory.InventoryUiState
import com.pedrozc90.prototype.ui.theme.PrototypeTheme
import com.pedrozc90.rfid.objects.TagMetadata

@Composable
fun InventoryActions(
    state: InventoryUiState,
    onStart: () -> Unit,
    onStop: () -> Unit,
    onKillTag: () -> Unit,
    onReset: () -> Unit,
    onSave: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        verticalArrangement = Arrangement.spacedBy(space = dimensionResource(R.dimen.padding_smallest)),
        modifier = modifier
    ) {
        Button(
            enabled = !state.isRunning,
            onClick = onStart,
            modifier = Modifier.fillMaxWidth()
        ) {
            Box(
                modifier = Modifier.fillMaxWidth()
            ) {
                val textId = if (state.isRunning) R.string.running else R.string.start
                Text(
                    text = stringResource(textId),
                    modifier = Modifier.align(Alignment.Center)
                )
                if (state.isRunning) {
                    CircularProgressIndicator(
                        color = MaterialTheme.colorScheme.secondary,
                        trackColor = MaterialTheme.colorScheme.surfaceVariant,
                        modifier = Modifier
                            .size(24.dp)
                            .align(Alignment.CenterEnd),
                        strokeWidth = 2.dp
                    )
                }
            }
        }

        Button(
            enabled = state.isRunning,
            onClick = onStop,
            modifier = Modifier.fillMaxWidth()
        ) {
            Text(text = stringResource(R.string.stop))
        }

        Button(
            enabled = !state.isRunning && state.processed > 0,
            onClick = onKillTag,
            modifier = Modifier.fillMaxWidth()
        ) {
            Text(text = "Kill Tag")
        }

        Button(
            enabled = !state.isRunning && state.processed > 0,
            onClick = onReset,
            modifier = Modifier.fillMaxWidth()
        ) {
            Text(text = stringResource(R.string.reset))
        }

        Button(
            enabled = !state.isRunning && state.processed > 0,
            onClick = onSave,
            modifier = Modifier.fillMaxWidth()
        ) {
            Text(text = stringResource(R.string.save))
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun InventoryActionsPreview() {
    val state = InventoryUiState(
        items = listOf(
            TagMetadata(rfid = "E2000016591702080740B3D4", tid = "0"),
            TagMetadata(rfid = "E2000016591702080740B3D5", tid = "1"),
            TagMetadata(rfid = "E2000016591702080740B3D6", tid = "2"),
            TagMetadata(rfid = "E2000016591702080740B3D7", tid = "3"),
            TagMetadata(rfid = "E2000016591702080740B3D8"),
        ),
        isRunning = true
    )
    PrototypeTheme {
        InventoryActions(
            state = state,
            onStart = {},
            onStop = {},
            onKillTag = {},
            onReset = {},
            onSave = {}
        )
    }
}
