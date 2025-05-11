package com.example.notesphere.utils

import android.content.Context
import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.example.notesphere.network.RetrofitClient
import com.example.notesphere.viewmodels.LoginViewModel
import com.example.notesphere.viewmodels.NotesViewModel
import com.example.notesphere.viewmodels.RegisterViewModel

class ViewModelFactory(
    private val context: Context
) : ViewModelProvider.Factory {

    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        Log.d("ViewModelFactory", "Creating ViewModel for ${modelClass.simpleName}")
        return when {
            modelClass.isAssignableFrom(LoginViewModel::class.java) -> {
                Log.d("ViewModelFactory", "Creating LoginViewModel")
                val authManager = AuthManager(context)
                val apiService = RetrofitClient.getApiService(authManager)
                LoginViewModel(apiService, authManager) as T
            }
            modelClass.isAssignableFrom(RegisterViewModel::class.java) -> {
                Log.d("ViewModelFactory", "Creating RegisterViewModel")
                RegisterViewModel(RetrofitClient.publicApiService) as T
            }
            modelClass.isAssignableFrom(NotesViewModel::class.java) -> {
                Log.d("ViewModelFactory", "Creating NotesViewModel")
                NotesViewModel(context) as T
            }
            else -> throw IllegalArgumentException("Unknown ViewModel class: ${modelClass.name}")
        }
    }
}