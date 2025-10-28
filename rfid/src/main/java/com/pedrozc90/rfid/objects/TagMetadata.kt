package com.pedrozc90.rfid.objects

import com.rscja.deviceapi.entity.UHFTAGInfo

data class TagMetadata(
    val rfid: String,
    val tid: String? = null,
    val rssi: String? = null,
    val user: String? = null,
    val antenna: Int? = null,
    val timestamp: Long = System.currentTimeMillis()
) {

    companion object {

        fun of(info: UHFTAGInfo) = TagMetadata(
            rfid = info.epc,
            tid = info.tid,
            rssi = info.rssi,
            user = info.user,
            antenna = info.ant?.toInt()
        )

    }

}
