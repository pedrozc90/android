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
    private val tagRepository: TagRepository,
    private val inventoryRepository: InventoryRepository
) : ViewModel() {

    private val device = FakeRfidDevice()

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
    fun start() {
        if (_job?.isActive == true) return

        _uiState.update { it.copy(isRunning = true) }

        _job = viewModelScope.launch {
            // collect from reader and forward quickly to the actor (trySend won't suspends)
            device.flow.collect { item ->
                enqueue(item)
            }
        }

        device.start()
    }

    fun stop() {
        // stop device first to stop new inputs
        device.stop()

        // stop actor
        _job?.cancel()
        _job = null

        // update state
        _uiState.update { it.copy(isRunning = false) }

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
        try {
            _channel.close()
        } catch (e: Exception) {
            Log.w(TAG, "channel close error", e)
        }
        _consumer.cancel()
        _job?.cancel()
        device.stop()
    }

}
