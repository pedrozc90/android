package com.pedrozc90.prototype.ui.screens.settings

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.PlainTooltip
import androidx.compose.material3.Slider
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TooltipAnchorPosition
import androidx.compose.material3.TooltipBox
import androidx.compose.material3.TooltipDefaults
import androidx.compose.material3.rememberTooltipState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.dimensionResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.pedrozc90.prototype.R
import com.pedrozc90.prototype.core.bluetooth.BluetoothDeviceDto
import com.pedrozc90.prototype.core.devices.DeviceType
import com.pedrozc90.prototype.core.di.AppViewModelProvider
import com.pedrozc90.prototype.ui.components.SelectField
import com.pedrozc90.prototype.ui.screens.devices.DevicesContent
import com.pedrozc90.prototype.ui.theme.PrototypeTheme
import com.pedrozc90.rfid.core.DeviceFrequency

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    modifier: Modifier = Modifier,
    onNavigateBack: () -> Unit,
    onNavigateUp: () -> Unit,
    viewModel: SettingsViewModel = viewModel(factory = AppViewModelProvider.Factory)
) {
    val state by viewModel.uiState.collectAsState()

    var display by remember { mutableStateOf(false) }

    SettingsContent(
        state = state,
        onValueChange = { viewModel.update(it) },
        onClickSelectDevice = { display = !display },
        onClickTestConnection = { viewModel.testConnection() },
        onSaveClick = {
            viewModel.onSave()
            onNavigateUp()
        },

        display = display,
        onDismiss = {
            display = false
            viewModel.stopScan()
        },

        onClickStart = { viewModel.startScan() },
        onClickStop = { viewModel.stopScan() },
        onClickItem = { viewModel.pairDevice(it) },

        modifier = modifier
    )
}


@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun SettingsContent(
    state: SettingsUiState,
    onValueChange: (SettingsUiState) -> Unit,
    onClickSelectDevice: () -> Unit,
    onClickTestConnection: () -> Unit,
    onSaveClick: () -> Unit,

    display: Boolean = false,
    onDismiss: () -> Unit,

    onClickStart: () -> Unit,
    onClickStop: () -> Unit,
    onClickItem: (BluetoothDeviceDto) -> Unit,

    modifier: Modifier = Modifier
) {
    Column(
        verticalArrangement = Arrangement.spacedBy(space = dimensionResource(R.dimen.padding_medium)),
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = modifier
            .fillMaxSize()
            .padding(dimensionResource(R.dimen.padding_medium))
    ) {
        SettingsHeader()
        SettingsBody(
            state = state,
            onValueChange = onValueChange,
            onClickSelectDevice = onClickSelectDevice,
            onClickTestConnection = onClickTestConnection,
            onSaveClick = onSaveClick
        )
    }

    if (display) {
        ModalBottomSheet(
            onDismissRequest = onDismiss,
            modifier = Modifier.fillMaxHeight()
        ) {
            DevicesContent(
                state = state.devices,
                onClickStart = onClickStart,
                onClickStop = onClickStop,
                onClickItem = onClickItem
            )
        }
    }
}

@Composable
private fun SettingsHeader(
    modifier: Modifier = Modifier
) {
    Text(
        text = stringResource(R.string.settings),
        style = MaterialTheme.typography.titleLarge,
        modifier = modifier.fillMaxWidth()
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun SettingsBody(
    state: SettingsUiState,
    onValueChange: (SettingsUiState) -> Unit,
    onClickSelectDevice: () -> Unit,
    onClickTestConnection: () -> Unit,
    onSaveClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val tooltipState = rememberTooltipState()

    Column(
        verticalArrangement = Arrangement.spacedBy(8.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = modifier
            .fillMaxSize()
    ) {
        SelectField(
            enabled = !state.isBuiltIn,
            label = "Device",
            value = state.type,
            items = DeviceType.entries,
            onLabel = { it.label },
            onSelect = { onValueChange(state.copy(type = it)) }
        )

        if (state.type.bluetooth) {
            Button(
                onClick = onClickSelectDevice,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(text = "Select Device")
            }

            TextField(
                enabled = true,
                readOnly = true,
                label = {
                    Text(text = "Device Name / MAC Address")
                },
                value = state.macAddress ?: "",
                onValueChange = { onValueChange(state.copy(macAddress = it)) },
                modifier = Modifier.fillMaxWidth()
            )

            Button(
                onClick = onClickTestConnection,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(text = "Test Connection")
            }
        }

        SelectField(
            enabled = (state.type.bluetooth && !state.macAddress.isNullOrBlank()) || !state.type.bluetooth,
            label = "Frequency",
            value = state.frequency,
            items = DeviceFrequency.options,
            onLabel = { it.label },
            onSelect = { onValueChange(state.copy(frequency = it)) }
        )

        Column(
            verticalArrangement = Arrangement.spacedBy(2.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.fillMaxWidth()
        ) {
            Text(
                text = "Power",
                modifier = Modifier.fillMaxWidth(),
                style = MaterialTheme.typography.labelSmall
            )

            TooltipBox(
                modifier = Modifier,
                positionProvider = TooltipDefaults.rememberTooltipPositionProvider(
                    TooltipAnchorPosition.Above,
                    8.dp
                ),
                tooltip = {
                    PlainTooltip { Text(text = "${state.power} dbm") }
                },
                state = tooltipState
            ) {
                Slider(
                    enabled = state.powerMax > 0,
                    value = state.power.toFloat(),
                    valueRange = state.powerMin.toFloat()..state.powerMax.toFloat(),
                    steps = 99, // max value is divided by steps + 1
                    onValueChange = { onValueChange(state.copy(power = it.toInt())) },
                    // onValueChangeFinished = onValueChangeFinished,
                    modifier = Modifier
                )
            }

            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
                modifier = Modifier.fillMaxWidth(),
            ) {
                Text(text = "${state.powerMin}", style = MaterialTheme.typography.labelMedium)
                Text(text = "${state.powerMax}", style = MaterialTheme.typography.labelMedium)
            }
        }

        Spacer(modifier = Modifier.weight(1f))

        Button(
            enabled = state.isValid(),
            onClick = onSaveClick,
            modifier = Modifier.fillMaxWidth()
        ) {
            Text(text = stringResource(R.string.save))
        }
    }
}

@Preview(showBackground = true)
@Composable
fun SettingsScreenPreview() {
    val state = SettingsUiState(
        type = DeviceType.CHAINWAY_BLE,
        macAddress = "00:11:22:33:44:55",
        power = 50,
        powerMin = 10,
        powerMax = 75
    )
    PrototypeTheme {
        SettingsContent(
            state = state,
            onValueChange = {},
            onClickSelectDevice = {},
            onSaveClick = {},
            display = false,
            onDismiss = {},
            onClickStart = {},
            onClickStop = {},
            onClickItem = {},
            onClickTestConnection = {}
        )
    }
}
