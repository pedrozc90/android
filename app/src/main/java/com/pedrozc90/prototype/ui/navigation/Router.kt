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
import com.pedrozc90.prototype.ui.screens.inventory.InventoryBasicScreen
import com.pedrozc90.prototype.ui.screens.inventory.InventoryBatchScreen
import com.pedrozc90.prototype.ui.screens.login.LoginScreen
import com.pedrozc90.prototype.ui.screens.products.ProductDetailsScreen
import com.pedrozc90.prototype.ui.screens.products.ProductEntryScreen
import com.pedrozc90.prototype.ui.screens.products.ProductListScreen
import com.pedrozc90.prototype.ui.screens.products.ProductRemoteScreen
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

        // Home
        composable(route = Routes.Home.route) {
            HomeScreen()
        }

        // Settings
        composable(route = Routes.Settings.route) {
            SettingsScreen(
                onNavigateBack = onNavigateBack,
                onNavigateUp = onNavigateUp
            )
        }

        // Inventory
        composable(route = Routes.Inventory.route) {
            InventoryBasicScreen()
        }

        composable(route = Routes.InventoryBatch.route) {
            InventoryBatchScreen()
        }

        // Login
        composable(route = Routes.Login.route) {
            LoginScreen(
                onNavigateBack = onNavigateBack,
                onNavigateUp = onNavigateUp,
                onNavigateToHome = {
                    navController.navigate(Routes.Home.route)
                }
            )
        }

        // Products
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

        composable(route = Routes.ProductRemote.route) {
            ProductRemoteScreen(
                onNavigateToItem = { },
                onNavigateToNewItem = { }
            )
        }

        composable(route = Routes.ProductEntry.route) {
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
