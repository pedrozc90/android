package com.pedrozc90.prototype.ui.screens.settings

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
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
fun SettingsScreen(
    modifier: Modifier = Modifier,
    model: SettingsViewModel = viewModel(factory = AppViewModelProvider.Factory)
) {
    val state by model.uiState.collectAsState()

    SettingsContent(
        state = state,
        modifier = modifier
    )
}

@Composable
private fun SettingsContent(
    state: SettingsUiState,
    modifier: Modifier = Modifier
) {
    Column(
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = modifier
            .fillMaxSize()
    ) {
        Text(
            text = stringResource(R.string.settings),
            style = MaterialTheme.typography.titleLarge
        )
    }
}

@Preview(showBackground = true)
@Composable
fun SettingsScreenPreview() {
    PrototypeTheme {
        SettingsContent(state = SettingsUiState())
    }
}
