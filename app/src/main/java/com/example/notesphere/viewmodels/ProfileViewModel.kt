package com.example.notesphere.viewmodels

import android.app.Application
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.notesphere.data.ProfileResponse
import com.example.notesphere.network.ApiService
import com.example.notesphere.network.RetrofitClient
import com.example.notesphere.utils.AuthManager
import kotlinx.coroutines.launch

class ProfileViewModel(application: Application) : AndroidViewModel(application) {
    private val context = application.applicationContext
    private val apiService: ApiService = RetrofitClient.getApiService(AuthManager(context))

    var userProfile: com.example.notesphere.data.User? by mutableStateOf(null)
        private set
    var isLoading by mutableStateOf(false)
        private set
    var error by mutableStateOf<String?>(null)
        internal set

    fun loadUserProfile(userId: String) {
        viewModelScope.launch {
            isLoading = true
            error = null
            try {
                val response = apiService.getProfile(userId)
                if (response.isSuccessful) {
                    response.body()?.let {
                        if (it.success) {
                            userProfile = it.user
                        } else {
                            error = it.message ?: "Failed to load profile"
                        }
                    } ?: run {
                        error = "Empty response from server"
                    }
                } else {
                    error = "Server error: ${response.code()} - ${response.message()}"
                }
            } catch (e: Exception) {
                error = "Network error: ${e.message}"
            } finally {
                isLoading = false
            }
        }
    }

    fun clearProfile() {
        userProfile = null
        error = null
    }
}