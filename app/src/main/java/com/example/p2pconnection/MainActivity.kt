package com.example.p2pconnection

import android.Manifest
import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.content.pm.PackageManager
import android.net.wifi.WifiManager
import android.net.wifi.p2p.WifiP2pConfig
import android.net.wifi.p2p.WifiP2pDevice
import android.net.wifi.p2p.WifiP2pManager
import android.net.wifi.p2p.WifiP2pManager.ActionListener
import android.os.BatteryManager
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Looper
import android.provider.Settings
import android.util.Log
import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import android.view.View
import android.widget.Toast
import androidx.core.app.ActivityCompat
import java.lang.Exception
import java.lang.Integer.max
import android.util.Log.v as v1

class MainActivity : AppCompatActivity(),
    WifiP2pManager.ChannelListener, DeviceListFragment.DeviceActionListener {
    //In manifest file the coarse location is also set -- as it is required.

    private val intentFilter = IntentFilter()
    var isWifiP2pEnabled: Boolean = false
    private lateinit var receiver: Listener
    // Get WiFi Channel to connect to the P2P framework
    private lateinit var manager:WifiP2pManager
    private lateinit var channel: WifiP2pManager.Channel
    private var retryChannel = false

    lateinit var fragmentList:MutableList<String>
    lateinit var fragmentDetails: MutableList<String>

    // Sets the Wifi P2p enable to a value specified
    fun setIsWifiP2pEnabled(isWifiP2pEnabled:Boolean){
        this.isWifiP2pEnabled = isWifiP2pEnabled
    }

    companion object{
        private val TAG:String = "Main Activity"
        /*
                init {
                    if (OpenCVLoader.initDebug()){
                        Log.d(TAG,"OpenCV works properly")
                    } else{
                        Log.d(TAG, "OpenCV does not work properly")
                    }
                }
        */
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
            1001 -> if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                Log.v(TAG, "Permission Granted")
            }else {
                Toast.makeText(this@MainActivity, "Permission denied", Toast.LENGTH_SHORT).show()
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
        if(!wifiManager.isP2pSupported){
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
        receiver = Listener(manager, channel, this)
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

        manager = getSystemService(Context.WIFI_P2P_SERVICE) as WifiP2pManager
        channel = manager.initialize(this, mainLooper, null)

        if (!initP2P()){
            finish()
        }

    }

    fun resetData() {
        if (fragmentList.isNotEmpty())
            fragmentList.clear()
        if (fragmentDetails.isNotEmpty())
            fragmentDetails.clear()
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        val inflater: MenuInflater = menuInflater
        inflater.inflate(R.menu.action_items, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem):Boolean {
        return when(item.itemId){
            R.id.atn_direct_enable -> {
                if (channel!= null) {
                    startActivity(Intent(Settings.ACTION_WIRELESS_SETTINGS))
                }
                else{
                    Log.e(TAG, "channel or manager is null")
                }
                true
            }

            R.id.atn_direct_discover -> {
                if (!isWifiP2pEnabled) {
                    Toast.makeText(this@MainActivity, R.string.p2p_off_warning,
                        Toast.LENGTH_SHORT
                    ).show()
                }
                val fragment = supportFragmentManager.findFragmentById(R.id.frag_list) as DeviceListFragment
                fragment.onInitiateDiscovery()
                networkDiscovery(manager, channel)
                true
            }

            else -> super.onOptionsItemSelected(item)
        }
    }

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
                Log.e(TAG, "Discovery Initialization Failed, status: $p0")
            }
        })
    }

    override fun showDetails(device: WifiP2pDevice) {
        val fragment = supportFragmentManager.findFragmentById(R.id.frag_detail) as DeviceDetailFragment
        fragment.showDetails(device)
    }

    fun getBattery():Int{
        val bm = this.getSystemService(Context.BATTERY_SERVICE) as BatteryManager
        val batteryPct:Int = bm.getIntProperty(BatteryManager.BATTERY_PROPERTY_CAPACITY)
        Log.v(TAG, "Battery value is $batteryPct")
        return batteryPct
    }

    fun getGOIntent(batteryPct:Int):Int{
        val goIntent = (3/17)*batteryPct + 45/17
        return max(goIntent, 0)
    }

    @SuppressLint("MissingPermission")
    override fun connect(config: WifiP2pConfig) {
        Log.v(TAG, "Setting up configuration")
        // TODO: Does this put too much load on the connect Thread
        //val batteryPct = getBattery()
        //config.groupOwnerIntent = getGOIntent(batteryPct)

        //Log.v(TAG, "config val ${config.groupOwnerIntent}")

        manager.connect(channel, config, object: ActionListener{
            override fun onSuccess() {
                // This function can be ignored
                Log.v(TAG, "Connection was formed successfully")
            }

            override fun onFailure(p0: Int) {
                Toast.makeText(this@MainActivity,
                    "Connect failed. Retry. code : $p0",
                    Toast.LENGTH_SHORT
                ).show()
            }

        })
    }
    private fun deletePersistentGroups() {
        try {
            val methods = WifiP2pManager::class.java.methods
            for (i in methods.indices) {
                if (methods[i].name == "deletePersistentGroup") {
                    // Delete any persistent group
                    for (netid in 0..32) {
                        methods[i].invoke(manager, channel, netid, null)
                    }
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    override fun disconnect(){
        val fragment = supportFragmentManager.findFragmentById(R.id.frag_detail) as DeviceDetailFragment
        fragment.resetViews()
        manager.removeGroup(channel, object: ActionListener{
            override fun onSuccess() {
                fragment.view?.visibility = View.GONE
                deletePersistentGroups()
            }
            override fun onFailure(p0: Int) {
                Log.d(TAG, "Disconnect failed. Reason :$p0")
            }
        })
    }

    override fun onChannelDisconnected() {
       if(!retryChannel){
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

    override fun cancelDisconnect() {
        val fragment = supportFragmentManager.findFragmentById(R.id.frag_list) as DeviceListFragment
        if(fragment.getDevice().status == WifiP2pDevice.CONNECTED){
            disconnect()
        }
        else if (fragment.getDevice().status == WifiP2pDevice.AVAILABLE ||
                fragment.getDevice().status == WifiP2pDevice.INVITED){
            manager.cancelConnect(channel, object: ActionListener{
                override fun onSuccess() {
                    Toast.makeText(this@MainActivity, "Aborting connection",
                    Toast.LENGTH_SHORT).show()
                }

                override fun onFailure(p0: Int) {
                    Toast.makeText(this@MainActivity,
                        "Connect abort request failed $p0"
                        , Toast.LENGTH_SHORT).show()
                }

            })

        }
    }

}