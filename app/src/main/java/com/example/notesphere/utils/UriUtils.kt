package com.example.notesphere.utils

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.toRequestBody
import java.io.ByteArrayOutputStream

fun uriToMultipart(context: Context, uri: Uri): MultipartBody.Part {
    val bitmap = BitmapFactory.decodeStream(context.contentResolver.openInputStream(uri))
    val outputStream = ByteArrayOutputStream()
    bitmap.compress(Bitmap.CompressFormat.JPEG, 20, outputStream) // Adjusted to 50% quality

    return MultipartBody.Part.createFormData(
        "profilePhoto",
        "compressed.jpg",
        outputStream.toByteArray().toRequestBody("image/jpeg".toMediaTypeOrNull())
    )
}