package com.example.p2pconnection

import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Button
import android.widget.MediaController
import android.widget.VideoView
import androidx.appcompat.app.AppCompatActivity

class VideoPlayer: AppCompatActivity(){
    lateinit var button:Button
    lateinit var videoView: VideoView
    lateinit var mediaController: MediaController
    private val TAG: String = "Video Player"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.videoplayer)

        button = findViewById(R.id.buttonVideo)
        videoView = findViewById(R.id.videoView)
        mediaController = MediaController(this)

    }

    fun playVideo(view: View){
        // The following two lines must be replaced with an intent
        val videoUrl = intent.getStringExtra("uri")
        val uri:Uri = Uri.parse(videoUrl)
        videoView.setVideoURI(uri)
        videoView.setMediaController(mediaController)
        mediaController.setAnchorView(videoView)
        videoView.start()
    }

    override fun onResume() {
        videoView.resume()
        super.onResume()
    }

    override fun onPause() {
        videoView.suspend()
        super.onPause()
    }

    override fun onDestroy() {
        videoView.stopPlayback()
        super.onDestroy()
    }

    fun returnActivity(view: View){
        Log.v(TAG,"Trying to exit out of the activity")
        finish()
    }

}