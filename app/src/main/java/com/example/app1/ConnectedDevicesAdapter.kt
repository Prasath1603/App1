package com.example.app1

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.TextView

class ConnectedDevicesAdapter(context: Context, private val devices: List<String>) :
    ArrayAdapter<String>(context, 0, devices) {

    override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {
        // Reuse the view if possible
        val view = convertView ?: LayoutInflater.from(context).inflate(R.layout.item_connected_device, parent, false)

        // Find the TextView in the layout
        val deviceNameTv: TextView = view.findViewById(R.id.deviceNameTv)

        // Set the device name and address
        deviceNameTv.text = devices[position]

        return view
    }
}
