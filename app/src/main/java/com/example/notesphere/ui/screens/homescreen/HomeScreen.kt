package com.example.notesphere.ui.screens.notes

import android.widget.Toast
import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.*
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
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
import com.google.accompanist.pager.*
import com.google.accompanist.swiperefresh.SwipeRefresh
import com.google.accompanist.swiperefresh.rememberSwipeRefreshState
import kotlinx.coroutines.launch

enum class SortOption {
    TITLE_ASC, TITLE_DESC, DATE_ASC, DATE_DESC, POPULARITY
}

enum class FilterType {
    TITLE, TOPIC, AUTHOR
}

@OptIn(ExperimentalPagerApi::class, ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(navController: NavController) {
    val viewModel: NotesViewModel = viewModel(factory = ViewModelFactory(LocalContext.current))
    val notes by viewModel.notes
    val isLoading by viewModel.isLoading
    val errorMessage by viewModel.errorMessage
    val userId by viewModel.userId
    val context = LocalContext.current
    val pagerState = rememberPagerState()
    val coroutineScope = rememberCoroutineScope()

    var searchQuery by remember { mutableStateOf("") }
    var selectedFilter by remember { mutableStateOf<FilterType?>(null) }
    var sortOption by remember { mutableStateOf(SortOption.DATE_DESC) }
    var showSortMenu by remember { mutableStateOf(false) }
    val focusRequester = remember { FocusRequester() }

    LaunchedEffect(errorMessage) {
        if (errorMessage.isNotEmpty()) {
            Toast.makeText(context, errorMessage, Toast.LENGTH_LONG).show()
        }
    }

    val filteredNotes = notes.filter {
        when (selectedFilter) {
            FilterType.TITLE -> it.title.contains(searchQuery, ignoreCase = true)
            FilterType.TOPIC -> it.topics.any { topic -> 
                topic.contains(searchQuery, ignoreCase = true)
            }
            FilterType.AUTHOR -> it.user.username.contains(searchQuery, ignoreCase = true)
            null -> it.title.contains(searchQuery, ignoreCase = true) ||
                    it.subject.contains(searchQuery, ignoreCase = true) ||
                    it.topics.any { topic -> topic.contains(searchQuery, ignoreCase = true) } ||
                    it.user.username.contains(searchQuery, ignoreCase = true)
        }
    }

    val sortedNotes = when (sortOption) {
        SortOption.TITLE_ASC -> filteredNotes.sortedBy { it.title }
        SortOption.TITLE_DESC -> filteredNotes.sortedByDescending { it.title }
        SortOption.DATE_ASC -> filteredNotes.sortedBy { it.createdAt }
        SortOption.DATE_DESC -> filteredNotes.sortedByDescending { it.createdAt }
        SortOption.POPULARITY -> filteredNotes.sortedByDescending { it.stars }
    }

    Scaffold(
        floatingActionButton = {
            FloatingActionButton(
                onClick = { navController.navigate("addNote") },
                modifier = Modifier.padding(bottom = 64.dp),
                containerColor = MaterialTheme.colorScheme.primary
            ) {
                Icon(Icons.Default.Add, "Add Note")
            }
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            SearchBar(
                searchQuery = searchQuery,
                onSearchQueryChange = { searchQuery = it },
                focusRequester = focusRequester,
                sortOption = sortOption,
                onSortOptionChange = { sortOption = it },
                showSortMenu = showSortMenu,
                onShowSortMenuChange = { showSortMenu = it },
                navController = navController,
                userId = userId
            )

            FilterChips(
                selectedFilter = selectedFilter,
                onFilterSelected = { selectedFilter = it }
            )

            HorizontalPager(
                count = 2,
                state = pagerState,
                modifier = Modifier.fillMaxSize()
            ) { page ->
                when (page) {
                    0 -> MainContent(
                        sortedNotes = sortedNotes,
                        isLoading = isLoading,
                        errorMessage = errorMessage,
                        onRetry = { viewModel.fetchNotes() },
                        navController = navController,
                        viewModel = viewModel
                    )
                    1 -> WorkspaceScreen()
                }
            }

            HorizontalPagerIndicator(
                pagerState = pagerState,
                modifier = Modifier
                    .align(Alignment.CenterHorizontally)
                    .padding(16.dp),
                activeColor = MaterialTheme.colorScheme.primary,
                inactiveColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.3f)
            )
        }
    }
}

@Composable
fun SearchBar(
    searchQuery: String,
    onSearchQueryChange: (String) -> Unit,
    focusRequester: FocusRequester,
    sortOption: SortOption,
    onSortOptionChange: (SortOption) -> Unit,
    showSortMenu: Boolean,
    onShowSortMenuChange: (Boolean) -> Unit,
    navController: NavController,
    userId: String?
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        OutlinedTextField(
            value = searchQuery,
            onValueChange = onSearchQueryChange,
            modifier = Modifier
                .weight(1f)
                .focusRequester(focusRequester),
            placeholder = { Text(stringResource(R.string.search_notes)) },
            leadingIcon = {
                Icon(
                    Icons.Default.Search,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.onSurfaceVariant
                )
            },
            trailingIcon = {
                AnimatedVisibility(
                    visible = searchQuery.isNotEmpty(),
                    enter = fadeIn() + expandHorizontally(),
                    exit = fadeOut() + shrinkHorizontally()
                ) {
                    IconButton(onClick = { onSearchQueryChange("") }) {
                        Icon(
                            Icons.Default.Close,
                            contentDescription = stringResource(R.string.clear)
                        )
                    }
                }
            },
            singleLine = true,
            shape = RoundedCornerShape(12.dp),
            colors = OutlinedTextFieldDefaults.colors(
                focusedContainerColor = MaterialTheme.colorScheme.surface,
                unfocusedContainerColor = MaterialTheme.colorScheme.surface
            )
        )

        Box {
            IconButton(
                onClick = { onShowSortMenuChange(true) },
                modifier = Modifier
                    .size(48.dp)
                    .clip(CircleShape)
                    .background(MaterialTheme.colorScheme.primaryContainer)
            ) {
                Icon(
                    Icons.Default.Sort,
                    contentDescription = stringResource(R.string.sort),
                    tint = MaterialTheme.colorScheme.onPrimaryContainer
                )
            }

            DropdownMenu(
                expanded = showSortMenu,
                onDismissRequest = { onShowSortMenuChange(false) }
            ) {
                SortOption.values().forEach { option ->
                    DropdownMenuItem(
                        text = { Text(getSortOptionLabel(option)) },
                        onClick = {
                            onSortOptionChange(option)
                            onShowSortMenuChange(false)
                        }
                    )
                }
            }
        }

        IconButton(
            onClick = {
                if (userId != null) {
                    navController.navigate("profile/$userId")
                } else {
                    navController.navigate("login")
                }
            },
            modifier = Modifier
                .size(48.dp)
                .clip(CircleShape)
                .background(MaterialTheme.colorScheme.primaryContainer)
        ) {
            Icon(
                Icons.Default.Person,
                contentDescription = stringResource(R.string.profile),
                tint = MaterialTheme.colorScheme.onPrimaryContainer
            )
        }
    }
}

@Composable
fun FilterChips(
    selectedFilter: FilterType?,
    onFilterSelected: (FilterType?) -> Unit
) {
    LazyRow(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        items(FilterType.values()) { filter ->
            FilterChip(
                selected = selectedFilter == filter,
                onClick = { onFilterSelected(if (selectedFilter == filter) null else filter) },
                label = { Text(getFilterLabel(filter)) },
                colors = FilterChipDefaults.filterChipColors(
                    selectedContainerColor = MaterialTheme.colorScheme.primaryContainer,
                    selectedLabelColor = MaterialTheme.colorScheme.onPrimaryContainer
                )
            )
        }
    }
}

@Composable
fun MainContent(
    sortedNotes: List<Note>,
    isLoading: Boolean,
    errorMessage: String,
    onRetry: () -> Unit,
    navController: NavController,
    viewModel: NotesViewModel
) {
    SwipeRefresh(
        state = rememberSwipeRefreshState(isLoading),
        onRefresh = onRetry
    ) {
        Box(modifier = Modifier.fillMaxSize()) {
            when {
                isLoading -> LoadingState()
                errorMessage.isNotEmpty() && sortedNotes.isEmpty() -> ErrorState(errorMessage, onRetry)
                sortedNotes.isEmpty() -> EmptyState()
                else -> NotesGrid(sortedNotes, navController, viewModel)
            }
        }
    }
}

@Composable
fun WorkspaceScreen() {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = "Workspace Coming Soon",
            style = MaterialTheme.typography.headlineMedium,
            color = MaterialTheme.colorScheme.onBackground
        )
    }
}

private fun getSortOptionLabel(option: SortOption): String = when (option) {
    SortOption.TITLE_ASC -> "Title (A-Z)"
    SortOption.TITLE_DESC -> "Title (Z-A)"
    SortOption.DATE_ASC -> "Oldest First"
    SortOption.DATE_DESC -> "Newest First"
    SortOption.POPULARITY -> "Most Popular"
}

private fun getFilterLabel(filter: FilterType): String = when (filter) {
    FilterType.TITLE -> "Title"
    FilterType.TOPIC -> "Topic"
    FilterType.AUTHOR -> "Author"
}

@Composable
private fun LoadingState() {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        CircularProgressIndicator(
            modifier = Modifier.size(48.dp),
            color = MaterialTheme.colorScheme.primary
        )
    }
}

@Composable
private fun ErrorState(message: String, onRetry: () -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = message,
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.error,
            textAlign = TextAlign.Center
        )
        Spacer(modifier = Modifier.height(16.dp))
        FilledTonalButton(onClick = onRetry) {
            Text("Retry")
        }
    }
}

@Composable
private fun EmptyState() {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = stringResource(R.string.no_notes_found),
            style = MaterialTheme.typography.titleMedium,
            color = MaterialTheme.colorScheme.onSurface
        )
        Text(
            text = stringResource(R.string.create_or_check_back),
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

@Composable
private fun NotesGrid(
    notes: List<Note>,
    navController: NavController,
    viewModel: NotesViewModel
) {
    LazyVerticalGrid(
        columns = GridCells.Adaptive(minSize = 300.dp),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
        horizontalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        items(notes, key = { it.id }) { note ->
            NoteCard(
                note = note,
                onStarClick = { viewModel.starNote(note.id) },
                onClick = { navController.navigate("noteDetails/${note.id}") }
            )
        }
    }
}

@Composable
private fun NoteCard(
    note: Note,
    onStarClick: () -> Unit,
    onClick: () -> Unit
) {
    val context = LocalContext.current
    val userId by remember(context) { mutableStateOf(AuthManager(context).getUserId()) }
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()
    val scale by animateFloatAsState(
        targetValue = if (isPressed) 0.98f else 1f,
        animationSpec = tween(150),
        label = "Card Scale"
    )

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(
                interactionSource = interactionSource,
                indication = null,
                onClick = onClick
            )
            .scale(scale),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 6.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = note.title,
                    style = MaterialTheme.typography.titleLarge,
                    color = MaterialTheme.colorScheme.onSurface,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.weight(1f)
                )
                IconButton(
                    onClick = {
                        if (userId == null) {
                            Toast.makeText(context, "Please log in to star notes", Toast.LENGTH_SHORT).show()
                        } else {
                            onStarClick()
                        }
                    }
                ) {
                    Icon(
                        Icons.Default.Star,
                        contentDescription = "Star",
                        tint = if (userId != null && note.starredBy.contains(userId)) Color.Yellow else MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
            Text(
                text = note.subject,
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.primary
            )
            val authorText = note.user?.let { "By ${it.username} (${it.college})" } ?: "By Unknown User"
            Text(
                text = authorText,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Text(
                text = "Semester: ${note.semester}",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = note.topics.joinToString(", "),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.weight(1f)
                )
                Text(
                    text = formatDate(note.createdAt),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            Row(
                horizontalArrangement = Arrangement.spacedBy(4.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    Icons.Default.Star,
                    contentDescription = null,
                    tint = Color.Yellow,
                    modifier = Modifier.size(16.dp)
                )
                Text(
                    text = "${note.stars} Stars",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}