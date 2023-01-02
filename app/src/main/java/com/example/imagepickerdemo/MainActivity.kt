package com.example.imagepickerdemo

import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.net.toUri
import com.example.imagepickerdemo.databinding.ActivityMainBinding
import com.rktech.imagepicker.ImagePicker
import com.rktech.imagepicker.interfaces.OnError
import com.rktech.imagepicker.interfaces.OnResult
import com.rktech.imagepicker.utils.PickerOptions
import java.io.File

class MainActivity : AppCompatActivity() {

    private val imagePicker = ImagePicker(
        this, PickerOptions(
            captureImageButtonIconAndText = Pair(R.drawable.ic_camera,"Open Camera"),
            selectImageButtonIconAndText = Pair(R.drawable.ic_gallery,"Open Gallery"),
        ),
        onResult = object : OnResult {
            override fun onResult(path: String?) {

                val imageFilePath = path
                val imageFileUri = File(path!!).toUri()

                binding.imageView.apply {
                    setImageURI(imageFileUri)
                }
            }
        },
        onError = object : OnError {
            override fun onError(e: String?) {
                Toast.makeText(this@MainActivity, e ?: "", Toast.LENGTH_LONG).show()
            }
        }
    )

    private lateinit var binding: ActivityMainBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.buttonImage.setOnClickListener {
            imagePicker.openImagePicker()
        }
    }
}