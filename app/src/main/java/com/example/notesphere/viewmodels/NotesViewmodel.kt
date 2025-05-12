package com.example.notesphere.viewmodels

import android.content.Context
import android.util.Log
import androidx.compose.runtime.State
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.notesphere.data.Note
import com.example.notesphere.network.ApiService
import com.example.notesphere.network.RetrofitClient
import com.example.notesphere.utils.AuthManager
import com.example.notesphere.utils.toRequestBody
import kotlinx.coroutines.launch
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody
import okhttp3.RequestBody.Companion.asRequestBody
import java.io.File

class NotesViewModel(private val context: Context) : ViewModel() {
    private val _notes = mutableStateOf<List<Note>>(emptyList())
    val notes: State<List<Note>> = _notes

    private val _noteDetails = mutableStateOf<Note?>(null)
    val noteDetails: State<Note?> = _noteDetails

    private val _downloadUrls = mutableStateOf<List<String>>(emptyList())
    val downloadUrls: State<List<String>> = _downloadUrls

    private val _isLoading = mutableStateOf(false)
    val isLoading: State<Boolean> = _isLoading

    private val _errorMessage = mutableStateOf("")
    val errorMessage: State<String> = _errorMessage

    private val apiService: ApiService = RetrofitClient.getApiService(AuthManager(context))
    private val authManager = AuthManager(context)

    init {
        fetchNotes()
    }

    fun fetchNotes() {
        viewModelScope.launch {
            _isLoading.value = true
            _errorMessage.value = ""
            try {
                val token = authManager.getToken()
                val response = apiService.getNotes(token?.let { "Bearer $it" })
                if (response.isSuccessful) {
                    _notes.value = response.body()?.notes ?: emptyList()
                } else {
                    _errorMessage.value = "Failed to load notes: ${response.message()} (${response.code()})"
                }
            } catch (e: Exception) {
                _errorMessage.value = "Network error: ${e.message}"
                Log.e("NotesViewModel", "FetchNotes error", e)
            }
            _isLoading.value = false
        }
    }

    fun fetchNoteById(noteId: String) {
        viewModelScope.launch {
            _isLoading.value = true
            _errorMessage.value = ""
            try {
                val token = authManager.getToken()
                val response = apiService.getNoteById(noteId, token?.let { "Bearer $it" })
                if (response.isSuccessful) {
                    _noteDetails.value = response.body()?.note
                    _downloadUrls.value = response.body()?.downloadUrls ?: emptyList()
                } else {
                    _errorMessage.value = "Failed to load note: ${response.message()} (${response.code()})"
                    _noteDetails.value = null
                    _downloadUrls.value = emptyList()
                }
            } catch (e: Exception) {
                _errorMessage.value = "Network error: ${e.message}"
                Log.e("NotesViewModel", "FetchNoteById error", e)
                _noteDetails.value = null
                _downloadUrls.value = emptyList()
            }
            _isLoading.value = false
        }
    }

    fun uploadNote(
        title: String,
        subject: String,
        topics: String,
        descriptions: List<String>,
        filePaths: List<String>,
        semester: Int,
        onSuccess: () -> Unit
    ) {
        viewModelScope.launch {
            _isLoading.value = true
            _errorMessage.value = ""
            try {
                val token = authManager.getToken() ?: run {
                    _errorMessage.value = "Please log in to upload a note"
                    _isLoading.value = false
                    return@launch
                }
                val titleBody = title.toRequestBody()
                val subjectBody = subject.toRequestBody()
                val topicsBody = topics.toRequestBody()
                val semesterBody = semester.toString().toRequestBody()

                val descriptionParts = descriptions.mapIndexed { index, desc ->
                    MultipartBody.Part.createFormData("descriptions[$index]", desc)
                }
                val fileParts = filePaths.mapIndexed { index, path ->
                    val file = File(path)
                    val requestFile = file.asRequestBody("application/pdf".toMediaTypeOrNull())
                    MultipartBody.Part.createFormData("files[$index]", file.name, requestFile)
                }

                val response = apiService.uploadNote(
                    title = titleBody,
                    subject = subjectBody,
                    topics = topicsBody,
                    descriptions = descriptionParts,
                    semester = semesterBody,
                    files = fileParts,
                    token = "Bearer $token"
                )
                if (response.isSuccessful) {
                    _errorMessage.value = "Note uploaded successfully"
                    fetchNotes()
                    onSuccess()
                } else {
                    _errorMessage.value = "Upload failed: ${response.message()} (${response.code()})"
                }
            } catch (e: Exception) {
                _errorMessage.value = "Upload error: ${e.message}"
                Log.e("NotesViewModel", "UploadNote error", e)
            }
            _isLoading.value = false
        }
    }

    fun starNote(noteId: String) {
        viewModelScope.launch {
            try {
                val token = authManager.getToken() ?: run {
                    _errorMessage.value = "Please log in to star a note"
                    return@launch
                }
                val response = apiService.starNote(noteId, "Bearer $token")
                if (response.isSuccessful) {
                    fetchNotes()
                    // Refresh note details if on NoteDetailsScreen
                    _noteDetails.value?.let { fetchNoteById(noteId) }
                } else {
                    _errorMessage.value = "Failed to star note: ${response.message()} (${response.code()})"
                }
            } catch (e: Exception) {
                _errorMessage.value = "Star error: ${e.message}"
                Log.e("NotesViewModel", "StarNote error", e)
            }
        }
    }

    fun deleteNote(noteId: String) {
        viewModelScope.launch {
            _isLoading.value = true
            _errorMessage.value = ""
            try {
                val token = authManager.getToken() ?: run {
                    _errorMessage.value = "Please log in to delete a note"
                    _isLoading.value = false
                    return@launch
                }
                val response = apiService.deleteNote(noteId, "Bearer $token")
                if (response.isSuccessful) {
                    _errorMessage.value = "Note deleted successfully"
                    fetchNotes()
                    // Clear note details if deleted
                    if (_noteDetails.value?.id == noteId) {
                        _noteDetails.value = null
                        _downloadUrls.value = emptyList()
                    }
                } else {
                    _errorMessage.value = "Delete failed: ${response.message()} (${response.code()})"
                }
            } catch (e: Exception) {
                _errorMessage.value = "Delete error: ${e.message}"
                Log.e("NotesViewModel", "DeleteNote error", e)
            }
            _isLoading.value = false
        }
    }

    fun searchNotes(query: String) {
        viewModelScope.launch {
            _isLoading.value = true
            _errorMessage.value = ""
            try {
                val token = authManager.getToken()
                val response = apiService.searchNotes(query, token?.let { "Bearer $it" })
                if (response.isSuccessful) {
                    _notes.value = response.body()?.notes ?: emptyList()
                } else {
                    _errorMessage.value = "Search failed: ${response.message()} (${response.code()})"
                }
            } catch (e: Exception) {
                _errorMessage.value = "Search error: ${e.message}"
                Log.e("NotesViewModel", "SearchNotes error", e)
            }
            _isLoading.value = false
        }
    }

    fun getUserNotes() {
        viewModelScope.launch {
            _isLoading.value = true
            _errorMessage.value = ""
            try {
                val token = authManager.getToken() ?: run {
                    _errorMessage.value = "Please log in to view your notes"
                    _isLoading.value = false
                    return@launch
                }
                val response = apiService.getUserNotes("Bearer $token")
                if (response.isSuccessful) {
                    _notes.value = response.body()?.notes ?: emptyList()
                } else {
                    _errorMessage.value = "Failed to load user notes: ${response.message()} (${response.code()})"
                }
            } catch (e: Exception) {
                _errorMessage.value = "Network error: ${e.message}"
                Log.e("NotesViewModel", "GetUserNotes error", e)
            }
            _isLoading.value = false
        }
    }
}
