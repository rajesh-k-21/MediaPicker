package com.rktech.imagepicker.utils

import com.rktech.imagepicker.cropper.CropImageOptions

data class PickerOptions(
    val captureImageButtonIconAndText: Pair<Int?, String?>? = null,
    val selectImageButtonIconAndText: Pair<Int?, String?>? = null,
    val bottomSheetBackgroundColor: Int? = null,

    val isCompressEnable: Boolean = true,
    val isCropEnable: Boolean = false,
    val cropOptions: CropImageOptions? = null
)