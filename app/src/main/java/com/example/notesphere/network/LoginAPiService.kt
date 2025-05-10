package com.example.notesphere.network

import com.example.notesphere.data.LoginRequest
import com.example.notesphere.data.LoginResponse
import com.example.notesphere.data.ProfilePhotoResponse
import com.example.notesphere.data.RegisterRequest
import com.example.notesphere.data.RegisterResponse
import okhttp3.MultipartBody
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.Header
import retrofit2.http.Multipart
import retrofit2.http.POST
import retrofit2.http.Part

interface ApiService {
    @POST("/api/users/register")
    suspend fun register(@Body request: RegisterRequest): Response<RegisterResponse>

    @POST("/api/users/login")
    suspend fun login(@Body request: LoginRequest): Response<LoginResponse>

    @Multipart
    @POST("/api/users/upload-profile-photo")
    suspend fun uploadProfilePhoto(
        @Header("Authorization") token: String,
        @Part photo: MultipartBody.Part
    ): Response<ProfilePhotoResponse>
}