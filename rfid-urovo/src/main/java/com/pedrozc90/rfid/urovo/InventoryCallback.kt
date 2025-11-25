package com.pedrozc90.rfid.urovo

import com.pedrozc90.rfid.objects.TagMetadata
import com.ubx.usdk.rfid.aidl.IRfidCallback

class InventoryCallback(private val callback: (tag: TagMetadata) -> Unit) : IRfidCallback {

    override fun onInventoryTag(p0: String?, p1: String?, p2: String?) {
        val tag = TagMetadata(rfid = p0 ?: "", rssi = p1, tid = p2)
        callback.invoke(tag)
    }

    override fun onInventoryTagEnd() {
        /* ignore */
    }

}
