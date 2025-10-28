package com.pedrozc90.prototype.ui.screens.inventory

import com.pedrozc90.rfid.objects.TagMetadata

data class InventoryUiState(
    val inventoryId: Long? = null,
    val items: List<TagMetadata> = emptyList(),
    val isRunning: Boolean = false,
    val isStopping: Boolean = false,
    val pending: Int = 0,
    val repeated: Int = 0
) {

    val counter: Int
        get() = items.size

    val lastIndex: Int
        get() = items.lastIndex

}
