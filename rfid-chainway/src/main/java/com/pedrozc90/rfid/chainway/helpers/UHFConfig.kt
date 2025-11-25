package com.pedrozc90.rfid.chainway.helpers

import android.util.Log
import com.rscja.deviceapi.RFIDWithUHFBLE
import com.rscja.deviceapi.interfaces.IUHF

/**
 * Inventory configuration helper that accepts the IUHF interface (portable across different transports:
 * BLE/USB/UART implementations that implement IUHF).
 *
 * Key points:
 * - Uses IUHF methods directly (no reflection).
 * - Calls setSupportRssi only when the concrete instance is RFIDWithUHFBLE (that implementation exposes it).
 * - Chooses the appropriate inventory mode method on the IUHF instance based on requested flags.
 *
 * Note: behavior for "tid-only" is SDK/firmware-dependent; many readers always return EPC in inventory.
 * We attempt a best-effort call to setEPCAndTIDUserMode(1,0) for tid-only, but if your reader has a different
 * API to request TID-only results you can adapt this helper.
 */
object UHFConfig {
    private const val TAG = "UHFConfig"

    /**
     * Configure the reader inventory/result mode.
     *
     * @param reader an implementation of IUHF (e.g. RFIDWithUHFBLE, RFIDWithUHFUART).
     * @param epc include EPC in inventory results (default true)
     * @param tid include TID in inventory results (default true)
     * @param rssi include RSSI in results (default true). Note: setSupportRssi is only available on
     *             certain concrete classes (e.g. RFIDWithUHFBLE). For other IUHF implementations this is skipped.
     * @param user include USER memory in results (default false)
     * @return true if the requested mode call succeeded (or at least was invoked without throwing),
     *         false if an error occurred or the mode method returned false.
     */
    fun configureInventory(
        reader: IUHF,
        epc: Boolean = true,
        tid: Boolean = true,
        rssi: Boolean = true,
        user: Boolean = false
    ): Boolean {
        // Attempt to enable RSSI only for implementations that expose that API (RFIDWithUHFBLE in the SDK)
        if (reader is RFIDWithUHFBLE) {
            try {
                reader.setSupportRssi(rssi)
                Log.d(TAG, "setSupportRssi($rssi) invoked on RFIDWithUHFBLE")
            } catch (t: Throwable) {
                Log.w(TAG, "setSupportRssi failed on RFIDWithUHFBLE", t)
            }
        } else {
            Log.d(TAG, "reader is not RFIDWithUHFBLE; skipping setSupportRssi")
        }

        return try {
            val result = when {
                epc && tid && user -> {
                    // Prefer the 2-int signature that exists on IUHF
                    Log.d(TAG, "Requesting EPC + TID + USER mode (setEPCAndTIDUserMode(1,1))")
                    reader.setEPCAndTIDUserMode(1, 1)
                }

                epc && tid -> {
                    Log.d(TAG, "Requesting EPC + TID mode (setEPCAndTIDMode())")
                    reader.setEPCAndTIDMode()
                }

                epc -> {
                    Log.d(TAG, "Requesting EPC-only mode (setEPCMode())")
                    reader.setEPCMode()
                }

                tid -> {
                    // TID-only is uncommon; best-effort call. Many readers still return EPC.
                    Log.d(TAG, "Requesting TID mode (best-effort via setEPCAndTIDUserMode(1,0))")
                    reader.setEPCAndTIDUserMode(1, 0)
                }

                else -> {
                    // No fields requested => fallback to EPC mode to ensure inventory works
                    Log.w(TAG, "No result fields requested; falling back to setEPCMode()")
                    reader.setEPCMode()
                }
            }
            Log.d(TAG, "configureInventory result=$result")
            result
        } catch (t: Throwable) {
            Log.e(TAG, "configureInventory failed", t)
            false
        }
    }
}
