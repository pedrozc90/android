package com.pedrozc90.prototype

import android.util.Log
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material3.DrawerValue
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalNavigationDrawer
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults.topAppBarColors
import androidx.compose.material3.rememberDrawerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.dimensionResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.navigation.NavHostController
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.pedrozc90.prototype.ui.navigation.PrototypeDrawer
import com.pedrozc90.prototype.ui.navigation.Router
import com.pedrozc90.prototype.ui.navigation.Routes
import com.pedrozc90.prototype.ui.theme.PrototypeTheme
import kotlinx.coroutines.launch

private const val TAG = "PrototypeApp"

@Composable
fun PrototypeApp(
    modifier: Modifier = Modifier,
    navController: NavHostController = rememberNavController()
) {
    val entryState by navController.currentBackStackEntryAsState()
    val currentRoute = entryState?.destination?.route
        ?: navController.currentDestination?.route
        ?: Routes.Home.route // "home"
    Log.d(TAG, "Current route: $currentRoute")

    val drawerState = rememberDrawerState(initialValue = DrawerValue.Closed)
    val scope = rememberCoroutineScope()

    ModalNavigationDrawer(
        drawerState = drawerState,
        drawerContent = {
            PrototypeDrawer(
                currentRoute = currentRoute,
                onClickItem = { route ->
                    Log.d(TAG, "Navigating to ${route.route}")
                    navController.navigate(route.route) {
                        // Avoid multiple copies of the same destination when reselecting the same item
                        launchSingleTop = true
                        // Restore state when reselecting a previously selected item
                        restoreState = true
                        // Pop up to the start destination of the graph to avoid building up a large stack of destinations
                        // on the back stack as users select items
                        popUpTo(navController.graph.startDestinationId) {
                            saveState = true
                        }
                    }

                    // Close drawer
                    scope.launch {
                        drawerState.close()
                    }
                },
                modifier = Modifier
            )
        }
    ) {
        PrototypeContent(
            navController = navController,
            onClickDrawer = {
                scope.launch {
                    if (drawerState.isClosed) {
                        drawerState.open()
                    } else {
                        drawerState.close()
                    }
                }
            },
            modifier = modifier
        )
    }
}

@Composable
private fun PrototypeContent(
    navController: NavHostController,
    onClickDrawer: () -> Unit,
    modifier: Modifier
) {
    Scaffold(
        topBar = {
            PrototypeTopBar(
                onNavigate = onClickDrawer
            )
        },
        containerColor = MaterialTheme.colorScheme.background,
        modifier = modifier
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
private fun PrototypeTopBar(
    onNavigate: () -> Unit,
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
        navigationIcon = {
            IconButton(onClick = onNavigate) {
                Icon(
                    imageVector = Icons.Default.Menu,
                    contentDescription = "Menu"
                )
            }
        },
        modifier = modifier
    )
}

@Preview(showBackground = true)
@Composable
fun PrototypeAppPreview() {
    val navController = rememberNavController()
    LaunchedEffect(Unit) {
        navController.navigate(Routes.Home.route)
    }

    PrototypeTheme {
        PrototypeApp(navController = navController)
    }
}

@Preview(showBackground = true)
@Composable
fun PrototypeAppDarkThemePreview() {
    val navController = rememberNavController()
    LaunchedEffect(Unit) {
        navController.navigate(Routes.Home.route)
    }

    PrototypeTheme(darkTheme = true) {
        PrototypeApp(navController = navController)
    }
}
