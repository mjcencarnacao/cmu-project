package com.cmu.project.core

import android.graphics.Bitmap
import java.io.ByteArrayOutputStream

object Utils {
    fun convertBitmapToByteArray(bitmap: Bitmap): ByteArray {
        val stream = ByteArrayOutputStream()
        bitmap.compress(Bitmap.CompressFormat.JPEG, 100, stream)
        return stream.toByteArray()
    }
}