package com.rktech.imagepicker.ui

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.content.res.ColorStateList
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.MediaStore
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.ComponentActivity
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.content.FileProvider
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.rktech.imagepicker.R
import com.rktech.imagepicker.cropper.CropImage
import com.rktech.imagepicker.cropper.CropImageContract
import com.rktech.imagepicker.cropper.CropImageContractOptions
import com.rktech.imagepicker.cropper.CropImageOptions
import com.rktech.imagepicker.databinding.ImagePickerBottomSheetBinding
import com.rktech.imagepicker.interfaces.OnError
import com.rktech.imagepicker.interfaces.OnResult
import com.rktech.imagepicker.utils.Const
import com.rktech.imagepicker.utils.FileUtils
import com.rktech.imagepicker.utils.FileUtils.createImageFile
import com.rktech.imagepicker.utils.PickerOptions
import java.io.File
import java.io.IOException

class ImagePickerBottomSheet(
    private val activity: ComponentActivity,
    private val pickerOptions: PickerOptions,
    private val onResult: OnResult,
    private val onError: OnError
) : BottomSheetDialogFragment() {

    private var captureImagePath: String? = null

    private lateinit var binding: ImagePickerBottomSheetBinding

    private val requestCameraPermission = activity.registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        if (isGranted) {
            dispatchTakePictureIntent()
        } else {
            onError.onError("Please allow permission from setting")
            dismissAllowingStateLoss()
        }
    }

    private val requestReadStorePermission = activity.registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        if (isGranted) {
            openGallery()
        } else {
            onError.onError("Please allow permission from setting")
            dismissAllowingStateLoss()
        }
    }

    private val pickPhotoForTiramisu =
        activity.registerForActivityResult(ActivityResultContracts.PickVisualMedia()) { uri ->
            if (uri != null) {
                if (pickerOptions.isCropEnable) {
                    launchImageCrop(uri)
                } else
                    try {
                        captureImagePath = FileUtils.getFile(requireContext(), uri)?.absolutePath
                        if (pickerOptions.isCompressEnable)
                            captureImagePath =
                                FileUtils.compressImage(captureImagePath, captureImagePath!!)
                        onResult.onResult(captureImagePath)
                        dismissAllowingStateLoss()
                    } catch (e: Exception) {
                        e.printStackTrace()
                    }
            } else {
                Log.e("ImagePicker", "No media selected")
            }
        }

    private val capturePhotoIntent =
        activity.registerForActivityResult(ActivityResultContracts.StartActivityForResult()) {
            if (captureImagePath != null) {
                if (pickerOptions.isCropEnable) {
                    launchImageCrop(Uri.fromFile(File(captureImagePath!!)))
                } else
                    try {
                        val fileIntent = Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE)
                        activity.sendBroadcast(fileIntent.apply {
                            data = Uri.fromFile(File(captureImagePath!!))
                        })
                        onResult.onResult(captureImagePath)
                        dismissAllowingStateLoss()
                    } catch (e: Exception) {
                        e.printStackTrace()
                    }
            } else {
                onError.onError("Error code :- ${it.resultCode}")
                dismissAllowingStateLoss()
            }
        }

    private val pickPhotoIntent =
        activity.registerForActivityResult(ActivityResultContracts.StartActivityForResult()) {
            if (it.data?.data != null) {
                if (pickerOptions.isCropEnable) {
                    launchImageCrop(it.data?.data!!)
                } else {
                    try {
                        captureImagePath =
                            FileUtils.getFile(requireContext(), it.data?.data)?.absolutePath
                        if (pickerOptions.isCompressEnable)
                            captureImagePath =
                                FileUtils.compressImage(captureImagePath, captureImagePath!!)
                        onResult.onResult(captureImagePath)
                        dismissAllowingStateLoss()
                    } catch (e: Exception) {
                        e.printStackTrace()
                    }
                }
            }
        }

    //Crop area
    private val cropImageResult = registerForActivityResult(CropImageContract()) { result ->
        when {
            result.isSuccessful -> {
                Log.d("AIC-Sample", "Original bitmap: ${result.originalBitmap}")
                Log.d("AIC-Sample", "Original uri: ${result.originalUri}")
                Log.d("AIC-Sample", "Output bitmap: ${result.bitmap}")
                Log.d("AIC-Sample", "Output uri: ${result.getUriFilePath(requireContext())}")

                try {
                    captureImagePath =
                        FileUtils.getFile(requireContext(), result.uriContent)?.absolutePath

                    if (pickerOptions.isCompressEnable)
                        captureImagePath =
                            FileUtils.compressImage(captureImagePath, captureImagePath!!)

                    onResult.onResult(captureImagePath)

                    dismissAllowingStateLoss()

                } catch (e: Exception) {
                    e.printStackTrace()
                }

            }
            result is CropImage.CancelledResult -> {
                onError.onError("cropping image was cancelled by the user")
                dismissAllowingStateLoss()
            }
            else -> {
                onError.onError("cropping image failed")
                dispatchTakePictureIntent()
            }
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = ImagePickerBottomSheetBinding.inflate(inflater)
        return binding.root
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setStyle(STYLE_NORMAL, R.style.AppBottomSheetDialogTheme)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        Const.AUTHORITY = activity.packageName + ".provider"

        initUi()
    }

    private fun initUi() = with(binding) {

        imageButtonCamera.setOnClickListener { textViewCamera.performClick() }
        textViewCamera.setOnClickListener {
            clickOnCamera()
        }

        imageButtonGallery.setOnClickListener { textViewGallery.performClick() }
        textViewGallery.setOnClickListener {
            clickOnGallery()
        }

        pickerOptions.apply {

            bottomSheetBackgroundColor?.let {
                constraintMain.backgroundTintList =
                    ColorStateList.valueOf(ContextCompat.getColor(activity, it))
            }

            captureImageButtonIconAndText?.let { pair ->
                pair.first?.let {
                    imageButtonCamera.setImageDrawable(ContextCompat.getDrawable(activity, it))
                }
                pair.second?.let {
                    textViewCamera.text = it
                }
            }

            selectImageButtonIconAndText?.let { pair ->
                pair.first?.let {
                    imageButtonGallery.setImageDrawable(ContextCompat.getDrawable(activity, it))
                }
                pair.second?.let {
                    textViewGallery.text = it
                }
            }
        }
    }

    private fun clickOnGallery() {
        if (ActivityResultContracts.PickVisualMedia.isPhotoPickerAvailable()) {
            pickPhotoForTiramisu.launch(PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly))
        } else {
            if (Build.VERSION.SDK_INT in 23..29) {
                if (ActivityCompat.checkSelfPermission(
                        requireActivity(),
                        Manifest.permission.READ_EXTERNAL_STORAGE
                    ) != PackageManager.PERMISSION_GRANTED
                ) {
                    requestReadStorePermission.launch(Manifest.permission.READ_EXTERNAL_STORAGE)
                } else {
                    openGallery()
                }
            } else {
                openGallery()
            }
        }
    }

    private fun clickOnCamera() {
        if (Build.VERSION.SDK_INT >= 23) {
            if (
                ActivityCompat.checkSelfPermission(
                    requireActivity(),
                    Manifest.permission.CAMERA
                ) != PackageManager.PERMISSION_GRANTED
            ) {
                requestCameraPermission.launch(Manifest.permission.CAMERA)
            } else {
                dispatchTakePictureIntent()
            }
        } else
            dispatchTakePictureIntent()
    }

    private fun openGallery() {
        val galleryIntent =
            Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI).apply {
                type = "image/*"
            }
        pickPhotoIntent.launch(galleryIntent)

    }

    private fun launchImageCrop(fileUri: Uri) {
        cropImageResult.launch(
            CropImageContractOptions(
                uri = fileUri,
                cropImageOptions = CropImageOptions(),
            ),
        )
    }

    private fun dispatchTakePictureIntent() {
        var photoURI: Uri? = null
        val takePictureIntent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
        if (takePictureIntent.resolveActivity(requireActivity().packageManager) != null) {
            var photoFile: File? = null
            try {

                photoFile = createImageFile(activity)
                captureImagePath = photoFile.absolutePath

                photoURI = FileProvider.getUriForFile(
                    requireContext(),
                    activity.packageName + ".provider",
                    photoFile
                )

            } catch (ex: IOException) {
                ex.printStackTrace()
            }

            if (photoFile != null) {
                takePictureIntent.putExtra(
                    MediaStore.EXTRA_OUTPUT,
                    photoURI
                )

                //Grant uri permission for all packages
                val resInfoList =
                    requireContext().packageManager.queryIntentActivities(
                        takePictureIntent,
                        PackageManager.MATCH_DEFAULT_ONLY
                    )
                for (resolveInfo in resInfoList) {
                    val packageName = resolveInfo.activityInfo.packageName
                    requireContext().grantUriPermission(
                        packageName,
                        photoURI,
                        Intent.FLAG_GRANT_WRITE_URI_PERMISSION or Intent.FLAG_GRANT_READ_URI_PERMISSION
                    )
                }
                capturePhotoIntent.launch(takePictureIntent)
            }
        }
    }
}