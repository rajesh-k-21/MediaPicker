package com.rktech.mediapicker.utils

import com.rktech.mediapicker.cropper.CropImageOptions

data class PickerOptions(

    val captureImageButtonIconAndText: Pair<Int?, String?>? = null,
    val selectImageButtonIconAndText: Pair<Int?, String?>? = null,

    val captureVideoButtonIconAndText: Pair<Int?, String?>? = null,
    val selectVideoButtonIconAndText: Pair<Int?, String?>? = null,

    val bottomSheetBackgroundColor: Int? = null,

    val isVideoPickEnable : Boolean = true,
    val isPhotoPickEnable : Boolean = true,
    val isCompressEnable: Boolean = true,
    val isCropEnable: Boolean = false,
    val cropOptions: CropImageOptions? = null,

    val maxVideoSizeInMb : Int = 0,
    val maxVideoDurationInMin : Int = 0,
)