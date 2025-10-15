package com.pedrozc90.prototype.utils

import com.pedrozc90.prototype.data.Tag

object EpcUtils {

    fun encode(itemReference: String, serialNumber: Long): String {
        return "EPC:${itemReference}:${serialNumber.toString().padStart(7, '0')}"
    }

    fun decode(rfid: String): Pair<String, Long> {
        rfid.split(":").let {
            if (it.size != 3 || it[0] != "EPC") {
                throw IllegalArgumentException("Invalid RFID format")
            }
            return Pair(first = it[1], second = it[2].toLong())
        }
    }

    fun toTag(rfid: String, readId: Long): Tag {
        val (itemReference, serialNumber) = decode(rfid)
        return Tag(
            rfid = rfid,
            itemReference = itemReference,
            serialNumber = serialNumber,
            readId = readId
        )
    }

}
