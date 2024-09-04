package com.example.app1

import android.Manifest
import android.annotation.SuppressLint
import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothDevice
import android.bluetooth.BluetoothManager
import android.bluetooth.BluetoothProfile
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.provider.Settings
import android.util.Log
import android.widget.Button
import android.widget.ImageView
import android.widget.ListView
import android.widget.TextView
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat

class MainActivity : ComponentActivity() {

    private lateinit var bAdapter: BluetoothAdapter
    private lateinit var bluetoothStatusTv: TextView
    private lateinit var bluetoothIv: ImageView
    private lateinit var turnOnBtn: Button
    private lateinit var turnOffBtn: Button
    private lateinit var discoverableBtn: Button
    private lateinit var pairedBtn: Button
    private lateinit var currentlyConnectedTv: TextView
    private lateinit var connectedDevicesListView: ListView

    private val enableBluetoothLauncher =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            if (result.resultCode == RESULT_OK) {
                bluetoothIv.setImageResource(R.drawable.ic_bluetooth_on)
                Toast.makeText(this, "Bluetooth is now ON", Toast.LENGTH_LONG).show()
                Log.d("Bluetooth", "Bluetooth turned on")
            } else {
                Toast.makeText(this, "Failed to enable Bluetooth", Toast.LENGTH_LONG).show()
                Log.d("Bluetooth", "Bluetooth enabling failed or was canceled")
            }
        }

    @SuppressLint("MissingPermission")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        // Initialize Views
        bluetoothStatusTv = findViewById(R.id.bluetoothStatusTv)
        bluetoothIv = findViewById(R.id.bluetoothIv)
        turnOnBtn = findViewById(R.id.turnOnBtn)
        turnOffBtn = findViewById(R.id.turnOffBtn)
        discoverableBtn = findViewById(R.id.discoverableBtn)
        pairedBtn = findViewById(R.id.pairedBtn)
        currentlyConnectedTv = findViewById(R.id.currentlyConnectedTv)
        connectedDevicesListView = findViewById(R.id.connectedDevicesListView)

        // Initialize Bluetooth Adapter using BluetoothManager
        val bluetoothManager = getSystemService(Context.BLUETOOTH_SERVICE) as BluetoothManager
        bAdapter = bluetoothManager.adapter

        // Request Bluetooth permissions if needed
        requestBluetoothPermissions()

        // Check if Bluetooth is available
        if (bAdapter == null) {
            bluetoothStatusTv.text = "Bluetooth is not available."
            Log.d("Bluetooth", "Bluetooth is not available on this device")
        } else {
            bluetoothStatusTv.text = "Bluetooth is available."
            Log.d("Bluetooth", "Bluetooth is available on this device")
        }

        // Set Bluetooth status icon
        updateBluetoothIcon()

        // Turn On Bluetooth
        turnOnBtn.setOnClickListener {
            Log.d("Bluetooth", "Turn On Button Clicked")
            if (bAdapter.isEnabled) {
                Toast.makeText(this, "Already on", Toast.LENGTH_LONG).show()
            } else {
                val intent = Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE)
                enableBluetoothLauncher.launch(intent)
                Log.d("Bluetooth", "Request to enable Bluetooth sent")
            }
        }

        // Turn Off Bluetooth
        turnOffBtn.setOnClickListener {
            if (bAdapter.isEnabled) {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                    // Guide user to Bluetooth settings to turn off Bluetooth for Android 12 and above
                    val intent = Intent(Settings.ACTION_BLUETOOTH_SETTINGS)
                    startActivity(intent)
                    Toast.makeText(this, "Turn off Bluetooth manually in Bluetooth settings", Toast.LENGTH_LONG).show()
                } else {
                    bAdapter.disable()
                    bluetoothIv.setImageResource(R.drawable.ic_bluetooth_off)
                    Toast.makeText(this, "Bluetooth turned off", Toast.LENGTH_LONG).show()
                    Log.d("Bluetooth", "Bluetooth turned off")
                }
            } else {
                Toast.makeText(this, "Bluetooth is already off", Toast.LENGTH_LONG).show()
            }
        }

        // Make Discoverable
        discoverableBtn.setOnClickListener {
            if (bAdapter.isDiscovering) {
                Toast.makeText(this, "Already Discovering", Toast.LENGTH_LONG).show()
            } else {
                val intent = Intent(BluetoothAdapter.ACTION_REQUEST_DISCOVERABLE)
                intent.putExtra(BluetoothAdapter.EXTRA_DISCOVERABLE_DURATION, 300)
                startActivity(intent)
                Log.d("Bluetooth", "Bluetooth discovery requested")
            }
        }

        // Get Paired Devices
        pairedBtn.setOnClickListener {
            showCurrentlyPairedAndConnectedDevices()
        }
    }

    private fun requestBluetoothPermissions() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.BLUETOOTH) != PackageManager.PERMISSION_GRANTED ||
            ContextCompat.checkSelfPermission(this, Manifest.permission.BLUETOOTH_ADMIN) != PackageManager.PERMISSION_GRANTED ||
            (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S && ContextCompat.checkSelfPermission(this, Manifest.permission.BLUETOOTH_SCAN) != PackageManager.PERMISSION_GRANTED) ||
            (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S && ContextCompat.checkSelfPermission(this, Manifest.permission.BLUETOOTH_CONNECT) != PackageManager.PERMISSION_GRANTED)
        ) {
            ActivityCompat.requestPermissions(this, arrayOf(
                Manifest.permission.BLUETOOTH,
                Manifest.permission.BLUETOOTH_ADMIN,
                Manifest.permission.BLUETOOTH_SCAN,
                Manifest.permission.BLUETOOTH_CONNECT
            ), 1)
        }
    }

    private fun updateBluetoothIcon() {
        bluetoothIv.setImageResource(if (bAdapter.isEnabled) R.drawable.ic_bluetooth_on else R.drawable.ic_bluetooth_off)
    }

    @SuppressLint("MissingPermission")
    private fun showCurrentlyPairedAndConnectedDevices() {
        val pairedDevices = bAdapter.bondedDevices
        val connectedDevices = mutableListOf<String>()

        val adapter = bAdapter.getProfileProxy(this, object : BluetoothProfile.ServiceListener {
            override fun onServiceConnected(profile: Int, proxy: BluetoothProfile?) {
                if (proxy != null) {
                    val connectedDevicesList = mutableListOf<BluetoothDevice>()

                    // Collect connected devices from different profiles
                    when (profile) {
                        BluetoothProfile.A2DP -> {
                            connectedDevicesList.addAll(proxy.connectedDevices)
                        }
                        BluetoothProfile.HEADSET -> {
                            connectedDevicesList.addAll(proxy.connectedDevices)
                        }
                        BluetoothProfile.GATT -> {
                            connectedDevicesList.addAll(proxy.connectedDevices)
                        }
                    }

                    // Remove duplicate devices
                    val uniqueConnectedDevices = connectedDevicesList.distinctBy { it.address }

                    // Find common devices that are both paired and connected
                    val commonDevices = uniqueConnectedDevices.filter { device -> pairedDevices.contains(device) }
                    val deviceInfos = commonDevices.map { device -> "${device.name} (${device.address})" }

                    // Set up the adapter and attach it to the ListView
                    val listAdapter = ConnectedDevicesAdapter(this@MainActivity, deviceInfos)
                    connectedDevicesListView.adapter = listAdapter

                    // Update UI
                    if (deviceInfos.isNotEmpty()) {
                        currentlyConnectedTv.text = "Currently Connected Devices"
                    } else {
                        currentlyConnectedTv.text = "No paired and connected devices found."
                    }
                }
            }

            override fun onServiceDisconnected(profile: Int) {
                Log.d("Bluetooth", "Bluetooth profile service disconnected")
            }
        }, BluetoothProfile.A2DP)
    }
}
