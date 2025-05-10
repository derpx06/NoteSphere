package com.example.notesphere.viewmodels

import androidx.compose.runtime.State
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.launch

data class User(
    val username: String = "",
    val email: String = "",
    val password: String = "",
    val role: String = "",
    val college: String = "",
    val profilePhotoUri: String? = null
)

class RegisterViewModel : ViewModel() {
    private val _user = mutableStateOf(User())
    val user: State<User> = _user

    private val _errorMessage = mutableStateOf("")
    val errorMessage: State<String> = _errorMessage

    fun updateUsername(username: String) {
        _user.value = _user.value.copy(username = username)
    }

    fun updateEmail(email: String) {
        _user.value = _user.value.copy(email = email)
    }

    fun updatePassword(password: String) {
        _user.value = _user.value.copy(password = password)
    }

    fun updateRole(role: String) {
        _user.value = _user.value.copy(role = role)
    }

    fun updateCollege(college: String) {
        _user.value = _user.value.copy(college = college)
    }

    fun updateProfilePhotoUri(uri: String?) {
        _user.value = _user.value.copy(profilePhotoUri = uri)
    }

    fun validateRegistration(confirmPassword: String): Boolean {
        return when {
            _user.value.username.isEmpty() -> {
                _errorMessage.value = "Username is required"
                false
            }
            _user.value.username.length < 3 -> {
                _errorMessage.value = "Username must be at least 3 characters"
                false
            }
            _user.value.email.isEmpty() -> {
                _errorMessage.value = "Email is required"
                false
            }
            !isValidEmail(_user.value.email) -> {
                _errorMessage.value = "Invalid email format"
                false
            }
            _user.value.password.isEmpty() -> {
                _errorMessage.value = "Password is required"
                false
            }
            _user.value.password.length < 6 -> {
                _errorMessage.value = "Password must be at least 6 characters"
                false
            }
            _user.value.password != confirmPassword -> {
                _errorMessage.value = "Passwords do not match"
                false
            }
            _user.value.role.isEmpty() -> {
                _errorMessage.value = "Role is required"
                false
            }
            _user.value.college.isEmpty() -> {
                _errorMessage.value = "College/University is required"
                false
            }
            _user.value.college.length < 3 -> {
                _errorMessage.value = "College name must be at least 3 characters"
                false
            }
            else -> {
                _errorMessage.value = ""
                true
            }
        }
    }

    fun register(onSuccess: () -> Unit) {
        viewModelScope.launch {
            // Simulate registration (no DB)
            onSuccess()
        }
    }

    private fun isValidEmail(email: String): Boolean {
        return email.matches(Regex("[a-zA-Z0-9._-]+@[a-zA-Z0-9.-]+\\.[a-zA-Z]{2,}"))
    }
}