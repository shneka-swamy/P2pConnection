package com.example.p2pconnection

import android.content.ContentResolver
import android.content.Context
import android.graphics.Bitmap
import android.net.Uri
import android.util.Log
import androidx.work.Worker
import androidx.work.WorkerParameters
import org.bytedeco.javacv.AndroidFrameConverter
import org.bytedeco.javacv.FFmpegFrameGrabber
import java.io.*
import java.net.InetSocketAddress
import java.net.Socket

class FileTransfer(context: Context, workerParams: WorkerParameters) :
    Worker(context, workerParams) {

    private val SOCKET_TIMEOUT:Int = 5000
    companion object {
        const val EXTRAS_FILE_PATH: String = "file_url"
        const val EXTRAS_GROUP_OWNER_ADDRESS: String = "go_host"
        const val EXTRAS_GROUP_OWNER_PORT: String = "go_port"
        const val TAG: String = "File Transfer"
    }

    // This function is used to splits the video into frames
    private fun frameGrabber(inputStream: InputStream, outputStream: OutputStream){
        val frames = sequence {
            val grabber = FFmpegFrameGrabber(inputStream, 0)
            grabber.start()
            Log.v(TAG, "starting Total frames")
            while (true) {
                // This will grab all the images.
                //yield(grabber.grabImage() ?: break)
                // Trying to grab just the key frames to decrease the number of frames getting sent.
                yield(grabber.grabKeyFrame() ?: break)
            }
            grabber.close()
        }.constrainOnce()
        val converter = AndroidFrameConverter()
        var count = 0
        val send_frame = 10
        for(frame in frames) {
            val bitmap = converter.convert(frame)
            val file = File(applicationContext.getExternalFilesDir("received"),
                "client-"+System.currentTimeMillis()+".jpeg")
            val dirs = File(file.parent!!)
            if (!dirs.exists())
                dirs.mkdirs()
            file.createNewFile()
            if (count == send_frame) {
                bitmap.compress(Bitmap.CompressFormat.JPEG, 100, outputStream)
                outputStream.flush()
                outputStream.close()
            } else {
                val oStream = FileOutputStream(file)
                bitmap.compress(Bitmap.CompressFormat.JPEG, 100, oStream)
                oStream.flush()
                oStream.close()
            }
            count += 1
        }
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
            Log.d(TAG, "Opening Client Socket")
            socket.bind(null)
            socket.connect((InetSocketAddress(host, port)), SOCKET_TIMEOUT)
            Log.d(TAG, "Client Socket ${socket.isConnected}")
            val stream: OutputStream = socket.getOutputStream()
            val cr:ContentResolver = context.contentResolver
            val inputStream: InputStream = cr.openInputStream(Uri.parse(fileUri))!!
            frameGrabber(inputStream, stream)
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