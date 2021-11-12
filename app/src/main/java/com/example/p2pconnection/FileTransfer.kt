package com.example.p2pconnection

import android.content.ContentResolver
import android.content.Context
import android.net.Uri
import android.util.Log
import androidx.work.Worker
import androidx.work.WorkerParameters
import java.io.FileNotFoundException
import java.io.IOException
import java.io.InputStream
import java.io.OutputStream
import java.net.InetSocketAddress
import java.net.Socket

class FileTransfer(context: Context, workerParams: WorkerParameters) :
    Worker(context, workerParams) {

    private val SOCKET_TIMEOUT:Int = 5000
    val EXTRAS_FILE_PATH: String = "file_url"
    val EXTRAS_GROUP_OWNER_ADDRESS: String = "go_host"
    val EXTRAS_GROUP_OWNER_PORT:String = "go_port"
    val TAG: String = "File Transfer"

    override fun doWork(): Result {
        val context: Context = applicationContext
        val fileUri: String? = inputData.getString(EXTRAS_FILE_PATH)
        val host: String? = inputData.getString(EXTRAS_GROUP_OWNER_ADDRESS)
        val port: Int = inputData.getInt(EXTRAS_GROUP_OWNER_PORT, 0)
        val socket = Socket()
        try {
            Log.d(TAG, "Opening Client Socket")
            socket.bind(null)
            socket.connect((InetSocketAddress(host, port)), SOCKET_TIMEOUT)
            Log.d(TAG, "Client Socket ${socket.isConnected}")
            val stream: OutputStream = socket.getOutputStream()
            val cr:ContentResolver = context.contentResolver
            val inputStream: InputStream = cr.openInputStream(Uri.parse(fileUri))!!
            DeviceDetailFragment.copyFile(inputStream, stream)
            Log.d(TAG, "Client: Data written")
        } catch (e: FileNotFoundException){
            Log.d(TAG, e.toString())
        } catch (e : IOException) {
          Log.e(TAG, e.message.toString())
        }
        finally {
            if (socket.isConnected){
                try {
                    socket.close()
                }catch (e: IOException){
                    e.printStackTrace()
                }
            }
        }
        return Result.success()
    }
}