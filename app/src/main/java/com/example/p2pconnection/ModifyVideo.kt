// This class is utilised for profiling only -- need not be included for the actual code.
package com.example.p2pconnection

import android.content.ContentResolver
import android.content.Context
import android.graphics.Bitmap
import android.net.Uri
import android.util.Log
import org.bytedeco.ffmpeg.global.avutil
import org.bytedeco.javacv.*
import java.io.File
import java.io.FileOutputStream
import java.io.InputStream
import java.io.OutputStream

// This class is utilised to modify the video to satisfy the required condition
class ModifyVideo(context: Context, cr: ContentResolver, fileUri: Uri) {
    val classContext: Context = context
    val mCr: ContentResolver = cr
    val mFileUri: Uri = fileUri

    companion object {
        private const val TAG: String = "Modify Video"
    }

    // This function is used to get the meta data of a given frame using FFMPEG
    private fun getMetaData(grabber: FFmpegFrameGrabber){
        val metaData = grabber.metadata
        Log.v(TAG, "Metadata Video $metaData")
        val metaOp = grabber.videoOptions
        Log.v(TAG, "Metadata Audio $metaOp")
        val codecVideo = grabber.videoCodec
        Log.v(TAG, "Video Codec $codecVideo")
        val codecName = grabber.videoCodecName
        Log.v(TAG, "Video Codec Name $codecName")
    }

    fun sendFrame(outputStream: OutputStream) {
        FFmpegLogCallback.set()
        val height = 5760
        val width = 2880
        val inputStream: InputStream = mCr.openInputStream(mFileUri)!!
        val file = File(
            classContext.getExternalFilesDir("received"),
            "monkey_key_frame" + ".mp4"
        )
        //val recorder = FFmpegFrameRecorder(file, height, width, 1)
        val frames = sequence {
            val grabber = FFmpegFrameGrabber(inputStream, 0)
            Log.d(TAG, "pixelFormat is ${grabber.pixelFormat}")
            //recorder.pixelFormat = avutil.AV_PIX_FMT_BGR24
            //recorder.videoCodec = grabber.videoCodec
           // recorder.videoCodecName = grabber.videoCodecName
            //grabber.setVideoOption("thread", "8")
            grabber.start()
            while (true) {
                yield(grabber.grabFrame() ?: break)
            }
            grabber.stop()
        }.constrainOnce()

        val converter = AndroidFrameConverter()
        for (frame in frames) {
            var sentTime = 0
            while (true) {
                val bitmap = converter.convert(frame)
                bitmap.compress(Bitmap.CompressFormat.JPEG, 100, outputStream)
                outputStream.flush()
                outputStream.close()
                sentTime += 1
                if (sentTime % 10 == 0) {
                    Log.v(TAG, "Sent frame ${sentTime} times")
                }
            }
        }
    }

    fun sendFrame2(outputStream: OutputStream) {
        FFmpegLogCallback.set()
        val height = 5760
        val width = 2880
        val inputStream: InputStream = mCr.openInputStream(mFileUri)!!
        val grabber = FFmpegFrameGrabber(inputStream, 0)
        grabber.start()
        getMetaData(grabber)
        var frame: Frame? = null
        val startTime = System.currentTimeMillis()
        var nFrames = 0
        val converter = AndroidFrameConverter()
        var sentTime = 0
        while (grabber.grabFrame().also { frame = it } != null) {
            while (true) {
                val bitmap = converter.convert(frame)
                bitmap.compress(Bitmap.CompressFormat.JPEG, 100, outputStream)
                sentTime += 1
                if (sentTime % 10 == 0) {
                    Log.v(TAG, "Sent frame ${sentTime} times")
                }
            }
            outputStream.flush()
            outputStream.close()
            nFrames += 1
            if (nFrames % 10 == 0 && false)
                Log.d(TAG, "nFrames $nFrames done")
        }

    }


    // This is the method used to get the key frames by using FFMPEG
    fun frameRecorder(outputStream: OutputStream){
        val inputStream : InputStream = mCr.openInputStream(mFileUri)!!
        val file = File(classContext.getExternalFilesDir("received"),
            "monkey_key_frame"+".mp4")
        val dirs = File(file.parent!!)
        if (!dirs.exists())
            dirs.mkdirs()
        if (file.exists())
            file.delete()

        if (true) {
            val height = 5760
            val width = 2880
            //FFmpegLogCallback.set()
            val recorder = FFmpegFrameRecorder(file, height, width, 1)
            val grabber = FFmpegFrameGrabber(inputStream, 0)
            recorder.pixelFormat = avutil.AV_PIX_FMT_YUV420P
            recorder.videoCodec = grabber.videoCodec
            recorder.videoCodecName = grabber.videoCodecName
            grabber.start()
            getMetaData(grabber)
            recorder.start()
            var frame: Frame? = null
            val startTime = System.currentTimeMillis()
            var nFrames = 0
            while (grabber.grabFrame().also { frame = it } != null) {
                recorder.record(frame)
                nFrames += 1
                if (nFrames % 10 == 0 && false)
                    Log.d(TAG, "nFrames $nFrames done")
            }
            val difference = (System.currentTimeMillis() - startTime)/1000
            Log.d(TAG, "Total frames $nFrames took $difference")
            recorder.stop()
            grabber.stop()

        } else {
            FFmpegLogCallback.set()
            val height = 5760
            val width = 2880
            val recorder = FFmpegFrameRecorder(file, height, width, 1)
            val frames = sequence {
                val grabber = FFmpegFrameGrabber(inputStream, 0)
                Log.d(TAG, "pixelFormat is ${grabber.pixelFormat}")
                recorder.pixelFormat = avutil.AV_PIX_FMT_YUV420P
                recorder.videoCodec = grabber.videoCodec
                recorder.videoCodecName = grabber.videoCodecName
                //grabber.setVideoOption("thread", "8")
                grabber.start()
                while (true) {
                    yield(grabber.grabFrame() ?: break)
                }
                grabber.stop()
            }.constrainOnce()
            recorder.setVideoOption("preset", "ultrafast")
            //recorder.setVideoOption("thread", "8")
            recorder.start()
            var nFrames = 0
            for (frame in frames) {
                recorder.record(frame)
                nFrames += 1
                if (nFrames % 10 == 0)
                    Log.d(TAG, "nFrames $nFrames done")
            }
            Log.v(TAG, "Out of loop")
            recorder.stop()
        }
        Log.v(TAG, "Put in the file")
        //     DeviceDetailFragment.copyFile(inputStream, outputStream)
    }

}