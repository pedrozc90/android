package com.pedrozc90.prototype.ui.screens.inventory

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.pedrozc90.prototype.Constants
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
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.DelicateCoroutinesApi
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kotlinx.coroutines.withTimeoutOrNull

private sealed class ActorCmd {
    data class Input(val item: TagMetadata) : ActorCmd()
    data class Flush(val ack: CompletableDeferred<Unit>) : ActorCmd()
    object Stop : ActorCmd()
}

private const val TAG = "InventoryBatchViewModel"

class InventoryBatchViewModel(
    private val manager: DeviceManager,
    private val preferences: PreferencesRepository,
    private val tagRepository: TagRepository,
    private val inventoryRepository: InventoryRepository
) : ViewModel() {
    private var device: RfidDevice? = null

    private var _job: Job? = null

    private val _uiState = MutableStateFlow(InventoryUiState())
    val uiState = _uiState.asStateFlow()

    // in-memory dedupe for the life of this ViewModel
    private val _uniques = mutableListOf<String>()

    // channel + consumer pattern. UNLIMITED decouples producers from consumer; change to bounded if required.
    private val _channel = Channel<ActorCmd>(capacity = Channel.UNLIMITED)
    private val _consumer = viewModelScope.launch {
        val batch = mutableListOf<TagMetadata>()
        var flushedAt = System.currentTimeMillis()

        suspend fun flush(now: Long = System.currentTimeMillis()) {
            if (batch.isEmpty()) return

            val list = batch.toList()
            batch.clear()
            flushedAt = now

            // persist to database
            withContext(Dispatchers.IO) {
                persistBatch(list)
            }

            // update UI state on main thread
            withContext(Dispatchers.Main) {
                _uiState.update { it.copy(items = it.items + list) }
            }
        }

        while (isActive) {
            // wait up to BATCH_TIMEOUT for the next command; if none arrives, flush any pending batch
            val cmd = withTimeoutOrNull(Constants.BATCH_TIMEOUT) { _channel.receive() }
            if (cmd == null) {
                val now = System.currentTimeMillis()
                val elapsed = now - flushedAt
                if (batch.isNotEmpty() && elapsed >= Constants.BATCH_TIMEOUT) {
                    flush(now = now)
                }
                continue
            }

            when (cmd) {
                is ActorCmd.Input -> {
                    // deduplicate in-memory
                    if (_uniques.add(cmd.item.rfid)) {
                        batch.add(cmd.item)
                    } else {
                        _uiState.update { it.copy(repeated = it.repeated + 1) }
                    }

                    // flush by size
                    if (batch.size >= Constants.BATCH_SIZE) {
                        flush()
                    }
                }

                is ActorCmd.Flush -> {
                    // flush synchronously, then signal ack
                    flush()
                    cmd.ack.complete(Unit)
                }

                is ActorCmd.Stop -> {
                    flush()
                    _uiState.update { it.copy(isStopping = true) }
                    break
                }
            }
        }

        // flush remaining when consumer stops
        if (batch.isNotEmpty()) {
            flush()
        }

        // consumer stopped
        _uiState.update { it.copy(isStopping = false) }
    }


    // PUBLIC API
    fun enqueue(item: TagMetadata) {
        _uiState.update { it.copy(pending = it.pending + 1) }

        // try to send to actor without suspending; if the buffer is full, drop it
        val result = _channel.trySend(ActorCmd.Input(item))
        if (!result.isSuccess) {
            Log.e(TAG, "Dropped Epc ${item.rfid} (actor is busy)")
        }
    }

    // Request flush and wait for completion (used by onSave)
    private suspend fun flushAndAwait() {
        val ack = CompletableDeferred<Unit>()
        _channel.send(ActorCmd.Flush(ack))
        ack.await()
    }

    private suspend fun persistBatch(items: List<TagMetadata>) {
        val state = _uiState.value

        val inventoryId = state.inventoryId ?: inventoryRepository.insert(Inventory())
        Log.d(TAG, "Persisting batch of ${items.size} items to inventory $inventoryId")

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

    // ACTIONS
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
            } catch (tr: Throwable) {
                Log.e(TAG, "Error during device initialization", tr)
            }
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

    private fun handleTagEvent(tag: TagMetadata) {
        _uiState.update { it.copy(received = it.received + 1) }
        enqueue(tag)
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

    @OptIn(DelicateCoroutinesApi::class)
    fun onDispose() {
        try {
            Log.d(TAG, "Closing channel ${_channel}")
            if (!_channel.isClosedForSend) {
                val closed = _channel.close()
                if (closed) {
                    Log.d(TAG, "Channel closed successfully")
                } else {
                    Log.e(TAG, "Channel close failed")
                }
            }
        } catch (e: Exception) {
            Log.w(TAG, "Failed to close channel", e)
        }

        if (_consumer.isActive) {
            _consumer.cancel()
        }

        if (_job?.isActive == true) {
            _job?.cancel()
            _job = null
        }

        // stop device (if you want to ensure it's not scanning) and close resources
        try {
            device?.stop()
            device?.close()
        } catch (t: Throwable) {
            Log.w(TAG, "Error while closing device", t)
        }
    }

    fun start() {
        if (_uiState.value.isRunning) return
        val started = device?.start() ?: false
        _uiState.update { it.copy(isRunning = started) }
    }

    fun stop() {
        // stop device first to stop new inputs
        val stopped = device?.stop() ?: false

        // update state
        _uiState.update { it.copy(isRunning = !stopped) }

        viewModelScope.launch {
            try {
                // flush pending inputs and stop actor
                val ack = CompletableDeferred<Unit>()
                _channel.send(ActorCmd.Flush(ack))
                ack.await()

                // send stop command
                _channel.send(ActorCmd.Stop)
            } catch (e: Exception) {
                Log.e(TAG, "Error while sending flush/stop", e)
                // ensure isStopping cleared even if signaling failed
                _uiState.update { it.copy(isStopping = false) }
            }
        }
    }

    fun reset() {
        _uiState.update { InventoryUiState() }
    }

    fun save() {
        // Force flush of pending items by closing the actor or sending a special message.
        // Option: close actor (epcActor.close()) to flush its remaining items and stop it.
        // Here we explicitly flush by sending a small signal and then persist remaining items.
        viewModelScope.launch {
            // request flush and wait for completion
            val ack = CompletableDeferred<Unit>()
            _channel.send(ActorCmd.Flush(ack))
            ack.await()

            // clear UI state after persisting if that's your desired behavior
            _uiState.update { it.copy(items = emptyList(), pending = 0, repeated = 0) }
        }
    }

    // ensure we cleanup and stop consumer when ViewModel is cleared
    override fun onCleared() {
        super.onCleared()
        onDispose()
    }

}
