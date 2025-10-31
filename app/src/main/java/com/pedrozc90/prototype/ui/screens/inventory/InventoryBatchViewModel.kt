package com.pedrozc90.prototype.ui.screens.inventory

import android.annotation.SuppressLint
import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.pedrozc90.prototype.Constants
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
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
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
    private val device: RfidDevice,
    private val preferences: PreferencesRepository,
    private val tagRepository: TagRepository,
    private val inventoryRepository: InventoryRepository
) : ViewModel() {
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
            val address = preferences.getDevice()
            device.init(opts = Options(address = address))
        }

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

    private fun handleTagEvent(tag: TagMetadata) {
        _uiState.update { it.copy(received = it.received + 1) }
        enqueue(tag)
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

    fun onDispose() {
        try {
            val closed = _channel.close()
            if (closed) {
                Log.d(TAG, "Channel closed successfully")
            } else {
                Log.e(TAG, "Channel close failed")
            }
        } catch (e: Exception) {
            Log.w(TAG, "Failed to close channel", e)
        }

        if (_consumer.isActive == true) {
            _consumer.cancel()
        }

        if (_job?.isActive == true) {
            _job?.cancel()
            _job = null
        }

        device.stop()
    }

    fun start() {
        if (_uiState.value.isRunning) return
        val started = device.start()
        _uiState.update { it.copy(isRunning = started) }
    }

    fun stop() {
        // stop device first to stop new inputs
        val stopped = device.stop()

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
        Log.d(TAG, "Resetting inventory state")
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
