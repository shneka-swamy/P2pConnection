package com.example.p2pconnection

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.util.Log
import androidx.core.content.FileProvider
import androidx.work.Worker
import androidx.work.WorkerParameters
import java.io.*
import java.net.InetSocketAddress
import java.net.Socket


class FileTransfer(context: Context, workerParams: WorkerParameters) :
    Worker(context, workerParams) {

    companion object {
        //const val EXTRAS_FILE_PATH: String = "file_url"
        const val EXTRAS_GROUP_OWNER_ADDRESS: String = "go_host"
        const val EXTRAS_GROUP_OWNER_PORT: String = "go_port"
        const val EXTRAS_VIDEO_INDEX: String = "video_index"
        private const val TAG: String = "File Transfer"
        private const val SOCKET_TIMEOUT:Int = 5000
    }

    val socket = Socket()

    // TODO: This function is the sender side of the code that can be changed
    override fun doWork(): Result {
        val context: Context = applicationContext
        //val fileUri: String = inputData.getString(EXTRAS_FILE_PATH)!!
        //Log.v(TAG, "In do work $fileUri")
        val host: String = inputData.getString(EXTRAS_GROUP_OWNER_ADDRESS)!!
        Log.v(TAG,"In Do work $host")
        val port: Int = inputData.getInt(EXTRAS_GROUP_OWNER_PORT, 0)
        val sendIndex: Int = inputData.getInt(EXTRAS_VIDEO_INDEX, 0)
        try {
            Log.d(TAG, "Opening Client Socket $host $port")
            socket.bind(null)
            socket.connect((InetSocketAddress(host, port)), SOCKET_TIMEOUT)
            Log.d(TAG, "Client Socket ${socket.isConnected}")
            val stream: OutputStream = socket.getOutputStream()
            stream.write(sendIndex)
            stream.flush()
            Log.d(TAG, "Data sent to Server")

//            val cr:ContentResolver = context.contentResolver
//            var inputStream: InputStream? = null
//            try {
//                inputStream = cr.openInputStream(Uri.parse(fileUri))
//            }catch (e:FileNotFoundException){
//                Log.d(TAG,e.toString())
//            }
//            DeviceDetailFragment.copyFile(inputStream!!, stream)

            // This section of code is used to run the profiler
            //val mv = ModifyVideo(context, cr, Uri.parse(fileUri))
            //mv.sendFrame2(stream)
            //while(true)
            //mv.frameRecorder(stream)
            //frameGrabber(inputStream, stream)

            Log.v(TAG, "waiting for response")
            val inputstream: InputStream = socket.getInputStream()
            Log.v(TAG, "waiting ")

            val f = File(
                context.getExternalFilesDir("received"),
                "wifip2pshared-${System.currentTimeMillis()}.${Constants.FILE_EXTENSION}"
            )
            val dirs = File(f.parent!!)
            if (!dirs.exists())
                dirs.mkdirs()
            f.createNewFile()
            DeviceDetailFragment.copyFile(inputstream, FileOutputStream(f))
            val recvFile = File(f.absolutePath)
            val fileUri: Uri = FileProvider.getUriForFile(context,
                BuildConfig.APPLICATION_ID + ".provider",
                recvFile
            )
            Log.v(TAG, "File received")
            val intent = Intent(context, VideoPlayer::class.java)
            intent.putExtra("uri", fileUri.toString())
            intent.putExtra("contentType", Constants.CONTENT_TYPE)
            intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK
            context.startActivity(intent)

        } catch (e: FileNotFoundException){
            Log.d(TAG, e.toString())
        } catch (e : IOException) {
          Log.e(TAG, e.message.toString())
        }
        finally {
            if (socket.isConnected){
                try {
                    Log.v(TAG,"Closing the socket at the server side")
                    socket.close()
                }catch (e: IOException){
                    e.printStackTrace()
                }
            }
        }
        return Result.success()
    }

}