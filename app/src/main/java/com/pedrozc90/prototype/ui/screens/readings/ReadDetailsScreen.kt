package com.pedrozc90.prototype.ui.screens.readings

import androidx.compose.animation.animateContentSize
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ExpandLess
import androidx.compose.material.icons.filled.ExpandMore
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.pedrozc90.prototype.R
import com.pedrozc90.prototype.data.read.Read
import com.pedrozc90.prototype.ui.AppViewModelProvider
import com.pedrozc90.prototype.ui.theme.PrototypeTheme
import com.pedrozc90.prototype.utils.EpcUtils

@Composable
fun ReadDetailsScreen(
    modifier: Modifier = Modifier,
    model: ReadDetailsViewModel = viewModel(factory = AppViewModelProvider.Factory)
) {
    val state by model.uiState.collectAsState()

    ReadDetailsBody(
        state = state,
        modifier = modifier
    )
}

@Composable
private fun ReadDetailsBody(
    state: ReadDetailsUiState,
    modifier: Modifier = Modifier
) {
    var expanded by remember { mutableStateOf(false) }

    Column(
        verticalArrangement = Arrangement.spacedBy(8.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = modifier
            .fillMaxSize()
    ) {
        Row(
            horizontalArrangement = Arrangement.SpaceEvenly,
            modifier = Modifier.fillMaxWidth()
        ) {
            Text(text = stringResource(R.string.id), style = MaterialTheme.typography.bodyLarge)
            Spacer(modifier = Modifier.weight(1f))
            Text(text = state.readId.toString(), style = MaterialTheme.typography.bodyLarge)
        }

        Row(
            horizontalArrangement = Arrangement.SpaceEvenly,
            modifier = Modifier.fillMaxWidth()
        ) {
            Text(text = stringResource(R.string.tags), style = MaterialTheme.typography.bodyLarge)
            Spacer(modifier = Modifier.weight(1f))
            Text(text = state.tags.size.toString(), style = MaterialTheme.typography.bodyLarge)
        }

        IconButton(
            onClick = { expanded = !expanded },
            modifier = Modifier.fillMaxWidth()
        ) {
            Icon(
                imageVector = if (!expanded) Icons.Filled.ExpandMore else Icons.Filled.ExpandLess,
                contentDescription = "Expand"
            )
        }

        if (expanded) {
            ReadDetailsList(state)
        }
    }
}

@Composable
private fun ReadDetailsList(
    state: ReadDetailsUiState,
    modifier: Modifier = Modifier
) {
    LazyColumn {
        items(items = state.tags, key = { it.id }) { tag ->
            Column(
                modifier = modifier
                    .fillMaxWidth()
                    .animateContentSize(
                        animationSpec = spring(
                            dampingRatio = Spring.DampingRatioMediumBouncy,
                            stiffness = Spring.StiffnessVeryLow
                        )
                    )
                    .padding(8.dp)
            ) {
                Text(
                    text = tag.rfid,
                    style = MaterialTheme.typography.bodyMedium
                )
            }
            HorizontalDivider(color = Color.DarkGray, thickness = 1.dp)
        }
    }
}

@Preview(showBackground = true)
@Composable
fun ReadDetailsBodyPreview() {
    val readId = 765L
    val tags = listOf(
        // 10101010
        EpcUtils.toTag(readId = readId, rfid = "EPC:10101010:0000001"),
        EpcUtils.toTag(readId = readId, rfid = "EPC:10101010:0000002"),
        EpcUtils.toTag(readId = readId, rfid = "EPC:10101010:0000003"),
        EpcUtils.toTag(readId = readId, rfid = "EPC:10101010:0000004"),
        EpcUtils.toTag(readId = readId, rfid = "EPC:10101010:0000005"),
        EpcUtils.toTag(readId = readId, rfid = "EPC:10101010:0000006"),
        EpcUtils.toTag(readId = readId, rfid = "EPC:10101010:0000007"),
        EpcUtils.toTag(readId = readId, rfid = "EPC:10101010:0000008"),
        EpcUtils.toTag(readId = readId, rfid = "EPC:10101010:0000009"),
        EpcUtils.toTag(readId = readId, rfid = "EPC:10101010:0000010"),
        // 10101000
        EpcUtils.toTag(readId = readId, rfid = "EPC:10101000:0000108"),
        EpcUtils.toTag(readId = readId, rfid = "EPC:10101000:0000109"),
        EpcUtils.toTag(readId = readId, rfid = "EPC:10101000:0000110")
    )

    PrototypeTheme {
        ReadDetailsBody(
            state = ReadDetailsUiState(
                readId = readId,
                read = Read(id = readId),
                tags = tags
            )
        )
    }
}

