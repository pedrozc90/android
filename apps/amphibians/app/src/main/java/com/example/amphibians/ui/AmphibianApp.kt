package com.example.amphibians.ui

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.amphibians.R
import com.example.amphibians.ui.screens.HomeScreen
import com.example.amphibians.ui.screens.AmphibiansViewModel
import com.example.amphibians.ui.theme.AmphibiansTheme

@Composable
fun AmphibiansApp() {
    Scaffold(
        topBar = { AmphibiansTopBar() },
        modifier = Modifier.fillMaxSize()
    ) { paddings ->
        Surface(
            color = MaterialTheme.colorScheme.background,
            modifier = Modifier.padding(paddings)
        ) {
            val amphibiansViewModel: AmphibiansViewModel =
                viewModel(factory = AmphibiansViewModel.Factory)
            HomeScreen(
                amphibiansUiState = amphibiansViewModel.amphibiansUiState,
                retryAction = amphibiansViewModel::getAmphibians
            )
        }
    }
}

@Composable
fun AmphibiansTopBar(modifier: Modifier = Modifier) {
    Text(
        text = stringResource(R.string.app_name),
        style = MaterialTheme.typography.headlineMedium,
        textAlign = TextAlign.Center,
        modifier = modifier.fillMaxWidth()
    )
}

@Preview(showBackground = true)
@Composable
fun AmphibiansAppPreview() {
    AmphibiansTheme {
        AmphibiansApp()
    }
}
