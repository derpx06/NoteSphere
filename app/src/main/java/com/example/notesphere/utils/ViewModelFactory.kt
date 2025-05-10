package com.example.notesphere.utils

import androidx.compose.runtime.Composable
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.notesphere.network.RetrofitClient
import com.example.notesphere.viewmodels.LoginViewModel

class ViewModelFactory(
    private val authManager: AuthManager
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(LoginViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return LoginViewModel(
                apiService = RetrofitClient.apiService,
                authManager = authManager
            ) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class: ${modelClass.name}")
    }
}

@Composable
inline fun <reified VM : ViewModel> viewModelFactory(
    factory: ViewModelProvider.Factory
): VM {
    return viewModel(factory = factory)
}