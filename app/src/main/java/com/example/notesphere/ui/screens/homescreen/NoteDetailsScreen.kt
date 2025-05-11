package com.example.notesphere.ui.screens.notes

import android.content.Context
import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.example.notesphere.data.Note
import com.example.notesphere.network.RetrofitClient
import com.example.notesphere.utils.ViewModelFactory
import com.example.notesphere.utils.formatDate
import com.example.notesphere.viewmodels.NotesViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NoteDetailsScreen(navController: NavController, noteId: String) {
    val context = LocalContext.current
    val viewModel: NotesViewModel = viewModel(factory = ViewModelFactory(context))
    val note by viewModel.noteDetails
    val downloadUrls by viewModel.downloadUrls
    val isLoading by viewModel.isLoading
    val errorMessage by viewModel.errorMessage

    if (note == null && !isLoading && errorMessage.isEmpty()) {
        viewModel.fetchNoteById(noteId)
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = note?.title ?: "Loading...",
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                },
                actions = {
                    IconButton(
                        onClick = { viewModel.starNote(noteId) },
                        enabled = note != null
                    ) {
                        Icon(
                            Icons.Default.Star,
                            contentDescription = "Star",
                            tint = if (note?.starredBy?.isNotEmpty() == true) Color.Yellow else MaterialTheme.colorScheme.onSurface
                        )
                    }
                    IconButton(
                        onClick = {
                            note?.let {
                                val shareIntent = Intent(Intent.ACTION_SEND).apply {
                                    type = "text/plain"
                                    putExtra(Intent.EXTRA_TEXT, "Check out this note: ${it.title}\n\nDownload link: ${downloadUrls.firstOrNull()}")
                                }
                                context.startActivity(Intent.createChooser(shareIntent, "Share Note"))
                            }
                        },
                        enabled = note != null
                    ) {
                        Icon(Icons.Default.Share, contentDescription = "Share")
                    }
                }
            )
        }
    ) { padding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            when {
                isLoading -> CenterProgressIndicator()
                errorMessage.isNotEmpty() -> ErrorMessage(message = errorMessage) {
                    viewModel.fetchNoteById(noteId)
                }
                note == null -> EmptyStateMessage()
                else -> NoteContent(note!!, downloadUrls, context)
            }
        }
    }
}

@Composable
private fun NoteContent(note: Note, downloadUrls: List<String>, context: Context) {
    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item { NoteHeader(note) }
        item { MetadataSection(note) }
        item { FilesHeader() }
        items(note.filePath) { file ->
            val index = note.filePath.indexOf(file)
            PdfCard(
                file = file,
                downloadUrl = downloadUrls.getOrNull(index) ?: "",
                context = context
            )
        }
    }
}

@Composable
private fun NoteHeader(note: Note) {
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Text(
            text = note.title,
            style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.primary
        )
        Text(
            text = note.subject,
            style = MaterialTheme.typography.titleMedium,
            color = MaterialTheme.colorScheme.secondary
        )
    }
}

@Composable
private fun MetadataSection(note: Note) {
    Card(
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            InfoItem("Author", "${note.user.username} (${note.user.college})")
            InfoItem("Topics", note.topics.joinToString(", "))
            InfoItem("Created", formatDate(note.createdAt))
            InfoItem("Stars", note.stars.toString())
        }
    }
}

@Composable
private fun InfoItem(label: String, value: String) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(text = "$label:", style = MaterialTheme.typography.bodyMedium)
        Text(text = value, style = MaterialTheme.typography.bodyMedium)
    }
}

@Composable
private fun FilesHeader() {
    Text(
        text = "Attachments",
        style = MaterialTheme.typography.titleLarge,
        modifier = Modifier.padding(vertical = 8.dp)
    )
}

@Composable
fun PdfCard(file: com.example.notesphere.data.FilePath, downloadUrl: String, context: Context) {
    Card(
        shape = RoundedCornerShape(16.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Text(
                text = file.description.ifEmpty { "PDF Document" },
                style = MaterialTheme.typography.bodyMedium
            )

            Button(
                onClick = {
                    val fullUrl = "${RetrofitClient.BASE_URL}$downloadUrl"
                    val intent = Intent(Intent.ACTION_VIEW, Uri.parse(fullUrl))
                    context.startActivity(intent)
                },
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.primary,
                    contentColor = MaterialTheme.colorScheme.onPrimary
                )
            ) {
                Icon(
                    Icons.Default.Download,
                    contentDescription = "Download",
                    modifier = Modifier.size(18.dp))
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Download PDF")
            }
        }
    }
}

@Composable
private fun CenterProgressIndicator() {
    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        CircularProgressIndicator()
    }
}

@Composable
private fun ErrorMessage(message: String, onRetry: () -> Unit) {
    Column(
        modifier = Modifier.fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(
            text = message,
            color = MaterialTheme.colorScheme.error,
            style = MaterialTheme.typography.bodyLarge,
            modifier = Modifier.padding(16.dp)
        )
        Button(onClick = onRetry) {
            Text("Try Again")
        }
    }
}

@Composable
private fun EmptyStateMessage() {
    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        Text(
            text = "Note not found",
            style = MaterialTheme.typography.titleMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}