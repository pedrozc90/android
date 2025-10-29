package com.pedrozc90.prototype.ui.screens.debug

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.lifecycle.viewmodel.compose.viewModel
import com.pedrozc90.prototype.R
import com.pedrozc90.prototype.core.di.AppViewModelProvider
import com.pedrozc90.prototype.ui.theme.PrototypeTheme

@Composable
fun DebugScreen(
    modifier: Modifier = Modifier,
    viewModel: DebugViewModel = viewModel(factory = AppViewModelProvider.Factory)
) {
    val state by viewModel.uiState.collectAsState()

    DebugContent(
        state = state,
        onClickInit = { viewModel.onInit() },
        onClickStart = { viewModel.onStart() },
        onClickStop = { viewModel.onStop() },
        modifier = modifier
    )
}

@Composable
private fun DebugContent(
    state: DebugUiState,
    onClickInit: () -> Unit,
    onClickStart: () -> Unit,
    onClickStop: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = modifier.fillMaxSize()
    ) {
        val device = state.device ?: "No Device"
        Text(text = "Device: $device")

        val status = state.status ?: "None"
        Text(text = "Status: $status")

        val tags = state.items.size
        Text(text = "Tags: $tags")

        Button(
            onClick = onClickInit,
            modifier = Modifier.fillMaxWidth()
        ) {
            Text(
                text = "Init Device"
            )
        }

        Button(
            onClick = onClickStart,
            modifier = Modifier.fillMaxWidth()
        ) {
            Text(
                text = stringResource(R.string.start)
            )
        }

        Button(
            onClick = onClickStop,
            modifier = Modifier.fillMaxWidth()
        ) {
            Text(
                text = stringResource(R.string.stop)
            )
        }
    }
}

@Preview(showBackground = true)
@Composable
fun DebugScreenPreview() {
    PrototypeTheme {
        DebugContent(
            state = DebugUiState(),
            onClickInit = {},
            onClickStart = {},
            onClickStop = {}
        )
    }
}
