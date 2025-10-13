package com.pedrozc90.prototype.ui.screens.reader

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.pedrozc90.prototype.data.Tag
import com.pedrozc90.prototype.data.TagBaseRepository
import com.pedrozc90.prototype.devices.FakeRfidReader
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

interface ReaderViewModelContract {
    val _uiState: StateFlow<ReaderUiState>
    fun onStart()
    fun onStop()
    fun onSave()
}

class ReaderViewModel(
    val repository: TagBaseRepository
) : ViewModel(), ReaderViewModelContract {

    val reader = FakeRfidReader(_delayMs = 10L)

    var _job: Job? = null

    private val _isRunning = MutableStateFlow<Boolean>(false)

    // keep a internal mutable list to avoid StateFlow issues with mutability
    private val _internalEpcList = mutableListOf<String>()
    private val _epcList = MutableStateFlow<List<String>>(emptyList())

    override val _uiState = combine(_epcList, _isRunning) { epcs, isRunning ->
        ReaderUiState(
            epcs = epcs,
            isRunning = isRunning
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000),
        initialValue = ReaderUiState()
    )

    override fun onStart() {
        startReading()
    }

    fun startReading() {
        if (_job?.isActive == true) return
        _isRunning.value = true
        _job = viewModelScope.launch {
            reader.flow.collect { epc ->
                // mutate internal list, then publish a snapshot to the StateFlow
                _internalEpcList.add(epc)
                _epcList.value = _internalEpcList.toList()
            }
        }
        reader.startReading()
    }

    override fun onStop() {
        stopReading()
    }

    fun stopReading() {
        reader.stopReading()
        _job?.cancel()
        _job = null
        _isRunning.value = false
    }

    override fun onSave() {
        persistTags()
        _internalEpcList.clear()
        _epcList.value = _internalEpcList.toList()
    }

    private fun persistTags() {
        viewModelScope.launch {
            val epcs = _uiState.value.epcs
            if (epcs.isNotEmpty()) {
                val tags = epcs.map { rfid -> Tag(rfid = rfid) }
                repository.insertMany(tags)
            }
        }
    }

}

data class ReaderUiState(
    val epcs: List<String> = listOf<String>(),
    val isRunning: Boolean = false
) {
    val counter: Int
        get() = epcs.size

    val lastIndex: Int
        get() = epcs.lastIndex
}
