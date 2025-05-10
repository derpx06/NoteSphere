package com.example.notesphere.ui.components

import android.Manifest
import android.net.Uri
import android.os.Build
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.animateContentSize
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Error
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import coil.compose.rememberAsyncImagePainter
import com.example.notesphere.R
import com.example.notesphere.utils.createImageUri
import com.example.notesphere.viewmodels.LoginViewModel
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProfileImageSection(
    viewModel: LoginViewModel,
    isEditable: Boolean = true
) {
    val sheetState = rememberModalBottomSheetState()
    val scope = rememberCoroutineScope()
    val context = LocalContext.current
    var tempImageUri by remember { mutableStateOf<Uri?>(null) }

    // Permission launchers
    val cameraPermissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) { permissions ->
        val cameraGranted = permissions[Manifest.permission.CAMERA] == true
        val storageGranted = permissions.getOrDefault(
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) Manifest.permission.READ_MEDIA_IMAGES
            else Manifest.permission.WRITE_EXTERNAL_STORAGE, true
        )
        if (cameraGranted && storageGranted) {
            tempImageUri = createImageUri(context)
            if (tempImageUri != null) {
                viewModel.takePictureLauncher?.launch(tempImageUri!!)
            } else {
                viewModel.showAlert("Failed to create image file")
            }
        } else {
            viewModel.showAlert("Camera and storage permissions required")
        }
    }

    val takePictureLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.TakePicture()
    ) { success ->
        if (success && tempImageUri != null) {
            viewModel.updateProfileImageUri(tempImageUri)
        } else {
            viewModel.showAlert("Failed to capture image")
        }
        tempImageUri = null
        viewModel.updateShowBottomSheet(false)
    }

    val pickImageLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.PickVisualMedia()
    ) { uri ->
        uri?.let(viewModel::updateProfileImageUri)
        viewModel.updateShowBottomSheet(false)
    }

    LaunchedEffect(Unit) {
        viewModel.takePictureLauncher = takePictureLauncher
        viewModel.pickImageLauncher = pickImageLauncher
    }

    Card(
        modifier = Modifier
            .size(100.dp)
            .clip(CircleShape)
            .shadow(8.dp, CircleShape)
            .clickable(enabled = isEditable) { viewModel.updateShowBottomSheet(true) },
        shape = CircleShape,
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        )
    ) {
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            if (viewModel.uiState.value.profileImageUri != null) {
                Image(
                    painter = rememberAsyncImagePainter(viewModel.uiState.value.profileImageUri),
                    contentDescription = "Profile Image",
                    contentScale = ContentScale.Crop,
                    modifier = Modifier.fillMaxSize()
                )
            } else {
                Icon(
                    painter = painterResource(R.drawable.ic_default_profile),
                    contentDescription = "Default Profile",
                    modifier = Modifier.size(48.dp),
                    tint = MaterialTheme.colorScheme.primary
                )
            }

            if (isEditable) {
                Box(
                    modifier = Modifier
                        .align(Alignment.BottomEnd)
                        .padding(8.dp)
                        .size(32.dp)
                        .clip(CircleShape)
                        .background(MaterialTheme.colorScheme.primary)
                        .clickable { viewModel.updateShowBottomSheet(true) },
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        painter = painterResource(R.drawable.ic_edit),
                        contentDescription = "Edit Profile Image",
                        tint = MaterialTheme.colorScheme.onPrimary,
                        modifier = Modifier.size(20.dp)
                    )
                }
            }
        }
    }

    if (viewModel.uiState.value.showBottomSheet) {
        ModalBottomSheet(
            onDismissRequest = { viewModel.updateShowBottomSheet(false) },
            sheetState = sheetState,
            shape = RoundedCornerShape(topStart = 16.dp, topEnd = 16.dp),
            containerColor = MaterialTheme.colorScheme.surface
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp)
                    .animateContentSize(),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = "Select Profile Photo",
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.onSurface,
                    modifier = Modifier.padding(bottom = 16.dp)
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceEvenly
                ) {
                    OutlinedButton(
                        onClick = {
                            scope.launch {
                                sheetState.hide()
                                val permissions = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                                    arrayOf(Manifest.permission.CAMERA, Manifest.permission.READ_MEDIA_IMAGES)
                                } else {
                                    arrayOf(Manifest.permission.CAMERA, Manifest.permission.WRITE_EXTERNAL_STORAGE)
                                }
                                cameraPermissionLauncher.launch(permissions)
                            }
                        },
                        modifier = Modifier
                            .weight(1f)
                            .padding(8.dp),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Icon(
                                painter = painterResource(R.drawable.ic_camera),
                                contentDescription = "Camera",
                                modifier = Modifier.size(32.dp)
                            )
                            Text(
                                text = "Camera",
                                style = MaterialTheme.typography.labelMedium,
                                modifier = Modifier.padding(top = 4.dp)
                            )
                        }
                    }

                    OutlinedButton(
                        onClick = {
                            scope.launch {
                                sheetState.hide()
                                viewModel.onGallerySelected()
                            }
                        },
                        modifier = Modifier
                            .weight(1f)
                            .padding(8.dp),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Icon(
                                painter = painterResource(R.drawable.ic_gallery),
                                contentDescription = "Gallery",
                                modifier = Modifier.size(32.dp)
                            )
                            Text(
                                text = "Gallery",
                                style = MaterialTheme.typography.labelMedium,
                                modifier = Modifier.padding(top = 4.dp)
                            )
                        }
                    }
                }
            }
        }
    }

    if (viewModel.uiState.value.showAlert) {
        AlertDialog(
            onDismissRequest = { viewModel.dismissAlert() },
            title = { Text("Notice", style = MaterialTheme.typography.titleMedium) },
            text = { Text(viewModel.uiState.value.alertMessage, style = MaterialTheme.typography.bodyMedium) },
            confirmButton = {
                TextButton(onClick = { viewModel.dismissAlert() }) {
                    Text("OK", style = MaterialTheme.typography.labelLarge)
                }
            },
            shape = RoundedCornerShape(12.dp),
            containerColor = MaterialTheme.colorScheme.surface,
            icon = {
                Icon(
                    imageVector = Icons.Default.Error,
                    contentDescription = "Error",
                    tint = MaterialTheme.colorScheme.error
                )
            }
        )
    }
}