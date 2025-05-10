package com.example.notesphere.viewmodels

import android.net.Uri
import androidx.activity.compose.ManagedActivityResultLauncher
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.runtime.State
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.notesphere.ui.screens.login.UiState
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

class LoginViewModel : ViewModel() {
    private val _uiState = mutableStateOf(UiState())
    val uiState: State<UiState> = _uiState

    var takePictureLauncher: ManagedActivityResultLauncher<Uri, Boolean>? = null
    var pickImageLauncher: ManagedActivityResultLauncher<PickVisualMediaRequest, Uri?>? = null

    fun updateEmail(email: String) {
        _uiState.value = _uiState.value.copy(
            email = email,
            isEmailValid = isValidEmail(email)
        )
    }

    fun updatePassword(password: String) {
        _uiState.value = _uiState.value.copy(password = password)
    }

    fun updateProfileImageUri(uri: Uri?) {
        if (uri != null && uri.toString().isNotEmpty()) {
            _uiState.value = _uiState.value.copy(profileImageUri = uri.toString())
        } else {
            _uiState.value = _uiState.value.copy(profileImageUri = null)
            showAlert("Invalid image URI")
        }
    }

    fun updateShowBottomSheet(show: Boolean) {
        _uiState.value = _uiState.value.copy(showBottomSheet = show)
    }

    fun onGallerySelected() {
        pickImageLauncher?.launch(PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly))
    }

    fun showAlert(message: String) {
        _uiState.value = _uiState.value.copy(
            showAlert = true,
            alertMessage = message
        )
    }

    fun dismissAlert() {
        _uiState.value = _uiState.value.copy(
            showAlert = false,
            alertMessage = ""
        )
    }

    fun login(onSuccess: () -> Unit) {
        viewModelScope.launch {
            if (validateLogin()) {
                _uiState.value = _uiState.value.copy(isLoading = true)
                delay(1000)
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    showAlert = true,
                    alertMessage = "Login successful!"
                )
                onSuccess()
            }
        }
    }

    private fun validateLogin(): Boolean {
        return when {
            _uiState.value.email.isEmpty() -> {
                _uiState.value = _uiState.value.copy(errorMessage = "Email is required")
                false
            }
            !_uiState.value.isEmailValid -> {
                _uiState.value = _uiState.value.copy(errorMessage = "Invalid email format")
                false
            }
            _uiState.value.password.isEmpty() -> {
                _uiState.value = _uiState.value.copy(errorMessage = "Password is required")
                false
            }
            _uiState.value.password.length < 6 -> {
                _uiState.value = _uiState.value.copy(errorMessage = "Password must be at least 6 characters")
                false
            }
            else -> {
                _uiState.value = _uiState.value.copy(errorMessage = "")
                true
            }
        }
    }

    fun isValidEmail(email: String): Boolean {
        return email.matches(Regex("[a-zA-Z0-9._-]+@[a-zA-Z0-9.-]+\\.[a-zA-Z]{2,}"))
    }
}

