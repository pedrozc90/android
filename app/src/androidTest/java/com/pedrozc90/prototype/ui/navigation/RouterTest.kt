package com.pedrozc90.prototype.ui.navigation

import androidx.activity.ComponentActivity
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.performClick
import androidx.navigation.compose.ComposeNavigator
import androidx.navigation.testing.TestNavHostController
import com.pedrozc90.prototype.PrototypeApp
import com.pedrozc90.prototype.R
import com.pedrozc90.prototype.utils.assertCurrentRoute
import com.pedrozc90.prototype.utils.onNodeWithStringId
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
        navController.assertCurrentRoute(Routes.HOME)
    }

    @Test
    fun router_verifyFloatingButtonIsShownOnHome() {
        val addButton = composeTestRule.onNodeWithContentDescription("Add")
        addButton.assertExists()
    }

    @Test
    fun router_clickAddButton_navigateToReaderScreen() {
        navigateToReaderScreen()
        navController.assertCurrentRoute(Routes.READER)
        val addButton = composeTestRule.onNodeWithContentDescription("Add")
        addButton.assertDoesNotExist()
    }

    @Test
    fun router_clickGoBackButton_navigateToHomeScreen() {
        navigateToReaderScreen()
        navController.assertCurrentRoute(Routes.READER)
        val button = composeTestRule.onNodeWithStringId(R.string.go_back)
        button.assertExists()
        button.performClick()
        navController.assertCurrentRoute(Routes.HOME)
    }

    private fun navigateToReaderScreen() {
        val button = composeTestRule.onNodeWithContentDescription("Add")
        button.performClick()
    }

}
