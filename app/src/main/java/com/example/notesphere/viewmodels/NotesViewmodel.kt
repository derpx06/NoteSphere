package com.example.notesphere.viewmodels

import android.content.Context
import androidx.compose.runtime.State
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.notesphere.data.Note
import com.example.notesphere.network.RetrofitClient
import com.example.notesphere.utils.AuthManager
import kotlinx.coroutines.launch

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
                    _errorMessage.value = "Error: ${response.message()}"
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
                    _errorMessage.value = "Failed to fetch notes: ${response.message()}"
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
                    _errorMessage.value = "Please login first"
                    return@launch
                }
                val response = apiService.starNote(noteId, "Bearer $token")
                if (response.isSuccessful) {
                    fetchNoteById(noteId) // Refresh the note details
                } else {
                    _errorMessage.value = "Failed to star note: ${response.message()}"
                }
            } catch (e: Exception) {
                _errorMessage.value = "Error: ${e.message}"
            }
        }
    }


}