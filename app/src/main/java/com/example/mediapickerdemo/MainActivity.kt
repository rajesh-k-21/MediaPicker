package com.example.mediapickerdemo

import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.net.toUri
import com.example.imagepickerdemo.R
import com.example.imagepickerdemo.databinding.ActivityMainBinding
import com.google.android.exoplayer2.ExoPlayer
import com.google.android.exoplayer2.MediaItem
import com.google.android.exoplayer2.source.DefaultMediaSourceFactory
import com.google.android.exoplayer2.source.ProgressiveMediaSource
import com.google.android.exoplayer2.upstream.DefaultDataSource
import com.google.android.exoplayer2.util.Util
import com.rktech.imagepicker.ImagePicker
import com.rktech.imagepicker.interfaces.OnError
import com.rktech.imagepicker.interfaces.OnResult
import com.rktech.imagepicker.utils.PickerOptions
import java.io.File

class MainActivity : AppCompatActivity() {

    private var simpleExoPlayer: ExoPlayer? = null

    private val mediaDataSourceFactory by lazy {
        DefaultDataSource.Factory(this)
    }

    private val imagePicker = ImagePicker(
        this, PickerOptions(
            captureImageButtonIconAndText = Pair(R.drawable.ic_camera, "Capture image"),
            selectImageButtonIconAndText = Pair(R.drawable.ic_gallery, "Select image from gallery"),
            captureVideoButtonIconAndText = Pair(R.drawable.ic_camera, "Capture video"),
            selectVideoButtonIconAndText = Pair(R.drawable.ic_gallery, "Select video from gallery"),

            isVideoPickEnable = true,
            isPhotoPickEnable = true,
            isCompressEnable = true,
            isCropEnable = false,

            maxVideoSizeInMb = 5,
            maxVideoDurationInMin = 2,

            ), onResult = object : OnResult {
            override fun onResult(isImage: Boolean, path: String?) {
                if (isImage) {
                    val imageFileUri = File(path!!).toUri()
                    binding.imageView.apply {
                        visibility = View.VISIBLE
                        setImageURI(imageFileUri)
                    }
                    binding.playerView.visibility = View.GONE
                } else {
                    binding.imageView.visibility = View.GONE
                binding.playerView.visibility = View.VISIBLE
                intExo(path)
            }
        }
    }, onError = object : OnError {
        override fun onError(e: String?) {
            Toast.makeText(this@MainActivity, e ?: "", Toast.LENGTH_LONG).show()
        }
    })

    private lateinit var binding: ActivityMainBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.buttonImage.setOnClickListener {
            imagePicker.openImagePicker()
        }
    }

    private fun intExo(mediaPath: String?) {
        val mediaSource = ProgressiveMediaSource.Factory(mediaDataSourceFactory)
            .createMediaSource(MediaItem.fromUri(File(mediaPath!!).toUri()))

        val mediaSourceFactory = DefaultMediaSourceFactory(mediaDataSourceFactory)

        simpleExoPlayer = ExoPlayer.Builder(this).setMediaSourceFactory(mediaSourceFactory).build()

        simpleExoPlayer?.apply {
            addMediaSource(mediaSource)
            repeatMode = ExoPlayer.REPEAT_MODE_OFF
            playWhenReady = true
        }

        binding.playerView.apply {
            player = simpleExoPlayer
            requestFocus()
        }
    }

    public override fun onPause() {
        super.onPause()
        if (Util.SDK_INT <= 23) simpleExoPlayer?.release()
    }
}