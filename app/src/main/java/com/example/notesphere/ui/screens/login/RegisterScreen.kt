package com.example.notesphere.ui.screens.login

import android.net.Uri
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.foundation.background
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.ArrowDropUp
import androidx.compose.material.icons.filled.Error
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.example.notesphere.ui.components.CustomTextField
import com.example.notesphere.ui.components.ProfileImageSection
import com.example.notesphere.utils.uriToMultipart
import com.example.notesphere.viewmodels.LoginViewModel
import com.example.notesphere.viewmodels.RegisterViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RegisterScreen(navController: NavController) {
    val viewModel: RegisterViewModel = viewModel()
    val loginViewModel: LoginViewModel = viewModel()
    val user by viewModel.user
    val errorMessage by viewModel.errorMessage
    var passwordVisible by remember { mutableStateOf(false) }
    var confirmPassword by remember { mutableStateOf("") }
    var confirmPasswordError by remember { mutableStateOf(false) }
    val keyboardController = LocalSoftwareKeyboardController.current
    val scope = rememberCoroutineScope()
    val context = LocalContext.current

    // Animation state for card
    var isVisible by remember { mutableStateOf(false) }
    LaunchedEffect(Unit) {
        isVisible = true
    }

    Scaffold(
        topBar = {
            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(80.dp),
                shape = RoundedCornerShape(bottomStart = 24.dp, bottomEnd = 24.dp),
                color = MaterialTheme.colorScheme.primary,
                shadowElevation = 8.dp
            ) {
                CenterAlignedTopAppBar(
                    title = {
                        Text(
                            text = "NoteSphere",
                            style = MaterialTheme.typography.headlineSmall.copy(fontSize = 26.sp),
                            color = MaterialTheme.colorScheme.onPrimary,
                            maxLines = 1
                        )
                    },
                    navigationIcon = {
                        IconButton(onClick = {
                            if (navController.previousBackStackEntry != null) {
                                navController.popBackStack()
                            } else {
                                navController.navigate("login") {
                                    popUpTo("register") { inclusive = true }
                                }
                            }
                        })
                        {
                            Icon(
                                imageVector = Icons.Default.ArrowBack,
                                contentDescription = "Back",
                                tint = MaterialTheme.colorScheme.onPrimary
                            )
                        }
                    },
                    colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
                        containerColor = Color.Transparent
                    )
                )
            }
        },
        containerColor = MaterialTheme.colorScheme.background
    ) { padding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(5.dp)
                .background(
                    Brush.verticalGradient(
                        colors = listOf(
                            MaterialTheme.colorScheme.primary.copy(alpha = 0.1f),
                            MaterialTheme.colorScheme.secondary.copy(alpha = 0.2f),
                            MaterialTheme.colorScheme.primary.copy(alpha = 0.05f)
                        )
                    )
                ),
            contentAlignment = Alignment.Center
        ) {
            AnimatedVisibility(
                visible = isVisible,
                enter = fadeIn(animationSpec = tween(800)) + scaleIn(animationSpec = tween(800)),
                exit = fadeOut(animationSpec = tween(400))
            ) {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 24.dp)
                        .shadow(16.dp, RoundedCornerShape(16.dp)),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surface,
                        contentColor = MaterialTheme.colorScheme.onSurface
                    )
                ) {
                    LazyColumn(
                        modifier = Modifier
                            .padding(24.dp)
                            .fillMaxWidth(),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        item {
                            // Profile Image (Optional)
                            ProfileImageSection(viewModel = loginViewModel, isEditable = true)
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                text = "Profile photo is optional",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }

                        item {
                            // Username Field
                            CustomTextField(
                                value = user.username,
                                onValueChange = { viewModel.updateUsername(it) },
                                label = "Username",
                                isError = user.username.isNotEmpty() && user.username.length < 3,
                                trailingIcon = {
                                    if (user.username.isNotEmpty() && user.username.length < 3) {
                                        Icon(
                                            imageVector = Icons.Default.Error,
                                            contentDescription = "Invalid username",
                                            tint = MaterialTheme.colorScheme.error
                                        )
                                    }
                                },
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Text)
                            )
                        }

                        item {
                            // Email Field
                            CustomTextField(
                                value = user.email,
                                onValueChange = { viewModel.updateEmail(it) },
                                label = "Email",
                                isError = user.email.isNotEmpty() && !isValidEmail(user.email),
                                trailingIcon = {
                                    if (user.email.isNotEmpty() && !isValidEmail(user.email)) {
                                        Icon(
                                            imageVector = Icons.Default.Error,
                                            contentDescription = "Invalid email",
                                            tint = MaterialTheme.colorScheme.error
                                        )
                                    }
                                },
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email)
                            )
                        }

                        item {
                            // Password Field
                            CustomTextField(
                                value = user.password,
                                onValueChange = { viewModel.updatePassword(it) },
                                label = "Password",
                                visualTransformation = if (passwordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                                isError = user.password.isNotEmpty() && user.password.length < 6,
                                trailingIcon = {
                                    IconButton(onClick = { passwordVisible = !passwordVisible }) {
                                        Icon(
                                            imageVector = if (passwordVisible) Icons.Default.VisibilityOff else Icons.Default.Visibility,
                                            contentDescription = if (passwordVisible) "Hide password" else "Show password",
                                            tint = MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                    }
                                },
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password)
                            )
                        }

                        item {
                            // Confirm Password Field
                            CustomTextField(
                                value = confirmPassword,
                                onValueChange = {
                                    confirmPassword = it
                                    confirmPasswordError = it != user.password
                                },
                                label = "Confirm Password",
                                visualTransformation = if (passwordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                                isError = confirmPasswordError,
                                trailingIcon = {
                                    if (confirmPasswordError) {
                                        Icon(
                                            imageVector = Icons.Default.Error,
                                            contentDescription = "Passwords do not match",
                                            tint = MaterialTheme.colorScheme.error
                                        )
                                    }
                                },
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password)
                            )
                        }

                        item {
                            // Password Strength Indicator
                            if (user.password.isNotEmpty()) {
                                val strength = when {
                                    user.password.length >= 12 -> "Strong"
                                    user.password.length >= 8 -> "Medium"
                                    else -> "Weak"
                                }
                                val color = when (strength) {
                                    "Strong" -> Color(0xFF4CAF50)
                                    "Medium" -> Color(0xFFFFC107)
                                    "Weak" -> Color(0xFFF44336)
                                    else -> MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)
                                }
                                Text(
                                    text = "Password Strength: $strength",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = color,
                                    modifier = Modifier.padding(horizontal = 8.dp)
                                )
                            }
                        }

                        item {
                            // Role Dropdown
                            RoleDropdown(
                                selectedRole = user.role,
                                onRoleSelected = { viewModel.updateRole(it) }
                            )
                        }

                        item {
                            // College/University Field
                            CustomTextField(
                                value = user.college,
                                onValueChange = { viewModel.updateCollege(it) },
                                label = "College/University",
                                isError = user.college.isNotEmpty() && user.college.length < 3,
                                trailingIcon = {
                                    if (user.college.isNotEmpty() && user.college.length < 3) {
                                        Icon(
                                            imageVector = Icons.Default.Error,
                                            contentDescription = "Invalid college name",
                                            tint = MaterialTheme.colorScheme.error
                                        )
                                    }
                                },
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Text)
                            )
                        }

                        item {
                            // Error Message
                            if (errorMessage.isNotEmpty()) {
                                Text(
                                    text = errorMessage,
                                    color = MaterialTheme.colorScheme.error,
                                    style = MaterialTheme.typography.bodyMedium,
                                    modifier = Modifier.padding(horizontal = 8.dp)
                                )
                            }
                        }

                        item {
                            // Sign Up Button
                            val interactionSource = remember { MutableInteractionSource() }
                            val isPressed by interactionSource.collectIsPressedAsState()
                            val buttonScale by animateFloatAsState(
                                targetValue = if (isPressed) 0.95f else 1f,
                                animationSpec = spring(),
                                label = "ButtonScale"
                            )

                            Button(
                                onClick = {
                                    keyboardController?.hide()
                                    if (viewModel.validateRegistration(confirmPassword)) {
                                        // Simulate profile photo upload
                                        loginViewModel.uiState.value.profileImageUri?.let { uriString ->
                                            val uri = Uri.parse(uriString)
                                            uriToMultipart(context, uri) // Simulate upload
                                        }
                                        scope.launch {
                                            loginViewModel.showAlert("Registration successful!")
                                            delay(1000)
                                            navController.navigate("home") {
                                                popUpTo("register") { inclusive = true }
                                            }
                                        }
                                    }
                                },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(56.dp)
                                    .clip(RoundedCornerShape(12.dp))
                                    .scale(buttonScale),
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = MaterialTheme.colorScheme.primary,
                                    contentColor = MaterialTheme.colorScheme.onPrimary
                                ),
                                interactionSource = interactionSource
                            ) {
                                Text(
                                    text = "Sign Up",
                                    style = MaterialTheme.typography.titleMedium
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RoleDropdown(
    selectedRole: String,
    onRoleSelected: (String) -> Unit
) {
    var expanded by remember { mutableStateOf(false) }
    val roles = listOf("Student", "Teacher", "Other")

    ExposedDropdownMenuBox(
        expanded = expanded,
        onExpandedChange = { expanded = !expanded }
    ) {
        OutlinedTextField(
            value = selectedRole,
            onValueChange = {},
            readOnly = true,
            label = { Text("Role", style = MaterialTheme.typography.bodyMedium) },
            trailingIcon = {
                Icon(
                    imageVector = if (expanded) Icons.Default.ArrowDropUp else Icons.Default.ArrowDropDown,
                    contentDescription = "Dropdown",
                    tint = MaterialTheme.colorScheme.onSurfaceVariant
                )
            },
            modifier = Modifier
                .fillMaxWidth()
                .menuAnchor(),
            shape = RoundedCornerShape(12.dp),
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = MaterialTheme.colorScheme.primary,
                unfocusedBorderColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f),
                focusedContainerColor = MaterialTheme.colorScheme.surface,
                unfocusedContainerColor = MaterialTheme.colorScheme.surface
            ),
            textStyle = MaterialTheme.typography.bodyLarge
        )
        ExposedDropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false },
            modifier = Modifier.background(MaterialTheme.colorScheme.surface)
        ) {
            roles.forEach { role ->
                DropdownMenuItem(
                    text = { Text(role, style = MaterialTheme.typography.bodyLarge) },
                    onClick = {
                        onRoleSelected(role)
                        expanded = false
                    },
                    contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp)
                )
            }
        }
    }
}

private fun isValidEmail(email: String): Boolean {
    return email.matches(Regex("[a-zA-Z0-9._-]+@[a-zA-Z0-9.-]+\\.[a-zA-Z]{2,}"))
}