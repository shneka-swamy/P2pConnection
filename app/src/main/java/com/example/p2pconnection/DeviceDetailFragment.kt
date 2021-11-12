package com.example.p2pconnection

import android.annotation.SuppressLint
import android.app.Activity
import android.content.Intent
import android.net.Uri
import android.net.wifi.WpsInfo
import android.net.wifi.p2p.WifiP2pConfig
import android.net.wifi.p2p.WifiP2pDevice
import android.net.wifi.p2p.WifiP2pInfo
import android.net.wifi.p2p.WifiP2pManager
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ProgressBar
import android.widget.RelativeLayout
import android.widget.TextView
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.FileProvider
import androidx.fragment.app.Fragment
import androidx.work.*
import java.io.*
import java.net.ServerSocket
import java.net.Socket
import java.util.concurrent.Executors

class DeviceDetailFragment: Fragment(), WifiP2pManager.ConnectionInfoListener {

    private var mContentView: View? = null
    lateinit var device: WifiP2pDevice
    var progressBar: ProgressBar? = null
    private lateinit var info: WifiP2pInfo
    private val TAG: String = "Device Detail Fragment"
    var statusText = mContentView?.findViewById(R.id.status_text) as TextView

    // Abstract classes can use object but others can call the constructor
    @SuppressLint("ResourceType", "SetTextI18n")
    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        mContentView = inflater.inflate(R.layout.device_detail, null)
        val button = mContentView?.findViewById(R.id.btn_connect) as Button
        button.setOnClickListener {
            val config = WifiP2pConfig()
            config.deviceAddress = device.deviceAddress
            config.wps.setup = WpsInfo.PBC
            if (progressBar != null && progressBar!!.visibility == View.VISIBLE) {
                progressBar!!.visibility = View.GONE
            }
            progressBar = ProgressBar(activity, null, android.R.attr.progressBarStyleLarge)
            val params = RelativeLayout.LayoutParams(100, 100)
            params.addRule(RelativeLayout.CENTER_IN_PARENT)
            val layout = view?.findViewById(R.layout.device_detail) as RelativeLayout
            layout.addView(progressBar, params)
            progressBar!!.visibility = View.VISIBLE
            (activity as DeviceListFragment.DeviceActionListener).connect(config)
        }

        val disconnect = mContentView?.findViewById(R.id.btn_disconnect) as Button
        disconnect.setOnClickListener{
            (activity as DeviceListFragment.DeviceActionListener).disconnect()
        }
        val startClient = mContentView?.findViewById(R.id.btn_start_client) as Button
        startClient.setOnClickListener{
            // This part allows the user to pick an image from the gallery or other registered app
            val intent = Intent(Intent.ACTION_GET_CONTENT)
            intent.type = "image/*"
            // TODO: This segment of code is changed
            val launchActivity = registerForActivityResult(ActivityResultContracts.StartActivityForResult()){result ->
                // TODO: Must check if this mimics the functionality intended
                if(result.resultCode == Activity.RESULT_OK){
                    val uri: Uri? = result.data?.data
                    val statusText = mContentView!!.findViewById(R.id.status_text) as TextView
                    statusText.text = "Sending $uri"
                    Log.d(TAG, "Intent--- $uri")

                    val myData: Data = workDataOf("EXTRAS_FILE_PATH" to uri.toString(),
                                                    "EXTRAS_GROUP_OWNER_ADDRESS" to info.groupOwnerAddress.hostAddress,
                                                    "EXTRAS_GROUP_OWNER_PORT" to 8988)

                    val uploadWorkRequest: WorkRequest = OneTimeWorkRequestBuilder<FileTransfer>().setInputData(myData).build()
                    WorkManager.getInstance(requireActivity()).enqueue(uploadWorkRequest)

                }
            }

            launchActivity.launch(intent)
        }
        return mContentView
    }

    @SuppressLint("SetTextI18n")
    override fun onConnectionInfoAvailable(p0: WifiP2pInfo?) {
        if(progressBar != null && progressBar!!.visibility == View.VISIBLE){
            progressBar!!.visibility = View.GONE
        }
        // TODO: Requires to be null asserted ?
        this.info = p0!!
        this.view?.visibility = View.VISIBLE

        // Once the owner of the IP is known
        var view = mContentView?.findViewById(R.id.group_owner) as TextView
        view.text = resources.getString(R.string.group_owner_text) +
                    (if (info.isGroupOwner) resources.getString(R.string.yes)
                          else resources.getString(R.string.no))

        // Get the InetAddress from WifiP2pinfo struct
        view = mContentView!!.findViewById(R.id.device_info)
        view.text = "Group owner IP: " + info.groupOwnerAddress.hostAddress

        // The group owner is assigned as the file server. The file server is a single threaded,
        // single connection server socket.

        if(info.groupFormed && info.isGroupOwner){
            val executor = Executors.newSingleThreadExecutor()
            val handler = Handler(Looper.getMainLooper())
            executor.execute{
                val result: String? = doInBackground()
                handler.post {
                    onPostExecute(result)
                }
            }
        }
        else if(info.groupFormed){
            // The other device acts as a client and in this case we can enable the get file button
            val startClient = mContentView?.findViewById(R.id.btn_start_client) as Button
            startClient.visibility = View.VISIBLE
            statusText.text = resources.getString(R.string.client_text)
        }
        // Finally hide the connect button
        // TODO: How to integrate all these lines as one
        val connectBtn = mContentView?.findViewById(R.id.btn_connect) as TextView
        connectBtn.visibility = View.GONE
    }

    private fun doInBackground(): String? {
        try {
            val serverSocket = ServerSocket(8988)
            Log.d(TAG,"Server: Socket opened")
            var client:Socket = serverSocket.accept()
            Log.d(TAG, "Server: Connection Done")
            val f = File(
                requireContext().getExternalFilesDir("received"),
                "wifip2pshared-" + System.currentTimeMillis()
                        + ".jpg"
            )
            val dirs = File(f.parent!!)
            if (!dirs.exists())
                dirs.mkdirs()
            f.createNewFile()

            Log.d(TAG, "Server copying files $f.toString()")
            val inputstream: InputStream = client.getInputStream()
            copyFile(inputstream, FileOutputStream(f))
            serverSocket.close()
            return f.absolutePath
        }catch (e: IOException){
            Log.e(TAG, e.message.toString())
            return null
        }
    }

    @SuppressLint("SetTextI18n")
    private fun onPostExecute(result: String?){
        if(result != null){
            statusText.text = "File copied: $result"
            val recvFile = File(result)
            val fileUri: Uri = FileProvider.getUriForFile(
                                requireContext(),
                                "com.example.android.P2PConnection.fileprovider",
                                recvFile
            )

            val intent = Intent()
            intent.action = Intent.ACTION_VIEW
            intent.setDataAndType(fileUri, "image/*")
            intent.flags = Intent.FLAG_GRANT_READ_URI_PERMISSION
            context?.startActivity(intent)
        }
    }

    companion object {
        fun copyFile(inputStream: InputStream, out: OutputStream): Boolean {
            val buf = ByteArray(1024)
            var len: Int
            try {
                while (inputStream.read(buf) != -1) {
                    len = inputStream.read(buf)
                    out.write(buf, 0, len)
                }
                out.close()
                inputStream.close()
            } catch (e: IOException) {
                Log.d("TAG", e.toString())
                return false
            }
            return true
        }
    }

    // Updates the UI with the device data
    fun showDetails(device: WifiP2pDevice){
        this.device = device
        this.view?.visibility = View.VISIBLE
        var view = mContentView?.findViewById(R.id.device_address) as TextView
        view.text = device.deviceAddress
        view = mContentView?.findViewById(R.id.device_info) as TextView
        view.text = device.toString()
    }

    // Clear the UI after disconnect or direct mode disable operation
    fun resetViews(){
        val connectBtn =  mContentView?.findViewById(R.id.btn_connect) as Button
        connectBtn.visibility  = View.VISIBLE
        var view = mContentView?.findViewById(R.id.device_address) as TextView
        view.setText(R.string.empty)
        view  = mContentView?.findViewById(R.id.device_info) as TextView
        view.setText(R.string.empty)
        view = mContentView?.findViewById(R.id.group_owner) as TextView
        view.setText(R.string.empty)
        view  = mContentView?.findViewById(R.id.status_text) as TextView
        view.setText(R.string.empty)
        val startClient = mContentView?.findViewById(R.id.btn_start_client) as Button
        startClient.visibility = View.GONE
        this.view?.visibility = View.GONE
    }
}