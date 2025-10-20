package com.pedrozc90.rfid.core.objects

import com.rscja.deviceapi.entity.UHFTAGInfo

data class TagMetadata(
    val epc: String,
    val rssi: String? = null,
    val tid: String? = null,
    val antenna: String? = null,
    val user: String? = null,
    val timestamp: Long = System.currentTimeMillis()
) {

    // val toInstant: Instant
    //     get() = Instant.ofEpochMilli(timestamp)
    companion object {

        fun from(info: UHFTAGInfo): TagMetadata {
            return info.let {
                TagMetadata(
                    epc = it.epc,
                    rssi = it.rssi,
                    tid = it.tid,
                    antenna = it.ant,
                    user = it.user
                )
            }
        }

    }

}
