package com.rktech.mediapicker

import android.Manifest
import android.app.Activity
import android.content.Intent
import android.content.pm.PackageManager
import android.media.MediaScannerConnection
import android.net.Uri
import android.os.Build
import android.provider.MediaStore
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.FileProvider
import com.rktech.mediapicker.cropper.CropImage
import com.rktech.mediapicker.cropper.CropImageContract
import com.rktech.mediapicker.interfaces.OnError
import com.rktech.mediapicker.interfaces.OnResult
import com.rktech.mediapicker.ui.ImagePickerBottomSheet
import com.rktech.mediapicker.utils.FileUtils
import com.rktech.mediapicker.utils.PickerOptions
import com.rktech.mediapicker.utils.getFileSize
import com.rktech.mediapicker.utils.getVideoDuration
import com.rktech.mediapicker.utils.openPermissionSetting
import java.io.File
import java.io.IOException

class ImagePicker(
    private val activity: ComponentActivity,
    private val pickerOptions: PickerOptions,
    private val onResult: OnResult,
    private val onError: OnError
) {

    private var captureImagePath: String? = null
    private var captureVideoPath: String? = null

    private var isRequestCameraPermissionType: Int = 0
    private var isRequestReadStorePermissionType: Int = 0

    private var imagePickerBottomSheet: ImagePickerBottomSheet? = null

    private val requestCameraPermission = activity.registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        if (isGranted) {
            if (isRequestCameraPermissionType == 0)
                dispatchTakePictureIntent()
            else
                dispatchTakeVideoIntent()
        } else {
            activity.openPermissionSetting()
            imagePickerBottomSheet?.dismiss()
        }
    }

    private val requestReadStorePermission = activity.registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        if (isGranted) {
            imagePickerBottomSheet?.openGallery(isRequestReadStorePermissionType)
        } else {
            activity.openPermissionSetting()
            imagePickerBottomSheet?.dismiss()
        }
    }

    internal fun requestCameraPermission(type: Int) {
        isRequestCameraPermissionType = type
        requestCameraPermission.launch(Manifest.permission.CAMERA)
    }

    internal fun requestReadStorePermission(type: Int) {
        isRequestReadStorePermissionType = type
        requestReadStorePermission.launch(Manifest.permission.READ_EXTERNAL_STORAGE)
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
                        onResult.onResult(true, captureImagePath)
                        imagePickerBottomSheet?.dismiss()
                    } catch (e: Exception) {
                        e.printStackTrace()
                    }
            } else {
                Log.e("ImagePicker", "No media selected")
            }
        }

    internal val pickVideoForTiramisu =
        activity.registerForActivityResult(ActivityResultContracts.PickVisualMedia()) { uri ->
            if (uri != null) {
                try {

                    captureVideoPath = FileUtils.getFile(activity, uri)?.absolutePath

                    /* if (pickerOptions.isCompressEnable)
                       captureVideoPath =
                           FileUtils.compressImage(captureImagePath, captureVideoPath!!)*/

                    processingResultVideo(captureVideoPath)

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


                        /* val fileIntent = Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE)
                         activity.sendBroadcast(fileIntent.apply {
                             data = Uri.fromFile(File(captureImagePath!!))
                         })*/

                        val file = File(captureImagePath!!)
                        MediaScannerConnection.scanFile(
                            activity,
                            arrayOf(file.toString()),
                            null,
                            null
                        )

                        onResult.onResult(true, captureImagePath)
                        imagePickerBottomSheet?.dismiss()
                    } catch (e: Exception) {
                        e.printStackTrace()
                    }
            } else {
                onError.onError("Error code :- ${it.resultCode}")
                imagePickerBottomSheet?.dismiss()
            }
        }

    private val captureVideoIntent =
        activity.registerForActivityResult(ActivityResultContracts.StartActivityForResult()) {
            if (it.resultCode == Activity.RESULT_OK && captureVideoPath != null) {
                try {

                    /* val fileIntent = Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE)
                     activity.sendBroadcast(fileIntent.apply {
                         data = Uri.fromFile(File(captureVideoPath!!))
                     })*/

                    val file = File(captureVideoPath!!)
                    MediaScannerConnection.scanFile(activity, arrayOf(file.toString()), null, null)

                    onResult.onResult(false, captureVideoPath)
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
                        onResult.onResult(true, captureImagePath)
                        imagePickerBottomSheet?.dismiss()
                    } catch (e: Exception) {
                        e.printStackTrace()
                    }
                }
            }
        }

    internal val pickVideoIntent =
        activity.registerForActivityResult(ActivityResultContracts.StartActivityForResult()) {
            if (it.data?.data != null) {
                try {
                    captureVideoPath =
                        FileUtils.getFile(activity, it.data?.data)?.absolutePath

                    /*if (pickerOptions.isCompressEnable)
                        captureVideoPath =
                            FileUtils.compressImage(captureVideoPath, captureVideoPath!!)*/

                    processingResultVideo(captureVideoPath)

                    imagePickerBottomSheet?.dismiss()
                } catch (e: Exception) {
                    e.printStackTrace()
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

                        onResult.onResult(true, captureImagePath)

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

    @Suppress("DEPRECATION")
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

                val resInfoList = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                    activity.packageManager.queryIntentActivities(
                        Intent(Intent.ACTION_MAIN, null).addCategory(Intent.CATEGORY_LAUNCHER),
                        PackageManager.ResolveInfoFlags.of(PackageManager.MATCH_DEFAULT_ONLY.toLong())
                    )
                } else {
                    activity.packageManager.queryIntentActivities(
                        Intent(Intent.ACTION_MAIN, null).addCategory(Intent.CATEGORY_LAUNCHER),
                        PackageManager.GET_META_DATA
                    )
                }

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

    @Suppress("DEPRECATION")
    internal fun dispatchTakeVideoIntent() {
        var videoURI: Uri? = null
        val takeVideoIntent = Intent(MediaStore.ACTION_VIDEO_CAPTURE)
        if (takeVideoIntent.resolveActivity(activity.packageManager) != null) {
            var videoFile: File? = null
            try {

                videoFile = FileUtils.createVideoFile(activity)
                captureVideoPath = videoFile.absolutePath

                videoURI = FileProvider.getUriForFile(
                    activity,
                    activity.packageName + ".provider",
                    videoFile
                )

            } catch (ex: IOException) {
                ex.printStackTrace()
            }

            if (videoFile != null) {
                takeVideoIntent.putExtra(
                    MediaStore.EXTRA_OUTPUT,
                    videoURI
                )

                //Grant uri permission for all packages

                val resInfoList = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                    activity.packageManager.queryIntentActivities(
                        Intent(Intent.ACTION_MAIN, null).addCategory(Intent.CATEGORY_LAUNCHER),
                        PackageManager.ResolveInfoFlags.of(PackageManager.MATCH_DEFAULT_ONLY.toLong())
                    )
                } else {
                    activity.packageManager.queryIntentActivities(
                        Intent(Intent.ACTION_MAIN, null).addCategory(Intent.CATEGORY_LAUNCHER),
                        PackageManager.GET_META_DATA
                    )
                }

                for (resolveInfo in resInfoList) {
                    val packageName = resolveInfo.activityInfo.packageName
                    activity.grantUriPermission(
                        packageName,
                        videoURI,
                        Intent.FLAG_GRANT_WRITE_URI_PERMISSION or Intent.FLAG_GRANT_READ_URI_PERMISSION
                    )
                }
                captureVideoIntent.launch(takeVideoIntent)
            }
        }
    }

    private fun processingResultVideo(captureVideoPath: String?) {
        when {
            pickerOptions.maxVideoSizeInMb == 0 && pickerOptions.maxVideoDurationInMin == 0 -> {
                onResult.onResult(false, captureVideoPath)
            }

            pickerOptions.maxVideoSizeInMb != 0 && pickerOptions.maxVideoDurationInMin != 0 -> {
                if (getFileSize(File(captureVideoPath!!)) > pickerOptions.maxVideoSizeInMb * 1024 * 1024) {
                    onError.onError("The selected video exceeds the maximum file size of ${pickerOptions.maxVideoSizeInMb} MB.")
                } else {
                    getVideoDuration(captureVideoPath) { videoDuration ->
                        if (videoDuration > pickerOptions.maxVideoDurationInMin * 60 * 1000) {
                            onError.onError("The selected video exceeds the maximum duration of ${pickerOptions.maxVideoDurationInMin} minutes.")
                        } else {
                            onResult.onResult(false, captureVideoPath)
                        }
                    }
                }
            }

            pickerOptions.maxVideoSizeInMb != 0 -> {
                if (getFileSize(File(captureVideoPath!!)) > pickerOptions.maxVideoSizeInMb * 1024 * 1024) {
                    onError.onError("The selected video exceeds the maximum file size of ${pickerOptions.maxVideoSizeInMb} MB.")
                } else {
                    onResult.onResult(false, captureVideoPath)
                }
            }

            else -> {
                getVideoDuration(captureVideoPath!!) { videoDuration ->
                    if (videoDuration > pickerOptions.maxVideoDurationInMin * 60 * 1000) {
                        onError.onError("The selected video exceeds the maximum duration of ${pickerOptions.maxVideoDurationInMin} minutes.")
                    } else {
                        onResult.onResult(false, captureVideoPath)
                    }
                }
            }
        }
    }
}