package com.example.notesphere.data

import com.google.gson.annotations.SerializedName

data class NotesResponse(
    val success: Boolean,
    val notes: List<Note>
)

data class Note(
    @SerializedName("_id") val id: String,
    val title: String,
    val subject: String,
    val topics: List<String>,
    val filePath: List<FilePath>,
    val user: User,
    val stars: Int,
    val starredBy: List<User>,
    val createdAt: String,
    val updatedAt: String
)

data class FilePath(
    val path: String,
    val description: String
)

data class User(
    @SerializedName("_id") val id: String,
    val username: String,
    val email: String,
    val college: String,
    val role: String? = null // Optional to handle cases where role is absent
)

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

data class ProfilePhotoResponse(
    val success: Boolean,
    val message: String,
    val profilePhotoPath: String?
)
data class NoteDetailsResponse(
    val success: Boolean,
    val note: Note,
    val downloadUrls: List<String>
)