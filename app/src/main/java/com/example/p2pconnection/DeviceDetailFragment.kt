package com.example.p2pconnection

import android.net.wifi.p2p.WifiP2pInfo
import android.net.wifi.p2p.WifiP2pManager
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment

// Requires navigating to other classes in that are deprecated -- look into more detail
class DeviceDetailFragment: Fragment(), WifiP2pManager.ConnectionInfoListener {

    var mContentView: View? = null

    // TODO: This function is used in place of onActivityCreated
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
    }
    
    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        mContentView = inflater.inflate(R.layout.device_detail, null)

        return mContentView
    }

    override fun onConnectionInfoAvailable(p0: WifiP2pInfo?) {
        TODO("Not yet implemented")
    }

}