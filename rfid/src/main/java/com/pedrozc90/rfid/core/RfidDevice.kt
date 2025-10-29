package com.pedrozc90.rfid.core

import com.pedrozc90.rfid.objects.TagMetadata
import kotlinx.coroutines.flow.SharedFlow

interface RfidDevice {

    /**
     * Flow emitting the scanned tags
     */
    val flow: SharedFlow<TagMetadata>

    /**
     * Initialize the RFID device
     */
    fun init()

    /**
     * Close the RFID device and release resources
     */
    fun close()

    /**
     * Start the RFID device inventory process
     */
    fun start()

    /**
     * Stop the RFID device inventory process
     */
    fun stop()

}
