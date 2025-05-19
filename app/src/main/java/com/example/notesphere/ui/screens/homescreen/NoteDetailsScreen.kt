package com.example.notesphere.ui.screens.notes

import android.content.Context
import android.widget.Toast
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.draw.scale
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.example.notesphere.data.FilePath
import com.example.notesphere.data.Note
import com.example.notesphere.utils.ViewModelFactory
import com.example.notesphere.utils.formatDate
import com.example.notesphere.viewmodels.NotesViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NoteDetailsScreen(navController: NavController, noteId: String) {
    val context = LocalContext.current
    val viewModel: NotesViewModel = viewModel(factory = ViewModelFactory(context))
    val noteDetails by viewModel.noteDetails
    val downloadUrls by viewModel.downloadUrls
    val viewUrls by viewModel.viewUrls
    val isLoading by viewModel.isLoading
    val errorMessage by viewModel.errorMessage
    val userId by viewModel.userId

    // Fetch note details when noteId changes
    LaunchedEffect(noteId) {
        viewModel.fetchNoteById(noteId)
    }

    // Show toast for errors
    LaunchedEffect(errorMessage) {
        if (errorMessage.isNotEmpty()) {
            Toast.makeText(context, errorMessage, Toast.LENGTH_LONG).show()
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = noteDetails?.title ?: "Note Details",
                        style = MaterialTheme.typography.titleLarge.copy(
                            fontSize = 20.sp,
                            fontWeight = FontWeight.SemiBold
                        ),
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(
                            imageVector = Icons.Default.ArrowBack,
                            contentDescription = "Back",
                            modifier = Modifier.size(24.dp)
                        )
                    }
                },
                actions = {
                    // Star button
                    var isStarAnimating by remember { mutableStateOf(false) }
                    val isStarred by remember { derivedStateOf { userId != null && noteDetails?.starredBy?.contains(userId) == true } }
                    val starScale by animateFloatAsState(
                        targetValue = if (isStarAnimating) 1.3f else 1f,
                        animationSpec = spring(dampingRatio = 0.5f, stiffness = 1000f),
                        finishedListener = { isStarAnimating = false },
                        label = "Star Scale"
                    )
                    val starRotation by animateFloatAsState(
                        targetValue = if (isStarAnimating) 360f else 0f,
                        animationSpec = tween(400),
                        label = "Star Rotation"
                    )
                    IconButton(
                        onClick = {
                            if (userId == null) {
                                Toast.makeText(context, "Please log in to star notes", Toast.LENGTH_SHORT).show()
                            } else {
                                isStarAnimating = true
                                viewModel.starNote(noteId)
                            }
                        },
                        enabled = noteDetails != null,
                        modifier = Modifier
                            .size(40.dp)
                            .scale(starScale)
                            .rotate(starRotation)
                            .clip(CircleShape)
                            .background(MaterialTheme.colorScheme.surfaceContainer)
                            .shadow(if (isStarred) 4.dp else 0.dp, CircleShape)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Star,
                            contentDescription = "Star",
                            tint = if (isStarred) Color.Yellow else MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.size(24.dp)
                        )
                    }
                    // Share button
                    var isShareAnimating by remember { mutableStateOf(false) }
                    val shareRotation by animateFloatAsState(
                        targetValue = if (isShareAnimating) 360f else 0f,
                        animationSpec = tween(300),
                        finishedListener = { _: Float -> isShareAnimating = false },
                        label = "Share Rotation"
                    )
                    IconButton(
                        onClick = {
                            isShareAnimating = true
                            noteDetails?.let {
                                val shareIntent = android.content.Intent(android.content.Intent.ACTION_SEND).apply {
                                    type = "text/plain"
                                    putExtra(android.content.Intent.EXTRA_TEXT, "Check out this note: ${it.title}\n\nDownload link: ${downloadUrls.firstOrNull()}")
                                }
                                context.startActivity(android.content.Intent.createChooser(shareIntent, "Share Note"))
                            }
                        },
                        enabled = noteDetails != null,
                        modifier = Modifier.rotate(shareRotation)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Share,
                            contentDescription = "Share",
                            tint = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.size(24.dp)
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface,
                    titleContentColor = MaterialTheme.colorScheme.onSurface,
                    navigationIconContentColor = MaterialTheme.colorScheme.onSurface,
                    actionIconContentColor = MaterialTheme.colorScheme.onSurfaceVariant
                )
            )
        }
    ) { padding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .background(
                    Brush.verticalGradient(
                        colors = listOf(
                            MaterialTheme.colorScheme.surface,
                            MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.1f)
                        )
                    )
                )
        ) {
            when {
                isLoading -> CenterProgressIndicator()
                errorMessage.isNotEmpty() -> ErrorMessage(message = errorMessage) {
                    viewModel.fetchNoteById(noteId)
                }
                noteDetails == null -> EmptyStateMessage()
                else -> NoteContent(
                    note = noteDetails!!,
                    downloadUrls = downloadUrls,
                    viewUrls = viewUrls,
                    context = context,
                    navController = navController,
                    viewModel = viewModel,
                    userId = userId
                )
            }
        }
    }
}

@Composable
private fun NoteContent(
    note: Note,
    downloadUrls: List<String>,
    viewUrls: List<String>,
    context: Context,
    navController: NavController,
    viewModel: NotesViewModel,
    userId: String?
) {
    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp, vertical = 16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        item { Spacer(modifier = Modifier.height(8.dp)) }
        item { NoteHeader(note) }
        item { AuthorSection(note, navController) }
        item { MetadataSection(note) }
        item { FilesHeader() }
        if (note.filePath.isEmpty()) {
            item {
                Text(
                    text = "No files attached",
                    style = MaterialTheme.typography.bodyMedium.copy(fontSize = 16.sp),
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(vertical = 8.dp)
                )
            }
        } else {
            items(note.filePath, key = { "${note.id}_${note.filePath.indexOf(it)}" }) { file ->
                val index = note.filePath.indexOf(file)
                PdfCard(
                    file = file,
                    downloadUrl = downloadUrls.getOrNull(index) ?: "",
                    viewUrl = viewUrls.getOrNull(index) ?: "",
                    context = context,
                    noteId = note.id,
                    index = index,
                    viewModel = viewModel
                )
            }
        }
        if (userId != null && note.user.id == userId) {
            item {
                DeleteButton(
                    onClick = {
                        viewModel.deleteNote(note.id)
                        Toast.makeText(context, "Note deleted", Toast.LENGTH_SHORT).show()
                        navController.popBackStack()
                    }
                )
            }
        }
    }
}

@Composable
private fun NoteHeader(note: Note) {
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Text(
            text = note.title,
            style = MaterialTheme.typography.headlineMedium.copy(
                fontWeight = FontWeight.Bold,
                fontSize = 28.sp
            ),
            color = MaterialTheme.colorScheme.onSurface,
            maxLines = 2,
            overflow = TextOverflow.Ellipsis
        )
        Text(
            text = note.subject,
            style = MaterialTheme.typography.titleLarge.copy(
                fontWeight = FontWeight.Medium,
                fontSize = 20.sp
            ),
            color = MaterialTheme.colorScheme.primary,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )
    }
}

@Composable
private fun AuthorSection(note: Note, navController: NavController) {
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()
    val cardScale by animateFloatAsState(
        targetValue = if (isPressed) 0.98f else 1f,
        animationSpec = tween(100),
        label = "Card Scale"
    )

    AnimatedVisibility(
        visible = true,
        enter = fadeIn(tween(300)) + slideInVertically(tween(300)),
        exit = fadeOut(tween(300)) + slideOutVertically(tween(300))
    ) {
        Card(
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer),
            elevation = CardDefaults.cardElevation(defaultElevation = 6.dp),
            modifier = Modifier
                .fillMaxWidth()
                .scale(cardScale)
                .clickable(
                    interactionSource = interactionSource,
                    indication = null
                ) { navController.navigate("profile/${note.user.id}") }
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Box(
                    modifier = Modifier
                        .size(48.dp)
                        .clip(CircleShape)
                        .background(MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.1f)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Person,
                        contentDescription = "Author Profile",
                        tint = MaterialTheme.colorScheme.onPrimaryContainer,
                        modifier = Modifier.size(32.dp)
                    )
                }
                Column(
                    verticalArrangement = Arrangement.spacedBy(4.dp),
                    modifier = Modifier.weight(1f)
                ) {
                    Text(
                        text = note.user.username,
                        style = MaterialTheme.typography.titleMedium.copy(
                            fontWeight = FontWeight.Bold,
                            fontSize = 18.sp
                        ),
                        color = MaterialTheme.colorScheme.onPrimaryContainer,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    Text(
                        text = note.user.college,
                        style = MaterialTheme.typography.bodyMedium.copy(
                            fontWeight = FontWeight.Medium,
                            fontSize = 14.sp
                        ),
                        color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.7f),
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }
        }
    }
}

@Composable
private fun MetadataSection(note: Note) {
    AnimatedVisibility(
        visible = true,
        enter = fadeIn(tween(300)) + slideInVertically(tween(300)),
        exit = fadeOut(tween(300)) + slideOutVertically(tween(300))
    ) {
        Card(
            shape = RoundedCornerShape(12.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
            elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
        ) {
            Column(
                modifier = Modifier.padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                InfoItem(label = "Subject", value = note.subject)
                InfoItem(label = "Semester", value = note.semester.toString())
                InfoItem(label = "Topics", value = note.topics.joinToString(", "))
                InfoItem(label = "Created", value = formatDate(note.createdAt))
                InfoItem(label = "Stars", value = note.stars.toString())
            }
        }
    }
}

@Composable
private fun InfoItem(label: String, value: String) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = "$label:",
            style = MaterialTheme.typography.bodyMedium.copy(
                fontSize = 16.sp,
                fontWeight = FontWeight.Medium
            ),
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Text(
            text = value,
            style = MaterialTheme.typography.bodyMedium.copy(fontSize = 16.sp),
            color = MaterialTheme.colorScheme.onSurface,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )
    }
}

@Composable
private fun FilesHeader() {
    Text(
        text = "Attached Files",
        style = MaterialTheme.typography.titleLarge.copy(
            fontWeight = FontWeight.SemiBold,
            fontSize = 20.sp
        ),
        color = MaterialTheme.colorScheme.onSurface,
        modifier = Modifier.padding(vertical = 8.dp)
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun PdfCard(
    file: FilePath,
    downloadUrl: String,
    viewUrl: String,
    context: Context,
    noteId: String,
    index: Int,
    viewModel: NotesViewModel
) {
    var isVisible by remember { mutableStateOf(false) }
    LaunchedEffect(Unit) { isVisible = true }

    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()

    val cardScale by animateFloatAsState(
        targetValue = if (isPressed) 0.98f else 1f,
        animationSpec = tween(100),
        label = "Card Scale"
    )

    val viewButtonInteractionSource = remember { MutableInteractionSource() }
    val isViewButtonPressed by viewButtonInteractionSource.collectIsPressedAsState()
    val viewButtonScale by animateFloatAsState(
        targetValue = if (isViewButtonPressed) 0.95f else 1f,
        animationSpec = tween(100),
        label = "View Button Scale"
    )

    val downloadButtonInteractionSource = remember { MutableInteractionSource() }
    val isDownloadButtonPressed by downloadButtonInteractionSource.collectIsPressedAsState()
    val downloadButtonScale by animateFloatAsState(
        targetValue = if (isDownloadButtonPressed) 0.95f else 1f,
        animationSpec = tween(100),
        label = "Download Button Scale"
    )

    AnimatedVisibility(
        visible = isVisible,
        enter = fadeIn(tween(300)) + slideInVertically(tween(300)),
        exit = fadeOut(tween(300)) + slideOutVertically(tween(300))
    ) {
        Card(
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainerLow),
            elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
            modifier = Modifier
                .fillMaxWidth()
                .scale(cardScale)
                .clickable(
                    interactionSource = interactionSource,
                    indication = null
                ) {
                    if (viewUrl.isNotEmpty()) {
                        viewModel.viewNote(viewUrl)
                    } else {
                        Toast.makeText(context, "Unable to view file", Toast.LENGTH_SHORT).show()
                    }
                }
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier.weight(1f),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.PictureAsPdf,
                            contentDescription = "PDF Icon",
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(24.dp)
                        )
                        Column(
                            verticalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            Text(
                                text = file.description.ifEmpty { "Attachment ${index + 1}" },
                                style = MaterialTheme.typography.titleMedium.copy(
                                    fontSize = 18.sp,
                                    fontWeight = FontWeight.SemiBold
                                ),
                                color = MaterialTheme.colorScheme.onSurface,
                                maxLines = 2,
                                overflow = TextOverflow.Ellipsis
                            )
                            Text(
                                text = "File ${index + 1}",
                                style = MaterialTheme.typography.bodySmall.copy(
                                    fontSize = 14.sp,
                                    fontWeight = FontWeight.Medium
                                ),
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        IconButton(
                            onClick = {
                                if (viewUrl.isNotEmpty()) {
                                    viewModel.viewNote(viewUrl)
                                } else {
                                    Toast.makeText(context, "Unable to view file", Toast.LENGTH_SHORT).show()
                                }
                            },
                            modifier = Modifier
                                .size(40.dp)
                                .scale(viewButtonScale)
                                .clip(CircleShape)
                                .background(MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.9f))
                        ) {
                            Icon(
                                imageVector = Icons.Default.Visibility,
                                contentDescription = "View",
                                tint = MaterialTheme.colorScheme.onSecondaryContainer,
                                modifier = Modifier.size(24.dp)
                            )
                        }
                        IconButton(
                            onClick = {
                                if (downloadUrl.isNotEmpty()) {
                                    val intent = android.content.Intent(android.content.Intent.ACTION_VIEW).apply {
                                        setData(android.net.Uri.parse(downloadUrl))
                                        flags = android.content.Intent.FLAG_ACTIVITY_NEW_TASK
                                    }
                                    try {
                                        context.startActivity(intent)
                                        Toast.makeText(context, "Opening download in browser...", Toast.LENGTH_SHORT).show()
                                    } catch (e: Exception) {
                                        Toast.makeText(context, "Unable to open browser: ${e.message}", Toast.LENGTH_SHORT).show()
                                    }
                                } else {
                                    Toast.makeText(context, "Unable to download file", Toast.LENGTH_SHORT).show()
                                }
                            },
                            modifier = Modifier
                                .size(40.dp)
                                .scale(downloadButtonScale)
                                .clip(CircleShape)
                                .background(MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.9f))
                        ) {
                            Icon(
                                imageVector = Icons.Default.Download,
                                contentDescription = "Download",
                                tint = MaterialTheme.colorScheme.onPrimaryContainer,
                                modifier = Modifier.size(24.dp)
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun DeleteButton(onClick: () -> Unit) {
    FilledTonalButton(
        onClick = onClick,
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = 16.dp),
        shape = RoundedCornerShape(12.dp),
        colors = ButtonDefaults.filledTonalButtonColors(
            containerColor = MaterialTheme.colorScheme.errorContainer,
            contentColor = MaterialTheme.colorScheme.error
        )
    ) {
        Text(
            text = "Delete Note",
            style = MaterialTheme.typography.labelLarge.copy(fontSize = 16.sp)
        )
    }
}

@Composable
private fun CenterProgressIndicator() {
    val scale by rememberInfiniteTransition(label = "Progress Scale").animateFloat(
        initialValue = 0.9f,
        targetValue = 1.1f,
        animationSpec = infiniteRepeatable(
            animation = tween(1000, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "Progress Scale"
    )
    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        CircularProgressIndicator(
            modifier = Modifier
                .size(48.dp)
                .scale(scale),
            strokeWidth = 4.dp,
            color = MaterialTheme.colorScheme.primary
        )
    }
}

@Composable
private fun ErrorMessage(message: String, onRetry: () -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(
            text = message,
            color = MaterialTheme.colorScheme.error,
            style = MaterialTheme.typography.bodyLarge.copy(fontSize = 18.sp),
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
            textAlign = androidx.compose.ui.text.style.TextAlign.Center
        )
        FilledTonalButton(
            onClick = onRetry,
            modifier = Modifier
                .padding(8.dp)
                .height(40.dp),
            shape = RoundedCornerShape(12.dp)
        ) {
            Text(
                text = "Try Again",
                style = MaterialTheme.typography.labelLarge.copy(fontSize = 16.sp)
            )
        }
    }
}

@Composable
private fun EmptyStateMessage() {
    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        Text(
            text = "Note not found",
            style = MaterialTheme.typography.titleMedium.copy(fontSize = 18.sp),
            fontWeight = FontWeight.Medium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}