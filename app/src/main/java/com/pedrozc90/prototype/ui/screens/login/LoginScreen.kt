package com.pedrozc90.prototype.ui.screens.login

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.pedrozc90.prototype.R
import com.pedrozc90.prototype.core.di.AppViewModelProvider
import com.pedrozc90.prototype.ui.theme.PrototypeTheme
import kotlinx.coroutines.launch

@Composable
fun LoginScreen(
    modifier: Modifier = Modifier,
    onNavigateBack: () -> Unit,
    onNavigateUp: () -> Unit,
    viewModel: LoginViewModel = viewModel(factory = AppViewModelProvider.Factory),
    onNavigateToHome: () -> Unit
) {
    val state = viewModel.uiState
    val coroutineScope = rememberCoroutineScope()

    LoginContent(
        state = state,
        onValueChange = viewModel::update,
        onClickLogin = {
            coroutineScope.launch {
                viewModel.onLogin()
                onNavigateToHome()
            }
        },
        modifier = modifier
    )
}

@Composable
private fun LoginContent(
    modifier: Modifier = Modifier,
    state: LoginUiState,
    onValueChange: (LoginUiState) -> Unit,
    onClickLogin: () -> Unit
) {
    Column(
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = modifier.fillMaxSize()
    ) {
        Column(
            verticalArrangement = Arrangement.spacedBy(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = modifier.fillMaxWidth()
        ) {
            TextField(
                enabled = true,
                label = { Text(text = stringResource(R.string.username)) },
                value = state.username,
                onValueChange = { onValueChange(state.copy(username = it)) },
                modifier = Modifier.fillMaxWidth()
            )

            TextField(
                enabled = true,
                label = { Text(text = stringResource(R.string.password)) },
                value = state.password,
                onValueChange = { onValueChange(state.copy(password = it)) },
                modifier = Modifier.fillMaxWidth()
            )

            Button(
                enabled = state.isValid,
                onClick = onClickLogin,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(text = stringResource(R.string.login))
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun LoginScreenPreview() {
    PrototypeTheme {
        LoginContent(
            state = LoginUiState(),
            onValueChange = {},
            onClickLogin = {}
        )
    }
}
