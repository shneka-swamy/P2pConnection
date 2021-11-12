package com.example.p2pconnection

import android.content.Context
import android.net.wifi.p2p.WifiP2pDevice
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.TextView

// Creating a wifi peer to peer class with parameters and passing
open class WifiPeerListAdapter(context: Context, resource: Int, objects: MutableList<WifiP2pDevice>) :
    ArrayAdapter<WifiP2pDevice>(context, resource, objects) {

    val items = objects

    override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {
        lateinit var v: View

        if(convertView == null){
            val systemService = context.getSystemService(
                Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater
            v = systemService.inflate(R.layout.row_devices, null)
        }
        else{
            v = convertView
        }
        val device = items.getOrNull(position)
        if (device != null){
            val top: TextView? = v.findViewById(R.id.device_name)
            val bottom: TextView? = v.findViewById(R.id.device_details)
            if (top != null)
                top.text = device.deviceName
            if (bottom != null)
                bottom.text = DeviceListFragment.getDeviceStatus(device.status)
        }
        return v;
    }
}