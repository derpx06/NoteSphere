package com.example.notesphere.models

data class User(
    val email: String = "",
    val username: String = "",
    val password: String = "",
    val role: String = "",
    val college: String = "",
    val profilePhotoUri: String? = null
)