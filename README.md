# ImagePicker - Simple ImagePicker Library For Android

This library is created to make easy image selection 

## Features

- Easy to use
- Image picker using camera & gallery
- Support Android 5.0 (Lollipop) to 13 (Tiramisu)

## How to Add
Project level gradle file

```bash
   allprojects {
		repositories {
			...
			maven { url 'https://jitpack.io' }
		}
	}
```

App level gradle file
```bash
	dependencies {
	        implementation 'com.github.rajesh-k-21:MediaPicker:1.1'
	}
```

## Deployment

How to use this amazing lib and save your time

```bash
    private val imagePicker = ImagePicker(this, PickerOptions(
        captureImageButtonIconAndText = Pair(R.drawable.ic_camera, "Capture image"),
        selectImageButtonIconAndText = Pair(R.drawable.ic_gallery, "Select image from gallery"),
        captureVideoButtonIconAndText = Pair(R.drawable.ic_camera, "Capture video"),
        selectVideoButtonIconAndText = Pair(R.drawable.ic_gallery, "Select video from gallery"),
        isCropEnable = true,
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
```

```bash
class MainActivity : AppCompatActivity() {

    private var simpleExoPlayer: ExoPlayer? = null

    private val mediaDataSourceFactory by lazy {
        DefaultDataSource.Factory(this)
    }

    private val imagePicker = ImagePicker(this, PickerOptions(
        captureImageButtonIconAndText = Pair(R.drawable.ic_camera, "Capture image"),
        selectImageButtonIconAndText = Pair(R.drawable.ic_gallery, "Select image from gallery"),
        captureVideoButtonIconAndText = Pair(R.drawable.ic_camera, "Capture video"),
        selectVideoButtonIconAndText = Pair(R.drawable.ic_gallery, "Select video from gallery"),
        isCropEnable = true,
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
```

## setPickerOptions
```bash
      PickerOptions(
            captureImageButtonIconAndText = Pair(R.drawable.ic_camera, "Capture image"),
            selectImageButtonIconAndText = Pair(R.drawable.ic_gallery, "Select image from gallery"),
            captureVideoButtonIconAndText = Pair(R.drawable.ic_camera, "Capture video"),
            selectVideoButtonIconAndText = Pair(R.drawable.ic_gallery, "Select video from gallery"),
            
            bottomSheetBackgroundColor = R.color.white,
            cropOptions = CropImageOptions(
                cropShape = CropImageView.CropShape.RECTANGLE,
                cornerShape = CropImageView.CropCornerShape.RECTANGLE,
                cropCornerRadius = 10F,
                guidelines = CropImageView.Guidelines.ON,
                scaleType = CropImageView.ScaleType.FIT_CENTER,
                ...
                ),
                  
            isVideoPickEnable = true,
            isPhotoPickEnable = true,
            isCompressEnable = true,
            isCropEnable = false,
            
            maxVideoSizeInMb = 5,
            maxVideoDurationInMin = 2,

            )
        )
```

## Contributing

Contributions are always welcome!

See `contributing.md` for ways to get started.

Please adhere to this project's `code of conduct`.

## Authors

- [@rkahir21](https://github.com/rajesh-k-21)

## Happy Coding | Made with ❤ | Made in 🇮🇳 ... :)