// com/example/notesphere/viewmodels/LoginUiState.kt
package com.example.notesphere.viewmodels

data class LoginUiState(
    val email: String = "",
    val password: String = "",
    val isEmailValid: Boolean = true,
    val errorMessage: String = "",
    val isLoading: Boolean = false,
    val showAlert: Boolean = false,
    val alertMessage: String = "",
    val profileImageUri: String? = null,
    val showBottomSheet: Boolean = false
)