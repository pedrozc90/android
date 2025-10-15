package com.pedrozc90.prototype.ui.screens.settings

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.Slider
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.pedrozc90.prototype.R
import com.pedrozc90.prototype.ui.AppViewModelProvider
import com.pedrozc90.prototype.ui.theme.PrototypeTheme

@Composable
fun SettingsScreen(
    modifier: Modifier = Modifier,
    model: SettingsViewModel = viewModel(factory = AppViewModelProvider.Factory)
) {
    val state by model.uiState.collectAsState()

    Column(
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = modifier
            .fillMaxWidth()
            .padding(all = 16.dp)
    ) {
        PotencySlider(
            state = state,
            onValueChange = { model.setPotency(it) },
            onValueChangeFinished = { model.persistSettings() }
        )

        Button(
            onClick = { model.resetDatabase() }
        ) {
            Text(text = stringResource(R.string.reset_database))
        }
    }
}

@Composable
fun PotencySlider(
    state: SettingsUiState,
    onValueChange: (Float) -> Unit,
    onValueChangeFinished: () -> Unit,
    modifier: Modifier = Modifier
) {
    val potencyLabel = stringResource(R.string.potency)
    val potencyValue = state.potency
    Text(text = "$potencyLabel (${potencyValue})")
    Slider(
        value = state.potency,
        valueRange = 0f..100f,
        steps = 99, // max value is divided by steps + 1
        onValueChange = onValueChange,
        onValueChangeFinished = onValueChangeFinished,
        modifier = modifier
    )
}

@Preview(showBackground = true)
@Composable
fun SettingsScreenPreview() {
    PrototypeTheme {
        SettingsScreen()
    }
}
