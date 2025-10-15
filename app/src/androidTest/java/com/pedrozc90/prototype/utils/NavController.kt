package com.pedrozc90.prototype.utils

import androidx.navigation.NavController
import com.pedrozc90.prototype.ui.navigation.Routes
import org.junit.Assert.assertEquals

fun NavController.assertCurrentRoute(route: Routes) {
    assertEquals(route.route, this.currentBackStackEntry?.destination?.route)
}
