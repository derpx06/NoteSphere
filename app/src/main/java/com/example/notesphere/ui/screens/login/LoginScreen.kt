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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
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

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LoginScreen(
    navController: NavController,
    onLoginSuccess: () -> Unit,
    onForgotPassword: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val viewModel: LoginViewModel = viewModel(
        factory = ViewModelFactory(context = context)
    )
    Log.d("LoginScreen", "LoginViewModel created: $viewModel")
    val uiState by viewModel.uiState
    var passwordVisible by remember { mutableStateOf(false) }
    var rememberMe by remember { mutableStateOf(false) }
    val keyboardController = LocalSoftwareKeyboardController.current

    var isVisible by remember { mutableStateOf(false) }
    LaunchedEffect(Unit) {
        isVisible = true
    }

    Box(
        modifier = modifier
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
            AnimatedVisibility(
                visible = isVisible,
                enter = expandVertically(animationSpec = spring()),
                exit = shrinkVertically(animationSpec = spring())
            ) {
                Text(
                    text = "NoteSphere",
                    style = MaterialTheme.typography.headlineLarge.copy(fontSize = 36.sp),
                    color = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.padding(bottom = 24.dp, top = 15.dp)
                )
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
                        Column(
                            modifier = Modifier
                                .padding(24.dp)
                                .fillMaxWidth(),
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.spacedBy(16.dp)
                        ) {
                            Text(
                                text = "Sign In",
                                style = MaterialTheme.typography.headlineSmall.copy(fontSize = 28.sp),
                                color = MaterialTheme.colorScheme.primary
                            )

                            ProfileImageSection(viewModel = viewModel, isEditable = true)
                            Text(
                                text = "Profile photo is optional",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )

                            CustomTextField(
                                value = uiState.email,
                                onValueChange = { viewModel.updateEmail(it) },
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

                            CustomTextField(
                                value = uiState.password,
                                onValueChange = { viewModel.updatePassword(it) },
                                label = "Password",
                                visualTransformation = if (passwordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                                isError = uiState.password.isNotEmpty() && uiState.password.length < 6,
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

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Checkbox(
                                        checked = rememberMe,
                                        onCheckedChange = { rememberMe = it },
                                        colors = CheckboxDefaults.colors(
                                            checkedColor = MaterialTheme.colorScheme.primary
                                        )
                                    )
                                    Text(
                                        text = "Remember Me",
                                        style = MaterialTheme.typography.bodyMedium,
                                        color = MaterialTheme.colorScheme.onSurface
                                    )
                                }
                                TextButton(onClick = onForgotPassword) {
                                    Text(
                                        text = "Forgot Password?",
                                        style = MaterialTheme.typography.bodyMedium,
                                        color = MaterialTheme.colorScheme.primary
                                    )
                                }
                            }

                            if (uiState.errorMessage.isNotEmpty()) {
                                Text(
                                    text = uiState.errorMessage,
                                    color = MaterialTheme.colorScheme.error,
                                    style = MaterialTheme.typography.bodyMedium,
                                    modifier = Modifier.padding(horizontal = 8.dp)
                                )
                            }

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
                                    Log.d("LoginScreen", "Sign In clicked")
                                    viewModel.login(context) {
                                        Log.d("LoginScreen", "Login successful, navigating")
                                        onLoginSuccess()
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
                                enabled = !uiState.isLoading
                            ) {
                                if (uiState.isLoading) {
                                    CircularProgressIndicator(
                                        modifier = Modifier.size(24.dp),
                                        color = MaterialTheme.colorScheme.onPrimary,
                                        strokeWidth = 2.dp
                                    )
                                } else {
                                    Text(
                                        text = "Sign In",
                                        style = MaterialTheme.typography.titleMedium
                                    )
                                }
                            }

                            TextButton(
                                onClick = {
                                    Log.d("LoginScreen", "Navigating to register")
                                    navController.navigate("register") {
                                        popUpTo("login") { inclusive = false }
                                        launchSingleTop = true
                                    }
                                }
                            ) {
                                Text(
                                    text = "Don't have an account? Sign Up",
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = MaterialTheme.colorScheme.primary
                                )
                            }
                        }
                    }
                }
            }
        }

        if (uiState.showAlert) {
            AlertDialog(
                onDismissRequest = { viewModel.dismissAlert() },
                title = { Text("Notice", style = MaterialTheme.typography.titleMedium) },
                text = { Text(uiState.alertMessage, style = MaterialTheme.typography.bodyMedium) },
                confirmButton = {
                    TextButton(onClick = { viewModel.dismissAlert() }) {
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