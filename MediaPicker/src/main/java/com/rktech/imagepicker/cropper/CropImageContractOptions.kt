package com.rktech.imagepicker.cropper

import android.net.Uri

data class CropImageContractOptions(
  val uri: Uri?,
  val cropImageOptions: CropImageOptions,
)
