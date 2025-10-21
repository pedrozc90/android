package com.pedrozc90.rfid.core

import android.content.Context
import android.os.Build
import android.util.Log
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

/**
 * Lightweight detection for integrated readers (example: Chainway C72).
 *
 * Strategy:
 * 1) persisted user preference (not shown here, check before calling detect())
 * 2) Build.* string heuristics (fast, no permissions)
 * 3) presence of known vendor helper packages (if vendor ships an APK)
 *
 * If none match, detection returns null and you should show the dropdown to the user.
 */

private const val TAG = "RfidDetector"

enum class DetectionSource { PREF, BUILD_INFO, BONDED_BT, BLE_SCAN, PROBE, FALLBACK }

// Simple result
data class DetectionResult(
    val type: RfidDeviceType,
    val source: DetectionSource,
    val info: String? = null
)

object RfidDetector {

    // A small list of package name hints vendors might install (adjust to real names if known)
    private val chainwayPackages = listOf(
        "com.chainway", // generic
        "com.chainway.rainbow", // example helper/package name (replace with the real one if you know it)
        "com.rscja" // SDK vendor package prefix
    )

    /**
     * Detects an integrated Chainway-like device (C72) by checking Build properties and known packages.
     * Returns DetectionResult or null when no confident match is found.
     */
    suspend fun detectIntegratedChainway(context: Context): DetectionResult? {
        return withContext(Dispatchers.IO) {
            try {
                val manufacturer = (Build.MANUFACTURER ?: "").lowercase()
                val model = (Build.MODEL ?: "").lowercase()
                val device = (Build.DEVICE ?: "").lowercase()
                val product = (Build.PRODUCT ?: "").lowercase()
                Log.d(TAG, "Build info manufacturer=$manufacturer model=$model device=$device product=$product")

                // Heuristic: model or manufacturer contains 'chainway' or 'c72'
                if (manufacturer.contains("chainway") || model.contains("c72") || model.contains("c-72")) {
                    return@withContext DetectionResult(
                        type = RfidDeviceType.CHAINWAY,
                        source = DetectionSource.BUILD_INFO,
                        info = "manufacturer=$manufacturer model=$model"
                    )
                }

                // Also check for substrings that vendors sometimes use
                val combined = "$manufacturer|$model|$device|$product"
                if (combined.contains("chainway") || combined.contains("c72")) {
                    return@withContext DetectionResult(
                        type = RfidDeviceType.CHAINWAY,
                        source = DetectionSource.BUILD_INFO,
                        info = combined
                    )
                }

                // Check for presence of known vendor helper packages (fast, no permissions)
                val pm = context.packageManager
                for (pkgPrefix in chainwayPackages) {
                    val packages = try {
                        pm.getInstalledPackages(0)
                    } catch (e: Exception) {
                        emptyList()
                    }
                    // do a cheap scan: if any installed package startsWith pkgPrefix -> likely vendor
                    val match = packages.firstOrNull { it.packageName.startsWith(pkgPrefix) }
                    if (match != null) {
                        return@withContext DetectionResult(
                            type = RfidDeviceType.CHAINWAY,
                            source = DetectionSource.BUILD_INFO,
                            info = "package=${match.packageName}"
                        )
                    }
                }

                // nothing matched
                null
            } catch (e: Exception) {
                Log.w(TAG, "Error while detecting integrated device", e)
                null
            }
        }
    }

    /**
     * High-level detect function you can call. Right now it only tries integrated Chainway detection.
     * Add more heuristics here if you want (e.g., check persisted pref, bonded BT, BLE scan).
     */
    suspend fun detect(context: Context): DetectionResult? {
        // 1) You would check persisted preference before calling detect() in real flow.
        // 2) Try integrated detection (C72)
        val integrated = detectIntegratedChainway(context)
        if (integrated != null) return integrated

        // 3) Add more probes (bonded BT, BLE scan, SDK probe) if needed, here or in separate helpers.

        return null
    }

}
