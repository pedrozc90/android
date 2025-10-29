package com.pedrozc90.prototype.ui.screens.inventory

import android.annotation.SuppressLint
import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.pedrozc90.prototype.data.db.models.Inventory
import com.pedrozc90.prototype.data.db.models.Tag
import com.pedrozc90.prototype.data.local.PreferencesRepository
import com.pedrozc90.prototype.domain.repositories.InventoryRepository
import com.pedrozc90.prototype.domain.repositories.TagRepository
import com.pedrozc90.rfid.core.Options
import com.pedrozc90.rfid.core.RfidDevice
import com.pedrozc90.rfid.objects.DeviceEvent
import com.pedrozc90.rfid.objects.RfidDeviceStatus
import com.pedrozc90.rfid.objects.TagMetadata
import com.pedrozc90.rfid.utils.EpcUtils
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock

private const val TAG = "InventoryBasicViewModel"

class InventoryBasicViewModel(
    private val device: RfidDevice,
    private val preferences: PreferencesRepository,
    private val tagRepository: TagRepository,
    private val inventoryRepository: InventoryRepository
) : ViewModel() {
    private var _job: Job? = null

    private val _uiState = MutableStateFlow(InventoryUiState())
    val uiState = _uiState.asStateFlow()

    private val _uniques = HashSet<String>()
    private val _uniquesMutex = Mutex()

    fun onInit() {
        viewModelScope.launch {
            val opts = Options(address = preferences.getDevice(), battery = true)
            device.init(opts)
        }

        // start a long-lived collector to process device events sequentially
        if (_job?.isActive != true) {
            _job = viewModelScope.launch {
                device.events.collect { event ->
                    when (event) {
                        is DeviceEvent.TagEvent -> handleTagEvent(event.tag)
                        is DeviceEvent.StatusEvent -> handleStatusEvent(event.status)
                        is DeviceEvent.BatteryEvent -> handleBatteryEvent(event.level)
                        is DeviceEvent.ErrorEvent -> handleErrorEvent(event.throwable)
                    }
                }
            }
        }
    }

    /**
     * Stop the collector and close the device resources.
     * Decide whether this should be called by Composable onDispose() or only from onCleared().
     */
    fun onDispose() {
        // Cancel collector first so it stops processing events
        if (_job?.isActive == true) {
            _job?.cancel()
            _job = null
        }

        // stop device (if you want to ensure it's not scanning) and close resources
        try {
            device.stop()
            device.close()
        } catch (t: Throwable) {
            Log.w(TAG, "Error while closing device", t)
        }

        // update UI state to reflect device is not running
        _uiState.update { it.copy(isRunning = false) }
    }

    fun start() {
        if (_uiState.value.isRunning) return
        val started = device.start()
        _uiState.update { it.copy(isRunning = started) }
    }

    private suspend fun handleTagEvent(tag: TagMetadata) {
        val isNew = _uniquesMutex.withLock { _uniques.add(tag.rfid) }
        if (isNew) {
            _uiState.update { it.copy(received = it.received + 1, items = it.items + tag) }
        } else {
            _uiState.update { it.copy(received = it.received + 1, repeated = it.repeated + 1) }
        }
    }

    @SuppressLint("MissingPermission")
    private fun handleStatusEvent(status: RfidDeviceStatus) {
        val device = status.device?.address ?: status.device?.name ?: "No Device"
        _uiState.update { it.copy(status = status.status, device = device) }
    }

    private fun handleBatteryEvent(level: Int) {
        _uiState.update { it.copy(battery = level) }
    }

    private fun handleErrorEvent(cause: Throwable) {
        Log.e(TAG, "Device error event", cause)
    }

    fun stop() {
        val stopped = device.stop()
        _uiState.update { it.copy(isRunning = !stopped) }
    }

    fun reset() {
        Log.d(TAG, "Resetting inventory state")
    }

    fun persist() {
        viewModelScope.launch {
            val state = uiState.value

            val inventoryId = state.inventoryId ?: inventoryRepository.insert(Inventory())
            if (state.inventoryId == null) {
                _uiState.update { it.copy(inventoryId = inventoryId) }
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

    override fun onCleared() {
        super.onCleared()
        // ensure cleanup if ViewModel is being destroyed
        onDispose()
    }

}
