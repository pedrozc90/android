package com.pedrozc90.prototype.ui.screens.inventory

import com.pedrozc90.rfid.objects.TagMetadata
import com.pedrozc90.rfid.utils.Epc

data class InventoryUiState(
    val device: String? = null,
    val status: String? = null,
    val battery: Int? = null,

    val inventoryId: Long? = null,
    val items: List<TagMetadata> = emptyList(),

    val isRunning: Boolean = false,
    val isStopping: Boolean = false,

    val received: Int = 0,
    val pending: Int = 0,
    val repeated: Int = 0
) {

    val processed: Int
        get() = items.size

    val lastIndex: Int
        get() = items.lastIndex

}

data class InventoryTagMetadata(
    val tag: TagMetadata,
    val epc: Epc? = null
)
