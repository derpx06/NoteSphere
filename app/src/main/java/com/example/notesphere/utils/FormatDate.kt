package com.example.notesphere.utils

import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.TimeZone

fun formatDate(isoDate: String): String {
    return try {
        val parser = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", Locale.US).apply {
            timeZone = TimeZone.getTimeZone("UTC")
        }
        val date = parser.parse(isoDate) ?: return "Unknown Date"
        val formatter = SimpleDateFormat("MMM dd, yyyy", Locale.US)
        formatter.format(date)
    } catch (e: Exception) {
        "Unknown Date"
    }
}