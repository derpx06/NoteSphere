package com.example.notesphere.ui.screens.homescreen

import android.content.ContentResolver
import android.net.Uri
import android.provider.OpenableColumns
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.example.notesphere.ui.theme.NoteSphereTheme
import com.example.notesphere.utils.ViewModelFactory
import com.example.notesphere.viewmodels.NotesViewModel
import java.io.File

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddNoteScreen(navController: NavController) {
    NoteSphereTheme {
        val context = LocalContext.current
        val viewModel: NotesViewModel = viewModel(factory = ViewModelFactory(context))
        val isLoading by viewModel.isLoading
        val errorMessage by viewModel.errorMessage

        // Form state
        var title by remember { mutableStateOf("") }
        var subject by remember { mutableStateOf("") }
        var topics by remember { mutableStateOf(listOf("")) }
        var descriptionFilePairs by remember { mutableStateOf(listOf(Pair("", null as Uri?))) }
        var localError by remember { mutableStateOf("") }

        // File picker launcher
        val filePickerLauncher = rememberLauncherForActivityResult(
            contract = ActivityResultContracts.GetContent()
        ) { uri: Uri? ->
            uri?.let { selectedUri ->
                // Validate file size (50 MB)
                val fileSize = getFileSize(context.contentResolver, selectedUri)
                if (fileSize > 50 * 1024 * 1024) {
                    localError = "File exceeds 50 MB limit"
                    return@let
                }
                val index = descriptionFilePairs.indexOfFirst { it.second == null }
                if (index != -1) {
                    descriptionFilePairs = descriptionFilePairs.toMutableList().apply {
                        set(index, Pair(descriptionFilePairs[index].first, selectedUri))
                    }
                } else if (descriptionFilePairs.size < 5) {
                    descriptionFilePairs = descriptionFilePairs + Pair("", selectedUri)
                } else {
                    localError = "Maximum 5 files allowed"
                }
            }
        }

        // Handle error messages
        LaunchedEffect(errorMessage) {
            if (errorMessage.isNotEmpty()) {
                localError = errorMessage
            }
        }

        Scaffold(
            topBar = {
                CenterAlignedTopAppBar(
                    title = {
                        Text(
                            "Add Note",
                            style = MaterialTheme.typography.titleLarge
                        )
                    },
                    navigationIcon = {
                        IconButton(onClick = { navController.popBackStack() }) {
                            Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                        }
                    },
                    colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
                        containerColor = MaterialTheme.colorScheme.surface,
                        titleContentColor = MaterialTheme.colorScheme.onSurface,
                        navigationIconContentColor = MaterialTheme.colorScheme.onSurface
                    )
                )
            }
        ) { padding ->
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding)
                    .padding(horizontal = 16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Title and Subject Section
                item {
                    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                        OutlinedTextField(
                            value = title,
                            onValueChange = { title = it },
                            modifier = Modifier.fillMaxWidth(),
                            label = { Text("Title") },
                            shape = RoundedCornerShape(12.dp),
                            singleLine = true
                        )
                        OutlinedTextField(
                            value = subject,
                            onValueChange = { subject = it },
                            modifier = Modifier.fillMaxWidth(),
                            label = { Text("Subject") },
                            shape = RoundedCornerShape(12.dp),
                            singleLine = true
                        )
                    }
                }

                // Topics Section
                item {
                    Card(
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.surfaceContainerLow
                        )
                    ) {
                        Column(
                            modifier = Modifier.padding(16.dp),
                            verticalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            Text(
                                "Topics",
                                style = MaterialTheme.typography.titleMedium
                            )
                            topics.forEachIndexed { index, topic ->
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    OutlinedTextField(
                                        value = topic,
                                        onValueChange = {
                                            topics = topics.toMutableList().apply { set(index, it) }
                                        },
                                        modifier = Modifier.weight(1f),
                                        label = { Text("Topic ${index + 1}") },
                                        shape = RoundedCornerShape(12.dp),
                                        singleLine = true
                                    )
                                    if (topics.size > 1) {
                                        IconButton(
                                            onClick = {
                                                topics = topics.toMutableList().apply { removeAt(index) }
                                            },
                                            modifier = Modifier.size(40.dp)
                                        ) {
                                            Icon(Icons.Default.Close, contentDescription = "Remove")
                                        }
                                    }
                                }
                            }
                            FilledTonalButton(
                                onClick = { topics = topics + "" },
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(12.dp)
                            ) {
                                Icon(Icons.Default.Add, contentDescription = null)
                                Spacer(modifier = Modifier.width(8.dp))
                                Text("Add Topic")
                            }
                        }
                    }
                }

                // File Attachments
                itemsIndexed(descriptionFilePairs, key = { index, _ -> index }) { index, pair ->
                    FileAttachmentItem(
                        description = pair.first,
                        fileUri = pair.second,
                        index = index,
                        onDescriptionChange = {
                            descriptionFilePairs = descriptionFilePairs.toMutableList().apply {
                                set(index, Pair(it, pair.second))
                            }
                        },
                        onFileSelect = { filePickerLauncher.launch("application/pdf") },
                        onFileRemove = {
                            descriptionFilePairs = descriptionFilePairs.toMutableList().apply {
                                set(index, Pair(pair.first, null))
                            }
                        },
                        onRemove = {
                            descriptionFilePairs = descriptionFilePairs.toMutableList().apply {
                                removeAt(index)
                            }
                        },
                        showRemove = !(descriptionFilePairs.size == 1 && index == 0)
                    )
                }

                // Add File Button
                item {
                    FilledTonalButton(
                        onClick = {
                            if (descriptionFilePairs.size < 5) {
                                descriptionFilePairs = descriptionFilePairs + Pair("", null)
                            } else {
                                localError = "Maximum 5 files allowed"
                            }
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(48.dp),
                        shape = RoundedCornerShape(12.dp),
                        enabled = descriptionFilePairs.size < 5
                    ) {
                        Icon(Icons.Default.Add, contentDescription = null)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Add File")
                    }
                }

                // Error Message
                if (localError.isNotEmpty()) {
                    item {
                        Text(
                            localError,
                            color = MaterialTheme.colorScheme.error,
                            style = MaterialTheme.typography.bodySmall,
                            modifier = Modifier.padding(horizontal = 16.dp)
                        )
                    }
                }

                // Submit Button
                item {
                    FilledTonalButton(
                        onClick = {
                            // Validation
                            if (title.isBlank() || subject.isBlank()) {
                                localError = "Title and subject are required"
                                return@FilledTonalButton
                            }
                            if (topics.all { it.isBlank() }) {
                                localError = "At least one topic is required"
                                return@FilledTonalButton
                            }
                            if (descriptionFilePairs.count { it.second != null } > 5) {
                                localError = "Maximum 5 files allowed"
                                return@FilledTonalButton
                            }
                            // Validate file sizes before upload
                            descriptionFilePairs.forEach { pair ->
                                pair.second?.let { uri ->
                                    if (getFileSize(context.contentResolver, uri) > 50 * 1024 * 1024) {
                                        localError = "One or more files exceed 50 MB limit"
                                        return@FilledTonalButton
                                    }
                                }
                            }
                            val validTopics = topics.filter { it.isNotBlank() }.joinToString(",")
                            val validDescriptions = descriptionFilePairs
                                .filter { it.first.isNotBlank() || it.second != null }
                                .joinToString(",") { it.first }
                            val filePaths = descriptionFilePairs
                                .mapNotNull { pair ->
                                    pair.second?.let { uri ->
                                        getFileFromUri(context.contentResolver, uri)?.absolutePath
                                    }
                                }
                            viewModel.uploadNote(
                                title = title,
                                subject = subject,
                                topics = validTopics,
                                descriptions = validDescriptions,
                                filePaths = filePaths,
                                onSuccess = {
                                    localError = "Note uploaded successfully"
                                    navController.popBackStack()
                                }
                            )
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(48.dp),
                        shape = RoundedCornerShape(12.dp),
                        enabled = !isLoading
                    ) {
                        AnimatedVisibility(
                            visible = isLoading,
                            enter = fadeIn(tween(200)),
                            exit = fadeOut(tween(200))
                        ) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(32.dp),
                                color = MaterialTheme.colorScheme.onPrimaryContainer,
                                strokeWidth = 3.dp
                            )
                        }
                        AnimatedVisibility(
                            visible = !isLoading,
                            enter = fadeIn(tween(200)),
                            exit = fadeOut(tween(200))
                        ) {
                            Text("Save Note")
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun FileAttachmentItem(
    description: String,
    fileUri: Uri?,
    index: Int,
    onDescriptionChange: (String) -> Unit,
    onFileSelect: () -> Unit,
    onFileRemove: () -> Unit,
    onRemove: () -> Unit,
    showRemove: Boolean
) {
    Card(
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceContainerLow
        )
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                OutlinedTextField(
                    value = description,
                    onValueChange = onDescriptionChange,
                    modifier = Modifier.weight(1f),
                    label = { Text("Description") },
                    shape = RoundedCornerShape(12.dp),
                    singleLine = true
                )
                FilledTonalButton(
                    onClick = onFileSelect,
                    modifier = Modifier
                        .height(48.dp)
                        .weight(1f),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Icon(
                        if (fileUri != null) Icons.Default.Check else Icons.Default.UploadFile,
                        contentDescription = "Select file"
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        if (fileUri != null) "Replace File" else "Select File"
                    )
                }
                if (showRemove) {
                    IconButton(
                        onClick = onRemove,
                        modifier = Modifier.size(40.dp)
                    ) {
                        Icon(Icons.Default.Close, contentDescription = "Remove")
                    }
                }
            }
            if (fileUri != null) {
                AnimatedVisibility(
                    visible = true,
                    enter = fadeIn(tween(300)),
                    exit = fadeOut(tween(300))
                ) {
                    Card(
                        shape = RoundedCornerShape(12.dp),
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.surfaceContainerLowest
                        )
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(12.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(
                                fileUri.path?.substringAfterLast("/") ?: "File",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurface,
                                modifier = Modifier.weight(1f)
                            )
                            IconButton(
                                onClick = onFileRemove,
                                modifier = Modifier.size(40.dp)
                            ) {
                                Icon(
                                    Icons.Default.Close,
                                    contentDescription = "Remove File"
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

// Utility function to get file size
fun getFileSize(contentResolver: ContentResolver, uri: Uri): Long {
    return contentResolver.query(uri, null, null, null, null)?.use { cursor ->
        val sizeIndex = cursor.getColumnIndex(OpenableColumns.SIZE)
        cursor.moveToFirst()
        cursor.getLong(sizeIndex)
    } ?: 0L
}

// Utility function to convert URI to File
fun getFileFromUri(contentResolver: ContentResolver, uri: Uri): File? {
    val fileName = contentResolver.query(uri, null, null, null, null)?.use { cursor ->
        val nameIndex = cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME)
        cursor.moveToFirst()
        cursor.getString(nameIndex)
    } ?: "temp.pdf"

    val file = File.createTempFile("note_", ".pdf", null)
    try {
        contentResolver.openInputStream(uri)?.use { input ->
            file.outputStream().use { output ->
                input.copyTo(output)
            }
        }
        return file
    } catch (e: Exception) {
        file.delete()
        return null
    }
}