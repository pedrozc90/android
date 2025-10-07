package com.pedrozc90.prototype.ui.navigation

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import com.pedrozc90.prototype.ui.screens.home.HomeScreen
import com.pedrozc90.prototype.ui.screens.reader.ReaderScreen

@Composable
fun Router(
    navController: NavHostController,
    modifier: Modifier = Modifier
) {
    NavHost(
        navController = navController,
        startDestination = Routes.HOME.name,
        modifier = modifier
    ) {
        composable(route = Routes.HOME.name) {
            HomeScreen()
        }

        composable(route = Routes.READER.name) {
            ReaderScreen(
                nagivateBack = { navController.navigateUp() }
            )
        }
    }
}
