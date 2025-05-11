package com.example.notesphere.ui.screens.notes

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
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
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.example.notesphere.R
import com.example.notesphere.data.Note
import com.example.notesphere.utils.AuthManager
import com.example.notesphere.utils.ViewModelFactory
import com.example.notesphere.utils.formatDate
import com.example.notesphere.viewmodels.NotesViewModel
import com.google.accompanist.swiperefresh.SwipeRefresh
import com.google.accompanist.swiperefresh.rememberSwipeRefreshState
import java.text.SimpleDateFormat
import java.util.*

enum class SortOption {
    TITLE_ASC, TITLE_DESC, DATE_ASC, DATE_DESC
}

private fun parseDate(isoDate: String) = try {
    SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", Locale.US).apply {
        timeZone = TimeZone.getTimeZone("UTC")
    }.parse(isoDate)
} catch (e: Exception) {
    null
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(navController: NavController) {
    val context = LocalContext.current
    val viewModel: NotesViewModel = viewModel(factory = ViewModelFactory(context))
    val authManager = AuthManager(context)
    val notes by viewModel.notes
    val isLoading by viewModel.isLoading
    val errorMessage by viewModel.errorMessage

    var searchQuery by remember { mutableStateOf("") }
    var sortOption by remember { mutableStateOf(SortOption.DATE_DESC) }
    var showSortMenu by remember { mutableStateOf(false) }
    val focusRequester = remember { FocusRequester() }

    val filteredNotes = notes.filter {
        it.title.contains(searchQuery, ignoreCase = true) ||
                it.subject.contains(searchQuery, ignoreCase = true)
    }

    val sortedNotes = when (sortOption) {
        SortOption.TITLE_ASC -> filteredNotes.sortedBy { it.title }
        SortOption.TITLE_DESC -> filteredNotes.sortedByDescending { it.title }
        SortOption.DATE_ASC -> filteredNotes.sortedBy { parseDate(it.createdAt) }
        SortOption.DATE_DESC -> filteredNotes.sortedByDescending { parseDate(it.createdAt) }
    }

    Scaffold(
        floatingActionButton = {
            var isFabAnimating by remember { mutableStateOf(false) }
            val fabRotation by animateFloatAsState(
                targetValue = if (isFabAnimating) 360f else 0f,
                animationSpec = tween(350),
                finishedListener = { isFabAnimating = false },
                label = "FAB Rotation"
            )
            val interactionSource = remember { MutableInteractionSource() }
            val isPressed by interactionSource.collectIsPressedAsState()
            val scale by animateFloatAsState(
                targetValue = if (isPressed) 0.95f else 1f,
                animationSpec = tween(150),
                label = "FAB Scale"
            )

            FloatingActionButton(
                onClick = {
                    isFabAnimating = true
                    navController.navigate("addNote")
                },
                modifier = Modifier
                    .size(64.dp)
                    .scale(scale)
                    .rotate(fabRotation)
                    .shadow(10.dp, CircleShape),
                shape = CircleShape,
                containerColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.85f),
                contentColor = MaterialTheme.colorScheme.onPrimary,
                interactionSource = interactionSource
            ) {
                Icon(
                    Icons.Default.Add,
                    contentDescription = "Add Note",
                    modifier = Modifier.size(28.dp)
                )
            }
        },
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = authManager.getUserInfo()?.username?.let {
                            "Welcome, ${it.take(12)}${if (it.length > 12) "..." else ""}"
                        } ?: "NoteSphere",
                        style = MaterialTheme.typography.titleLarge.copy(fontSize = 20.sp),
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                },
                actions = {
                    IconButton(onClick = { /* Profile action */ }) {
                        Icon(
                            Icons.Default.AccountCircle,
                            contentDescription = stringResource(R.string.profile),
                            modifier = Modifier.size(24.dp),
                            tint = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface,
                    titleContentColor = MaterialTheme.colorScheme.onSurface,
                    actionIconContentColor = MaterialTheme.colorScheme.onSurfaceVariant
                )
            )
        }
    ) { padding ->
        SwipeRefresh(
            state = rememberSwipeRefreshState(isRefreshing = isLoading),
            onRefresh = { viewModel.fetchNotes() }
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding)
                    .background(
                        Brush.verticalGradient(
                            colors = listOf(
                                MaterialTheme.colorScheme.surface,
                                MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.05f)
                            )
                        )
                    )
            ) {
                // Search and Sort Row
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(20.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Surface(
                        modifier = Modifier
                            .weight(1f)
                            .shadow(6.dp, RoundedCornerShape(24.dp)),
                        shape = RoundedCornerShape(24.dp),
                        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.9f)
                    ) {
                        OutlinedTextField(
                            value = searchQuery,
                            onValueChange = { searchQuery = it },
                            modifier = Modifier
                                .fillMaxWidth()
                                .focusRequester(focusRequester),
                            placeholder = {
                                Text(
                                    stringResource(R.string.search_notes),
                                    style = MaterialTheme.typography.bodyMedium.copy(fontSize = 16.sp),
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            },
                            leadingIcon = {
                                Icon(
                                    Icons.Default.Search,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.onSurfaceVariant,
                                    modifier = Modifier.size(24.dp)
                                )
                            },
                            trailingIcon = {
                                AnimatedVisibility(
                                    visible = searchQuery.isNotEmpty(),
                                    enter = fadeIn(tween(250)),
                                    exit = fadeOut(tween(250))
                                ) {
                                    val clearInteractionSource = remember { MutableInteractionSource() }
                                    val isClearPressed by clearInteractionSource.collectIsPressedAsState()
                                    val clearScale by animateFloatAsState(
                                        targetValue = if (isClearPressed) 0.9f else 1f,
                                        animationSpec = tween(150),
                                        label = "Clear Scale"
                                    )
                                    IconButton(
                                        onClick = { searchQuery = "" },
                                        modifier = Modifier.scale(clearScale),
                                        interactionSource = clearInteractionSource
                                    ) {
                                        Icon(
                                            Icons.Default.Close,
                                            contentDescription = stringResource(R.string.clear),
                                            tint = MaterialTheme.colorScheme.onSurfaceVariant,
                                            modifier = Modifier.size(24.dp)
                                        )
                                    }
                                }
                            },
                            singleLine = true,
                            shape = RoundedCornerShape(24.dp),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedContainerColor = MaterialTheme.colorScheme.surfaceVariant,
                                unfocusedContainerColor = MaterialTheme.colorScheme.surfaceVariant,
                                focusedBorderColor = MaterialTheme.colorScheme.primary,
                                unfocusedBorderColor = MaterialTheme.colorScheme.outline,
                                cursorColor = MaterialTheme.colorScheme.primary
                            ),
                            textStyle = MaterialTheme.typography.bodyMedium.copy(fontSize = 16.sp),
                            keyboardOptions = KeyboardOptions(imeAction = ImeAction.Done),
                            keyboardActions = KeyboardActions(onDone = { focusRequester.freeFocus() })
                        )
                    }

                    TooltipBox(
                        positionProvider = TooltipDefaults.rememberPlainTooltipPositionProvider(),
                        tooltip = {
                            Surface(
                                shape = RoundedCornerShape(8.dp),
                                color = MaterialTheme.colorScheme.surfaceVariant,
                                shadowElevation = 2.dp
                            ) {
                                Text(
                                    text = "Sort Notes",
                                    modifier = Modifier.padding(8.dp),
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                            }
                        },
                        state = rememberTooltipState()
                    ) {
                        var isSortAnimating by remember { mutableStateOf(false) }
                        val sortRotation by animateFloatAsState(
                            targetValue = if (isSortAnimating) 360f else 0f,
                            animationSpec = tween(350),
                            finishedListener = { isSortAnimating = false },
                            label = "Sort Rotation"
                        )
                        val sortInteractionSource = remember { MutableInteractionSource() }
                        val isSortPressed by sortInteractionSource.collectIsPressedAsState()
                        val sortScale by animateFloatAsState(
                            targetValue = if (isSortPressed) 0.95f else 1f,
                            animationSpec = tween(150),
                            label = "Sort Scale"
                        )
                        IconButton(
                            onClick = {
                                isSortAnimating = true
                                showSortMenu = true
                            },
                            modifier = Modifier
                                .size(48.dp)
                                .scale(sortScale)
                                .rotate(sortRotation)
                                .clip(CircleShape)
                                .shadow(4.dp, CircleShape)
                                .background(MaterialTheme.colorScheme.surfaceContainerLow)
                        ) {
                            Icon(
                                Icons.Default.Sort,
                                contentDescription = stringResource(R.string.sort),
                                tint = MaterialTheme.colorScheme.onSurface,
                                modifier = Modifier.size(24.dp)
                            )
                        }
                    }

                    DropdownMenu(
                        expanded = showSortMenu,
                        onDismissRequest = { showSortMenu = false },
                        modifier = Modifier
                            .background(MaterialTheme.colorScheme.surface)
                            .shadow(4.dp)
                    ) {
                        listOf(
                            SortOption.TITLE_ASC to stringResource(R.string.title_asc),
                            SortOption.TITLE_DESC to stringResource(R.string.title_desc),
                            SortOption.DATE_ASC to stringResource(R.string.date_asc),
                            SortOption.DATE_DESC to stringResource(R.string.date_desc)
                        ).forEach { (option, text) ->
                            DropdownMenuItem(
                                text = {
                                    Text(
                                        text = text,
                                        style = MaterialTheme.typography.bodyMedium.copy(fontSize = 16.sp),
                                        color = if (sortOption == option) {
                                            MaterialTheme.colorScheme.primary
                                        } else {
                                            MaterialTheme.colorScheme.onSurface
                                        }
                                    )
                                },
                                onClick = {
                                    sortOption = option
                                    showSortMenu = false
                                },
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                            )
                        }
                    }
                }

                // Content Area
                Box(modifier = Modifier.fillMaxSize()) {
                    when {
                        isLoading -> LoadingState()
                        errorMessage.isNotEmpty() -> ErrorState(errorMessage) { viewModel.fetchNotes() }
                        sortedNotes.isEmpty() -> EmptyState()
                        else -> NotesGrid(sortedNotes, navController, viewModel)
                    }
                }
            }
        }
    }
}

@Composable
private fun LoadingState() {
    val scale by rememberInfiniteTransition(label = "Progress Scale").animateFloat(
        initialValue = 0.85f,
        targetValue = 1.15f,
        animationSpec = infiniteRepeatable(
            animation = tween(1200, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "Progress Scale"
    )
    Box(
        modifier = Modifier
            .fillMaxSize()
            .padding(32.dp),
        contentAlignment = Alignment.Center
    ) {
        CircularProgressIndicator(
            modifier = Modifier
                .size(48.dp)
                .scale(scale),
            color = MaterialTheme.colorScheme.primary,
            strokeWidth = 4.dp
        )
    }
}

@Composable
private fun ErrorState(message: String, onRetry: () -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(32.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Card(
            shape = RoundedCornerShape(24.dp),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surfaceContainerLow
            ),
            border = BorderStroke(0.5.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.2f)),
            modifier = Modifier
                .fillMaxWidth()
                .shadow(8.dp, RoundedCornerShape(24.dp))
        ) {
            Column(
                modifier = Modifier.padding(20.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Icon(
                    Icons.Default.Error,
                    contentDescription = null,
                    modifier = Modifier.size(48.dp),
                    tint = MaterialTheme.colorScheme.error
                )
                Text(
                    text = message,
                    style = MaterialTheme.typography.bodyLarge.copy(fontSize = 18.sp),
                    color = MaterialTheme.colorScheme.error,
                    textAlign = TextAlign.Center
                )
                FilledTonalButton(
                    onClick = onRetry,
                    modifier = Modifier
                        .height(40.dp)
                        .padding(top = 8.dp),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text(
                        stringResource(R.string.retry),
                        style = MaterialTheme.typography.labelLarge.copy(fontSize = 16.sp)
                    )
                }
            }
        }
    }
}

@Composable
private fun EmptyState() {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(32.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Card(
            shape = RoundedCornerShape(24.dp),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surfaceContainerLow
            ),
            border = BorderStroke(0.5.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.2f)),
            modifier = Modifier
                .fillMaxWidth()
                .shadow(8.dp, RoundedCornerShape(24.dp))
        ) {
            Column(
                modifier = Modifier.padding(20.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Icon(
                    Icons.Default.DocumentScanner,
                    contentDescription = null,
                    modifier = Modifier.size(64.dp),
                    tint = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Text(
                    text = stringResource(R.string.no_notes_found),
                    style = MaterialTheme.typography.titleMedium.copy(fontSize = 20.sp),
                    color = MaterialTheme.colorScheme.onSurface
                )
                Text(
                    text = stringResource(R.string.create_or_check_back),
                    style = MaterialTheme.typography.bodyMedium.copy(fontSize = 16.sp),
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    textAlign = TextAlign.Center
                )
            }
        }
    }
}

@Composable
private fun NotesGrid(
    notes: List<Note>,
    navController: NavController,
    viewModel: NotesViewModel
) {
    LazyVerticalGrid(
        columns = GridCells.Fixed(1),
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 20.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
        horizontalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        items(notes, key = { it.id }) { note ->
            NoteCard(
                note = note,
                onStarClick = { viewModel.starNote(note.id) },
                onClick = { navController.navigate("noteDetails/${note.id}") },
                index = notes.indexOf(note)
            )
        }
    }
}

@Composable
private fun NoteCard(
    note: Note,
    onStarClick: () -> Unit,
    onClick: () -> Unit,
    index: Int
) {
    var isVisible by remember { mutableStateOf(false) }
    LaunchedEffect(Unit) { isVisible = true }

    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()
    val scale by animateFloatAsState(
        targetValue = if (isPressed) 0.98f else 1f,
        animationSpec = tween(150),
        label = "Card Scale"
    )

    AnimatedVisibility(
        visible = isVisible,
        enter = fadeIn(tween(350, delayMillis = index * 100)) +
                slideInVertically(tween(350, delayMillis = index * 100)),
        exit = fadeOut(tween(350)) + slideOutVertically(tween(350))
    ) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .clickable(
                    interactionSource = interactionSource,
                    indication = null,
                    onClick = onClick
                )
                .scale(scale)
                .shadow(8.dp, RoundedCornerShape(24.dp)),
            shape = RoundedCornerShape(24.dp),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surfaceContainerLow
            ),
            border = BorderStroke(0.5.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.2f))
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(20.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = note.title,
                        style = MaterialTheme.typography.titleMedium.copy(fontSize = 22.sp),
                        color = MaterialTheme.colorScheme.onSurface,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier.weight(1f)
                    )
                    var isStarAnimating by remember { mutableStateOf(false) }
                    val starRotation by animateFloatAsState(
                        targetValue = if (isStarAnimating) 360f else 0f,
                        animationSpec = tween(350),
                        finishedListener = { isStarAnimating = false },
                        label = "Star Rotation"
                    )
                    val starInteractionSource = remember { MutableInteractionSource() }
                    val isStarPressed by starInteractionSource.collectIsPressedAsState()
                    val starScale by animateFloatAsState(
                        targetValue = if (isStarPressed) 0.95f else 1f,
                        animationSpec = tween(150),
                        label = "Star Scale"
                    )
                    IconButton(
                        onClick = {
                            isStarAnimating = true
                            onStarClick()
                        },
                        modifier = Modifier
                            .size(36.dp)
                            .scale(starScale)
                            .rotate(starRotation),
                        interactionSource = starInteractionSource
                    ) {
                        Icon(
                            Icons.Default.Star,
                            contentDescription = stringResource(R.string.star_note),
                            tint = if (note.starredBy.isNotEmpty()) {
                                Color.Yellow
                            } else {
                                MaterialTheme.colorScheme.onSurfaceVariant
                            },
                            modifier = Modifier.size(28.dp)
                        )
                    }
                }

                Text(
                    text = note.subject,
                    style = MaterialTheme.typography.bodyMedium.copy(fontSize = 16.sp),
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "By ${note.user.username}",
                        style = MaterialTheme.typography.labelMedium.copy(fontSize = 14.sp),
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier.weight(1f)
                    )
                    Text(
                        text = formatDate(note.createdAt),
                        style = MaterialTheme.typography.labelMedium.copy(fontSize = 14.sp),
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                Text(
                    text = note.topics.joinToString(", "),
                    style = MaterialTheme.typography.bodySmall.copy(fontSize = 12.sp),
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
        }
    }
}