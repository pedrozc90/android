package com.pedrozc90.prototype.ui.screens.reader

import androidx.lifecycle.ViewModel

class ReaderViewModel : ViewModel() {

    val _uiState = ReaderUiState()

}

data class ReaderUiState(
    val buffer: Set<String> = setOf()
)
