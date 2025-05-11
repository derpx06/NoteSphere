package com.example.notesphere.ui.screens.login

import android.util.Log
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.expandVertically
import androidx.compose.animation.shrinkVertically
import androidx.compose.animation.togetherWith
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
import com.example.notesphere.utils.ViewModelFactory
import com.example.notesphere.viewmodels.LoginViewModel
import com.example.notesphere.viewmodels.RegisterViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RegisterScreen(navController: NavController) {
    val context = LocalContext.current
    val viewModel: RegisterViewModel = viewModel()
    val loginViewModel: LoginViewModel = viewModel(
        factory = ViewModelFactory(context = context)
    )
    Log.d("RegisterScreen", "ViewModels created: RegisterViewModel=$viewModel, LoginViewModel=$loginViewModel")
    val user by viewModel.user
    val errorMessage by viewModel.errorMessage
    val isLoading by viewModel.isLoading
    var passwordVisible by remember { mutableStateOf(false) }
    var confirmPassword by remember { mutableStateOf("") }
    var confirmPasswordError by remember { mutableStateOf(false) }
    val keyboardController = LocalSoftwareKeyboardController.current

    var isVisible by remember { mutableStateOf(false) }
    LaunchedEffect(Unit) {
        isVisible = true
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    colors = listOf(
                        MaterialTheme.colorScheme.primary.copy(alpha = 0.03f),
                        MaterialTheme.colorScheme.background
                    )
                )
            ),
        contentAlignment = Alignment.TopCenter
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(top = 32.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                AnimatedVisibility(
                    visible = isVisible,
                    enter = expandVertically(animationSpec = spring()),
                    exit = shrinkVertically(animationSpec = spring())
                ) {
                    IconButton(onClick = {
                        if (navController.previousBackStackEntry != null) {
                            navController.popBackStack()
                        } else {
                            navController.navigate("login") {
                                popUpTo("register") { inclusive = true }
                                launchSingleTop = true
                            }
                        }
                    }) {
                        Icon(
                            imageVector = Icons.Default.ArrowBack,
                            contentDescription = "Back",
                            tint = MaterialTheme.colorScheme.primary
                        )
                    }
                }
                AnimatedVisibility(
                    visible = isVisible,
                    enter = expandVertically(animationSpec = spring()),
                    exit = shrinkVertically(animationSpec = spring())
                ) {
                    Text(
                        text = "NoteSphere",
                        style = MaterialTheme.typography.headlineLarge.copy(fontSize = 36.sp),
                        color = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.padding(bottom = 24.dp, top = 10.dp)
                    )
                }
                Spacer(modifier = Modifier.width(48.dp))
            }

            AnimatedContent(
                targetState = isVisible,
                transitionSpec = {
                    expandVertically(animationSpec = spring()) togetherWith shrinkVertically(animationSpec = spring())
                },
                label = "CardAnimation"
            ) { visible ->
                if (visible) {
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 32.dp)
                            .shadow(4.dp, RoundedCornerShape(16.dp)),
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
                                Text(
                                    text = "Sign Up",
                                    style = MaterialTheme.typography.headlineSmall.copy(fontSize = 28.sp),
                                    color = MaterialTheme.colorScheme.primary
                                )
                            }

                            item {
                                ProfileImageSection(viewModel = loginViewModel, isEditable = true)
                                Text(
                                    text = "Profile photo is optional",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }

                            item {
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
                                RoleDropdown(
                                    selectedRole = user.role,
                                    onRoleSelected = { viewModel.updateRole(it) }
                                )
                            }

                            item {
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
                                        Log.d("RegisterScreen", "Sign Up clicked")
                                        if (viewModel.validateRegistration(confirmPassword)) {
                                            viewModel.register {
                                                Log.d("RegisterScreen", "Registration successful, attempting login")
                                                loginViewModel.updateEmail(user.email)
                                                loginViewModel.updatePassword(user.password)
                                                loginViewModel.login(context) {
                                                    Log.d("RegisterScreen", "Login successful, navigating to home")
                                                    navController.navigate("home") {
                                                        popUpTo("register") { inclusive = true }
                                                        launchSingleTop = true
                                                    }
                                                }
                                            }
                                        }
                                    },
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(48.dp)
                                        .clip(RoundedCornerShape(12.dp))
                                        .scale(buttonScale),
                                    colors = ButtonDefaults.buttonColors(
                                        containerColor = MaterialTheme.colorScheme.primary,
                                        contentColor = MaterialTheme.colorScheme.onPrimary
                                    ),
                                    interactionSource = interactionSource,
                                    enabled = !isLoading
                                ) {
                                    if (isLoading) {
                                        CircularProgressIndicator(
                                            modifier = Modifier.size(24.dp),
                                            color = MaterialTheme.colorScheme.onPrimary,
                                            strokeWidth = 2.dp
                                        )
                                    } else {
                                        Text(
                                            text = "Sign Up",
                                            style = MaterialTheme.typography.titleMedium
                                        )
                                    }
                                }
                            }

                            item {
                                TextButton(
                                    onClick = {
                                        Log.d("RegisterScreen", "Navigating to login")
                                        navController.navigate("login") {
                                            popUpTo("register") { inclusive = true }
                                            launchSingleTop = true
                                        }
                                    }
                                ) {
                                    Text(
                                        text = "Already have an account? Sign In",
                                        style = MaterialTheme.typography.bodyMedium,
                                        color = MaterialTheme.colorScheme.primary
                                    )
                                }
                            }
                        }
                    }
                }
            }

            if (loginViewModel.uiState.value.showAlert) {
                AlertDialog(
                    onDismissRequest = { loginViewModel.dismissAlert() },
                    title = { Text("Notice", style = MaterialTheme.typography.titleMedium) },
                    text = { Text(loginViewModel.uiState.value.alertMessage, style = MaterialTheme.typography.bodyMedium) },
                    confirmButton = {
                        TextButton(onClick = { loginViewModel.dismissAlert() }) {
                            Text("OK", style = MaterialTheme.typography.labelLarge)
                        }
                    },
                    shape = RoundedCornerShape(12.dp),
                    containerColor = MaterialTheme.colorScheme.surface,
                    tonalElevation = 4.dp
                )
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