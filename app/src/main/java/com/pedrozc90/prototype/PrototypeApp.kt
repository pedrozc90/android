package com.pedrozc90.prototype

import android.util.Log
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults.topAppBarColors
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.dimensionResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.navigation.NavHostController
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.pedrozc90.prototype.ui.navigation.Router
import com.pedrozc90.prototype.ui.navigation.Routes
import com.pedrozc90.prototype.ui.theme.PrototypeTheme

@Composable
fun PrototypeApp(
    modifier: Modifier = Modifier,
    navController: NavHostController = rememberNavController()
) {
    val entryState by navController.currentBackStackEntryAsState()
    val currentRoute = Routes.valueOf(
        entryState?.destination?.route ?: Routes.HOME.name
    )
    Log.d("PrototypeApp", "Current route: $currentRoute")

    Scaffold(
        topBar = {
            PrototypeTopBar()
        },
        floatingActionButton = {
            if (currentRoute == Routes.HOME) {
                FloatingButton(
                    onClick = { navController.navigate(Routes.READER.name) }
                )
            }
        },
        containerColor = MaterialTheme.colorScheme.background,
        modifier = modifier.padding(all = dimensionResource(R.dimen.padding_small))
    ) { innerPaddings ->
        Router(
            navController = navController,
            modifier = Modifier
                .padding(innerPaddings)
                .fillMaxSize()
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PrototypeTopBar(
    modifier: Modifier = Modifier
) {
    TopAppBar(
        colors = topAppBarColors(
            containerColor = MaterialTheme.colorScheme.primaryContainer,
            titleContentColor = MaterialTheme.colorScheme.onPrimaryContainer
        ),
        title = {
            Text(text = stringResource(R.string.app_name))
        },
        modifier = modifier
    )
}

@Composable
fun FloatingButton(
    onClick: () -> Unit
) {
    FloatingActionButton(
        onClick = onClick
    ) {
        Icon(
            imageVector = Icons.Default.Add,
            contentDescription = "Add"
        )
    }
}

@Preview(showBackground = true)
@Composable
fun PrototypeAppPreview() {
    PrototypeTheme {
        PrototypeApp()
    }
}
