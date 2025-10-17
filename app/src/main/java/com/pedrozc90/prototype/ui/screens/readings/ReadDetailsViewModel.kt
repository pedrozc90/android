package com.pedrozc90.prototype.ui.screens.readings

import android.util.Log
import androidx.compose.foundation.interaction.DragInteraction
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.pedrozc90.prototype.data.ReadRepository
import com.pedrozc90.prototype.data.Tag
import com.pedrozc90.prototype.data.read.Read
import com.pedrozc90.prototype.ui.navigation.Routes
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

const val TAG = "ReadDetailsViewModel"

class ReadDetailsViewModel(
    private val readRepository: ReadRepository,
    savedStateHandle: SavedStateHandle
) : ViewModel() {

    private val _uiState = MutableStateFlow(ReadDetailsUiState())
    val uiState = _uiState.asStateFlow()

    private val readId: Long = checkNotNull(savedStateHandle[Routes.ReadDetails.ARG_ID])

    init {
        viewModelScope.launch {
            val result = readRepository.getReadWithTags(readId)
                .filterNotNull()
                .first()
            _uiState.update {
                it.copy(
                    readId = readId,
                    read = result.read,
                    tags = result.tags.sortedBy { it.serialNumber }
                    // ranges = TagRange.chunk(result.tags)
                )
            }
        }
    }

}

data class ReadDetailsUiState(
    val readId: Long? = null,
    val read: Read? = null,
    val tags: List<Tag> = emptyList()
    //val ranges: Map<String, List<TagRange>> = emptyMap()
)

data class TagRange(
    val tags: List<Tag> = emptyList(),
    val itemReference: String,
    val start: String,
    val end: String
) {
    companion object {

        /**
         * Breaks the incoming list of tags into ranges.
         *
         * Algorithm:
         * 1) Sort tags by itemReference, then by numeric value of rfid (hex).
         * 2) Group by itemReference, and for each group create ranges where subsequent tags
         *    have rfid (interpreted as hex) incrementing by exactly 1.
         *
         * Returns a list of TagRange. For single-tag ranges start == end.
         */
        fun chunk(list: List<Tag>): Map<String, List<TagRange>> {
            if (list.isEmpty()) return emptyMap()

            // Sort by itemReference then by numeric rfid value
            val sorted = list.sortedWith(
                comparator = compareBy<Tag>(
                    { it.itemReference },
                    { it.serialNumber })
            )

            val ranges = mutableListOf<TagRange>()
            val currentTags = mutableListOf<Tag>()
            var prevTag: Tag? = null

            fun flushCurrent() {
                if (currentTags.isNotEmpty()) {
                    val itemReference = currentTags.first().itemReference
                    val startRfid = currentTags.first().rfid
                    val endRfid = currentTags.last().rfid
                    ranges.add(
                        TagRange(
                            tags = currentTags.toList(),
                            itemReference = itemReference,
                            start = startRfid,
                            end = endRfid
                        )
                    )
                    currentTags.clear()
                }
            }

            for (tag in sorted) {
                // New itemReference group -> flush and start new
                if (prevTag == null || tag.itemReference != prevTag.itemReference) {
                    flushCurrent()
                    currentTags.add(tag)
                    prevTag = tag
                    continue
                }

                // Same itemReference: check serialNumber increment
                val prevSerial = prevTag.serialNumber
                val curSerial = tag.serialNumber

                if (curSerial == prevSerial + 1) {
                    // contiguous -> extend current range
                    currentTags.add(tag)
                } else {
                    // gap -> flush current range and start a new one
                    flushCurrent()
                    currentTags.add(tag)
                }

                prevTag = tag
            }

            // flush leftover
            flushCurrent()
            return ranges.groupBy { it.itemReference }
        }
    }
}
