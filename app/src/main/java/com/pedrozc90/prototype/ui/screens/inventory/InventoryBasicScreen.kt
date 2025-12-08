package com.pedrozc90.prototype.ui.screens.inventory

import android.content.Context
import android.util.Log
import android.widget.Toast
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.tooling.preview.Preview
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.pedrozc90.prototype.core.di.AppViewModelProvider
import com.pedrozc90.prototype.ui.screens.inventory.components.InventoryContent
import com.pedrozc90.prototype.ui.theme.PrototypeTheme
import com.pedrozc90.rfid.objects.TagMetadata

private const val TAG = "InventoryBasicScreen"

@Composable
fun InventoryBasicScreen(
    modifier: Modifier = Modifier,
    viewModel: InventoryBasicViewModel = viewModel(factory = AppViewModelProvider.Factory),
    context: Context = LocalContext.current
) {
    // lifecycle-aware collection avoids collecting while stopped
    val state by viewModel.uiState.collectAsStateWithLifecycle()

    LaunchedEffect(viewModel) {
        viewModel.errors.collect { msg ->
            Toast.makeText(context, msg, Toast.LENGTH_LONG).show()
        }
    }

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
        onKillTag = viewModel::killTag,
        onReset = { viewModel.reset() },
        onSave = { viewModel.persist() },
        modifier = modifier
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
