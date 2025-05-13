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
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody
import okhttp3.RequestBody.Companion.asRequestBody
import java.io.File
import java.io.FileOutputStream
import java.net.HttpURLConnection
import java.net.URL

class NotesViewModel(private val context: Context) : ViewModel() {
    private val _notes = mutableStateOf<List<Note>>(emptyList())
    val notes: State<List<Note>> = _notes

    private val _noteDetails = mutableStateOf<Note?>(null)
    val noteDetails: State<Note?> = _noteDetails

    private val _downloadUrls = mutableStateOf<List<String>>(emptyList())
    val downloadUrls: State<List<String>> = _downloadUrls

    private val _viewUrls = mutableStateOf<List<String>>(emptyList())
    val viewUrls: State<List<String>> = _viewUrls

    private val _isLoading = mutableStateOf(false)
    val isLoading: State<Boolean> = _isLoading

    private val _errorMessage = mutableStateOf("")
    val errorMessage: State<String> = _errorMessage

    private val _userId = mutableStateOf<String?>(null)
    val userId: State<String?> = _userId

    private val apiService: ApiService = RetrofitClient.getApiService(AuthManager(context))
    private val authManager = AuthManager(context)
    private val serverBaseUrl = RetrofitClient.BASE_URL.trimEnd('/')

    init {
        try {
            _userId.value = authManager.getUserId()
            fetchNotes()
        } catch (e: Exception) {
            _errorMessage.value = "Initialization error: ${e.message}"
            Log.e("NotesViewModel", "Init error", e)
        }
    }

    fun refreshUserId() {
        try {
            _userId.value = authManager.getUserId()
            Log.d("NotesViewModel", "Refreshed userId=${_userId.value}")
        } catch (e: Exception) {
            _errorMessage.value = "Failed to refresh user ID: ${e.message}"
            Log.e("NotesViewModel", "RefreshUserId error", e)
        }
    }

    fun isNoteStarred(note: Note): Boolean {
        try {
            val isStarred = _userId.value != null && note.starredBy.contains(_userId.value)
            Log.d("NotesViewModel", "Checking if note ${note.id} is starred by user ${_userId.value}: $isStarred")
            return isStarred
        } catch (e: Exception) {
            Log.e("NotesViewModel", "IsNoteStarred error for note ${note.id}", e)
            return false
        }
    }

    fun fetchNotes() {
        viewModelScope.launch {
            _isLoading.value = true
            _errorMessage.value = ""
            try {
                val token = authManager.getToken()
                val response = apiService.getNotes(token?.let { "Bearer $it" })
                if (response.isSuccessful) {
                    val rawNotes = response.body()?.notes ?: emptyList()
                    // Filter out notes with null title or subject
                    val validNotes = rawNotes.filter { note ->
                        if (note.title.isNotBlank() && note.subject.isNotBlank()) {
                            true
                        } else {
                            Log.w("NotesViewModel", "Skipping invalid note: id=${note.id}, title=${note.title}, subject=${note.subject}")
                            false
                        }
                    }
                    _notes.value = validNotes
                    if (validNotes.size < rawNotes.size) {
                        _errorMessage.value = "Some notes were skipped due to missing title or subject"
                    }
                } else {
                    _errorMessage.value = "Failed to load notes: ${response.message()} (${response.code()})"
                }
            } catch (e: Exception) {
                _errorMessage.value = "Network error: ${e.message}"
                Log.e("NotesViewModel", "FetchNotes error", e)
            } finally {
                _isLoading.value = false
            }
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
                    val note = response.body()?.note
                    if (note != null && note.title.isNotBlank() && note.subject.isNotBlank()) {
                        _noteDetails.value = note
                        _downloadUrls.value = response.body()?.downloadUrls?.map { url ->
                            if (url.startsWith("http")) url else "$serverBaseUrl$url"
                        } ?: emptyList()
                        _viewUrls.value = response.body()?.viewUrls?.map { url ->
                            if (url.startsWith("http")) url else "$serverBaseUrl$url"
                        } ?: emptyList()
                        Log.d("NotesViewModel", "Download URLs for note $noteId: ${_downloadUrls.value}")
                        Log.d("NotesViewModel", "View URLs for note $noteId: ${_viewUrls.value}")
                    } else {
                        _errorMessage.value = "Note has invalid title or subject"
                        _noteDetails.value = null
                        _downloadUrls.value = emptyList()
                        _viewUrls.value = emptyList()
                        Log.w("NotesViewModel", "Invalid note details: id=$noteId, title=${note?.title}, subject=${note?.subject}")
                    }
                } else {
                    _errorMessage.value = "Failed to load note: ${response.message()} (${response.code()})"
                    _noteDetails.value = null
                    _downloadUrls.value = emptyList()
                    _viewUrls.value = emptyList()
                }
            } catch (e: Exception) {
                _errorMessage.value = "Network error: ${e.message}"
                Log.e("NotesViewModel", "FetchNoteById error", e)
                _noteDetails.value = null
                _downloadUrls.value = emptyList()
                _viewUrls.value = emptyList()
            } finally {
                _isLoading.value = false
            }
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
                val descriptionsBody = descriptions.joinToString(",").toRequestBody()
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
                    semester = semesterBody,
                    files = fileParts,
                    token = "Bearer $token"
                )
                if (response.isSuccessful) {
                    _errorMessage.value = "Note uploaded successfully"
                    fetchNotes()
                    onSuccess()
                } else {
                    val errorBody = response.errorBody()?.string()
                    _errorMessage.value = "Upload failed: ${response.message()} (${response.code()}). Error: $errorBody"
                    Log.e("NotesViewModel", "Upload failed: ${response.code()}, ${response.message()}, Error Body: $errorBody")
                }
            } catch (e: Exception) {
                _errorMessage.value = "Upload error: ${e.message}"
                Log.e("NotesViewModel", "UploadNote error", e)
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun starNote(noteId: String) {
        viewModelScope.launch {
            _errorMessage.value = ""
            try {
                val token = authManager.getToken() ?: run {
                    _errorMessage.value = "Please log in to star a note"
                    Log.w("NotesViewModel", "starNote: No token available")
                    return@launch
                }
                Log.d("NotesViewModel", "Attempting to star/unstar note $noteId with token: Bearer $token")
                val response = apiService.starNote(noteId, "Bearer $token")
                if (response.isSuccessful) {
                    Log.d("NotesViewModel", "starNote successful for note $noteId")
                    response.body()?.note?.let { updatedNote ->
                        if (_noteDetails.value?.id == noteId) {
                            _noteDetails.value = updatedNote
                        }
                        _notes.value = _notes.value.map { note ->
                            if (note.id == noteId) updatedNote else note
                        }
                    }
                } else {
                    val errorBody = response.errorBody()?.string()
                    _errorMessage.value = "Failed to star note: ${response.message()} (${response.code()}). Error: $errorBody"
                    Log.e("NotesViewModel", "starNote failed: ${response.code()}, ${response.message()}, Error Body: $errorBody")
                }
            } catch (e: Exception) {
                _errorMessage.value = "Failed to star note: ${e.message}"
                Log.e("NotesViewModel", "StarNote error for note $noteId", e)
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
                    if (_noteDetails.value?.id == noteId) {
                        _noteDetails.value = null
                        _downloadUrls.value = emptyList()
                        _viewUrls.value = emptyList()
                    }
                } else {
                    _errorMessage.value = "Delete failed: ${response.message()} (${response.code()})"
                }
            } catch (e: Exception) {
                _errorMessage.value = "Delete error: ${e.message}"
                Log.e("NotesViewModel", "DeleteNote error", e)
            } finally {
                _isLoading.value = false
            }
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
                    val rawNotes = response.body()?.notes ?: emptyList()
                    val validNotes = rawNotes.filter { note ->
                        if (note.title.isNotBlank() && note.subject.isNotBlank()) {
                            true
                        } else {
                            Log.w("NotesViewModel", "Skipping invalid note in search: id=${note.id}, title=${note.title}, subject=${note.subject}")
                            false
                        }
                    }
                    _notes.value = validNotes
                    if (validNotes.size < rawNotes.size) {
                        _errorMessage.value = "Some search results were skipped due to missing title or subject"
                    }
                } else {
                    _errorMessage.value = "Search failed: ${response.message()} (${response.code()})"
                }
            } catch (e: Exception) {
                _errorMessage.value = "Search error: ${e.message}"
                Log.e("NotesViewModel", "SearchNotes error", e)
            } finally {
                _isLoading.value = false
            }
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
                    val rawNotes = response.body()?.notes ?: emptyList()
                    val validNotes = rawNotes.filter { note ->
                        if (note.title.isNotBlank() && note.subject.isNotBlank()) {
                            true
                        } else {
                            Log.w("NotesViewModel", "Skipping invalid user note: id=${note.id}, title=${note.title}, subject=${note.subject}")
                            false
                        }
                    }
                    _notes.value = validNotes
                    if (validNotes.size < rawNotes.size) {
                        _errorMessage.value = "Some user notes were skipped due to missing title or subject"
                    }
                } else {
                    _errorMessage.value = "Failed to load user notes: ${response.message()} (${response.code()})"
                }
            } catch (e: Exception) {
                _errorMessage.value = "Network error: ${e.message}"
                Log.e("NotesViewModel", "GetUserNotes error", e)
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun downloadNote(url: String, fileName: String) {
        viewModelScope.launch {
            _isLoading.value = true
            _errorMessage.value = ""
            try {
                val token = authManager.getToken() ?: run {
                    _errorMessage.value = "Please log in to download the note"
                    _isLoading.value = false
                    return@launch
                }
                withContext(Dispatchers.IO) {
                    val file = File(context.getExternalFilesDir(null), fileName)
                    if (file.exists()) {
                        _errorMessage.value = "File already exists at ${file.absolutePath}"
                        return@withContext
                    }
                    val connection = URL(url).openConnection() as HttpURLConnection
                    connection.setRequestProperty("Authorization", "Bearer $token")
                    connection.connect()
                    if (connection.responseCode == HttpURLConnection.HTTP_OK) {
                        connection.inputStream.use { input ->
                            FileOutputStream(file).use { output ->
                                input.copyTo(output)
                            }
                        }
                        _errorMessage.value = "File downloaded to ${file.absolutePath}"
                    } else {
                        _errorMessage.value = "Download failed: ${connection.responseMessage} (${connection.responseCode})"
                    }
                    connection.disconnect()
                }
            } catch (e: Exception) {
                _errorMessage.value = "Download error: ${e.message}"
                Log.e("NotesViewModel", "DownloadNote error", e)
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun viewNote(url: String) {
        viewModelScope.launch {
            _errorMessage.value = ""
            try {
                Log.d("NotesViewModel", "Viewing note at: $url")
                val intent = android.content.Intent(android.content.Intent.ACTION_VIEW).apply {
                    setData(android.net.Uri.parse(url))
                    flags = android.content.Intent.FLAG_ACTIVITY_NEW_TASK
                }
                context.startActivity(intent)
            } catch (e: Exception) {
                _errorMessage.value = "View error: ${e.message}"
                Log.e("NotesViewModel", "ViewNote error", e)
            }
        }
    }
}