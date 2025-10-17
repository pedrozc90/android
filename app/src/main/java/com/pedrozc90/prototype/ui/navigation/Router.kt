package com.pedrozc90.prototype.ui.navigation

import android.util.Log
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import com.pedrozc90.prototype.ui.screens.home.HomeScreen
import com.pedrozc90.prototype.ui.screens.reader.ReaderScreen
import com.pedrozc90.prototype.ui.screens.readings.ReadDetailsScreen
import com.pedrozc90.prototype.ui.screens.readings.ReadListScreen
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

        composable(route = Routes.Reader.route) {
            ReaderScreen(
                onNavigateUp = onNavigateUp
            )
        }

        composable(route = Routes.Settings.route) {
            SettingsScreen()
        }

        // route with argument
        composable(route = Routes.ReadList.route) {
            ReadListScreen(
                onNavigateToItem = { navController.navigate(Routes.ReadDetails.createRoute(it)) }
            )
        }

        // route with argument
        Routes.ReadDetails.let { route ->
            composable(
                route = route.route,
                arguments = listOf(navArgument(route.ARG_ID) {
                    type = NavType.LongType
                })
            ) { entry ->
                val id = entry.arguments?.getLong(route.ARG_ID)
                Log.d(route.route, "${ route.ARG_ID }: $id")
                ReadDetailsScreen()
            }
        }
    }

}
