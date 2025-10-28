package com.pedrozc90.prototype.ui.screens.inventory

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.pedrozc90.prototype.Constants
import com.pedrozc90.rfid.devices.fake.FakeRfidDevice
import com.pedrozc90.rfid.objects.TagMetadata
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class InventoryViewModel : ViewModel() {

    private val device = FakeRfidDevice()

    private var _job: Job? = null

    private val _uniques = mutableListOf<String>()
    private val _items = MutableStateFlow<List<TagMetadata>>(emptyList())
    private val _running = MutableStateFlow(false)

    val uiState: StateFlow<InventoryUiState> = combine(_items, _running) { items, isRunning ->
        InventoryUiState(items = items, isRunning = isRunning)
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(Constants.STATE_TIMEOUT),
        initialValue = InventoryUiState()
    )

    fun start() {
        if (_job?.isActive == true) return

        _running.update { true }

        _job = viewModelScope.launch {
            device.flow.collect { item ->
                if (_uniques.contains(item.rfid)) {
                    Log.d("ScannerViewModel", "Tag ${item.rfid} already scanned, skipping...")
                } else {
                    _uniques.add(item.rfid)
                    _items.update { it + item }
                }
            }
        }

        device.start()
    }

    fun stop() {
        device.stop()
        _job?.cancel()
        _job = null
        _running.update { false }
    }

}

data class InventoryUiState(
    val items: List<TagMetadata> = emptyList(),
    val isRunning: Boolean = false
) {

    val counter: Int
        get() = items.size

    val lastIndex: Int
        get() = items.lastIndex

}
