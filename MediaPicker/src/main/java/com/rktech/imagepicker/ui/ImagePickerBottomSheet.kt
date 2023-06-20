package com.rktech.imagepicker.ui

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.content.res.ColorStateList
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.MediaStore
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.ComponentActivity
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.rktech.imagepicker.ImagePicker
import com.rktech.imagepicker.R
import com.rktech.imagepicker.cropper.CropImageContractOptions
import com.rktech.imagepicker.cropper.CropImageOptions
import com.rktech.imagepicker.databinding.ImagePickerBottomSheetBinding
import com.rktech.imagepicker.utils.Const
import com.rktech.imagepicker.utils.PickerOptions

class ImagePickerBottomSheet(
    private val activity: ComponentActivity,
    private val pickerOptions: PickerOptions,
    private val imagePicker: ImagePicker,

    ) : BottomSheetDialogFragment() {

    private lateinit var binding: ImagePickerBottomSheetBinding

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
            clickOnCamera(0)
        }

        imageButtonGallery.setOnClickListener { textViewGallery.performClick() }
        textViewGallery.setOnClickListener {
            clickOnGallery(0)
        }

        imageButtonVideoCamera.setOnClickListener { textViewVideoCamera.performClick() }
        textViewVideoCamera.setOnClickListener {
            clickOnCamera(1)
        }

        imageButtonVideoGallery.setOnClickListener { textViewVideoGallery.performClick() }
        textViewVideoGallery.setOnClickListener {
            clickOnGallery(1)
        }

        pickerOptions.apply {

            if (isPhotoPickEnable) groupPhoto.visibility = View.VISIBLE

            if (isVideoPickEnable) groupVideo.visibility = View.VISIBLE

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

            captureVideoButtonIconAndText?.let { pair ->
                pair.first?.let {
                    imageButtonVideoCamera.setImageDrawable(ContextCompat.getDrawable(activity, it))
                }
                pair.second?.let {
                    textViewVideoCamera.text = it
                }
            }

            selectVideoButtonIconAndText?.let { pair ->
                pair.first?.let {
                    imageButtonVideoGallery.setImageDrawable(
                        ContextCompat.getDrawable(
                            activity,
                            it
                        )
                    )
                }
                pair.second?.let {
                    textViewVideoGallery.text = it
                }
            }


        }
    }

    private fun clickOnGallery(type: Int) {
        if (ActivityResultContracts.PickVisualMedia.isPhotoPickerAvailable()) {
            if (type == 0) {
                imagePicker.pickPhotoForTiramisu.launch(
                    PickVisualMediaRequest(
                        ActivityResultContracts.PickVisualMedia.ImageOnly
                    )
                )
            } else {
                imagePicker.pickVideoForTiramisu.launch(
                    PickVisualMediaRequest(
                        ActivityResultContracts.PickVisualMedia.VideoOnly
                    )
                )
            }
        } else {
            if (Build.VERSION.SDK_INT in 23..29) {
                if (ActivityCompat.checkSelfPermission(
                        requireActivity(),
                        Manifest.permission.READ_EXTERNAL_STORAGE
                    ) != PackageManager.PERMISSION_GRANTED
                ) {
                    imagePicker.requestReadStorePermission(type)
                } else {
                    openGallery(type)
                }
            } else {
                openGallery(type)
            }
        }
    }

    private fun clickOnCamera(type: Int) {
        if (Build.VERSION.SDK_INT >= 23) {
            if (ActivityCompat.checkSelfPermission(
                    requireActivity(), Manifest.permission.CAMERA
                ) != PackageManager.PERMISSION_GRANTED
            ) {
                imagePicker.requestCameraPermission(type)
            } else {
                if (type == 0) imagePicker.dispatchTakePictureIntent() else imagePicker.dispatchTakeVideoIntent()
            }
        } else {
            if (type == 0) imagePicker.dispatchTakePictureIntent() else imagePicker.dispatchTakeVideoIntent()
        }
    }

    internal fun openGallery(t: Int) {
        val galleryIntent = Intent(
            Intent.ACTION_PICK,
            if (t == 0) MediaStore.Images.Media.EXTERNAL_CONTENT_URI else MediaStore.Video.Media.EXTERNAL_CONTENT_URI
        ).apply {
            type = if (t == 0) "image/*" else "video/*"
        }

        if (t == 0)
            imagePicker.pickPhotoIntent.launch(galleryIntent)
        else
            imagePicker.pickVideoIntent.launch(galleryIntent)

    }

    internal fun launchImageCrop(fileUri: Uri) {
        imagePicker.cropImageResult.launch(
            CropImageContractOptions(
                uri = fileUri,
                cropImageOptions = CropImageOptions(),
            ),
        )
    }
}