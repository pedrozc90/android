package com.pedrozc90.rfid.core

import android.annotation.SuppressLint
import android.bluetooth.BluetoothAdapter
import android.bluetooth.le.BluetoothLeScanner
import android.bluetooth.le.ScanCallback
import android.bluetooth.le.ScanResult
import android.content.Context
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.flow.map
import kotlin.collections.emptyList
import kotlin.run

data class ScannedDevice(val name: String?, val macAddress: String, val rssi: Int)

/**
 * Simple BLE scanner helper using BluetoothLeScanner.
 * Caller must handle runtime permissions (BLUETOOTH_SCAN, BLUETOOTH_CONNECT) and Bluetooth enabled state.
 */
@SuppressLint("MissingPermission")
fun scanBleDevices(context: Context, scanPeriodMs: Long = 8000L): Flow<List<ScannedDevice>> = callbackFlow {
    val btAdapter = BluetoothAdapter.getDefaultAdapter() ?: run {
        trySend(emptyList())
        close()
        return@callbackFlow
    }
    val scanner: BluetoothLeScanner = btAdapter.bluetoothLeScanner ?: run {
        trySend(emptyList())
        close()
        return@callbackFlow
    }

    val results = mutableMapOf<String, ScannedDevice>()

    val callback = object : ScanCallback() {
        override fun onScanResult(callbackType: Int, result: ScanResult?) {
            result?.device?.let { device ->
                val mac = device.address ?: return
                val name = device.name ?: result.scanRecord?.deviceName
                val rssi = result.rssi
                results[mac] = ScannedDevice(name, mac, rssi)
                trySend(results.values.toList())
            }
        }

        override fun onBatchScanResults(resultsList: MutableList<ScanResult>?) {
            resultsList?.forEach { r ->
                r.device?.let { device ->
                    val mac = device.address ?: return@forEach
                    val name = device.name ?: r.scanRecord?.deviceName
                    results[mac] = ScannedDevice(name, mac, r.rssi)
                }
            }
            trySend(results.values.toList())
        }
    }

    scanner.startScan(callback)
    // stop after scanPeriodMs
    val stopper = android.os.Handler(android.os.Looper.getMainLooper())
    stopper.postDelayed({
        try {
            scanner.stopScan(callback)
        } catch (_: Throwable) {}
        close()
    }, scanPeriodMs)

    awaitClose {
        try {
            scanner.stopScan(callback)
        } catch (_: Throwable) {}
    }
}.map { it } // pass through
