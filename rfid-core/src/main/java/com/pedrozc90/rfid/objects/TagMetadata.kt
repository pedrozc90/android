package com.pedrozc90.rfid.objects

data class TagMetadata(
    val rfid: String,
    val tid: String? = null,
    val rssi: String? = null,
    val user: String? = null,
    val antenna: Int? = null,
    val timestamp: Long = System.currentTimeMillis()
) {

    companion object {

        // for extension

    }

}
