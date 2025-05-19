package com.example.notesphere.ui.screens.profile

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import coil.compose.AsyncImage
import com.example.notesphere.R
import com.example.notesphere.data.User
import com.example.notesphere.network.RetrofitClient
import com.example.notesphere.utils.AuthManager
import com.example.notesphere.viewmodels.ProfileViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProfileScreen(
    navController: NavController,
    userId: String? = null
) {
    val context = LocalContext.current
    val authManager = AuthManager(context)
    val viewModel: ProfileViewModel = viewModel()
    val finalUserId = userId ?: authManager.getAuthenticatedUser()?.id

    LaunchedEffect(finalUserId) {
        finalUserId?.let {
            viewModel.loadUserProfile(it)
        } ?: run {
            viewModel.error = "No user ID provided"
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Profile") },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface,
                    titleContentColor = MaterialTheme.colorScheme.onSurface,
                    navigationIconContentColor = MaterialTheme.colorScheme.onSurface
                )
            )
        }
    ) { padding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            when {
                viewModel.isLoading -> LoadingState()
                viewModel.error != null -> ErrorState(
                    error = viewModel.error!!,
                    onRetry = { finalUserId?.let { viewModel.loadUserProfile(it) } }
                )
                viewModel.userProfile != null -> ProfileContent(
                    user = viewModel.userProfile!!,
                    isCurrentUser = userId == null
                )
            }
        }
    }
}

@Composable
private fun ProfileContent(user: User, isCurrentUser: Boolean) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 24.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Spacer(modifier = Modifier.height(32.dp))

        // Profile Image
        AsyncImage(
            model = user.profilePhotoPath?.let {
                "${RetrofitClient.BASE_URL}${it.removePrefix("/")}"
            },
            contentDescription = "Profile picture",
            modifier = Modifier
                .size(120.dp)
                .clip(CircleShape)
                .background(MaterialTheme.colorScheme.surfaceVariant),
            contentScale = ContentScale.Crop,
            error = painterResource(id = R.drawable.ic_default_profile),
            placeholder = painterResource(id = R.drawable.ic_default_profile)
        )

        Spacer(modifier = Modifier.height(24.dp))

        // User Name and Stars
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center
        ) {
            Text(
                text = user.username,
                style = MaterialTheme.typography.headlineMedium.copy(
                    fontSize = 24.sp,
                    fontWeight = FontWeight.SemiBold
                ),
                color = MaterialTheme.colorScheme.onSurface
            )
            Spacer(modifier = Modifier.width(8.dp))
            Icon(
                imageVector = Icons.Default.Star,
                contentDescription = null,
                tint = Color(0xFFFFD700),
                modifier = Modifier.size(24.dp)
            )
            Text(
                text = user.stars.toString(),
                style = MaterialTheme.typography.bodyLarge.copy(
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Medium
                ),
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }

        // User Details
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 24.dp),
            shape = MaterialTheme.shapes.medium
        ) {
            Column(
                modifier = Modifier.padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                DetailItem("Email", user.email)
                DetailItem("College", user.college)
                DetailItem("Role",user.role.toString())
                user.description?.let {
                    DetailItem("About", it)
                }
            }
        }

        Spacer(modifier = Modifier.height(32.dp))
    }
}

@Composable
private fun DetailItem(title: String, value: String) {
    Column(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        Text(
            text = title,
            style = MaterialTheme.typography.bodyMedium.copy(
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                fontWeight = FontWeight.Medium
            )
        )
        Text(
            text = value,
            style = MaterialTheme.typography.bodyLarge.copy(
                color = MaterialTheme.colorScheme.onSurface
            )
        )
    }
}

@Composable
private fun LoadingState() {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        CircularProgressIndicator(
            modifier = Modifier.size(48.dp),
            strokeWidth = 4.dp
        )
    }
}

@Composable
private fun ErrorState(error: String, onRetry: () -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(
            text = error,
            style = MaterialTheme.typography.bodyLarge.copy(
                color = MaterialTheme.colorScheme.error
            ),
            textAlign = TextAlign.Center
        )
        Spacer(modifier = Modifier.height(16.dp))
        Button(onClick = onRetry) {
            Text("Retry")
        }
    }
}