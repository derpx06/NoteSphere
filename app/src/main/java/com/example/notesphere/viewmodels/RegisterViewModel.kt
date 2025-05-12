package com.example.notesphere.viewmodels

import androidx.compose.runtime.State
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.notesphere.data.RegisterRequest
import com.example.notesphere.network.ApiService
import com.example.notesphere.network.RetrofitClient
import kotlinx.coroutines.launch

data class User(
    val username: String = "",
    val email: String = "",
    val password: String = "",
    val role: String = "",
    val college: String = "",
    val profilePhotoUri: String? = null,
    val semester: Int? = null
)

class RegisterViewModel(
    private val apiService: ApiService = RetrofitClient.publicApiService
) : ViewModel() {
    private val _user = mutableStateOf(User())
    val user: State<User> = _user

    private val _errorMessage = mutableStateOf("")
    val errorMessage: State<String> = _errorMessage

    private val _isLoading = mutableStateOf(false)
    val isLoading: State<Boolean> = _isLoading

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
        _user.value = _user.value.copy(role = role.toLowerCase())
    }

    fun updateCollege(college: String) {
        _user.value = _user.value.copy(college = college)
    }

    fun updateProfilePhotoUri(uri: String?) {
        _user.value = _user.value.copy(profilePhotoUri = uri)
    }

    fun validateRegistration(confirmPassword: String, semester: Int): Boolean {
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
            semester < 1 || semester > 8 -> {
                _errorMessage.value = "Semester must be between 1 and 8"
                false
            }
            else -> {
                _errorMessage.value = ""
                true
            }
        }
    }

    fun register(semester: Int, onSuccess: () -> Unit) {
        viewModelScope.launch {
            if (!validateRegistration(_user.value.password, semester)) return@launch
            _isLoading.value = true
            try {
                val response = apiService.register(
                    RegisterRequest(
                        username = _user.value.username,
                        email = _user.value.email,
                        password = _user.value.password,
                        role = _user.value.role,
                        college = _user.value.college,
                       // profilePhotoUri = _user.value.profilePhotoUri,
                        //semester = semester
                    )
                )
                if (response.isSuccessful && response.body()?.success == true) {
                    _errorMessage.value = ""
                    _isLoading.value = false
                    onSuccess()
                } else {
                    _errorMessage.value = response.body()?.message ?: "Registration failed"
                    _isLoading.value = false
                }
            } catch (e: Exception) {
                _errorMessage.value = "Network error: ${e.message}"
                _isLoading.value = false
            }
        }
    }

    private fun isValidEmail(email: String): Boolean {
        return email.matches(Regex("[a-zA-Z0-9._-]+@[a-zA-Z0-9.-]+\\.[a-zA-Z]{2,}"))
    }
}