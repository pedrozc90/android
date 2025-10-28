package com.pedrozc90.prototype.ui.screens.devices

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Pending
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.Card
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.pedrozc90.prototype.core.bluetooth.BluetoothDeviceDto
import com.pedrozc90.prototype.core.di.AppViewModelProvider
import com.pedrozc90.prototype.ui.theme.Green
import com.pedrozc90.prototype.ui.theme.PrototypeTheme

@Composable
fun DevicesScreen(
    modifier: Modifier = Modifier,
    viewModel: DevicesViewModel = viewModel(factory = AppViewModelProvider.Factory)
) {
    val state by viewModel.uiState.collectAsState()

    DevicesContent(
        state = state,
        onStartScan = { viewModel.startScan() },
        onClickItem = { viewModel.pairDevice(it) },
        modifier = modifier
    )
}

@Composable
private fun DevicesContent(
    modifier: Modifier = Modifier,
    state: DevicesUiState,
    onStartScan: () -> Unit,
    onClickItem: (BluetoothDeviceDto) -> Unit,
) {
    val items = state.paired + state.scanned

    Column(
        verticalArrangement = Arrangement.spacedBy(8.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = modifier.fillMaxSize()
    ) {
        DevicesActions(
            onStartScan = onStartScan
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
    onStartScan: () -> Unit
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.End,
        modifier = modifier
    ) {
        Spacer(modifier = Modifier.weight(1f))

        IconButton(
            onClick = onStartScan,
            modifier = Modifier.fillMaxWidth()
        ) {
            Icon(
                imageVector = Icons.Default.Refresh,
                contentDescription = "Scan Devices"
            )
        }
    }
}

@Composable
private fun DevicesList(
    modifier: Modifier = Modifier,
    items: List<BluetoothDeviceDto>,
    onClickItem: (BluetoothDeviceDto) -> Unit
) {
    LazyColumn(
        verticalArrangement = Arrangement.spacedBy(8.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = modifier.fillMaxSize()
    ) {
        itemsIndexed(items = items) { idx, device ->
            DeviceItem(
                index = idx,
                device = device,
                //isConnecting = state.connectingAddress == device.address,
                onClick = { onClickItem(device) }
            )
        }
    }
}

@Composable
private fun DeviceItem(
    modifier: Modifier = Modifier,
    status: String = "Not Paired",
    index: Int = 0,
    device: BluetoothDeviceDto,
    onClick: () -> Unit
) {
    Card {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            modifier = modifier.fillMaxWidth()
        ) {
            Text(text = "#$index")

            Text(text = device.name)

            Spacer(modifier = Modifier.weight(1f))

            Text(text = device.address)

            Spacer(modifier = Modifier.weight(1f))

            IconButton(
                onClick = onClick,
                // enabled = !isConnecting,
                modifier = Modifier
            ) {
                DeviceStatusIcon(status = status)
            }
        }
    }
}

@Composable
private fun DeviceStatusIcon(status: String) {
    var imageVector: ImageVector? = null
    var tint: Color? = null

    if (status == "Connecting") {
        imageVector = Icons.Default.Pending
    } else if (status == "Connected") {
        imageVector = Icons.Default.Check
        tint = Green
    }

    if (imageVector != null) {
        if (tint != null) {
            Icon(imageVector = imageVector, contentDescription = status, tint = tint)
        } else {
            Icon(imageVector = imageVector, contentDescription = status)
        }
    }
}

@Preview(showBackground = true)
@Composable
fun DeviceItemPreview() {
    val state = DevicesUiState(
        paired = listOf(
            BluetoothDeviceDto(name = "Device 1", address = "AA:BB:CC:DD:EE:FF"),
            BluetoothDeviceDto(name = "Device 2", address = "11:22:33:44:55:66")
        ),
        scanned = listOf(
            BluetoothDeviceDto(name = "Device 3", address = "77:88:99:AA:BB:CC"),
            BluetoothDeviceDto(name = "Device 4", address = "DD:EE:FF:00:11:22")
        )
    )

    PrototypeTheme {
        DevicesContent(
            state = state,
            onStartScan = {},
            onClickItem = {}
        )
    }
}
