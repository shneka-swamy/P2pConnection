package com.example.p2pconnection

import android.content.ContentResolver
import android.content.Context
import android.net.Uri
import android.util.Log
import androidx.work.Worker
import androidx.work.WorkerParameters
import org.bytedeco.ffmpeg.global.avutil.AV_PIX_FMT_YUV420P
import org.bytedeco.javacv.*
import java.io.*
import java.net.InetSocketAddress
import java.net.Socket
import java.util.*


class FileTransfer(context: Context, workerParams: WorkerParameters) :
    Worker(context, workerParams) {

    private val SOCKET_TIMEOUT:Int = 5000
    companion object {
        const val EXTRAS_FILE_PATH: String = "file_url"
        const val EXTRAS_GROUP_OWNER_ADDRESS: String = "go_host"
        const val EXTRAS_GROUP_OWNER_PORT: String = "go_port"
        private const val TAG: String = "File Transfer"
    }

    // This function is used by the server to send data to the client
    override fun doWork(): Result {
        val context: Context = applicationContext
        val fileUri: String = inputData.getString(EXTRAS_FILE_PATH)!!
        Log.v(TAG, "In do work $fileUri")
        val host: String = inputData.getString(EXTRAS_GROUP_OWNER_ADDRESS)!!
        Log.v(TAG,"In Do work $host")
        val port: Int = inputData.getInt(EXTRAS_GROUP_OWNER_PORT, 0)
        val socket = Socket()
        try {
            Log.d(TAG, "Opening Client Socket $host $port")
            socket.bind(null)
            socket.connect((InetSocketAddress(host, port)), SOCKET_TIMEOUT)
            Log.d(TAG, "Client Socket ${socket.isConnected}")
            val stream: OutputStream = socket.getOutputStream()
            val cr:ContentResolver = context.contentResolver
            val mv = ModifyVideo(context, cr, Uri.parse(fileUri))
            mv.sendFrame2(stream)
            //while(true)
            //mv.frameRecorder(stream)
            //frameGrabber(inputStream, stream)
            Log.d(TAG, "Data sent to Client")
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