package com.example.notesphere.network

import com.example.notesphere.data.LoginRequest
import com.example.notesphere.data.LoginResponse
import com.example.notesphere.data.NoteDetailsResponse
import com.example.notesphere.data.NotesResponse
import com.example.notesphere.data.ProfilePhotoResponse
import com.example.notesphere.data.ProfileResponse
import com.example.notesphere.data.RegisterRequest
import com.example.notesphere.data.RegisterResponse
import com.example.notesphere.data.UpdateProfileRequest
import okhttp3.MultipartBody
import okhttp3.RequestBody
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.DELETE
import retrofit2.http.GET
import retrofit2.http.Header
import retrofit2.http.Multipart
import retrofit2.http.POST
import retrofit2.http.PUT
import retrofit2.http.Part
import retrofit2.http.Path
import retrofit2.http.Query

interface ApiService {
    @Multipart
    @POST("api/notes")
    suspend fun uploadNote(
        @Part title: RequestBody,
        @Part subject: RequestBody,
        @Part topics: RequestBody,
        @Part descriptions: List<MultipartBody.Part>,
        @Part semester: RequestBody,
        @Part files: List<MultipartBody.Part>,
        @Header("Authorization") token: String
    ): Response<Any>

    @DELETE("api/notes/{id}")
    suspend fun deleteNote(
        @Path("id") noteId: String,
        @Header("Authorization") token: String
    ): Response<Any>

    @GET("api/notes")
    suspend fun getNotes(
        @Header("Authorization") token: String? = null
    ): Response<NotesResponse>

    @GET("api/notes/search")
    suspend fun searchNotes(
        @Query("q") query: String,
        @Header("Authorization") token: String? = null
    ): Response<NotesResponse>

    @GET("api/notes/{id}")
    suspend fun getNoteById(
        @Path("id") noteId: String,
        @Header("Authorization") token: String? = null
    ): Response<NoteDetailsResponse>

    @POST("api/notes/star/{id}")
    suspend fun starNote(
        @Path("id") noteId: String,
        @Header("Authorization") token: String
    ): Response<Any>

    @POST("api/users/register")
    suspend fun register(
        @Body request: RegisterRequest
    ): Response<RegisterResponse>

    @POST("api/users/login")
    suspend fun login(
        @Body request: LoginRequest
    ): Response<LoginResponse>

    @GET("api/users/profile/{id}")
    suspend fun getProfile(
        @Path("id") userId: String,
        @Header("Authorization") token: String? = null
    ): Response<ProfileResponse>

    @PUT("api/users/profile")
    suspend fun updateProfile(
        @Body request: UpdateProfileRequest,
        @Header("Authorization") token: String
    ): Response<ProfileResponse>

    @Multipart
    @POST("api/users/upload-profile-photo")
    suspend fun uploadProfilePhoto(
        @Part photo: MultipartBody.Part,
        @Header("Authorization") token: String
    ): Response<ProfilePhotoResponse>

    @GET("api/users/notes")
    suspend fun getUserNotes(
        @Header("Authorization") token: String
    ): Response<NotesResponse>
}

