package com.pedrozc90.prototype.ui.screens.reader

import android.util.Log
import androidx.activity.ComponentActivity
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.lifecycle.ViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import org.junit.Before
import org.junit.Rule
import org.junit.Test

const val TAG = "FakeReaderViewModel"

class FakeReaderViewModel(
    private val state: ReaderUiState
) : ViewModel(), ReaderViewModelContract {

    override val uiState = MutableStateFlow(state)

    override fun onStart() {
        Log.d(TAG, "onStart called")
    }

    override fun onStop() {
        Log.d(TAG, "onStop called")
    }

    override fun onSave() {
        Log.d(TAG, "onSave called")
    }

}

class ReaderScreenTest {

    @get:Rule
    val composeTestRule = createAndroidComposeRule<ComponentActivity>()

    @Before
    fun setUp() {
        val fakeUiState = ReaderUiState(epcs = listOf("EPC1", "EPC2", "EPC3"), isRunning = false)
        val fakeViewModel = FakeReaderViewModel(fakeUiState)

        composeTestRule.setContent {
            ReaderScreen(
                onNavigateUp = {},
                model = fakeViewModel
            )
        }
    }

    @Test
    fun readerScreen_displaysCounterAndList() {
        // Assert: counter is displayed
        composeTestRule.onNodeWithText("3").assertIsDisplayed()

        // Assert: EPC items are displayed
        composeTestRule.onNodeWithText("EPC1").assertIsDisplayed()
        composeTestRule.onNodeWithText("EPC2").assertIsDisplayed()
        composeTestRule.onNodeWithText("EPC3").assertIsDisplayed()
    }

    @Test
    fun readerScreen_startStopButton_clickable() {
        // Button text for "Stop Reading" since isRunning = true
        composeTestRule.onNodeWithText("Stop Reading").performClick()
    }

}
