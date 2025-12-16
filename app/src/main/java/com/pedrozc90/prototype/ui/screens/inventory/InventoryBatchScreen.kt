package com.pedrozc90.prototype.ui.screens.inventory

import android.util.Log
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.lifecycle.viewmodel.compose.viewModel
import com.pedrozc90.prototype.R
import com.pedrozc90.prototype.core.di.AppViewModelProvider
import com.pedrozc90.prototype.ui.screens.inventory.components.InventoryContent
import com.pedrozc90.prototype.ui.theme.PrototypeTheme
import com.pedrozc90.rfid.objects.TagMetadata

private const val TAG = "InventoryBatchScreen"

@Composable
fun InventoryBatchScreen(
    modifier: Modifier = Modifier,
    viewModel: InventoryBatchViewModel = viewModel(factory = AppViewModelProvider.Factory)
) {
    val state by viewModel.uiState.collectAsState()

    // Stop the reader when the Composable leaves composition (optional).

    val lifecycleOwner = LocalLifecycleOwner.current
    DisposableEffect(lifecycleOwner) {
        val observer = LifecycleEventObserver { _, event ->
            when (event) {
                Lifecycle.Event.ON_START -> {
                    Log.d(TAG, "Lifecycle ON_START")
                    viewModel.onInit()
                }

                Lifecycle.Event.ON_STOP -> {
                    Log.d(TAG, "Lifecycle ON_STOP")
                    viewModel.onDispose()
                }

                else -> {
                    Log.d(TAG, "Lifecycle $event")
                }
            }
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose {
            // this calls the ViewModel cleanup method (non-suspending)
            // viewModel.onDispose()
            lifecycleOwner.lifecycle.removeObserver(observer)
        }
    }

    InventoryContent(
        state = state,
        onStart = { viewModel.start() },
        onStop = { viewModel.stop() },
        onKillTag = { Log.e(TAG, "Method not implemented.") },
        onReset = { viewModel.reset() },
        onSave = { viewModel.save() },
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
            onKillTag = {},
            onReset = {},
            onSave = {}
        )
    }
}
