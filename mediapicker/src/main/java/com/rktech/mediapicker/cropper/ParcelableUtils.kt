package com.rktech.mediapicker.cropper

import android.content.Intent
import android.os.Bundle
import android.os.Parcelable
import java.io.Serializable

inline fun <reified T : Parcelable> Bundle.parcelable(key: String): T? = when {
    // Does not work yet, https://issuetracker.google.com/issues/240585930
    //SDK_INT >= 33 -> getParcelable(key, T::class.java)
    else -> @Suppress("DEPRECATION") getParcelable(key) as? T
}

inline fun <reified T : Parcelable> Intent.parcelable(key: String): T? = when {
    // Does not work yet, https://issuetracker.google.com/issues/240585930
    // SDK_INT >= 33 -> getParcelableExtra(key, T::class.java)
    else -> @Suppress("DEPRECATION") getParcelableExtra(key) as? T
}

inline fun <reified T : java.io.Serializable> Bundle.serializable(key: String): T? = when {
    // Does not work yet, https://issuetracker.google.com/issues/240585930
    //SDK_INT >= 33 -> getSerializable(key, T::class.java) as? T
    else -> @Suppress("DEPRECATION") getSerializable(key) as? T
}

inline fun <reified T : Serializable> Intent.serializable(key: String): T? = when {
    // Does not work yet, https://issuetracker.google.com/issues/240585930
    //SDK_INT >= 33 -> getSerializableExtra(key, T::class.java) as? T
    else -> @Suppress("DEPRECATION") getSerializableExtra(key) as? T
}
