package com.pedrozc90.prototype.ui.screens.scan

import android.Manifest
import android.util.Log
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.lifecycle.viewmodel.compose.viewModel
import com.pedrozc90.prototype.ui.AppViewModelProvider
import com.pedrozc90.prototype.ui.theme.PrototypeTheme

@Composable
fun ScanScreen(
    modifier: Modifier = Modifier,
    model: ScanViewModel = viewModel(factory = AppViewModelProvider.Factory)
) {
    val state by model.uiState.collectAsState()

    val permissionsLauncher =
        rememberLauncherForActivityResult(contract = ActivityResultContracts.RequestMultiplePermissions()) { perms ->
            Log.e("ScanScreen", "Permissions result: $perms")
        }

    LaunchedEffect(Unit) {
        permissionsLauncher.launch(
            arrayOf(
                Manifest.permission.BLUETOOTH_SCAN,
                Manifest.permission.BLUETOOTH_CONNECT,
                Manifest.permission.ACCESS_FINE_LOCATION // if your target requires
            )
        )
        model.onInit()
    }

    DisposableEffect(Unit) {
        onDispose {
            model.onDispose()
        }
    }

    Column(
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = modifier
    ) {
        Text(text = "Connection: ${state.connection}")
        Column {
            Button(
                onClick = { model.startInventory() },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Start")
            }
            Button(
                onClick = { model.stopInventory() },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Stop")
            }
            Button(
                onClick = { model.disconnect() },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Disconnect")
            }
        }
        LazyColumn {
            items(state.tags) { tag ->
                // Text(text = "${tag.epc}  RSSI:${tag.rssi}")
                Text(text = tag)
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun ScanScreenPreview() {
    PrototypeTheme {
        ScanScreen()
    }
}
