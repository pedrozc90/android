package com.pedrozc90.rfid.chafon

enum class WorkMode(val mask: Int) {
    ANSWER(0x00),
    ACTIVE(0x01),
    TRIGGER(0x02)
}
