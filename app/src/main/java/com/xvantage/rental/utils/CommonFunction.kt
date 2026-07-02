package com.xvantage.rental.utils

import android.app.Activity
import android.app.AlertDialog
import android.app.DatePickerDialog
import android.app.Dialog
import android.content.Context
import android.content.Intent
import android.widget.Toast
import android.net.Uri
import android.provider.OpenableColumns
import android.text.format.Formatter
import com.xvantage.rental.utils.ImageCompressor
import android.widget.Button
import android.widget.EditText
import android.widget.LinearLayout
import android.widget.RatingBar
import com.xvantage.rental.R
import java.text.SimpleDateFormat
import java.util.Calendar
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.asRequestBody
import java.util.Locale

class CommonFunction {

    fun navigation(context: Context, activity: Class<out Activity>) {
        val intent = Intent(context, activity)
        context.startActivity(intent)
    }
    fun toast(context: Context, message:String) {
        Toast.makeText(context, "$message", Toast.LENGTH_SHORT).show()
    }

    fun getFileName(context: Context, uri: Uri): String {
        var fileName = "Unknown"
        val cursor = context.contentResolver.query(uri, null, null, null, null)
        cursor?.use {
            if (it.moveToFirst()) {
                fileName = it.getString(it.getColumnIndexOrThrow(OpenableColumns.DISPLAY_NAME))
            }
        }
        return fileName
    }

    fun getFileSize(context: Context, uri: Uri): String {
        var fileSize = "Unknown"
        val cursor = context.contentResolver.query(uri, null, null, null, null)
        cursor?.use {
            if (it.moveToFirst()) {
                val sizeIndex = it.getColumnIndex(OpenableColumns.SIZE)
                if (sizeIndex != -1) {
                    val size = it.getLong(sizeIndex)
                    fileSize = Formatter.formatFileSize(context, size)
                }
            }
        }
        return fileSize
    }

    fun showDatePickerDialog(
        context: Context,
        initialDate: Calendar = Calendar.getInstance(),
        dateFormat: String = "dd MMM, yyyy",
        onDateSelected: (String) -> Unit
    ) {
        val dateListener = DatePickerDialog.OnDateSetListener { _, year, monthOfYear, dayOfMonth ->
            // Update the calendar with the selected date
            initialDate[Calendar.YEAR] = year
            initialDate[Calendar.MONTH] = monthOfYear
            initialDate[Calendar.DAY_OF_MONTH] = dayOfMonth

            // Format the date and return it through the callback
            val sdf = SimpleDateFormat(dateFormat, Locale.US)
            onDateSelected(sdf.format(initialDate.time))
        }

        // Create and show the DatePickerDialog
        val datePickerDialog = DatePickerDialog(
            context,
            AlertDialog.THEME_HOLO_LIGHT, // Change theme as needed
            dateListener,
            initialDate.get(Calendar.YEAR),
            initialDate.get(Calendar.MONTH),
            initialDate.get(Calendar.DAY_OF_MONTH)
        )
        datePickerDialog.show()
    }

    fun showRatingDialog(context: Context) {
        val dialog = Dialog(context)
        dialog.setContentView(R.layout.dialog_rating)

        val window = dialog.window
        val layoutParams = window?.attributes
        layoutParams?.width = LinearLayout.LayoutParams.MATCH_PARENT
        layoutParams?.height = LinearLayout.LayoutParams.WRAP_CONTENT
        window?.attributes = layoutParams

        val ratingBar = dialog.findViewById<RatingBar>(R.id.ratingBar)
        val reviewText = dialog.findViewById<EditText>(R.id.etReview)
        val cancelButton = dialog.findViewById<Button>(R.id.btn_cancel)
        val submitButton = dialog.findViewById<Button>(R.id.btn_submit)

        cancelButton.setOnClickListener {
            dialog.dismiss()
        }

        submitButton.setOnClickListener {
            val rating = ratingBar.rating
            val review = reviewText.text.toString()

            if (rating >= 4) {
//                val intent = Intent(
//                    Intent.ACTION_VIEW,
//                    Uri.parse("https://play.google.com/store/apps/details?id=com.whatsapp")
//                )
//                context.startActivity(intent)
                CommonFunction().toast(context, "Thanks for your feedback!")
            } else {
                CommonFunction().toast(context, "Thanks for your feedback!")
            }
            dialog.dismiss()
        }

        dialog.show()
    }

    fun getMultipartFromUri(
        context: Context,
        uri: Uri?,
        partName: String
    ): MultipartBody.Part? {

        if (uri == null) return null

        val file =
            ImageCompressor.compressImage(
                context,
                uri
            )

        val mimeType = when (file.extension.lowercase()) {

            "jpg", "jpeg" -> "image/jpeg"

            "png" -> "image/png"

            else -> "image/jpeg"
        }

        val requestBody = file.asRequestBody(
            mimeType.toMediaTypeOrNull()
        )

        android.util.Log.e("UPLOAD", "File Name = ${file.name}")
        android.util.Log.e("UPLOAD", "Extension = ${file.extension}")
        android.util.Log.e("UPLOAD", "Mime = $mimeType")

        return MultipartBody.Part.createFormData(
            partName,
            file.name,
            requestBody
        )
    }

    fun getMultipartListFromUris(
        context: Context,
        uris: List<Uri>,
        partName: String
    ): List<MultipartBody.Part> {

        val parts = mutableListOf<MultipartBody.Part>()

        uris.forEach { uri ->

            val part = getMultipartFromUri(
                context,
                uri,
                partName
            )

            part?.let {

                parts.add(it)

            }
        }

        return parts
    }

}