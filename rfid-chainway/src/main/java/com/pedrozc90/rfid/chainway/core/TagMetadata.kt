package com.pedrozc90.rfid.chainway.core

import com.pedrozc90.rfid.objects.TagMetadata
import com.rscja.deviceapi.entity.UHFTAGInfo

fun TagMetadata.Companion.of(info: UHFTAGInfo) = TagMetadata(
    rfid = info.epc,
    tid = info.tid,
    rssi = info.rssi,
    user = info.user,
    antenna = info.ant?.toInt()
)
