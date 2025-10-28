package com.pedrozc90.prototype.ui.screens.inventory

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.pedrozc90.prototype.Constants
import com.pedrozc90.prototype.data.db.models.Inventory
import com.pedrozc90.prototype.data.db.models.Tag
import com.pedrozc90.prototype.domain.repositories.InventoryRepository
import com.pedrozc90.prototype.domain.repositories.TagRepository
import com.pedrozc90.rfid.devices.fake.FakeRfidDevice
import com.pedrozc90.rfid.objects.TagMetadata
import com.pedrozc90.rfid.utils.EpcUtils
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

private const val TAG = "InventoryBasicViewModel"

class InventoryBasicViewModel(
    private val tagRepository: TagRepository,
    private val inventoryRepository: InventoryRepository
) : ViewModel() {

    private val device = FakeRfidDevice()

    private var _job: Job? = null

    private val _inventoryId = MutableStateFlow<Long?>(null)
    private val _uniques = mutableListOf<String>()
    private val _items = MutableStateFlow<List<TagMetadata>>(emptyList())
    private val _running = MutableStateFlow(false)

    val uiState: StateFlow<InventoryUiState> =
        combine(_items, _running, _inventoryId) { items, isRunning, inventoryId ->
            InventoryUiState(items = items, isRunning = isRunning, inventoryId = inventoryId)
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

    fun persist() {
        viewModelScope.launch {
            val state = uiState.value

            val inventoryId = state.inventoryId ?: inventoryRepository.insert(Inventory())
            if (state.inventoryId == null) {
                _inventoryId.update { inventoryId }
            }

            Log.d(TAG, "Persisting ${state.items.size} items to inventory $inventoryId")

            val items = state.items
            if (items.isNotEmpty()) {
                val tags = items.map { row ->
                    val epc = EpcUtils.decode(row.rfid)
                    Tag(
                        rfid = epc.rfid,
                        itemReference = epc.itemReference,
                        serialNumber = epc.serialNumber,
                        inventoryId = inventoryId
                    )
                }
                tagRepository.insertMany(tags)
            }
        }
    }

}
