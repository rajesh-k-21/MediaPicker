package com.rktech.mediapicker.utils

import android.Manifest

object Const {

    const val TAG = "com.rktech.mediapicker"

    var AUTHORITY = ""

    val PERMISSIONS_GALLERY = arrayOf(Manifest.permission.READ_EXTERNAL_STORAGE)
    val PERMISSIONS_CAMERA = arrayOf(Manifest.permission.CAMERA)

    const val REQUEST_CAMERA_PERMISSION = 1
    const val REQUEST_GALLERY_PERMISSION = 2

    const val REQUEST_TAKE_PHOTO = 1
    const val RESULT_LOAD_IMAGE = 2
    const val BANUBA_VIDEO = 25
    const val RESULT_LOAD_VIDEO = 6
    const val REQUEST_IMAGE_AND_VIDEO = 3

    /*const val CROP_IMAGE_ACTIVITY_REQUEST_CODE = 250*/
    const val REQUEST_TRIM_VIDEO = 11
    const val VIEW_IMAGE = 1
    const val VIEW_VIDEO = 2
    const val VIEW_DEFAULT = 0
    const val VIEW_CAMERA = 1
    const val VIEW_GALLERY = 2
}