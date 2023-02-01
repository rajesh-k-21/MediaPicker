package com.rktech.imagepicker

import android.app.Activity
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.provider.MediaStore
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.FileProvider
import com.rktech.imagepicker.cropper.CropImage
import com.rktech.imagepicker.cropper.CropImageContract
import com.rktech.imagepicker.interfaces.OnError
import com.rktech.imagepicker.interfaces.OnResult
import com.rktech.imagepicker.ui.ImagePickerBottomSheet
import com.rktech.imagepicker.utils.FileUtils
import com.rktech.imagepicker.utils.PickerOptions
import java.io.File
import java.io.IOException

class ImagePicker(
    private val activity: ComponentActivity,
    pickerOptions: PickerOptions,
    onResult: OnResult,
    onError: OnError
) {

    private var captureImagePath: String? = null
    private var imagePickerBottomSheet: ImagePickerBottomSheet? = null

    internal val requestCameraPermission = activity.registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        if (isGranted) {
            dispatchTakePictureIntent()
        } else {
            onError.onError("Please allow permission from setting")
            imagePickerBottomSheet?.dismiss()
        }
    }

    internal val requestReadStorePermission = activity.registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        if (isGranted) {
            imagePickerBottomSheet?.openGallery()
        } else {
            onError.onError("Please allow permission from setting")
            imagePickerBottomSheet?.dismiss()
        }
    }

    internal val pickPhotoForTiramisu =
        activity.registerForActivityResult(ActivityResultContracts.PickVisualMedia()) { uri ->
            if (uri != null) {
                if (pickerOptions.isCropEnable) {
                    imagePickerBottomSheet?.launchImageCrop(uri)
                } else
                    try {
                        captureImagePath = FileUtils.getFile(activity, uri)?.absolutePath
                        if (pickerOptions.isCompressEnable)
                            captureImagePath =
                                FileUtils.compressImage(captureImagePath, captureImagePath!!)
                        onResult.onResult(captureImagePath)
                        imagePickerBottomSheet?.dismiss()
                    } catch (e: Exception) {
                        e.printStackTrace()
                    }
            } else {
                Log.e("ImagePicker", "No media selected")
            }
        }

    private val capturePhotoIntent =
        activity.registerForActivityResult(ActivityResultContracts.StartActivityForResult()) {
            if (it.resultCode == Activity.RESULT_OK && captureImagePath != null) {
                if (pickerOptions.isCropEnable) {
                    imagePickerBottomSheet?.launchImageCrop(Uri.fromFile(File(captureImagePath!!)))
                } else
                    try {
                        val fileIntent = Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE)
                        activity.sendBroadcast(fileIntent.apply {
                            data = Uri.fromFile(File(captureImagePath!!))
                        })
                        onResult.onResult(captureImagePath)
                        imagePickerBottomSheet?.dismiss()
                    } catch (e: Exception) {
                        e.printStackTrace()
                    }
            } else {
                onError.onError("Error code :- ${it.resultCode}")
                imagePickerBottomSheet?.dismiss()
            }
        }

    internal val pickPhotoIntent =
        activity.registerForActivityResult(ActivityResultContracts.StartActivityForResult()) {
            if (it.data?.data != null) {
                if (pickerOptions.isCropEnable) {
                    imagePickerBottomSheet?.launchImageCrop(it.data?.data!!)
                } else {
                    try {
                        captureImagePath =
                            FileUtils.getFile(activity, it.data?.data)?.absolutePath
                        if (pickerOptions.isCompressEnable)
                            captureImagePath =
                                FileUtils.compressImage(captureImagePath, captureImagePath!!)
                        onResult.onResult(captureImagePath)
                        imagePickerBottomSheet?.dismiss()
                    } catch (e: Exception) {
                        e.printStackTrace()
                    }
                }
            }
        }

    //Crop area
    internal val cropImageResult =
        activity.registerForActivityResult(CropImageContract()) { result ->
            when {
                result.isSuccessful -> {
                    Log.d("AIC-Sample", "Original bitmap: ${result.originalBitmap}")
                    Log.d("AIC-Sample", "Original uri: ${result.originalUri}")
                    Log.d("AIC-Sample", "Output bitmap: ${result.bitmap}")
                    Log.d("AIC-Sample", "Output uri: ${result.getUriFilePath(activity)}")

                    try {
                        captureImagePath =
                            FileUtils.getFile(activity, result.uriContent)?.absolutePath

                        if (pickerOptions.isCompressEnable)
                            captureImagePath =
                                FileUtils.compressImage(captureImagePath, captureImagePath!!)

                        onResult.onResult(captureImagePath)

                        imagePickerBottomSheet?.dismiss()

                    } catch (e: Exception) {
                        e.printStackTrace()
                    }

                }
                result is CropImage.CancelledResult -> {
                    onError.onError("cropping image was cancelled by the user")
                    imagePickerBottomSheet?.dismiss()
                }
                else -> {
                    onError.onError("cropping image failed")
                    dispatchTakePictureIntent()
                }
            }
        }


    init {
        imagePickerBottomSheet = ImagePickerBottomSheet(activity, pickerOptions, this)
    }

    fun openImagePicker() {
        imagePickerBottomSheet?.show(
            (activity as AppCompatActivity).supportFragmentManager,
            imagePickerBottomSheet?.tag
        )
    }

    internal fun dispatchTakePictureIntent() {
        var photoURI: Uri? = null
        val takePictureIntent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
        if (takePictureIntent.resolveActivity(activity.packageManager) != null) {
            var photoFile: File? = null
            try {

                photoFile = FileUtils.createImageFile(activity)
                captureImagePath = photoFile.absolutePath

                photoURI = FileProvider.getUriForFile(
                    activity,
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
                    activity.packageManager.queryIntentActivities(
                        takePictureIntent,
                        PackageManager.MATCH_DEFAULT_ONLY
                    )
                for (resolveInfo in resInfoList) {
                    val packageName = resolveInfo.activityInfo.packageName
                    activity.grantUriPermission(
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