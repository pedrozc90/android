package com.pedrozc90.prototype.ui.screens.inventory

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.pedrozc90.prototype.core.devices.DeviceManager
import com.pedrozc90.prototype.data.db.models.Inventory
import com.pedrozc90.prototype.data.db.models.Tag
import com.pedrozc90.prototype.data.local.PreferencesRepository
import com.pedrozc90.prototype.domain.repositories.InventoryRepository
import com.pedrozc90.prototype.domain.repositories.TagRepository
import com.pedrozc90.rfid.core.RfidDevice
import com.pedrozc90.rfid.objects.DeviceEvent
import com.pedrozc90.rfid.objects.RfidDeviceStatus
import com.pedrozc90.rfid.objects.TagMetadata
import com.pedrozc90.rfid.utils.EpcUtils
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock

private const val TAG = "InventoryBasicViewModel"

class InventoryBasicViewModel(
    private val manager: DeviceManager,
    private val preferences: PreferencesRepository,
    private val tagRepository: TagRepository,
    private val inventoryRepository: InventoryRepository
) : ViewModel() {
    private var device: RfidDevice? = null

    private var _job: Job? = null

    private val _uiState = MutableStateFlow(InventoryUiState())
    val uiState = _uiState.asStateFlow()

    private val _uniques = HashSet<String>()
    private val _uniquesMutex = Mutex()

    private val _notifications = MutableSharedFlow<String>(extraBufferCapacity = 1)
    val errors = _notifications.asSharedFlow()

    fun onInit() {
        viewModelScope.launch {
            try {
                val type = preferences.deviceType
                if (type == null) {
                    throw UnsupportedOperationException("Unable to identity device type")
                }

                device = manager.build(type)
                Log.d(TAG, "Built device '$device' of type '$type'")

                val settings = preferences.getSettings().first()

                val opts = settings.toRfidOptions()

                _uiState.update { it.copy(settings = settings) }

                device?.init(opts = opts)
            } catch (t: Throwable) {
                Log.e(TAG, "Error during device initialization", t)
                _notifications.tryEmit("Error during device initialization. Reason: ${t.message}")
            }
        }

        if (device == null) {
            return
        }

        // start a long-lived collector to process device events sequentially
        if (_job?.isActive != true) {
            Log.d(TAG, "Starting device event collector job")
            _job = viewModelScope.launch(Dispatchers.IO) {
                device!!.events.collect { event ->
                    when (event) {
                        is DeviceEvent.TagEvent -> handleTagEvent(event.tag)
                        is DeviceEvent.StatusEvent -> handleStatusEvent(event.status)
                        is DeviceEvent.BatteryEvent -> handleBatteryEvent(event.level)
                        is DeviceEvent.ErrorEvent -> handleErrorEvent(event.throwable)
                    }
                }
            }
        } else {
            Log.d(TAG, "Event collector job is already running")
        }
    }

    private suspend fun handleTagEvent(tag: TagMetadata) {
        val isNew = _uniquesMutex.withLock { _uniques.add(tag.rfid) }
        if (isNew) {
            _uiState.update { it.copy(received = it.received + 1, items = it.items + tag) }
        } else {
            _uiState.update { it.copy(received = it.received + 1, repeated = it.repeated + 1) }
        }
    }

    private fun handleStatusEvent(status: RfidDeviceStatus) {
        _uiState.update { it.copy(status = status.status) }
    }

    private fun handleBatteryEvent(level: Int) {
        _uiState.update { it.copy(battery = level) }
    }

    private fun handleErrorEvent(cause: Throwable) {
        Log.e(TAG, "Device error event", cause)
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
        viewModelScope.launch {
            try {
                val device = device
                if (device != null) {
                    device.stop()
                    device.close()
                }
            } catch (t: Throwable) {
                Log.w(TAG, "Error while closing device", t)
            }
        }

        // update UI state to reflect device is not running
        _uiState.update { it.copy(isRunning = false) }
    }

    fun start() {
        viewModelScope.launch(context = Dispatchers.IO) {
            if (!_uiState.value.isRunning) {
                val started = device?.start() ?: false
                _uiState.update { it.copy(isRunning = started) }
            }
        }
    }

    fun stop() {
        viewModelScope.launch(context = Dispatchers.IO) {
            if (_uiState.value.isRunning) {
                val stopped = device?.stop() ?: false
                _uiState.update { it.copy(isRunning = !stopped) }
            }
        }
    }

    fun killTag(tag: TagMetadata) {
        viewModelScope.launch {
            try {
                val device = device
                if (device != null) {
                    val killed = device.kill(rfid = tag.rfid)
                    if (!killed) {
                        throw RuntimeException("Failed to kill tag ${tag.rfid}")
                    }

                    val msg = "Tag ${tag.rfid} killed successfully"
                    Log.d(TAG, msg)
                    _notifications.emit(msg)
                }
            } catch (e: Exception) {
                val msg = e.message ?: "Error killing tag ${tag.rfid}"
                Log.e(TAG, msg, e)
                _notifications.emit(msg)
            }
        }
    }

    fun reset() {
        _uiState.update { InventoryUiState() }
        _uniques.clear()
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

    // ensure we cleanup and stop consumer when ViewModel is cleared
    override fun onCleared() {
        super.onCleared()
        onDispose()
    }

}
