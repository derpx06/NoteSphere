package com.example.notesphere.ui.screens.login

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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
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
import com.example.notesphere.viewmodels.LoginViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LoginScreen(
    navController: NavController,
    onLoginSuccess: () -> Unit,
    onForgotPassword: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    val viewModel: LoginViewModel = viewModel()
    val uiState by viewModel.uiState
    var passwordVisible by remember { mutableStateOf(false) }
    var rememberMe by remember { mutableStateOf(false) }
    val keyboardController = LocalSoftwareKeyboardController.current

    // Animation state for card
    var isVisible by remember { mutableStateOf(false) }
    LaunchedEffect(Unit) {
        isVisible = true
    }

    Scaffold(
        topBar = {
            NoteSphereTopBar(navController = navController)
        },
        containerColor = MaterialTheme.colorScheme.background,
        modifier = modifier
    ) { padding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
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
                    LoginForm(
                        uiState = uiState,
                        onEmailChange = { viewModel.updateEmail(it) },
                        onPasswordChange = { viewModel.updatePassword(it) },
                        onPasswordVisibilityToggle = { passwordVisible = !passwordVisible },
                        isPasswordVisible = passwordVisible,
                        onRememberMeChange = { rememberMe = it },
                        isRememberMeChecked = rememberMe,
                        onLoginClick = {
                            keyboardController?.hide()
                            viewModel.login(onLoginSuccess)
                        },
                        onForgotPassword = onForgotPassword,
                        onRegisterClick = {
                            navController.navigate("register") {
                                popUpTo("login") { inclusive = false }
                            }
                        }
                    )
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NoteSphereTopBar(navController: NavController) {
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
                    }
                }) {
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
}

@Composable
fun LoginForm(
    uiState: UiState,
    onEmailChange: (String) -> Unit,
    onPasswordChange: (String) -> Unit,
    onPasswordVisibilityToggle: () -> Unit,
    isPasswordVisible: Boolean,
    onRememberMeChange: (Boolean) -> Unit,
    isRememberMeChecked: Boolean,
    onLoginClick: () -> Unit,
    onForgotPassword: () -> Unit,
    onRegisterClick: () -> Unit
) {
    Column(
        modifier = Modifier
            .padding(24.dp)
            .fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Profile Image (Editable)
        ProfileImageSection(viewModel = viewModel<LoginViewModel>(), isEditable = true)

        // Email Field
        CustomTextField(
            value = uiState.email,
            onValueChange = onEmailChange,
            label = "Email",
            isError = uiState.email.isNotEmpty() && !uiState.isEmailValid,
            trailingIcon = {
                if (uiState.email.isNotEmpty() && !uiState.isEmailValid) {
                    Icon(
                        imageVector = Icons.Default.Error,
                        contentDescription = "Invalid email",
                        tint = MaterialTheme.colorScheme.error
                    )
                }
            },
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email)
        )

        // Password Field with Toggle
        CustomTextField(
            value = uiState.password,
            onValueChange = onPasswordChange,
            label = "Password",
            visualTransformation = if (isPasswordVisible) VisualTransformation.None else PasswordVisualTransformation(),
            isError = uiState.password.isNotEmpty() && uiState.password.length < 6,
            trailingIcon = {
                IconButton(onClick = onPasswordVisibilityToggle) {
                    Icon(
                        imageVector = if (isPasswordVisible) Icons.Default.VisibilityOff else Icons.Default.Visibility,
                        contentDescription = if (isPasswordVisible) "Hide password" else "Show password",
                        tint = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            },
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password)
        )

        // Password Strength Indicator
        PasswordStrengthIndicator(password = uiState.password)

        // Remember Me Checkbox
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Checkbox(
                checked = isRememberMeChecked,
                onCheckedChange = onRememberMeChange,
                colors = CheckboxDefaults.colors(
                    checkedColor = MaterialTheme.colorScheme.primary
                )
            )
            Text(
                text = "Remember Me",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurface,
                modifier = Modifier.padding(start = 8.dp)
            )
        }

        // Error Message
        if (uiState.errorMessage.isNotEmpty()) {
            Text(
                text = uiState.errorMessage,
                color = MaterialTheme.colorScheme.error,
                style = MaterialTheme.typography.bodyMedium,
                modifier = Modifier.padding(horizontal = 8.dp)
            )
        }

        // Login Button with Animation
        val interactionSource = remember { MutableInteractionSource() }
        val isPressed by interactionSource.collectIsPressedAsState()
        val buttonScale by animateFloatAsState(
            targetValue = if (isPressed) 0.95f else 1f,
            animationSpec = spring(),
            label = "ButtonScale"
        )

        Button(
            onClick = onLoginClick,
            modifier = Modifier
                .fillMaxWidth()
                .height(56.dp)
                .clip(RoundedCornerShape(12.dp))
                .scale(buttonScale),
            enabled = !uiState.isLoading,
            colors = ButtonDefaults.buttonColors(
                containerColor = MaterialTheme.colorScheme.primary,
                contentColor = MaterialTheme.colorScheme.onPrimary
            ),
            interactionSource = interactionSource
        ) {
            if (uiState.isLoading) {
                CircularProgressIndicator(
                    color = MaterialTheme.colorScheme.onPrimary,
                    modifier = Modifier.size(24.dp)
                )
            } else {
                Text("Sign In", style = MaterialTheme.typography.titleMedium)
            }
        }

        // Forgot Password and Register Links
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            TextButton(onClick = onForgotPassword) {
                Text(
                    "Forgot Password?",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.primary
                )
            }
            TextButton(onClick = onRegisterClick) {
                Text(
                    "Create Account",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.primary
                )
            }
        }
    }
}

@Composable
fun PasswordStrengthIndicator(password: String) {
    val strength = when {
        password.length >= 12 -> "Strong"
        password.length >= 8 -> "Medium"
        password.isNotEmpty() -> "Weak"
        else -> ""
    }
    val color = when (strength) {
        "Strong" -> Color(0xFF4CAF50)
        "Medium" -> Color(0xFFFFC107)
        "Weak" -> Color(0xFFF44336)
        else -> MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)
    }

    if (strength.isNotEmpty()) {
        Text(
            text = "Password Strength: $strength",
            style = MaterialTheme.typography.bodySmall,
            color = color,
            modifier = Modifier.padding(horizontal = 8.dp)
        )
    }
}
data class UiState(
    val email: String = "",
    val password: String = "",
    val isEmailValid: Boolean = true,
    val errorMessage: String = "",
    val isLoading: Boolean = false,
    val showAlert: Boolean = false,
    val alertMessage: String = "",
    val profileImageUri: String? = null,
    val showBottomSheet: Boolean = false
)