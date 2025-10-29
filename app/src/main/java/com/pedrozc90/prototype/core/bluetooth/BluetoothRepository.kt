package com.pedrozc90.prototype.core.bluetooth

import android.Manifest
import android.annotation.SuppressLint
import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothDevice
import android.bluetooth.BluetoothManager
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.content.pm.PackageManager
import android.os.Build
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.result.contract.ActivityResultContracts
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

interface BluetoothRepository {

    val scannedDevices: StateFlow<List<BluetoothDeviceDto>>
    val pairedDevices: StateFlow<List<BluetoothDeviceDto>>

    fun onCreate(activity: ComponentActivity)

    fun start()

    fun stop()

    fun onDestroy()

}

private const val TAG = "DefaultBluetoothRepository"

class DefaultBluetoothRepository(private val context: Context) : BluetoothRepository {

    private val manager: BluetoothManager? by lazy {
        context.getSystemService(BluetoothManager::class.java)
    }

    private val adapter: BluetoothAdapter? by lazy {
        manager?.adapter
    }

    private val _scannedDevices = MutableStateFlow<List<BluetoothDeviceDto>>(emptyList())
    override val scannedDevices: StateFlow<List<BluetoothDeviceDto>>
        get() = _scannedDevices.asStateFlow()

    private val _pairedDevices = MutableStateFlow<List<BluetoothDeviceDto>>(emptyList())
    override val pairedDevices: StateFlow<List<BluetoothDeviceDto>>
        get() = _pairedDevices.asStateFlow()

    @SuppressLint("MissingPermission")
    private val receiver = BluetoothDeviceReceiver { device ->
        val dto = device.toDto()
        _scannedDevices.update { devices ->
            if (devices.any { it.address == dto.address }) {
                devices
            } else {
                devices + dto
            }
        }
    }

    init {
        updatePairedDevices()
    }

    override fun onCreate(activity: ComponentActivity) {
        Log.d(TAG, "Setting up Bluetooth controller")

        if (adapter == null) {
            Log.e(TAG, "Device does not support Bluetooth")
        }

        val isBluetoothEnabled = adapter?.isEnabled == true

        val launcher =
            activity.registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { /* noop */ }

        val permissionsLauncher =
            activity.registerForActivityResult(ActivityResultContracts.RequestMultiplePermissions()) { permissions ->
                val canEnableBluetooth = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                    // val bluetoothScanGranted = permissions[android.Manifest.permission.BLUETOOTH_SCAN] ?: false
                    // val bluetoothConnectGranted = permissions[android.Manifest.permission.BLUETOOTH_CONNECT] ?: false
                    permissions[Manifest.permission.BLUETOOTH_CONNECT] == true
                } else true

                if (canEnableBluetooth && !isBluetoothEnabled) {
                    val intent = Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE)
                    launcher.launch(intent)
                }
            }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            permissionsLauncher.launch(
                arrayOf(
                    Manifest.permission.BLUETOOTH_SCAN,
                    Manifest.permission.BLUETOOTH_CONNECT
                )
            )
        }

        start()
    }

    override fun start() {
        Log.d(TAG, "Starting Bluetooth device discovery")
        val result = context.checkSelfPermission(Manifest.permission.BLUETOOTH_SCAN)
        if (result == PackageManager.PERMISSION_GRANTED) {
            // Register for broadcasts when a device is discovered.
            val filter = IntentFilter(BluetoothDevice.ACTION_FOUND)
            context.registerReceiver(receiver, filter)

            updatePairedDevices()

            adapter?.startDiscovery()
        }
    }

    override fun stop() {
        Log.d(TAG, "Stopping Bluetooth device discovery")
        val result = context.checkSelfPermission(Manifest.permission.BLUETOOTH_SCAN)
        if (result == PackageManager.PERMISSION_GRANTED) {
            adapter?.cancelDiscovery()
        }
    }

    override fun onDestroy() {
        Log.d(TAG, "Releasing Bluetooth controller")
        context.unregisterReceiver(receiver)
    }

    private fun updatePairedDevices() {
        val result = context.checkSelfPermission(Manifest.permission.BLUETOOTH_CONNECT)
        if (result != PackageManager.PERMISSION_GRANTED) return

        val pairedDevices = adapter?.bondedDevices
            ?.map { it.toDto().copy(paired = true) }
            ?.also { devices ->
                _pairedDevices.update { devices }
            }
        pairedDevices?.forEach { Log.d(TAG, "Paired device: ${it}") }
    }

}
