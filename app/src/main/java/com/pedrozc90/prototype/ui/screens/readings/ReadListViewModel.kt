package com.pedrozc90.prototype.ui.screens.readings

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.pedrozc90.prototype.Constants
import com.pedrozc90.prototype.data.ReadRepository
import com.pedrozc90.prototype.data.read.ReadSummary
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn

class ReadListViewModel(
    private val readRepository: ReadRepository
) : ViewModel() {

    val uiState = readRepository.getSummary()
        .map { ReadListUiState(it) }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(Constants.STATE_TIMEOUT),
            initialValue = ReadListUiState()
        )

}

data class ReadListUiState(
    val items: List<ReadSummary> = emptyList()
)
