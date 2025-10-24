package com.pedrozc90.prototype.ui.navigation

import android.util.Log
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalDrawerSheet
import androidx.compose.material3.NavigationDrawerItem
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.pedrozc90.prototype.R
import com.pedrozc90.prototype.ui.theme.PrototypeTheme

@Composable
fun PrototypeDrawer(
    modifier: Modifier = Modifier,
    currentRoute: String,
    routes: List<Routes> = Routes.menu,
    onClickItem: (route: Routes) -> Unit
) {
    val scrollState = rememberScrollState()

    ModalDrawerSheet(
        modifier = modifier
    ) {
        Column(
            modifier = Modifier
                .padding(horizontal = 16.dp)
                .verticalScroll(scrollState)
        ) {
            Spacer(modifier = Modifier.height(12.dp))

            Text(
                text = stringResource(R.string.app_name),
                style = MaterialTheme.typography.titleLarge,
                modifier = Modifier.padding(16.dp)
            )

            HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp))

            routes.forEachIndexed { index, route ->
                PrototypeDrawerItem(
                    index = index,
                    route = route,
                    selected = route.route == currentRoute,
                    onClick = { onClickItem(route) }
                )
            }

            HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp))

            Routes.Settings.let { route ->
                PrototypeDrawerItem(
                    route = route,
                    selected = route.route == currentRoute,
                    onClick = { onClickItem(route) }
                )
            }
            Spacer(modifier = Modifier.height(12.dp))
        }
    }
}

@Composable
private fun PrototypeDrawerItem(
    index: Int = 0,
    route: Routes,
    selected: Boolean = false,
    onClick: () -> Unit
) {
    val title = stringResource(route.title)

    NavigationDrawerItem(
        label = { Text(text = title) },
        icon = {
            route.icon?.let {
                Icon(
                    imageVector = if (selected) it.selected else it.unselected,
                    contentDescription = title
                )
            }
        },
        selected = selected,
        onClick = onClick
    )
}

@Preview(showBackground = true)
@Composable
fun PrototypeAppPreview() {
    PrototypeTheme {
        PrototypeDrawer(
            currentRoute = "home",
            onClickItem = { Log.d("Drawer", "Clicked on ${it.route}") }
        )
    }
}
