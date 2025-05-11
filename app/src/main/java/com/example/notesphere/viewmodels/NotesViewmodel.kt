package com.example.notesphere.viewmodels

import android.content.Context
import androidx.compose.runtime.State
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.notesphere.data.Note
import com.example.notesphere.network.RetrofitClient
import com.example.notesphere.utils.AuthManager
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.asRequestBody
import okhttp3.RequestBody.Companion.toRequestBody
import java.io.File
import java.net.SocketTimeoutException
import java.net.UnknownHostException

class NotesViewModel(val context: Context) : ViewModel() {
    private val apiService = RetrofitClient.getApiService(AuthManager(context))
    private val _notes = mutableStateOf<List<Note>>(emptyList())
    val notes: State<List<Note>> = _notes
    private val _isLoading = mutableStateOf(false)
    val isLoading: State<Boolean> = _isLoading
    private val _errorMessage = mutableStateOf("")
    val errorMessage: State<String> = _errorMessage

    private val _noteDetails = mutableStateOf<Note?>(null)
    private val _downloadUrls = mutableStateOf<List<String>>(emptyList())
    val noteDetails: State<Note?> = _noteDetails
    val downloadUrls: State<List<String>> = _downloadUrls

    init {
        fetchNotes()
    }

    fun uploadNote(
        title: String,
        subject: String,
        topics: String,
        descriptions: String,
        filePaths: List<String>,
        onSuccess: () -> Unit
    ) {
        viewModelScope.launch {
            _isLoading.value = true
            _errorMessage.value = ""
            var attempt = 0
            val maxAttempts = 3

            while (attempt < maxAttempts) {
                try {
                    val token = AuthManager(context).getToken() ?: run {
                        _errorMessage.value = "Please log in to upload notes"
                        _isLoading.value = false
                        return@launch
                    }

                    // Validate file sizes (50 MB)
                    filePaths.forEach { path ->
                        val file = File(path)
                        if (file.length() > 50 * 1024 * 1024) {
                            _errorMessage.value = "File ${file.name} exceeds 50 MB limit"
                            _isLoading.value = false
                            return@launch
                        }
                    }

                    // Prepare request bodies
                    val titleBody = title.toRequestBody("text/plain".toMediaTypeOrNull())
                    val subjectBody = subject.toRequestBody("text/plain".toMediaTypeOrNull())
                    val topicsBody = topics.toRequestBody("text/plain".toMediaTypeOrNull())
                    val descriptionsBody = descriptions.toRequestBody("text/plain".toMediaTypeOrNull())

                    // Prepare file parts
                    val fileParts = filePaths.map { path ->
                        val file = File(path)
                        val requestFile = file.asRequestBody("application/pdf".toMediaTypeOrNull())
                        MultipartBody.Part.createFormData("files", file.name, requestFile)
                    }

                    val response = apiService.uploadNote(
                        title = titleBody,
                        subject = subjectBody,
                        topics = topicsBody,
                        descriptions = descriptionsBody,
                        files = fileParts,
                        token = "Bearer $token"
                    )

                    if (response.isSuccessful) {
                        response.body()?.let {
                            _errorMessage.value = "Note uploaded successfully"
                            fetchNotes() // Refresh notes list
                            onSuccess()
                        } ?: run {
                            _errorMessage.value = "Upload failed: No response data"
                        }
                        _isLoading.value = false
                        return@launch
                    } else {
                        val errorBody = response.errorBody()?.string()
                        _errorMessage.value = when (response.code()) {
                            401 -> "Unauthorized: Please log in again"
                            400 -> errorBody?.let { parseErrorMessage(it) } ?: "Invalid input data"
                            500 -> "Server error, please try again later"
                            else -> "Upload failed: ${response.message()}"
                        }
                        if (response.code() == 401 || response.code() == 400) {
                            _isLoading.value = false
                            return@launch
                        }
                    }
                } catch (e: Exception) {
                    when (e) {
                        is SocketTimeoutException, is UnknownHostException -> {
                            _errorMessage.value = "Network issue, retrying... (${attempt + 1}/$maxAttempts)"
                            if (attempt < maxAttempts - 1) {
                                delay(1000L * (1 shl attempt)) // Exponential backoff
                            }
                        }
                        else -> {
                            _errorMessage.value = "Error: ${e.message ?: "Unknown error"}"
                            _isLoading.value = false
                            return@launch
                        }
                    }
                }
                attempt++
            }
            _errorMessage.value = "Failed to upload note after $maxAttempts attempts"
            _isLoading.value = false
        }
    }

    fun fetchNoteById(noteId: String) {
        viewModelScope.launch {
            _isLoading.value = true
            try {
                val response = apiService.getNoteById(noteId)
                if (response.isSuccessful) {
                    response.body()?.let { detailsResponse ->
                        _noteDetails.value = detailsResponse.note
                        _downloadUrls.value = detailsResponse.downloadUrls
                        _errorMessage.value = ""
                    } ?: run {
                        _errorMessage.value = "Note not found"
                    }
                } else {
                    _errorMessage.value = when (response.code()) {
                        401 -> "Unauthorized: Please log in again"
                        404 -> "Note not found"
                        else -> "Error: ${response.message()}"
                    }
                }
            } catch (e: Exception) {
                _errorMessage.value = "Network error: ${e.message}"
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun fetchNotes() {
        viewModelScope.launch {
            _isLoading.value = true
            try {
                val response = apiService.getNotes()
                if (response.isSuccessful) {
                    response.body()?.let {
                        _notes.value = it.notes
                        _errorMessage.value = ""
                    } ?: run {
                        _errorMessage.value = "No notes received"
                    }
                } else {
                    _errorMessage.value = when (response.code()) {
                        401 -> "Unauthorized: Please log in again"
                        else -> "Failed to fetch notes: ${response.message()}"
                    }
                }
            } catch (e: Exception) {
                _errorMessage.value = "Network error: ${e.message}"
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun starNote(noteId: String) {
        viewModelScope.launch {
            try {
                val token = AuthManager(context).getToken() ?: run {
                    _errorMessage.value = "Please log in to star notes"
                    return@launch
                }
                val response = apiService.starNote(noteId, "Bearer $token")
                if (response.isSuccessful) {
                    fetchNoteById(noteId) // Refresh the note details
                } else {
                    _errorMessage.value = when (response.code()) {
                        401 -> "Unauthorized: Please log in again"
                        else -> "Failed to star note: ${response.message()}"
                    }
                }
            } catch (e: Exception) {
                _errorMessage.value = "Error: ${e.message}"
            }
        }
    }

    private fun parseErrorMessage(errorBody: String): String {
        // Basic JSON parsing for error messages (adjust based on server response format)
        return try {
            val json = errorBody.replace(Regex("[{}\"]"), "")
            val message = json.split(",").find { it.contains("message") }?.split(":")?.lastOrNull()
            message ?: "Invalid input data"
        } catch (e: Exception) {
            "Invalid input data"
        }
    }
}