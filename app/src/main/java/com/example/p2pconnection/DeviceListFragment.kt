package com.example.p2pconnection

import android.net.wifi.p2p.WifiP2pConfig
import android.net.wifi.p2p.WifiP2pDevice
import android.os.Bundle
import android.view.View
import androidx.fragment.app.Fragment

class DeviceListFragment: Fragment(R.layout.device_list) {
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

    }
    interface DeviceActionListener{
        fun showDetails(device:WifiP2pDevice)
        fun cancelDisconnect()
        fun connect(config:WifiP2pConfig)
        fun disconnect()
    }

}