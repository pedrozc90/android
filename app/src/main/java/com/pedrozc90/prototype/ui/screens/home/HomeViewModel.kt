package com.pedrozc90.prototype.ui.screens.home

import androidx.lifecycle.ViewModel

class HomeViewModel : ViewModel() {

    val _uiState = HomeUiState()

}

data class HomeUiState(
    val name: String = "Home"
)
