package com.xvantage.rental.utils

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import java.io.File
import java.io.FileOutputStream

object ImageCompressor {

    fun compressImage(
        context: Context,
        uri: Uri
    ): File {

        val inputStream =
            context.contentResolver.openInputStream(uri)
                ?: throw Exception("Unable to open image")

        val bitmap =
            BitmapFactory.decodeStream(inputStream)

        inputStream.close()

        val maxWidth = 1024
        val maxHeight = 1024

        var width = bitmap.width
        var height = bitmap.height

        val ratio = width.toFloat() / height.toFloat()

        if (width > maxWidth || height > maxHeight) {

            if (ratio > 1) {

                width = maxWidth
                height = (width / ratio).toInt()

            } else {

                height = maxHeight
                width = (height * ratio).toInt()

            }
        }

        val resizedBitmap =
            Bitmap.createScaledBitmap(
                bitmap,
                width,
                height,
                true
            )

        val file =
            File(
                context.cacheDir,
                "IMG_${System.currentTimeMillis()}.jpg"
            )

        var quality = 80

        do {

            FileOutputStream(file).use {

                resizedBitmap.compress(
                    Bitmap.CompressFormat.JPEG,
                    quality,
                    it
                )

            }

            quality -= 10

        } while (
            file.length() > 500 * 1024 &&
            quality >= 30
        )

        android.util.Log.e(
            "IMAGE_COMPRESS",
            "Compressed Size = ${file.length() / 1024} KB"
        )

        bitmap.recycle()
        resizedBitmap.recycle()

        return file
    }

}