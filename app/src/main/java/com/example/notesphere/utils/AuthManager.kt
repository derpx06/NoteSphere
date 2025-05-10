package com.example.notesphere.utils

import android.content.Context
import androidx.core.content.edit

class AuthManager(context: Context) {
    private val prefs = context.getSharedPreferences("NoteSpherePrefs", Context.MODE_PRIVATE)
    private val KEY_TOKEN = "auth_token"

    fun saveToken(token: String) {
        prefs.edit { putString(KEY_TOKEN, token) }
    }

    fun getToken(): String? {
        return prefs.getString(KEY_TOKEN, null)
    }

    fun clearToken() {
        prefs.edit { remove(KEY_TOKEN) }
    }
}