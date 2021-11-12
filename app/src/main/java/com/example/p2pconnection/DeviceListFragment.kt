package com.example.p2pconnection


import android.annotation.SuppressLint
import android.content.Context
import android.net.wifi.p2p.WifiP2pConfig
import android.net.wifi.p2p.WifiP2pDevice
import android.net.wifi.p2p.WifiP2pDeviceList
import android.net.wifi.p2p.WifiP2pManager
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ListView
import android.widget.ProgressBar
import android.widget.RelativeLayout
import android.widget.TextView
import androidx.fragment.app.ListFragment


// This class uses functionality of WifiPeerListAdapter class
class DeviceListFragment (context: Context): ListFragment(),WifiP2pManager.PeerListListener {

    private val peers = mutableListOf<WifiP2pDevice>()
    val classContext = context;
    private var mContentView: View? = null
    private lateinit var device: WifiP2pDevice
    var progressBar: ProgressBar? = null

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

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        this.listAdapter = WifiPeerListAdapter(classContext, R.layout.row_devices, peers)
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        mContentView = inflater.inflate(R.layout.device_list, null)
        return mContentView
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
        var view = mContentView?.findViewById(R.id.my_name) as TextView
        view.text = device.deviceName
        view = mContentView?.findViewById(R.id.my_status) as TextView
        view.text = getDeviceStatus(device.status)
    }

    override fun onPeersAvailable(peerList: WifiP2pDeviceList?) {
        if (progressBar != null && progressBar!!.visibility == View.VISIBLE)
            progressBar!!.visibility = View.GONE
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
        if (progressBar != null && progressBar!!.visibility == View.VISIBLE)
            progressBar!!.visibility = View.GONE
        progressBar = ProgressBar(activity, null, android.R.attr.progressBarStyleLarge)
        val params = RelativeLayout.LayoutParams(100, 100)
        params.addRule(RelativeLayout.CENTER_IN_PARENT)
        // TODO: How to add view to a listener -- cannot define layout
        val layout = view?.findViewById(R.layout.device_list) as RelativeLayout
        layout.addView(progressBar, params)
        progressBar!!.visibility = View.VISIBLE

    }

    interface DeviceActionListener{
        fun showDetails(device:WifiP2pDevice)
        fun cancelDisconnect()
        fun connect(config:WifiP2pConfig)
        fun disconnect()
    }


}