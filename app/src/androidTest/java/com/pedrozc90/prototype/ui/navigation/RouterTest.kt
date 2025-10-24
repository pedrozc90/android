package com.pedrozc90.prototype.ui.navigation

import androidx.activity.ComponentActivity
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.navigation.compose.ComposeNavigator
import androidx.navigation.testing.TestNavHostController
import com.pedrozc90.prototype.PrototypeApp
import com.pedrozc90.prototype.utils.assertCurrentRoute
import org.junit.Before
import org.junit.Rule
import org.junit.Test

class RouterTest {

    @get:Rule
    val composeTestRule = createAndroidComposeRule<ComponentActivity>()

    private lateinit var navController: TestNavHostController

    @Before
    fun setUp() {
        composeTestRule.setContent {
            navController = TestNavHostController(LocalContext.current).apply {
                navigatorProvider.addNavigator(ComposeNavigator())
            }
            PrototypeApp(navController = navController)
        }
    }

    @Test
    fun router_verifyStartDestination() {
        navController.assertCurrentRoute(Routes.Home)
    }

}
