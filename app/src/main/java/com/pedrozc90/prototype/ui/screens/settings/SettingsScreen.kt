package com.pedrozc90.prototype.ui.screens.settings

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Slider
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.dimensionResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.pedrozc90.prototype.R
import com.pedrozc90.prototype.core.di.AppViewModelProvider
import com.pedrozc90.prototype.ui.components.SelectField
import com.pedrozc90.prototype.ui.theme.PrototypeTheme

@Composable
fun SettingsScreen(
    modifier: Modifier = Modifier,
    onNavigateBack: () -> Unit,
    onNavigateUp: () -> Unit,
    viewModel: SettingsViewModel = viewModel(factory = AppViewModelProvider.Factory)
) {
    val state = viewModel.uiState

    SettingsContent(
        state = state,
        onValueChange = { viewModel.update(it) },
        onSaveClick = {
            viewModel.onSave()
            onNavigateUp()
        },
        modifier = modifier
    )
}

@Composable
private fun SettingsContent(
    state: SettingsUiState,
    onValueChange: (SettingsUiState) -> Unit,
    onSaveClick: () -> Unit,
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
            onSaveClick = onSaveClick
        )
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
    onSaveClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        verticalArrangement = Arrangement.spacedBy(8.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = modifier
            .fillMaxSize()
    ) {
        TextField(
            enabled = true,
            readOnly = true,
            label = {
                Text(text = "Device")
            },
            value = state.device,
            onValueChange = { onValueChange(state.copy(device = it)) },
            modifier = Modifier.fillMaxWidth()
        )

        val options = mapOf(
            "china_1" to "China Standard 1 (840~845 MHz)",
            "china_2" to "China Standard 2 (920~925 MHz)",
            "europe" to "Europe Standard (865~868 MHz)",
            "united_states" to "United States Standard (902~928 MHz)",
            "korea" to "Korea (917~923 MHz)",
            "japan" to "Japan (916.8~920.8 MHz)",
            "south_africa" to "South Africa (915~919 MHz)",
            "taiwan" to "Taiwan (920~928 MHz)",
            "vietnam" to "Vietnam (918~923 MHz)",
            "peru" to "Peru (915~928 MHz)",
            "russia" to "Russia (860~867.6 MHz)",
            "morocco" to "Morocco (914~921 MHz)",
            "malaysia" to "Malaysia (919~923 MHz)",
            "brazil" to "Brazil (902~907.5 MHz)",
            "brazil_2" to "Brazil (915~928 MHz)",
        )

        // var frequencyValue by remember { mutableStateOf(options.keys.first()) }
        SelectField(
            label = "Frequency",
            value = state.frequency,
            items = options.keys.toList(),
            onLabel = { options[it] ?: "none" },
            onSelect = { onValueChange(state.copy(frequency = it)) }
        )

        Column(
            verticalArrangement = Arrangement.spacedBy(2.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.fillMaxWidth()
        ) {
            Text(
                text = "Power: ${state.power}",
                modifier = Modifier.fillMaxWidth(),
                style = MaterialTheme.typography.labelSmall
            )
            Slider(
                value = state.power.toFloat(),
                valueRange = state.minPower.toFloat()..state.maxPower.toFloat(),
                steps = 99, // max value is divided by steps + 1
                onValueChange = { onValueChange(state.copy(power = it.toInt())) },
                // onValueChangeFinished = onValueChangeFinished,
                modifier = Modifier
            )
        }

        Spacer(modifier = Modifier.weight(1f))

        Button(
            enabled = state.device.isNotBlank(),
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
        device = "00:11:22:33:44:55",
        power = 50
    )
    PrototypeTheme {
        SettingsContent(
            state = state,
            onValueChange = {},
            onSaveClick = {}
        )
    }
}
