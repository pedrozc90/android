package com.pedrozc90.rfid.chafon

import com.pedrozc90.rfid.exceptions.RfidDeviceException

class ChafonException private constructor(
    cause: Throwable? = null,
    message: String? = null,
    code: Int = -1
) : RfidDeviceException(cause, message) {

    companion object {

        private val _map = mapOf(
            0x00 to "Execution successful.",
            0x01 to "The parameter value is incorrect or out of range, or the module does not support the parameter value.",
            0x02 to "Command execution failed due to an internal error in the module (setting frequency or setting power)",
            0x03 to "Reserve",
            0x12 to "The entire inventory command is executed",
            0x13 to "No tags were inventory",
            0x14 to "Tag response timeout",
            0x15 to "Demodulation tag response error",
            0x16 to "Protocol authentication failed",
            0x17 to "Wrong password",
            0xFF to "No more data"
        )

        fun of(code: Int): ChafonException {
            val message = _map[code] ?: "Unknown error code: $code"
            return ChafonException(message = message, code = code)
        }

    }

}
