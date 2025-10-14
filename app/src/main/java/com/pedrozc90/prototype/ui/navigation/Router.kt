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
        composable(route = Routes.Home.route) {
            HomeScreen()
        }

        composable(route = Routes.Reader.route) {
            ReaderScreen(
                navigateBack = { navController.navigateUp() }
            )
        }

        composable(route = Routes.Settings.route) {
            SettingsScreen()
        }

        // route with argument
        composable(
            route = Routes.ArticleDetails.route,
            arguments = listOf(navArgument(Routes.ArticleDetails.ARG_ID) {
                type = NavType.LongType
            })
        ) { backStackEntry ->
            val id = backStackEntry.arguments?.getLong(Routes.ArticleDetails.ARG_ID)
            Log.d("Router", "Article id: $id")
            // ArticleDetailScreen(articleId = id)
        }
    }
}
