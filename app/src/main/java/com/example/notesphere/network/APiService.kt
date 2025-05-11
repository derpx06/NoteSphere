package com.example.notesphere.network

import com.example.notesphere.data.LoginRequest
import com.example.notesphere.data.LoginResponse
import com.example.notesphere.data.Note
import com.example.notesphere.data.NoteDetailsResponse
import com.example.notesphere.data.NotesResponse
import com.example.notesphere.data.RegisterRequest
import com.example.notesphere.data.RegisterResponse
import okhttp3.MultipartBody
import okhttp3.RequestBody
import retrofit2.Response
import retrofit2.http.*

interface ApiService {
    @POST("api/users/register")
    suspend fun register(@Body request: RegisterRequest): Response<RegisterResponse>

    @POST("api/users/login")
    suspend fun login(@Body request: LoginRequest): Response<LoginResponse>

    @GET("api/notes")
    suspend fun getNotes(): Response<NotesResponse>

    @GET("api/notes/{id}")
    suspend fun getNoteById(@Path("id") id: String): Response<NoteDetailsResponse>

    @POST("api/notes/star/{id}")
    suspend fun starNote(
        @Path("id") id: String,
        @Header("Authorization") token: String
    ): Response<Unit>

    @Multipart
    @POST("api/notes")
    suspend fun uploadNote(
        @Part("title") title: RequestBody,
        @Part("subject") subject: RequestBody,
        @Part("topics") topics: RequestBody,
        @Part("descriptions") descriptions: RequestBody,
        @Part files: List<MultipartBody.Part>,
        @Header("Authorization") token: String
    ): Response<Note>

    @Multipart
    @POST("upload-profile-photo")
    suspend fun uploadProfilePhoto(
        @Header("Authorization") token: String,
        @Part photo: MultipartBody.Part
    ): Response<LoginResponse>
}