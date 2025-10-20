package com.pedrozc90.rfid.core

import com.rscja.deviceapi.interfaces.ConnectionStatus

enum class RfidConnectionStatus {
    DISCONNECTING,
    DISCONNECTED,
    CONNECTING,
    CONNECTED,
    ERROR,
    UNKNOWN;

    companion object {

        fun of(value: ConnectionStatus): RfidConnectionStatus = when (value) {
            ConnectionStatus.DISCONNECTED -> DISCONNECTED
            ConnectionStatus.CONNECTING -> CONNECTING
            ConnectionStatus.CONNECTED -> CONNECTED
        }

        fun toConnectionStatus(value: RfidConnectionStatus): ConnectionStatus = when (value) {
            DISCONNECTED, DISCONNECTING, ERROR, UNKNOWN -> ConnectionStatus.DISCONNECTED
            CONNECTING -> ConnectionStatus.CONNECTING
            CONNECTED -> ConnectionStatus.CONNECTED
        }

    }

}
