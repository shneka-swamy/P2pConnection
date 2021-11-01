package com.example.p2pconnection

import android.Manifest
import android.annotation.SuppressLint
import android.content.Context
import android.content.IntentFilter
import android.content.pm.PackageManager
import android.net.wifi.WifiManager
import android.net.wifi.WpsInfo
import android.net.wifi.p2p.WifiP2pConfig
import android.net.wifi.p2p.WifiP2pDevice
import android.net.wifi.p2p.WifiP2pManager
import android.net.wifi.p2p.WifiP2pManager.ActionListener
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Looper
import android.util.Log
import android.view.Menu
import android.widget.Toast
import androidx.core.app.ActivityCompat
import android.util.Log.v as v1


// This class extends Activity and implements Channel Listener and Action Listener
// TODO: In the example requires Device action listener
// TODO: Must implement fragments (Device Detail and List) to make the operation simple
class MainActivity : AppCompatActivity(),
    WifiP2pManager.ChannelListener, DeviceListFragment.DeviceActionListener {
    //In manifest file the coarse location is also set -- as it is required.

    private val intentFilter = IntentFilter()
    var isWifiP2pEnabled: Boolean = false
    private lateinit var receiver: Listener
    // Get WiFi Channel to connect to the P2P framework
    private var manager = getSystemService(Context.WIFI_P2P_SERVICE) as WifiP2pManager
    private val channel = manager.initialize(this, mainLooper, null)
    @Suppress("PrivatePropertyName")
    private val TAG = "Main Activity"
    private var retryChannel = false

    lateinit var fragmentList:MutableList<String>
    lateinit var fragmentDetails: MutableList<String>

    // Sets the Wifi P2p enable to a value specified
    fun setIsWifiP2pEnabled(isWifiP2pEnabled:Boolean){
        this.isWifiP2pEnabled = isWifiP2pEnabled
    }

    // Checks if a particular permission is granted or not
    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    )
    {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        when(requestCode){
            1 -> if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                // TODO: Permission was granted - what to do
                Log.v(TAG, "Permission Granted")
            }else {
                // TODO: Permission was not granted - Remove the functionality
                Toast.makeText(this, "Permission denied", Toast.LENGTH_SHORT).show()
            }
            else -> return
        }
    }

    // Initialise the P2P connection
    private fun initP2P():Boolean{
        // Check device capability
        val wifiManager = getSystemService(Context.WIFI_SERVICE) as WifiManager
        if(!packageManager.hasSystemFeature(PackageManager.FEATURE_WIFI_DIRECT)){
            Log.e(TAG,"Wfi Direct service is not supported")
            return false
        }
        if(wifiManager?.isP2pSupported){
            Log.e(TAG,"Cannot connect to Wifi service or Wifi is off")
            return false
        }
        if(channel == null || manager == null){
            Log.e(TAG, "Cannot get Wifi direct service or it cannot be initialised")
            return false
        }
        return true
    }

    public override fun onResume() {
        super.onResume()
        // To initialise an object it will be =
        Listener(manager, channel, this)
        registerReceiver(receiver, intentFilter)
    }

    public override fun onPause() {
        super.onPause()
        unregisterReceiver(receiver)
    }

    // These functions are added for setting up broadcast receiver
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        // Indicate the change in Wi-Fi P2P state.
        intentFilter.addAction(WifiP2pManager.WIFI_P2P_STATE_CHANGED_ACTION)

        // Indicate the change in the list of available peers.
        intentFilter.addAction(WifiP2pManager.WIFI_P2P_PEERS_CHANGED_ACTION)

        // Indicate the state of the Wifi connectivity changed
        intentFilter.addAction(WifiP2pManager.WIFI_P2P_CONNECTION_CHANGED_ACTION)

        // Indicate the device's detail change
        intentFilter.addAction(WifiP2pManager.WIFI_P2P_THIS_DEVICE_CHANGED_ACTION)


        if (!initP2P()){
            finish()
        }
        // For network discovery
        networkDiscovery(manager, channel)
    }

    //TODO: Must be changed to fragment
    //TODO: Check if the function needs to be public
    private fun resetData() {
        if (fragmentList.isNotEmpty())
            fragmentList.clear()
        if (fragmentDetails.isNotEmpty())
            fragmentDetails.clear()
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        TODO("Must implement this function after creating a menu")
        return super.onCreateOptionsMenu(menu)
    }



    // This function is for network discovery.
    // Discovery process remains active till the P2P group is formed
    // TODO: Get the missing permission from the user
    private fun networkDiscovery(manager: WifiP2pManager, channel: WifiP2pManager.Channel) {
        if (ActivityCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_FINE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            ActivityCompat.requestPermissions(this,
                arrayOf(Manifest.permission.ACCESS_FINE_LOCATION), 1)
            return
        }

        manager.discoverPeers(channel, object : ActionListener {
            override fun onSuccess() {
                v1(TAG, "Discovery Initialization Successful")
            }

            override fun onFailure(p0: Int) {
                v1(TAG, "Discovery Initialization Failed")
            }
        })
    }


    // TODO: See if permission can be added
    @SuppressLint("MissingPermission")
    override fun connect(config: WifiP2pConfig) {
        manager.connect(channel, config, object: ActionListener{
            override fun onSuccess() {
                TODO("Not yet implemented")
            }

            override fun onFailure(p0: Int) {
                Toast.makeText(this@MainActivity,
                    "Connect failed. Retry.",
                    Toast.LENGTH_SHORT
                ).show()
            }

        })
    }

    override fun disconnect(){
        manager.removeGroup(channel, object: ActionListener{
            override fun onSuccess() {
                TODO("Implement with fragment")

            }

            override fun onFailure(p0: Int) {
                Log.d(TAG, "Disconnect failed. Reason :$p0")
            }

        })
    }

    override fun onChannelDisconnected() {
       if(manager!= null && !retryChannel){
           Toast.makeText(this@MainActivity,
           "Channel Lost. Trying again", Toast.LENGTH_LONG).show()
            resetData()
           retryChannel = true
           manager.initialize(this, Looper.getMainLooper(), this)
       }
       else{
           Toast.makeText(this@MainActivity,
           "Severe, Channel is lost permanently", Toast.LENGTH_LONG).show()
       }
    }

    override fun showDetails(device: WifiP2pDevice) {
        TODO("Not yet implemented")
    }

    override fun cancelDisconnect() {
        TODO("Not yet implemented")
    }

}