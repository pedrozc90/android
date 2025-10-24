package com.pedrozc90.prototype.ui.navigation

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import com.pedrozc90.prototype.ui.screens.home.HomeScreen
import com.pedrozc90.prototype.ui.screens.settings.SettingsScreen

@Composable
fun Router(
    navController: NavHostController,
    modifier: Modifier = Modifier
) {
    NavHost(
        navController = navController,
        startDestination = Routes.Home.route,
        modifier = modifier
    ) {
        val onNavigateBack: () -> Unit = { navController.popBackStack() }
        val onNavigateUp: () -> Unit = { navController.navigateUp() }

        composable(route = Routes.Home.route) {
            HomeScreen()
        }

        composable(route = Routes.Settings.route) {
            SettingsScreen()
        }

    }

}
