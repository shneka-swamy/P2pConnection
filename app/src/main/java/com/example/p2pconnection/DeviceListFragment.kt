package com.example.p2pconnection


import android.annotation.SuppressLint
import android.app.ActionBar
import android.app.AlertDialog
import android.content.Context
import android.graphics.Color
import android.net.wifi.p2p.WifiP2pConfig
import android.net.wifi.p2p.WifiP2pDevice
import android.net.wifi.p2p.WifiP2pDeviceList
import android.net.wifi.p2p.WifiP2pManager
import android.os.Bundle
import android.util.Log
import android.view.*
import android.widget.*
import androidx.fragment.app.ListFragment


// This class uses functionality of WifiPeerListAdapter class
class DeviceListFragment: ListFragment(),WifiP2pManager.PeerListListener {

    private val peers = mutableListOf<WifiP2pDevice>()
    private lateinit var mContentView: View
    private lateinit var device: WifiP2pDevice
    private lateinit var progressBar: ProgressBar

    // Way to create static methods and variables.
    companion object {
        private val TAG: String = "Device List Fragment"

        // Function used to return the status of a particular device given as the input
        fun getDeviceStatus(deviceStatus: Int): String {
            Log.d(TAG, "Peer status :$deviceStatus")
            return when (deviceStatus) {
                WifiP2pDevice.AVAILABLE -> "available"
                WifiP2pDevice.INVITED -> "invited"
                WifiP2pDevice.CONNECTED -> "connected"
                WifiP2pDevice.UNAVAILABLE -> "unavailable"
                WifiP2pDevice.FAILED -> "failed"
                else -> "unknown"
            }
        }
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        mContentView = inflater.inflate(R.layout.device_list, null)
        progressBar = mContentView.findViewById(R.id.progress_bar) as ProgressBar
        listAdapter = activity?.let { WifiPeerListAdapter(it, R.layout.row_devices, peers) }
        Log.v(TAG, "calling onCreateView of the fragment")
        return super.onCreateView(inflater, container, savedInstanceState)
    }

    // This function returns the value of device
    fun getDevice():WifiP2pDevice{
        return device
    }

    override fun onListItemClick(l: ListView, v: View, position: Int, id: Long) {
        val device = listAdapter?.getItem(position) as WifiP2pDevice
        // Remove the effects due to null
        (activity as DeviceActionListener?)!!.showDetails(device)
    }

    fun updateThisDevice(device: WifiP2pDevice){
        this.device = device
        //Checking if mContentView is null at any point
        var view = mContentView.findViewById(R.id.my_name) as TextView
        view.text = device.deviceName
        view = mContentView.findViewById(R.id.my_status) as TextView
        view.text = getDeviceStatus(device.status)
    }

    override fun onPeersAvailable(peerList: WifiP2pDeviceList?) {
        if (progressBar.visibility == View.VISIBLE)
            progressBar.visibility = View.GONE
        peers.clear()
        if (peerList != null) {
            peers.addAll(peerList.deviceList)
        }
        (listAdapter as WifiPeerListAdapter?)!!.notifyDataSetChanged()
        if (peers.size == 0){
            Log.d(TAG, "No devices found")
            return
        }
    }

    // This function clears all the peers.
    fun clearPeers(){
        peers.clear()
        (listAdapter as WifiPeerListAdapter?)!!.notifyDataSetChanged()
    }

    @SuppressLint("ResourceType")
    fun onInitiateDiscovery(){
        if (progressBar.visibility == View.VISIBLE)
            progressBar.visibility = View.GONE
        Log.v(TAG, "Processbar might be null $progressBar")
        progressBar.visibility = View.VISIBLE
    }

    interface DeviceActionListener{
        fun showDetails(device:WifiP2pDevice)
        fun cancelDisconnect()
        fun connect(config:WifiP2pConfig)
        fun disconnect()
    }
}