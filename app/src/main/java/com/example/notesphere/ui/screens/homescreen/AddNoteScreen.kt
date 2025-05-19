package com.example.notesphere.ui.screens.homescreen

import android.content.ContentResolver
import android.net.Uri
import android.provider.OpenableColumns
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.example.notesphere.utils.ViewModelFactory
import com.example.notesphere.viewmodels.NotesViewModel
import java.io.File

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddNoteScreen(navController: NavController) {
    val context = LocalContext.current
    val viewModel: NotesViewModel = viewModel(factory = ViewModelFactory(context))
    val isLoading by viewModel.isLoading
    val errorMessage by viewModel.errorMessage

    var title by remember { mutableStateOf("") }
    var subject by remember { mutableStateOf("") }
    var semester by remember { mutableStateOf(TextFieldValue("")) }
    var topics by remember { mutableStateOf(listOf("")) }
    var descriptionFilePairs by remember { mutableStateOf(listOf(Pair("", null as Uri?))) }
    var localError by remember { mutableStateOf("") }

    val filePickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        uri?.let { selectedUri ->
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

    LaunchedEffect(errorMessage) {
        if (errorMessage.isNotEmpty()) {
            localError = errorMessage
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Add Note") },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                }
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
            item {
                OutlinedTextField(
                    value = title,
                    onValueChange = { title = it },
                    label = { Text("Title") },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp)
                )
            }
            item {
                OutlinedTextField(
                    value = subject,
                    onValueChange = { subject = it },
                    label = { Text("Subject") },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp)
                )
            }
            item {
                OutlinedTextField(
                    value = semester,
                    onValueChange = { semester = it },
                    label = { Text("Semester") },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    keyboardOptions = KeyboardOptions.Default.copy(keyboardType = androidx.compose.ui.text.input.KeyboardType.Number)
                )
            }
            item {
                Card(
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Text("Topics", style = MaterialTheme.typography.titleMedium)
                        topics.forEachIndexed { index, topic ->
                            Row(
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                OutlinedTextField(
                                    value = topic,
                                    onValueChange = {
                                        topics = topics.toMutableList().apply { set(index, it) }
                                    },
                                    label = { Text("Topic ${index + 1}") },
                                    modifier = Modifier.weight(1f),
                                    shape = RoundedCornerShape(12.dp)
                                )
                                if (topics.size > 1) {
                                    IconButton(onClick = {
                                        topics = topics.toMutableList().apply { removeAt(index) }
                                    }) {
                                        Icon(Icons.Default.Close, contentDescription = "Remove")
                                    }
                                }
                            }
                        }
                        Button(
                            onClick = { topics = topics + "" },
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Text("Add Topic")
                        }
                    }
                }
            }
            itemsIndexed(descriptionFilePairs) { index, pair ->
                Card(
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        OutlinedTextField(
                            value = pair.first,
                            onValueChange = {
                                descriptionFilePairs = descriptionFilePairs.toMutableList().apply {
                                    set(index, Pair(it, pair.second))
                                }
                            },
                            label = { Text("File Description") },
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(12.dp)
                        )
                        Row(
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Button(
                                onClick = { filePickerLauncher.launch("application/pdf") },
                                modifier = Modifier.weight(1f),
                                shape = RoundedCornerShape(12.dp)
                            ) {
                                Text(if (pair.second != null) "Replace File" else "Select File")
                            }
                            if (pair.second != null) {
                                IconButton(onClick = {
                                    descriptionFilePairs = descriptionFilePairs.toMutableList().apply {
                                        set(index, Pair(pair.first, null))
                                    }
                                }) {
                                    Icon(Icons.Default.Close, contentDescription = "Remove File")
                                }
                            }
                        }
                        pair.second?.let { uri ->
                            Text(
                                text = uri.path?.substringAfterLast("/") ?: "Selected File",
                                style = MaterialTheme.typography.bodySmall,
                                modifier = Modifier.padding(top = 8.dp)
                            )
                        }
                    }
                }
            }
            item {
                Button(
                    onClick = {
                        if (descriptionFilePairs.size < 5) {
                            descriptionFilePairs = descriptionFilePairs + Pair("", null)
                        } else {
                            localError = "Maximum 5 files allowed"
                        }
                    },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    enabled = descriptionFilePairs.size < 5
                ) {
                    Text("Add Another File")
                }
            }
            if (localError.isNotEmpty()) {
                item {
                    Text(
                        text = localError,
                        color = MaterialTheme.colorScheme.error,
                        style = MaterialTheme.typography.bodyMedium
                    )
                }
            }
            item {
                Button(
                    onClick = {
                        if (title.isBlank() || subject.isBlank() || semester.text.isBlank()) {
                            localError = "Title, subject, and semester are required"
                            return@Button
                        }
                        val semesterInt = semester.text.toIntOrNull() ?: 0
                        if (semesterInt <= 0) {
                            localError = "Invalid semester"
                            return@Button
                        }
                        val validTopics = topics.filter { it.isNotBlank() }
                        if (validTopics.isEmpty()) {
                            localError = "At least one topic is required"
                            return@Button
                        }
                        val descriptions = descriptionFilePairs.map { it.first }
                        val filePaths = descriptionFilePairs.mapNotNull { pair ->
                            pair.second?.let { uri ->
                                val file = File(context.cacheDir, "temp_${System.currentTimeMillis()}.pdf")
                                context.contentResolver.openInputStream(uri)?.use { input ->
                                    file.outputStream().use { output -> input.copyTo(output) }
                                }
                                file.absolutePath
                            }
                        }
                        viewModel.uploadNote(
                            title = title,
                            subject = subject,
                            topics = validTopics.joinToString(","),
                            descriptions = descriptions,
                            filePaths = filePaths,
                            semester = semesterInt,
                            onSuccess = { navController.popBackStack() }
                        )
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 16.dp),
                    shape = RoundedCornerShape(12.dp),
                    enabled = !isLoading
                ) {
                    if (isLoading) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(24.dp),
                            color = MaterialTheme.colorScheme.onPrimary
                        )
                    } else {
                        Text("Upload Note")
                    }
                }
            }
        }
    }
}

fun getFileSize(contentResolver: ContentResolver, uri: Uri): Long {
    return contentResolver.query(uri, null, null, null, null)?.use { cursor ->
        cursor.moveToFirst()
        cursor.getLong(cursor.getColumnIndexOrThrow(OpenableColumns.SIZE))
    } ?: 0
}
