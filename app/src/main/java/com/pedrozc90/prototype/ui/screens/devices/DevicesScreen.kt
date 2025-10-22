package com.pedrozc90.prototype.ui.screens.devices

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Pending
import androidx.compose.material3.Button
import androidx.compose.material3.HorizontalDivider
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
import com.pedrozc90.prototype.ui.AppViewModelProvider
import com.pedrozc90.prototype.ui.theme.Green_OK
import com.pedrozc90.prototype.ui.theme.PrototypeTheme

@Composable
fun DevicesScreen(
    modifier: Modifier = Modifier,
    model: DevicesViewModel = viewModel(factory = AppViewModelProvider.Factory)
) {
    val state by model.uiState.collectAsState()

    DeviceList(
        state = state,
        onStartScan = { model.startScan() },
        onClickItem = { model.pairDevice(it) },
        modifier = modifier
    )
}

@Composable
private fun DeviceList(
    modifier: Modifier = Modifier,
    state: DevicesUiState,
    onStartScan: () -> Unit,
    onClickItem: (BluetoothDeviceDto) -> Unit,
) {
    Column(
        verticalArrangement = Arrangement.spacedBy(8.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = modifier.fillMaxSize()
    ) {
        Button(
            onClick = onStartScan,
            modifier = Modifier.fillMaxWidth()
        ) {
            Text(text = "Scan Devices")
        }

        val items = state.paired + state.scanned

        LazyColumn(
            //verticalArrangement = Arrangement.Center,
            //horizontalAlignment = Alignment.CenterHorizontally,
            modifier = modifier
                .fillMaxWidth()
                .padding(all = 16.dp)
        ) {
            itemsIndexed(items = items) { idx, device ->
                if (idx != 0) {
                    HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp))
                }
                DeviceItem(
                    device = device,
                    //isConnecting = state.connectingAddress == device.address,
                    onClick = { onClickItem(device) },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 8.dp)
                )
            }
        }

        Spacer(modifier = Modifier.weight(1f))
    }
}

@Composable
private fun DeviceItem(
    modifier: Modifier = Modifier,
    status: String = "Not Paired",
    device: BluetoothDeviceDto,
    onClick: () -> Unit
) {
    var imageVector: ImageVector? = null
    var tint: Color? = null

    if (status == "Connecting") {
        imageVector = Icons.Default.Pending
    } else if (status == "Connected") {
        imageVector = Icons.Default.Check
        tint = Green_OK
    }

    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween,
        modifier = modifier
    ) {
        Text(text = device.name)
        Spacer(modifier = Modifier.weight(1f))
        Text(text = device.address)
        Spacer(modifier = Modifier.weight(1f))
        IconButton(
            onClick = onClick,
            // enabled = !isConnecting,
            modifier = Modifier
        ) {
            if (imageVector != null) {
                if (tint != null) {
                    Icon(imageVector = imageVector, contentDescription = status, tint = tint)
                } else {
                    Icon(imageVector = imageVector, contentDescription = status)
                }
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun DeviceItemPreview() {
    PrototypeTheme {
        DeviceItem(
            device = BluetoothDeviceDto(name = "Device", address = "00:11:22:33:44:55"),
            onClick = {}
        )
    }
}
