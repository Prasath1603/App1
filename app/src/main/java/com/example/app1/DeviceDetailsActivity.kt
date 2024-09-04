package com.example.app1

import android.annotation.SuppressLint
import android.bluetooth.BluetoothDevice
import android.content.Intent
import android.os.Bundle
import android.widget.TextView
import androidx.activity.ComponentActivity

class DeviceDetailsActivity : ComponentActivity() {

    private lateinit var deviceNameTv: TextView
    private lateinit var batteryStatusTv: TextView
    private lateinit var modeTv: TextView

    @SuppressLint("MissingPermission")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_device_details)

        deviceNameTv = findViewById(R.id.deviceNameTv)
        batteryStatusTv = findViewById(R.id.batteryStatusTv)
        modeTv = findViewById(R.id.modeTv)

        val deviceInfo = intent.getStringExtra("DEVICE_INFO") ?: return
        deviceNameTv.text = deviceInfo

        // You would replace the following with actual code to get battery status and mode
        batteryStatusTv.text = "Battery Status: 80%" // Placeholder
        modeTv.text = "Mode: Normal" // Placeholder
    }
}
