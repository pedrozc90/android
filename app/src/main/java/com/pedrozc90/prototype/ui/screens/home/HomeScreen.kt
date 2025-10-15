package com.pedrozc90.prototype.ui.screens.home

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.lifecycle.viewmodel.compose.viewModel
import com.pedrozc90.prototype.ui.AppViewModelProvider
import com.pedrozc90.prototype.ui.theme.PrototypeTheme

@Composable
fun HomeScreen(
    modifier: Modifier = Modifier,
    model: HomeViewModel = viewModel(factory = AppViewModelProvider.Factory)
) {
    val state by model.uiState.collectAsState()

    Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(text = state.name)
    }
}

@Preview(showBackground = true)
@Composable
fun HomeScreenPreview() {
    PrototypeTheme {
        HomeScreen()
    }
}
