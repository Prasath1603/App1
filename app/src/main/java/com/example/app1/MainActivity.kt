package com.example.app1

import android.Manifest
import android.annotation.SuppressLint
import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothManager
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.provider.Settings
import android.util.Log
import android.widget.Button
import android.widget.ImageView
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
    private lateinit var pairedTv: TextView

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
        pairedTv = findViewById(R.id.pairedTv)

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
                    Toast.makeText(this, "Please turn off Bluetooth from settings.", Toast.LENGTH_LONG).show()
                    Log.d("Bluetooth", "Redirected to Bluetooth settings to turn off Bluetooth")
                } else {
                    // For older versions, use deprecated disable() method
                    bAdapter.disable()
                    updateBluetoothIcon()
                    Toast.makeText(this, "Bluetooth Turned Off", Toast.LENGTH_LONG).show()
                    Log.d("Bluetooth", "Bluetooth turned off using disable() method")
                }
            } else {
                Toast.makeText(this, "Bluetooth is already off", Toast.LENGTH_LONG).show()
            }
        }

        // Make Bluetooth Discoverable
        discoverableBtn.setOnClickListener {
            if (!bAdapter.isDiscovering) {
                val intent = Intent(BluetoothAdapter.ACTION_REQUEST_DISCOVERABLE)
                intent.putExtra(BluetoothAdapter.EXTRA_DISCOVERABLE_DURATION, 300)
                startActivity(intent)
                Toast.makeText(this, "Making device discoverable", Toast.LENGTH_LONG).show()
            }
        }

        // Get Paired Devices
        pairedBtn.setOnClickListener {
            if (bAdapter.isEnabled) {
                val pairedDevices = bAdapter.bondedDevices
                if (pairedDevices.isNotEmpty()) {
                    val pairedDevicesList = pairedDevices.joinToString(separator = "\n") { device -> device.name }
                    pairedTv.text = "Paired Devices:\n$pairedDevicesList"
                } else {
                    pairedTv.text = "No paired devices found."
                }
            } else {
                Toast.makeText(this, "Bluetooth is off. Please turn it on first.", Toast.LENGTH_LONG).show()
            }
        }
    }

    private fun updateBluetoothIcon() {
        if (bAdapter.isEnabled) {
            bluetoothIv.setImageResource(R.drawable.ic_bluetooth_on)
        } else {
            bluetoothIv.setImageResource(R.drawable.ic_bluetooth_off)
        }
    }

    private fun requestBluetoothPermissions() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            if (ContextCompat.checkSelfPermission(
                    this,
                    Manifest.permission.BLUETOOTH_CONNECT
                ) != PackageManager.PERMISSION_GRANTED ||
                ContextCompat.checkSelfPermission(
                    this,
                    Manifest.permission.BLUETOOTH_SCAN
                ) != PackageManager.PERMISSION_GRANTED
            ) {
                ActivityCompat.requestPermissions(
                    this,
                    arrayOf(
                        Manifest.permission.BLUETOOTH_CONNECT,
                        Manifest.permission.BLUETOOTH_SCAN
                    ),
                    1
                )
            }
        } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (ContextCompat.checkSelfPermission(
                    this,
                    Manifest.permission.ACCESS_FINE_LOCATION
                ) != PackageManager.PERMISSION_GRANTED
            ) {
                ActivityCompat.requestPermissions(
                    this,
                    arrayOf(Manifest.permission.ACCESS_FINE_LOCATION),
                    1
                )
            }
        }
    }
}
