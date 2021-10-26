package com.example.p2pconnection

import android.Manifest
import android.annotation.SuppressLint
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.net.wifi.WpsInfo
import android.net.wifi.p2p.WifiP2pConfig
import android.net.wifi.p2p.WifiP2pDevice
import android.net.wifi.p2p.WifiP2pManager
import android.util.Log
import android.widget.Toast
import androidx.core.app.ActivityCompat
import android.util.Log.d as d1


class Listener (private val manager: WifiP2pManager,
                         private val channel: WifiP2pManager.Channel,
                         private val activity: MainActivity, ): BroadcastReceiver() {


    private val TAG = "Listener"
    private val peers = mutableListOf<WifiP2pDevice>()

    // TODO: Check if the function position must be changed.
    private val peerListListener = WifiP2pManager.PeerListListener { peerList ->
        val refreshedPeers = peerList.deviceList
        if (refreshedPeers != peers) {
            peers.clear()
            peers.addAll(refreshedPeers)
            // TODO: Here an adapter view is used
        }
        if (peers.isEmpty()) {
            Log.d(TAG, "No devices found")
            return@PeerListListener
        }
    }

    // TODO: The functionality of the server and client must be implemented
    private val connectionListener = WifiP2pManager.ConnectionInfoListener { info ->
        // Contains P2P info structure
        val groupOwnerAddress: String = info.groupOwnerAddress.hostAddress
        // After group negotiation, we can determine the group owner
        // (server)
        if(info.groupFormed && info.isGroupOwner){
            // This has the functionality of a group owner.
            // In general it is creating a group owner thread and
            // accepting incoming connections
        }
        else if (info.groupFormed){
            // This is the functionality part of the other devices or the client
            // The idea is to create a peer thread that connects to the group
            // owner.
        }

    }
    // To get the network connection status: Stack overflow
    @SuppressLint("MissingPermission")
    private fun isNetworkAvailable():Boolean{
        val connectivityManager = activity.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        val nw = connectivityManager.activeNetwork ?: return false
        val actNw = connectivityManager.getNetworkCapabilities(nw) ?: return false

        return when {
            actNw.hasTransport(NetworkCapabilities.TRANSPORT_WIFI) -> true
            actNw.hasTransport(NetworkCapabilities.TRANSPORT_CELLULAR) -> true
            actNw.hasTransport(NetworkCapabilities.TRANSPORT_ETHERNET) -> true
            actNw.hasTransport(NetworkCapabilities.TRANSPORT_BLUETOOTH) -> true
            else -> false
        }
    }

    // Checking null pointer exception is not done in the documentation but is included here
    override fun onReceive(context: Context?, intent: Intent?) {
        when (intent?.action) {
            WifiP2pManager.WIFI_P2P_STATE_CHANGED_ACTION -> {
                // Determines if P2P is available or not and tell it to the activity
                val state = intent.getIntExtra(WifiP2pManager.EXTRA_WIFI_STATE, -1)
                activity.isWifiP2pEnabled = state == WifiP2pManager.WIFI_P2P_STATE_ENABLED
            }

            // TODO: The same as before - deal after knowing how to handle permissions
            WifiP2pManager.WIFI_P2P_PEERS_CHANGED_ACTION -> {
                // The Peer list changed - do accordingly
                if (context?.let {
                        ActivityCompat.checkSelfPermission(
                            it,
                            Manifest.permission.ACCESS_FINE_LOCATION
                        )
                    } != PackageManager.PERMISSION_GRANTED
                ) {
                    return
                }
                manager.requestPeers(channel, peerListListener)
                d1(TAG, "P2P peers changed")
            }

            // TODO: Network info is deprecated and hence a self defined function is used
            WifiP2pManager.WIFI_P2P_CONNECTION_CHANGED_ACTION -> {
                // Connection state changed - do accordingly
                manager?.let { mmanager ->
                    if (isNetworkAvailable()){
                        manager.requestConnectionInfo(channel, connectionListener)
                    }
                }

            }
            WifiP2pManager.WIFI_P2P_THIS_DEVICE_CHANGED_ACTION -> {
                // TODO: Must change this part after creating a fragment.
                 Log.v(TAG, "In wifi device changed")
            }

        }
    }

    // This function asks for password in case of devices that do not support wifi direct
    @SuppressLint("MissingPermission")
    fun requestPassword(){
        // TODO: What can be done with this value ? -- Look further
        manager.requestGroupInfo(channel) { group ->
            val groupPassword = group.passphrase
        }
    }

    @SuppressLint("MissingPermission")
    fun createGroup(){
        manager.createGroup(channel, object: WifiP2pManager.ActionListener{
            override fun onSuccess() {
                // Device is ready to accept incoming connections from peers
            }

            override fun onFailure(p0: Int) {
                Toast.makeText(
                    activity,
                    "P2P group creation failed. Retry.",
                    Toast.LENGTH_SHORT
                ).show()
            }
        })
    }

}

