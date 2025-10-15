package com.pedrozc90.prototype.ui.screens.reader

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.pedrozc90.prototype.data.ReadRepository
import com.pedrozc90.prototype.data.Tag
import com.pedrozc90.prototype.data.TagBaseRepository
import com.pedrozc90.prototype.data.read.Read
import com.pedrozc90.prototype.devices.FakeRfidReader
import com.pedrozc90.prototype.utils.EpcUtils
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.Job
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kotlinx.coroutines.withTimeoutOrNull

interface ReaderViewModelContract {
    val uiState: StateFlow<ReaderUiState>
    fun onStart()
    fun onStop()
    fun onSave()
}

const val TAG = "ReaderViewModel"
const val BATCH_SIZE = 15
const val BATCH_TIMEOUT = 150L // ms

private sealed class ActorCmd {
    data class Epc(val rfid: String) : ActorCmd()
    data class Flush(val ack: CompletableDeferred<Unit>) : ActorCmd()
}

@OptIn(ExperimentalCoroutinesApi::class)
class ReaderViewModel(
    val tagRepository: TagBaseRepository,
    val readRepository: ReadRepository,
) : ViewModel(), ReaderViewModelContract {

    val reader = FakeRfidReader(_delayMs = 1L)

    private var _job: Job? = null

    private val _uiState = MutableStateFlow(ReaderUiState())
    override val uiState = _uiState.asStateFlow()

    // in-memory dedupe for the life of this ViewModel
    private val _processed = mutableSetOf<String>()

    // channel + consumer pattern. UNLIMITED decouples producers from consumer; change to bounded if required.
    private val _channel = Channel<ActorCmd>(capacity = Channel.UNLIMITED)
    private val _consumer = viewModelScope.launch {
        val batch = mutableListOf<String>()
        var flushedAt = System.currentTimeMillis()

        suspend fun flush(now: Long = System.currentTimeMillis()) {
            if (batch.isEmpty()) return
            val payload = batch.toList()
            batch.clear()
            flushedAt = now

            // persist database
            withContext(Dispatchers.IO) {
                processBatch(payload)
            }

            // update UI state on main thread
            withContext(Dispatchers.Main) {
                updateEpcs(payload)
            }
        }

        while (isActive) {
            // wait up to BATCH_TIMEOUT for the next command; if none arrives, timeout returns null
            val cmd = withTimeoutOrNull(BATCH_TIMEOUT) { _channel.receive() }
            if (cmd == null) {
                val now = System.currentTimeMillis()
                val elapsed = now - flushedAt
                if (batch.isNotEmpty() && elapsed >= BATCH_TIMEOUT) {
                    flush(now = now)
                }
                continue
            }

            when (cmd) {
                is ActorCmd.Epc -> {
                    // deduplicate in-memory
                    if (_processed.add(cmd.rfid)) {
                        batch.add(cmd.rfid)
                    }

                    // flush by size
                    if (batch.size >= BATCH_SIZE) {
                        flush()
                    }
                }

                is ActorCmd.Flush -> {
                    // flush synchronously, then signal ack
                    flush()
                    cmd.ack.complete(Unit)
                }
            }
        }

        // flush remaining when consumer stops
        if (batch.isNotEmpty()) {
            flush()
        }
    }

    // PUBLIC API: quickly enqueue an EPC (non-suspending attempt)
    fun enqueueEpc(epc: String) {
        // try to send to actor without suspending; if the buffer is full, drop it
        val result = _channel.trySend(ActorCmd.Epc(rfid = epc))
        if (!result.isSuccess) {
            // optional count dropped events or log it
            Log.e(TAG, "Dropped EPC (actor is busy): $epc")
        }
    }

    // Request flush and wait for completion (used by onSave)
    private suspend fun flushAndAwait() {
        val ack = CompletableDeferred<Unit>()
        _channel.send(ActorCmd.Flush(ack))
        ack.await()
    }

    private suspend fun processBatch(payload: List<String>) {
        _uiState.update { state ->
            val readId = state.readId ?: readRepository.insert(Read())
            val tags = payload.map { rfid -> EpcUtils.toTag(rfid, readId) }
            tagRepository.insertMany(tags)
            state.copy(readId = readId)
        }
    }

    // Actions
    override fun onStart() {
        if (_job?.isActive == true) return

        setRunning(true)

        _job = viewModelScope.launch {
            // collect from reader and forward quickly to the actor (trySend won't suspends)
            reader.flow.collect { value ->
                enqueueEpc(value)
            }
        }

        reader.startReading()
    }

    override fun onStop() {
        reader.stopReading()
        _job?.cancel()
        _job = null
        setRunning(false)
    }

    override fun onSave() {
        // Force flush of pending items by closing the actor or sending a special message.
        // Option: close actor (epcActor.close()) to flush its remaining items and stop it.
        // Here we explicitly flush by sending a small signal and then persist remaining items.
        viewModelScope.launch {
            // request flush and wait for completion
            val ack = CompletableDeferred<Unit>()
            _channel.send(ActorCmd.Flush(ack))
            ack.await()

            // clear UI state after persisting if that's your desired behavior
            resetEpcs()
        }
    }

    // State
    private fun setRunning(value: Boolean) {
        _uiState.update { it.copy(isRunning = value) }
    }

    private fun updateEpcs(value: List<String>) {
        _uiState.update { it.copy(epcs = it.epcs + value) }
    }

    private fun resetEpcs() {
        _uiState.update { it.copy(epcs = emptyList()) }
    }

    private fun setReadId(value: Long) {
        _uiState.update { it.copy(readId = value) }
    }

}

data class ReaderUiState(
    val readId: Long? = null,
    val epcs: List<String> = listOf(),
    val isRunning: Boolean = false
) {
    val counter: Int
        get() = epcs.size

    val lastIndex: Int
        get() = epcs.lastIndex
}
