package com.pedrozc90.prototype.ui.screens.reader

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.pedrozc90.prototype.devices.FakeRfidReader
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class ReaderViewModel() : ViewModel() {

    val reader = FakeRfidReader(_delayMs = 10L)

    private val _isRunning = MutableStateFlow<Boolean>(false)
    private val _epcList = MutableStateFlow<List<String>>(emptyList())

    var _job: Job? = null

    val _uiState = combine(_epcList, _isRunning) { epcs, isRunning ->
        ReaderUiState(
            epcs = epcs,
            isRunning = isRunning
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000),
        initialValue = ReaderUiState()
    )

    fun startReading() {
        if (_job?.isActive == true) return
        _isRunning.value = true
        _job = viewModelScope.launch {
            reader.flow.collect { epc ->
                _epcList.value = _epcList.value + epc
            }
        }
        reader.startReading()
    }

    fun stopReading() {
        reader.stopReading()
        _job?.cancel()
        _job = null
        _isRunning.value = false
    }

}

data class ReaderUiState(
    val epcs: List<String> = listOf<String>(),
    val isRunning: Boolean = false
) {
    fun counter(): Int = epcs.size
}
