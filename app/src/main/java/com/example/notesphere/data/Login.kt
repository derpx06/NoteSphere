package com.example.notesphere.data

data class RegisterRequest(
    val username: String,
    val email: String,
    val password: String,
    val role: String,
    val college: String
)

data class RegisterResponse(
    val success: Boolean,
    val message: String
)

data class LoginRequest(
    val email: String,
    val password: String
)

data class LoginResponse(
    val success: Boolean,
    val token: String?,
    val user: User?,
    val message: String?
)

data class User(
    val id: String,
    val username: String,
    val email: String,
    val role: String,
    val college: String
)

data class ProfilePhotoResponse(
    val success: Boolean,
    val message: String,
    val profilePhotoPath: String?
)