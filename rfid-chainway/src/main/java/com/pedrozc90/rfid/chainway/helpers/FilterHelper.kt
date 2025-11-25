package com.pedrozc90.rfid.chainway.helpers

import android.util.Log
import com.rscja.deviceapi.interfaces.IUHF

/**
 * Utility wrapper to validate/normalize filter inputs and call IUHF.setFilter safely.
 *
 * Notes:
 * - The SDK demo expects `length` in bits (e.g. EPC hex length * 4).
 * - EPC pointer used in the demo is 32 (bit offset for EPC field / word offset 2).
 */
object FilterHelper {

    private const val TAG = "FilterHelper"

    /**
     * Validate and set a filter.
     *
     * @param reader underlying SDK object (IUHF)
     * @param bank memory bank constant (IUHF.Bank_EPC / Bank_TID / Bank_USER)
     * @param startBit pointer / start address (demo uses 32 for EPC)
     * @param lengthBits length in bits (demo UIs send bits; typical = epcHex.length * 4)
     * @param dataHex hex string (may contain spaces); must be hex chars
     * @return true if SDK call succeeded, false otherwise
     */
    fun setFilter(
        reader: IUHF,
        bank: Int,
        startBit: Int,
        lengthBits: Int,
        dataHex: String
    ): Boolean {
        try {
            val cleaned = dataHex.replace("\\s".toRegex(), "")
            if (lengthBits < 0) {
                Log.e(TAG, "lengthBits must be >= 0")
                return false
            }

            if (lengthBits == 0) {
                // Common convention in the demo: length == 0 disables the filter for that bank
                Log.i(TAG, "Disabling filter for bank=$bank (lengthBits==0)")
                return reader.setFilter(bank, 0, 0, "00")
            }

            if (cleaned.isEmpty()) {
                Log.e(TAG, "filter data is empty but lengthBits > 0")
                return false
            }

            if (!cleaned.matches(Regex("^[0-9a-fA-F]*$"))) {
                Log.e(TAG, "filter data must be hex: '$dataHex'")
                return false
            }

            // required number of hex chars to cover lengthBits
            if (lengthBits % 4 != 0) {
                Log.w(
                    TAG,
                    "lengthBits ($lengthBits) is not multiple of 4; rounding up to cover hex nybbles"
                )
            }
            val requiredHexChars = (lengthBits + 3) / 4 // ceil(lengthBits / 4)

            if (cleaned.length < requiredHexChars) {
                Log.e(
                    TAG,
                    "Provided data too short: need $requiredHexChars hex chars for $lengthBits bits but got ${cleaned.length}"
                )
                return false
            }

            // Trim to the required hex chars (filter uses the leftmost bits). Demo allowed extra data but used only first len bits.
            var payload = if (cleaned.length > requiredHexChars) cleaned.substring(
                0,
                requiredHexChars
            ) else cleaned

            // Ensure even hex length for SDK (demo pads odd length with '0')
            if (payload.length % 2 != 0) {
                payload += "0"
            }

            Log.d(TAG, "Setting filter bank=$bank ptr=$startBit lenBits=$lengthBits data=$payload")
            val result = reader.setFilter(bank, startBit, lengthBits, payload)
            if (!result) Log.w(TAG, "reader.setFilter returned false")
            return result
        } catch (t: Throwable) {
            Log.e(TAG, "setFilter failed", t)
            return false
        }
    }

    /**
     * Convenience: set a filter that matches the full EPC value.
     *
     * @param reader SDK instance
     * @param epcHex EPC hex string (no spaces required; spaces tolerated)
     * @param startBit default 32 (demo uses 32 for EPC start)
     * @return true if successful
     */
    fun setFilterByEpc(reader: IUHF, epcHex: String, startBit: Int = 32): Boolean {
        val cleaned = epcHex.replace("\\s".toRegex(), "")
        if (cleaned.isEmpty()) {
            Log.e(TAG, "EPC empty")
            return false
        }
        // length in bits = number of hex chars * 4
        val lengthBits = cleaned.length * 4
        return setFilter(reader, IUHF.Bank_EPC, startBit, lengthBits, cleaned)
    }

    /**
     * Helper for TID bank.
     * @param reader SDK instance
     * @param startBit pointer in bits (or words depending on SDK; demo UI uses an int)
     * @param lengthBits length in bits
     */
    fun setFilterByTid(
        reader: IUHF,
        startBit: Int,
        lengthBits: Int,
        dataHex: String
    ): Boolean {
        return setFilter(reader, IUHF.Bank_TID, startBit, lengthBits, dataHex)
    }

    /**
     * Helper for USER bank.
     */
    fun setFilterByUser(
        reader: IUHF,
        startBit: Int,
        lengthBits: Int,
        dataHex: String
    ): Boolean {
        return setFilter(reader, IUHF.Bank_USER, startBit, lengthBits, dataHex)
    }

    /**
     * Disable all filters for EPC, TID and USER as demo did.
     */
    fun disableAllFilters(reader: IUHF): Boolean {
        return try {
            val okEpc = reader.setFilter(IUHF.Bank_EPC, 0, 0, "00")
            val okTid = reader.setFilter(IUHF.Bank_TID, 0, 0, "00")
            val okUser = reader.setFilter(IUHF.Bank_USER, 0, 0, "00")
            okEpc && okTid && okUser
        } catch (t: Throwable) {
            Log.e(TAG, "disableAllFilters failed", t)
            false
        }
    }

}
