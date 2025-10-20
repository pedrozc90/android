package com.pedrozc90.rfid.core

import com.pedrozc90.rfid.core.objects.Gen2Dto
import com.pedrozc90.rfid.core.objects.TagMetadata
import kotlinx.coroutines.flow.SharedFlow

interface RfidDevice {

    val flow: SharedFlow<TagMetadata>
    val connectionStatus: SharedFlow<RfidConnectionStatus>

    /**
     * Initialize the SDK/reader. May throw.
     */
    suspend fun init(opts: RfidOptions = RfidOptions())

    suspend fun dispose()

    /**
     * Start inventory. Return true on success.
     */
    suspend fun startInventory(): Boolean

    /**
     * Stop inventory. Return true on success.
     */
    suspend fun stopInventory(): Boolean

    suspend fun getVersion(): String?

    suspend fun getPower(): Int

    suspend fun setPower(value: Int): Boolean

    suspend fun getGen2Settings(): Gen2Dto

    suspend fun setGen2Settings(value: Gen2Dto): Boolean

    suspend fun getBatteryLevel(): Int

}
