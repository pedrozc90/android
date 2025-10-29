package com.pedrozc90.prototype.ui.navigation

import androidx.annotation.StringRes
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Bluetooth
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Login
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Token
import androidx.compose.material.icons.outlined.Bluetooth
import androidx.compose.material.icons.outlined.Home
import androidx.compose.material.icons.outlined.Login
import androidx.compose.material.icons.outlined.Settings
import androidx.compose.material.icons.outlined.Token
import androidx.compose.ui.graphics.vector.ImageVector
import com.pedrozc90.prototype.R

/**
 * A sealed class describing all app screens/routes.
 *
 * Each screen has:
 * - route: the nav graph path (use placeholders for args, e.g. "article/{id}")
 * - title: string resource id for title (use @StringRes)
 * - icon: optional ImageVector for use in navigation UI (could also use @DrawableRes Int)
 *
 * Use concreteRoute(...) helpers for routes with runtime args.
 */
sealed class Routes(
    val route: String,
    @param:StringRes val title: Int,
    val icon: Icon? = null
) {

    object Home : Routes(
        route = "home",
        title = R.string.home,
        icon = Icon(selected = Icons.Default.Home, unselected = Icons.Outlined.Home)
    )

    object Settings : Routes(
        route = "settings",
        title = R.string.settings,
        icon = Icon(selected = Icons.Default.Settings, unselected = Icons.Outlined.Settings)
    )

    object Devices : Routes(
        route = "devices",
        title = R.string.devices,
        icon = Icon(selected = Icons.Default.Bluetooth, unselected = Icons.Outlined.Bluetooth)
    )

    object Login : Routes(
        route = "login",
        title = R.string.login,
        icon = Icon(selected = Icons.Default.Login, unselected = Icons.Outlined.Login)
    )

    object Products : Routes(
        route = "products",
        title = R.string.products,
        icon = Icon(selected = Icons.Default.Home, unselected = Icons.Outlined.Home)
    )

    object ProductRemote : Routes(
        route = "products/remote",
        title = R.string.products_remote,
        icon = Icon(selected = Icons.Default.Token, unselected = Icons.Outlined.Token)
    )

    object ProductEntry : Routes(
        route = "products/entry",
        title = R.string.products_entry
    )

    object ProductDetails : Routes(
        route = "products/{productId}",
        title = R.string.products_details
    ) {
        const val ARG_ID = "productId"
        fun createRoute(productId: Long) = "products/${productId}"
    }

    companion object {

        val all by lazy {
            listOf(
                Home,
                Login,
                Products,
                ProductEntry,
                ProductRemote,
                ProductDetails,
                Devices,
                Settings
            )
        }

        // items to show in nav drawer/menu
        val menu by lazy {
            listOf(
                Home,
                Login,
                Products,
                ProductRemote,
                Devices
            )
        }

        fun find(route: String): Routes {
            return all.find { it.route == route } ?: Home
        }

    }

    data class Icon(
        val selected: ImageVector,
        val unselected: ImageVector
    )

}
