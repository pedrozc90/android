package com.pedrozc90.prototype.ui.screens.devices

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Bluetooth
import androidx.compose.material.icons.filled.BluetoothConnected
import androidx.compose.material.icons.filled.BluetoothDisabled
import androidx.compose.material.icons.filled.Devices
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.outlined.Bluetooth
import androidx.compose.material3.Card
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.pedrozc90.prototype.R
import com.pedrozc90.prototype.core.bluetooth.BluetoothDeviceDto
import com.pedrozc90.prototype.core.di.AppViewModelProvider
import com.pedrozc90.prototype.ui.theme.Background
import com.pedrozc90.prototype.ui.theme.PrototypeTheme

@Composable
fun DevicesScreen(
    modifier: Modifier = Modifier,
    viewModel: DevicesViewModel = viewModel(factory = AppViewModelProvider.Factory)
) {
    val state by viewModel.uiState.collectAsState()

    DevicesContent(
        state = state,
        onClickStart = { viewModel.startScan() },
        onClickStop = { viewModel.stopScan() },
        onClickItem = { viewModel.pairDevice(it) },
        modifier = modifier
    )
}

@Composable
fun DevicesContent(
    modifier: Modifier = Modifier,
    state: DevicesUiState,
    onClickStart: () -> Unit,
    onClickStop: () -> Unit,
    onClickItem: (BluetoothDeviceDto) -> Unit,
) {
    val items = state.paired + state.scanned

    Column(
        verticalArrangement = Arrangement.spacedBy(8.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = modifier.fillMaxSize()
    ) {
        DevicesActions(
            scanning = state.scanning,
            onClickStart = onClickStart,
            onClickStop = onClickStop
        )

        DevicesList(
            items = items,
            onClickItem = onClickItem
        )
    }
}

@Composable
private fun DevicesActions(
    modifier: Modifier = Modifier,
    scanning: Boolean = true,
    onClickStart: () -> Unit,
    onClickStop: () -> Unit
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.End,
        modifier = modifier.fillMaxWidth()
    ) {
        Spacer(modifier = Modifier.weight(1f))

        val textId = when (scanning) {
            true -> R.string.scanning
            false -> R.string.scan_devices
        }
        Text(text = stringResource(textId))

        IconButton(
            onClick = if (scanning) onClickStop else onClickStart,
            colors = IconButtonDefaults.iconButtonColors().copy(containerColor = Background)
        ) {
            if (scanning) {
                val connectingDescription = stringResource(R.string.connecting_to_bluetooth_device)
                CircularProgressIndicator(
                    modifier = Modifier
                        .size(24.dp)
                        .semantics { contentDescription = connectingDescription },
                    strokeWidth = 2.dp
                )
            } else {
                Icon(
                    imageVector = Icons.Default.Refresh,
                    contentDescription = stringResource(R.string.scan_devices)
                )
            }
        }
    }
}

@Composable
private fun DevicesList(
    modifier: Modifier = Modifier,
    items: List<BluetoothDeviceDto>,
    onClickItem: (BluetoothDeviceDto) -> Unit
) {
    if (items.isEmpty()) {
        Column(
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.fillMaxWidth()
        ) {
            Icon(imageVector = Icons.Default.Devices, contentDescription = "No Devices")
            Text(text = "No Devices found")
        }
    } else {
        LazyColumn(
            verticalArrangement = Arrangement.spacedBy(8.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = modifier.fillMaxSize()
        ) {
            itemsIndexed(items = items) { idx, device ->
                DeviceItem(
                    device = device,
                    onClick = { onClickItem(device) }
                )
            }
        }
    }
}

@Composable
private fun DeviceItem(
    modifier: Modifier = Modifier,
    status: String? = null,
    device: BluetoothDeviceDto,
    onClick: () -> Unit
) {
    Card(
        onClick = onClick,
        modifier = modifier.fillMaxWidth()
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            DeviceItemIcon(device = device, status = status)

            Column {
                Text(
                    text = "${device.name} ${device.address}",
                    style = MaterialTheme.typography.bodyLarge
                )

                Text(
                    text = if (device.paired) "Paired" else "Not Paired",
                    style = MaterialTheme.typography.bodySmall
                )
            }

            Spacer(modifier = Modifier.weight(1f))

            DeviceItemStatus(
                device = device,
                status = status
            )
        }
    }
}

@Composable
private fun DeviceItemIcon(
    device: BluetoothDeviceDto,
    status: String? = null
) {
    val imageVector: ImageVector = when (status) {
        "Connecting" -> Icons.Outlined.Bluetooth
        "Connected" -> Icons.Default.BluetoothConnected
        "Disabled" -> Icons.Default.BluetoothDisabled
        else -> Icons.Default.Bluetooth
    }

    Icon(
        imageVector = imageVector,
        contentDescription = device.name,
        modifier = Modifier.padding(12.dp)
    )
}

@Composable
private fun DeviceItemStatus(
    device: BluetoothDeviceDto,
    status: String? = null
) {
    if (status == null) return
    Text(text = status, style = MaterialTheme.typography.bodyMedium)
}

@Preview(showBackground = true)
@Composable
fun DeviceItemPreview() {
    val state = DevicesUiState(
        paired = listOf(
            BluetoothDeviceDto(name = "Device 1", address = "AA:BB:CC:DD:EE:FF", paired = true),
            BluetoothDeviceDto(name = "Device 2", address = "11:22:33:44:55:66", paired = true)
        ),
        scanned = listOf(
            BluetoothDeviceDto(name = "Device 3", address = "77:88:99:AA:BB:CC"),
            BluetoothDeviceDto(name = "Device 4", address = "DD:EE:FF:00:11:22")
        )
    )

    PrototypeTheme {
        DevicesContent(
            state = state,
            onClickStart = {},
            onClickStop = {},
            onClickItem = {}
        )
    }
}

@Preview(showBackground = true)
@Composable
fun EmptyDevicesListPreview() {
    val state = DevicesUiState(
        paired = listOf(),
        scanned = listOf()
    )

    PrototypeTheme {
        DevicesContent(
            state = state,
            onClickStart = {},
            onClickStop = {},
            onClickItem = {}
        )
    }
}
