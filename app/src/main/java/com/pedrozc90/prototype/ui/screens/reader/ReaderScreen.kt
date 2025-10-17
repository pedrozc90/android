package com.pedrozc90.prototype.ui.screens.reader

import androidx.annotation.StringRes
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.grid.rememberLazyGridState
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.dimensionResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.pedrozc90.prototype.R
import com.pedrozc90.prototype.ui.AppViewModelProvider
import com.pedrozc90.prototype.ui.theme.PrototypeTheme

@Composable
fun ReaderScreen(
    onNavigateUp: () -> Unit,
    modifier: Modifier = Modifier,
    model: ReaderViewModelContract = viewModel<ReaderViewModel>(factory = AppViewModelProvider.Factory)
) {
    val state by model.uiState.collectAsState()

    // Ensure the reader is stopped when the Composable is removed from composition
    DisposableEffect(model) {
        onDispose {
            // stop producing events and allow consumer to finish persisting backlog
            model.onStop()
        }
    }

    Column(
        verticalArrangement = Arrangement.SpaceBetween,
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = modifier.fillMaxSize()
    ) {
        // Top: Counter
        ReaderCounter(
            state = state,
            modifier = Modifier.padding(top = 8.dp)
        )

        // Middle: List with a fixed size and scrollable
        ReaderList(
            state = state,
            modifier = Modifier
                .weight(1f)
                .padding(vertical = 8.dp)
        )

        // Bottom: Actions
        ReaderActions(
            state = state,
            textId = if (state.isRunning) R.string.stop_reading else R.string.start_reading,
            onClick = {
                if (state.isRunning) {
                    model.onStop()
                } else {
                    model.onStart()
                }
            },
            onSaveEnabled = !state.isRunning && state.counter > 0,
            onSave = { model.onSave() },
            onGoBack = onNavigateUp,
            modifier = Modifier
        )
    }
}

@Composable
private fun ReaderCounter(
    state: ReaderUiState,
    modifier: Modifier = Modifier
) {
    Column {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center,
            modifier = modifier
                .fillMaxWidth()
                .padding(horizontal = 8.dp)
        ) {
            Text(
                text = stringResource(R.string.epcs_read),
                style = MaterialTheme.typography.headlineMedium
            )
            Spacer(modifier = Modifier.padding(horizontal = dimensionResource(R.dimen.padding_medium)))
            Text(
                text = state.counter.toString(),
                style = MaterialTheme.typography.headlineLarge
            )
        }
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center,
            modifier = modifier
                .fillMaxWidth()
                .padding(horizontal = 8.dp)
        ) {
            Text(
                text = "Pending:",
                style = MaterialTheme.typography.headlineMedium
            )
            Spacer(modifier = Modifier.padding(horizontal = dimensionResource(R.dimen.padding_medium)))
            Text(
                text = state.pending.toString(),
                style = MaterialTheme.typography.headlineLarge
            )
        }
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center,
            modifier = modifier
                .fillMaxWidth()
                .padding(horizontal = 8.dp)
        ) {
            Text(
                text = "Repeats:",
                style = MaterialTheme.typography.headlineMedium
            )
            Spacer(modifier = Modifier.padding(horizontal = dimensionResource(R.dimen.padding_medium)))
            Text(
                text = state.repeats.toString(),
                style = MaterialTheme.typography.headlineLarge
            )
        }
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center,
            modifier = modifier
                .fillMaxWidth()
                .padding(horizontal = 8.dp)
        ) {
            Text(
                text = "In Batch:",
                style = MaterialTheme.typography.headlineMedium
            )
            Spacer(modifier = Modifier.padding(horizontal = dimensionResource(R.dimen.padding_medium)))
            Text(
                text = state.inBatch.toString(),
                style = MaterialTheme.typography.headlineLarge
            )
        }
    }
}

@Composable
private fun ReaderList(
    state: ReaderUiState, modifier: Modifier = Modifier
) {
    val gridState = rememberLazyGridState()

    // automatically scroll to the bottom when a new item is added
    LaunchedEffect(state.lastIndex) {
        val lastVisible = gridState.layoutInfo.visibleItemsInfo.lastOrNull()?.index
        if (state.lastIndex >= 0) {
            // gridState.animateScrollToItem(state.lastIndex)
            gridState.scrollToItem(state.lastIndex)
        }
    }

    LazyVerticalGrid(
        state = gridState,
        columns = GridCells.Fixed(1),
        modifier = modifier.padding(horizontal = 4.dp),
    ) {
        items(items = state.epcs) { row ->
            Text(
                text = row,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(4.dp)
            )
            HorizontalDivider()
        }
    }
}

@Composable
private fun ReaderActions(
    state: ReaderUiState,
    @StringRes textId: Int,
    onClick: () -> Unit,
    onSaveEnabled: Boolean,
    onSave: () -> Unit,
    onGoBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        verticalArrangement = Arrangement.spacedBy(8.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = modifier.fillMaxWidth()
    ) {
        // Start / Stop Reading Button
        Button(
            enabled = !state.isStopping, onClick = onClick, modifier = Modifier.fillMaxWidth()
        ) {
            Box(modifier = Modifier.fillMaxWidth()) {
                Text(
                    text = stringResource(textId),
                    modifier = Modifier.align(Alignment.Center)
                )

                state.isStopping.let {
                    if (it) {
                        CircularProgressIndicator(
                            color = MaterialTheme.colorScheme.secondary,
                            trackColor = MaterialTheme.colorScheme.surfaceVariant,
                            modifier = Modifier
                                .size(20.dp)
                                .align(Alignment.CenterEnd)
                        )
                    }
                }
            }
        }

        // Save Button
        Button(
            enabled = onSaveEnabled, onClick = onSave, modifier = Modifier.fillMaxWidth()
        ) {
            Text(text = stringResource(R.string.save))
        }

        // Return Button
        Button(
            onClick = onGoBack, modifier = Modifier.fillMaxWidth()
        ) {
            Text(text = stringResource(R.string.go_back))
        }
    }
}

@Preview(showBackground = true)
@Composable
fun HomeScreenPreview() {
    PrototypeTheme {
        ReaderScreen(
            onNavigateUp = {})
    }
}
