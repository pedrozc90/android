package com.pedrozc90.rfid.chafon

enum class ReadMode(val mask: Int) {
    RFID(0x00),
    QR_CODE(0x01)
}
