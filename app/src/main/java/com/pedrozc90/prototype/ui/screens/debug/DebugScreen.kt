package com.pedrozc90.prototype.ui.screens.debug

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Slider
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.pedrozc90.prototype.R
import com.pedrozc90.prototype.core.di.AppViewModelProvider
import com.pedrozc90.prototype.ui.components.SelectField
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

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun DebugContent(
    state: DebugUiState,
    onClickInit: () -> Unit,
    onClickStart: () -> Unit,
    onClickStop: () -> Unit,
    modifier: Modifier = Modifier
) {
    var display by remember { mutableStateOf(false) }
    val sheetState = rememberModalBottomSheetState()
    Column(
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = modifier.fillMaxSize()
    ) {
        IconButton(
            onClick = { display = !display }
        ) {
            Icon(
                imageVector = Icons.Default.Settings,
                contentDescription = "Settings"
            )
        }

        if (display) {
            ModalBottomSheet(
                onDismissRequest = { display = false },
                sheetState = sheetState,
                modifier = Modifier.fillMaxHeight()
            ) {
                DebugSettingsModel(
                    onApply = {},
                    onCancel = {}
                )
            }
        }

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

@Composable
fun DebugSettingsModel(
    power: Int = 15,
    onApply: () -> Unit,
    onCancel: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        verticalArrangement = Arrangement.spacedBy(8.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = modifier.padding(8.dp)
    ) {
        Column(
            verticalArrangement = Arrangement.spacedBy(2.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.fillMaxWidth()
        ) {
            Text(
                text = "RF Power: $power",
                modifier = Modifier.fillMaxWidth()
            )
            Slider(
                value = power.toFloat(),
                valueRange = 0f..30f,
                steps = 99, // max value is divided by steps + 1
                onValueChange = { },
                // onValueChangeFinished = onValueChangeFinished,
                modifier = Modifier
            )
        }

        val options = listOf("One", "Two", "Three")
        var selectedValue by remember { mutableStateOf(options.first()) }
        SelectField(
            label = "Field",
            value = selectedValue,
            items = options,
            onLabel = { it },
            onSelect = { selectedValue = it }
        )

        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            Button(
                onClick = onCancel,
                modifier = Modifier.weight(1f)
            ) {
                Text(text = "Cancel")
            }

            Button(
                onClick = onApply,
                modifier = Modifier.weight(1f)
            ) {
                Text(text = "Apply")
            }
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

@Preview(showBackground = true)
@Composable
fun DebugSettingsModalPreview() {
    PrototypeTheme {
        DebugSettingsModel(
            onApply = {},
            onCancel = {}
        )
    }
}
