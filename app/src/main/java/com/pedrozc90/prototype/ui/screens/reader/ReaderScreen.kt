package com.pedrozc90.prototype.ui.screens.reader

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
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
    nagivateBack: () -> Unit,
    modifier: Modifier = Modifier,
    model: ReaderViewModel = viewModel(factory = AppViewModelProvider.Factory)
) {
    Column(
        verticalArrangement = Arrangement.SpaceBetween,
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = modifier.fillMaxSize()
    ) {
        // Top: Counter
        ReaderCounter(
            modifier = Modifier
                .padding(top = 8.dp)
        )

        // Middle: List with a fixed size and scrollable
        ReaderList(
            modifier = Modifier
                .weight(1f)
                .padding(vertical = 8.dp)
        )

        // Bottom: Actions
        ReaderActions(
            onStartReading = {},
            onGoBack = nagivateBack,
            modifier = Modifier
        )
    }
}

@Composable
fun ReaderCounter(
    modifier: Modifier = Modifier
) {
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
        Spacer(
            modifier = Modifier.padding(horizontal = dimensionResource(R.dimen.padding_medium))
        )
        Text(
            text = "0",
            style = MaterialTheme.typography.headlineLarge
        )
    }
}

@Composable
fun ReaderList(
    modifier: Modifier = Modifier
) {
    val data = setOf<String>(
        "a",
        "b",
        "c",
//        "d",
//        "e",
//        "f",
//        "g",
//        "h",
//        "i",
//        "j",
//        "k",
//        "l",
//        "m",
//        "n",
//        "o",
//        "p",
//        "q",
//        "r",
//        "s",
//        "t",
//        "u",
//        "v",
//        "w",
//        "x",
//        "y",
//        "z"
    )

    LazyVerticalGrid(
        columns = GridCells.Fixed(1),
        modifier = modifier
//            .fillMaxSize()
            .padding(horizontal = 4.dp),
    ) {
        items(items = data.toList()) { row ->
            Text(
                text = row,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(4.dp)
            )
        }
    }
}

@Composable
fun ReaderActions(
    onStartReading: () -> Unit,
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
            onClick = onStartReading,
            modifier = Modifier
                .fillMaxWidth()
        ) {
            Text(
                text = stringResource(R.string.start_reading)
            )
        }

        // Return Button
        Button(
            onClick = onGoBack,
            modifier = Modifier
                .fillMaxWidth()
        ) {
            Text(
                text = stringResource(R.string.go_back)
            )
        }
    }
}

@Preview(showBackground = true)
@Composable
fun HomeScreenPreview() {
    PrototypeTheme {
        ReaderScreen(
            nagivateBack = {}
        )
    }
}
