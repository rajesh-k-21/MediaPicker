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
	        implementation 'com.github.rkahir21:ImagePicker:1.0'
	}

```

## Deployment

How to use this amazing lib and save your time

```bash
     private val imagePicker = ImagePicker(
        this, PickerOptions(
            captureImageButtonIconAndText = Pair(R.drawable.ic_camera,"Open Camera"),
            selectImageButtonIconAndText = Pair(R.drawable.ic_gallery,"Open Gallery")
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
```

```bash
class MainActivity : AppCompatActivity() {

    private val imagePicker = ImagePicker(this, PickerOptions(
        captureImageButtonIconAndText = Pair(R.drawable.ic_camera, "Open Camera"),
        selectImageButtonIconAndText = Pair(R.drawable.ic_gallery, "Open Gallery"),
    ), onResult = object : OnResult {
        override fun onResult(path: String?) {

            val imageFilePath = path
            val imageFileUri = File(path!!).toUri()

            binding.imageView.apply {
                setImageURI(imageFileUri)
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
}
```

## setPickerOptions
```bash
      PickerOptions(
            captureImageButtonIconAndText = Pair(R.drawable.ic_camera, "Open Camera"),
            selectImageButtonIconAndText = Pair(R.drawable.ic_gallery, "Open Gallery"),
            bottomSheetBackgroundColor = R.color.white,
            isCompressEnable = true,
            isCropEnable = true,
            cropOptions = CropImageOptions(
                cropShape = CropImageView.CropShape.RECTANGLE,
                cornerShape = CropImageView.CropCornerShape.RECTANGLE,
                cropCornerRadius = 10F,
                guidelines = CropImageView.Guidelines.ON,
                scaleType = CropImageView.ScaleType.FIT_CENTER,
                ...
                )
        )
```

## Contributing

Contributions are always welcome!

See `contributing.md` for ways to get started.

Please adhere to this project's `code of conduct`.

## Authors

- [@rkahir21](https://github.com/rkahir21)

## Happy Coding | Made with ❤ | Made in 🇮🇳 ... :)