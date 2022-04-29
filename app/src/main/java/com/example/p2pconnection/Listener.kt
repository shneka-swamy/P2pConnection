package com.example.p2pconnection

import android.annotation.SuppressLint
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.net.NetworkCapabilities.NET_CAPABILITY_WIFI_P2P
import android.net.wifi.p2p.WifiP2pDevice
import android.net.wifi.p2p.WifiP2pManager
import android.util.Log
import android.widget.Toast

class Listener(
    private val manager: WifiP2pManager,
    private val channel: WifiP2pManager.Channel,
    private val activity: MainActivity,
): BroadcastReceiver() {


    private val TAG = "Listener"
    private val peers = mutableListOf<WifiP2pDevice>()

    // NOTE: These functions are required in case of group formation
    // TODO: These functions are not called.

    private val peerListListener = WifiP2pManager.PeerListListener { peerList ->
        val refreshedPeers = peerList.deviceList
        if (refreshedPeers != peers) {
            peers.clear()
            peers.addAll(refreshedPeers)
        }
        if (peers.isEmpty()) {
            Log.d(TAG, "No devices found")
            return@PeerListListener
        }
    }
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
    @SuppressLint("MissingPermission")
    fun requestPassword(){
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

    // Checking null pointer exception is not done in the documentation but is included here
    @SuppressLint("MissingPermission")
    override fun onReceive(context: Context?, intent: Intent?) {
        when (intent?.action) {
            WifiP2pManager.WIFI_P2P_STATE_CHANGED_ACTION -> {
                // Determines if P2P is available or not and tell it to the activity
                val state = intent.getIntExtra(WifiP2pManager.EXTRA_WIFI_STATE, -1)
                activity.isWifiP2pEnabled = state == WifiP2pManager.WIFI_P2P_STATE_ENABLED
                Log.d(TAG, "P2P state changed- $state")
            }

            WifiP2pManager.WIFI_P2P_PEERS_CHANGED_ACTION -> {
                manager.requestPeers(channel, activity.supportFragmentManager.findFragmentById(R.id.frag_list) as WifiP2pManager.PeerListListener)
                Log.d(TAG, "P2P peers changed")
            }

            WifiP2pManager.WIFI_P2P_CONNECTION_CHANGED_ACTION -> {
                // Connection state changed - do accordingly
                manager.let { mmanager ->
                    val fragment = activity.
                        supportFragmentManager.findFragmentById(R.id.frag_detail) as DeviceDetailFragment
                    manager.requestConnectionInfo(channel, fragment)

                }

            }
            WifiP2pManager.WIFI_P2P_THIS_DEVICE_CHANGED_ACTION -> {
                val fragment = activity.supportFragmentManager.findFragmentById(R.id.frag_list) as DeviceListFragment
                fragment.updateThisDevice(
                    (intent.getParcelableExtra(
                        WifiP2pManager.EXTRA_WIFI_P2P_DEVICE
                    ) as WifiP2pDevice?)!!
                )
            }

        }
    }
}

