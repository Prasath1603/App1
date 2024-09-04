package com.example.app1

import android.content.Context
import android.content.Intent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.TextView

class ConnectedDevicesAdapter(
    context: Context,
    private val devices: List<String>
) : ArrayAdapter<String>(context, 0, devices) {

    override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {
        val view = convertView ?: LayoutInflater.from(context).inflate(R.layout.item_connected_device, parent, false)
        val deviceNameTv: TextView = view.findViewById(R.id.deviceNameTv)
        val viewDetailsBtn: Button = view.findViewById(R.id.viewDetailsBtn)

        val deviceInfo = devices[position]
        deviceNameTv.text = deviceInfo

        viewDetailsBtn.setOnClickListener {
            val intent = Intent(context, DeviceDetailsActivity::class.java).apply {
                putExtra("DEVICE_INFO", deviceInfo)
            }
            context.startActivity(intent)
        }

        return view
    }
}
