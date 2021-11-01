package com.example.p2pconnection

import android.content.Context
import android.net.wifi.p2p.WifiP2pConfig
import android.net.wifi.p2p.WifiP2pDevice
import android.net.wifi.p2p.WifiP2pDeviceList
import android.net.wifi.p2p.WifiP2pManager
import android.os.Bundle
import android.view.View
import androidx.fragment.app.Fragment
import androidx.fragment.app.ListFragment



class DeviceListFragment (context: Context): ListFragment(),WifiP2pManager.PeerListListener {

    private val peers = mutableListOf<WifiP2pDevice>()
    val classContext = context;

    // TODO: What to use instead of setListAdapter
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setListAdapter(object : WifiPeerListAdapter(classContext, R.layout.row_devices, peers))
    }

    fun getDeviceStatus(){

    }


    interface DeviceActionListener{
        fun showDetails(device:WifiP2pDevice)
        fun cancelDisconnect()
        fun connect(config:WifiP2pConfig)
        fun disconnect()
    }

    override fun onPeersAvailable(p0: WifiP2pDeviceList?) {
        TODO("Not yet implemented")
    }

}