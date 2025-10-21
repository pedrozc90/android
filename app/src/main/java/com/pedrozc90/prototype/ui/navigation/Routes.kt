package com.pedrozc90.prototype.ui.navigation

import androidx.annotation.StringRes
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.SettingsInputAntenna
import androidx.compose.material.icons.outlined.Home
import androidx.compose.material.icons.outlined.PlayArrow
import androidx.compose.material.icons.outlined.Settings
import androidx.compose.material.icons.outlined.SettingsInputAntenna
import androidx.compose.ui.graphics.vector.ImageVector
import com.pedrozc90.prototype.R

/**
 * A sealed class describing all app screens/routes.
 *
 * Each screen has:
 * - route: the nav graph path (use placeholders for args, e.g. "article/{id}")
 * - titleRes: string resource id for title (use @StringRes)
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

    object ReadList : Routes(
        route = "readings",
        title = R.string.readings,
        icon = Icon(selected = Icons.Default.Home, unselected = Icons.Outlined.Home)
    )

    object ReadDetails : Routes(
        route = "reads/{readId}",
        title = R.string.read_details,
        icon = null
    ) {
        const val ARG_ID = "readId"
        fun createRoute(readId: Long) = "reads/${readId}"
    }

    object Reader : Routes(
        route = "reader",
        title = R.string.reader,
        icon = Icon(
            selected = Icons.Default.SettingsInputAntenna,
            unselected = Icons.Outlined.SettingsInputAntenna
        )
    )

    object Scan : Routes(
        route = "scan",
        title = R.string.scan,
        icon = Icon(selected = Icons.Default.PlayArrow, unselected = Icons.Outlined.PlayArrow)
    )

    object Settings : Routes(
        route = "settings",
        title = R.string.settings,
        icon = Icon(selected = Icons.Default.Settings, unselected = Icons.Outlined.Settings)
    )

    companion object {

        val all by lazy { listOf(Home, Reader, Settings) }
        val menu by lazy { listOf(Home, ReadList, Reader) } // items to show in nav drawer/menu

        fun find(route: String): Routes {
            return all.find { it.route == route } ?: Home
        }

    }

    data class Icon(
        val selected: ImageVector,
        val unselected: ImageVector
    )

}
