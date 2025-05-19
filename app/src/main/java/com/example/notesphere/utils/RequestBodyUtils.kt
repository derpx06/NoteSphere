package com.example.notesphere.utils

import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.RequestBody
import okhttp3.RequestBody.Companion.toRequestBody

fun String.toRequestBody(): RequestBody {
    return toRequestBody("text/plain".toMediaTypeOrNull())
}

fun Int.toRequestBody(): RequestBody {
    return toString().toRequestBody("text/plain".toMediaTypeOrNull())
}
