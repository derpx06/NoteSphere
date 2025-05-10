package com.example.notesphere.viewmodels

import android.content.Context
import android.net.Uri
import androidx.activity.compose.ManagedActivityResultLauncher
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.runtime.State
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.notesphere.data.LoginRequest
import com.example.notesphere.network.ApiService
import com.example.notesphere.utils.AuthManager
import com.example.notesphere.utils.uriToMultipart
import kotlinx.coroutines.launch

data class UiState(
    val email: String = "",
    val password: String = "",
    val profileImageUri: String? = null,
    val isEmailValid: Boolean = true,
    val errorMessage: String = "",
    val alertMessage: String = "",
    val showAlert: Boolean = false,
    val showBottomSheet: Boolean = false,
    val isLoading: Boolean = false
)

class LoginViewModel(
    private val apiService: ApiService,
    private val authManager: AuthManager
) : ViewModel() {
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

    fun login(context: Context, onSuccess: () -> Unit) {
        viewModelScope.launch {
            if (!validateLogin()) return@launch
            _uiState.value = _uiState.value.copy(isLoading = true)
            try {
                val response = apiService.login(
                    LoginRequest(
                        email = _uiState.value.email,
                        password = _uiState.value.password
                    )
                )
                if (response.isSuccessful && response.body()?.success == true) {
                    val token = response.body()?.token
                    if (token != null) {
                        authManager.saveToken(token)
                        _uiState.value.profileImageUri?.let { uriString ->
                            uploadProfilePhoto(context, Uri.parse(uriString))
                        }
                        _uiState.value = _uiState.value.copy(
                            isLoading = false,
                            showAlert = true,
                            alertMessage = "Login successful!"
                        )
                        onSuccess()
                    } else {
                        _uiState.value = _uiState.value.copy(
                            isLoading = false,
                            errorMessage = "Login failed: No token received"
                        )
                    }
                } else {
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        errorMessage = response.body()?.message ?: "Login failed"
                    )
                }
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    errorMessage = "Network error: ${e.message}"
                )
            }
        }
    }

    fun uploadProfilePhoto(context: Context, uri: Uri) {
        viewModelScope.launch {
            val token = authManager.getToken() ?: return@launch showAlert("No token available")
            try {
                val multipart = uriToMultipart(context, uri)
                val response = apiService.uploadProfilePhoto("Bearer $token", multipart)
                if (response.isSuccessful && response.body()?.success == true) {
                    showAlert("Profile photo uploaded successfully")
                } else {
                    showAlert(response.body()?.message ?: "Failed to upload profile photo")
                }
            } catch (e: Exception) {
                showAlert("Upload error: ${e.message}")
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

    private fun isValidEmail(email: String): Boolean {
        return email.matches(Regex("[a-zA-Z0-9._-]+@[a-zA-Z0-9.-]+\\.[a-zA-Z]{2,}"))
    }
}