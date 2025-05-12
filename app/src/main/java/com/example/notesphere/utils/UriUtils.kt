package com.example.notesphere.utils

import android.content.Context
import android.net.Uri
import android.provider.OpenableColumns
import android.util.Log
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.asRequestBody
import java.io.File
import java.io.FileOutputStream

fun uriToMultipart(context: Context, uri: Uri): MultipartBody.Part {
    // Query the content resolver to get the file name
    val fileName = context.contentResolver.query(uri, null, null, null, null)?.use { cursor ->
        val nameIndex = cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME)
        cursor.moveToFirst()
        cursor.getString(nameIndex)
    } ?: "profile_photo.jpg"

    // Create a temporary file
    val tempFile = File(context.cacheDir, fileName)
    try {
        context.contentResolver.openInputStream(uri)?.use { input ->
            FileOutputStream(tempFile).use { output ->
                input.copyTo(output)
            }
        }
        Log.d("uriToMultipart", "Temporary file created: ${tempFile.absolutePath}, size=${tempFile.length()}")

        // Create RequestBody and MultipartBody.Part
        val requestFile = tempFile.asRequestBody("image/*".toMediaTypeOrNull())
        return MultipartBody.Part.createFormData("photo", fileName, requestFile)
    } catch (e: Exception) {
        Log.e("uriToMultipart", "Failed to convert URI to Multipart: $uri", e)
        throw e
    } finally {
        // Optionally delete temp file after upload (handled by ViewModel or cleanup)
    }
}
