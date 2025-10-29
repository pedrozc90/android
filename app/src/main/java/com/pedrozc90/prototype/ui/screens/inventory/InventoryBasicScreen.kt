package com.pedrozc90.prototype.ui.screens.inventory

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.pedrozc90.prototype.R
import com.pedrozc90.prototype.core.di.AppViewModelProvider
import com.pedrozc90.prototype.ui.screens.inventory.components.InventoryContent
import com.pedrozc90.prototype.ui.theme.PrototypeTheme
import com.pedrozc90.rfid.objects.TagMetadata

@Composable
fun InventoryBasicScreen(
    modifier: Modifier = Modifier,
    viewModel: InventoryBasicViewModel = viewModel(factory = AppViewModelProvider.Factory)
) {
    // lifecycle-aware collection avoids collecting while stopped
    val state by viewModel.uiState.collectAsStateWithLifecycle()

    // initialize once per ViewModel instance
    LaunchedEffect(viewModel) {
        viewModel.onInit()
    }

    // Stop the reader when the Composable leaves composition (optional).
    // If you want scanning to continue while navigating away, remove this DisposableEffect.
    DisposableEffect(viewModel) {
        onDispose {
            // this calls the ViewModel cleanup method (non-suspending)
            viewModel.onDispose()
        }
    }

    InventoryContent(
        state = state,
        onStart = { viewModel.start() },
        onStop = { viewModel.stop() },
        onReset = { viewModel.reset() },
        onSave = { viewModel.persist() },
        modifier = modifier
    )
}

@Composable
private fun InventoryHeader() {
    Text(
        text = stringResource(R.string.inventory),
        style = MaterialTheme.typography.titleLarge
    )
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
            onStop = {},
            onReset = {},
            onSave = {}
        )
    }
}
