package com.pedrozc90.prototype.ui.navigation

import android.util.Log
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import com.pedrozc90.prototype.ui.screens.home.HomeScreen
import com.pedrozc90.prototype.ui.screens.products.ProductDetailsScreen
import com.pedrozc90.prototype.ui.screens.products.ProductEntryScreen
import com.pedrozc90.prototype.ui.screens.products.ProductListScreen
import com.pedrozc90.prototype.ui.screens.settings.SettingsScreen

@Composable
fun Router(
    navController: NavHostController,
    modifier: Modifier = Modifier
) {
    NavHost(
        navController = navController,
        startDestination = Routes.Home.route,
        modifier = modifier.padding(8.dp)
    ) {
        val onNavigateBack: () -> Unit = { navController.popBackStack() }
        val onNavigateUp: () -> Unit = { navController.navigateUp() }

        composable(route = Routes.Home.route) {
            HomeScreen()
        }

        composable(route = Routes.Settings.route) {
            SettingsScreen()
        }

        composable(route = Routes.Products.route) {
            ProductListScreen(
                onNavigateToItem = { product ->
                    val path = Routes.ProductDetails.createRoute(product.id)
                    navController.navigate(path)
                },
                onNavigateToNewItem = {
                    navController.navigate(Routes.ProductEntry.route)
                }
            )
        }

        Routes.ProductEntry.let { route ->
            composable(route = route.route) { entry ->
                ProductEntryScreen(onNavigateBack = onNavigateBack)
            }
        composable(route = Routes.ProductEntry.route) { entry ->
            ProductEntryScreen(
                onNavigateBack = onNavigateBack,
                onNavigateUp = onNavigateUp
            )
        }

        Routes.ProductDetails.let { route ->
            composable(
                route = route.route,
                arguments = listOf(navArgument(route.ARG_ID) {
                    type = NavType.LongType
                })
            ) { entry ->
                val productId = entry.arguments?.getLong(route.ARG_ID)
                Log.d(route.route, "${route.ARG_ID}: ${productId}")
                ProductDetailsScreen(
                    onNavigateBack = onNavigateBack,
                    onNavigateUp = onNavigateUp
                )
            }
        }

    }

}
