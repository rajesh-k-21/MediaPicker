package com.rktech.imagepicker

import androidx.activity.ComponentActivity
import androidx.appcompat.app.AppCompatActivity
import com.rktech.imagepicker.interfaces.OnError
import com.rktech.imagepicker.interfaces.OnResult
import com.rktech.imagepicker.ui.ImagePickerBottomSheet
import com.rktech.imagepicker.utils.PickerOptions

class ImagePicker(
    private val activity: ComponentActivity,
    pickerOptions: PickerOptions,
    onResult: OnResult,
    onError: OnError
) {

    private var imagePickerBottomSheet: ImagePickerBottomSheet? = null

    init {
        imagePickerBottomSheet = ImagePickerBottomSheet(activity, pickerOptions, onResult, onError)
    }

    fun openImagePicker() {
        imagePickerBottomSheet?.show(
            (activity as AppCompatActivity).supportFragmentManager,
            imagePickerBottomSheet?.tag
        )
    }
}