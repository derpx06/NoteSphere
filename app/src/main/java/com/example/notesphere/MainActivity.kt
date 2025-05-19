package com.example.notesphere

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.platform.LocalContext
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.notesphere.ui.screens.homescreen.AddNoteScreen
import com.example.notesphere.ui.screens.login.LoginScreen
import com.example.notesphere.ui.screens.login.RegisterScreen
import com.example.notesphere.ui.screens.notes.HomeScreen
import com.example.notesphere.ui.screens.notes.NoteDetailsScreen
import com.example.notesphere.ui.screens.profile.ProfileScreen
import com.example.notesphere.ui.theme.NoteSphereTheme
import com.example.notesphere.utils.AuthManager

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val authManager = AuthManager(this)
        setContent {
            NoteSphereTheme {
                NoteSphereApp(authManager = authManager)
            }
        }
    }
}

@Composable
fun NoteSphereApp(authManager: AuthManager) {
    val navController = rememberNavController()

    LaunchedEffect(Unit) {
        if (!authManager.isLoggedIn()) {
            navController.navigate("login") {
                popUpTo(navController.graph.startDestinationId) { inclusive = true }
            }
        } else {
            navController.navigate("home") {
                popUpTo(navController.graph.startDestinationId) { inclusive = true }
            }
        }
    }

    NavHost(
        navController = navController,
        startDestination = "login"
    ) {
        composable("login") {
            LoginScreen(
                navController = navController,
                onLoginSuccess = {
                    navController.navigate("home") {
                        popUpTo("login") { inclusive = true }
                    }
                }
            )
        }
        composable("register") {
            RegisterScreen(navController = navController)
        }
        composable("home") {
            HomeScreen(navController = navController)
        }
        composable("addNote") {
            AddNoteScreen(navController = navController)
        }
        composable("noteDetails/{noteId}") { backStackEntry ->
            val noteId = backStackEntry.arguments?.getString("noteId") ?: ""
            NoteDetailsScreen(
                navController = navController,
                noteId = noteId,
                //context = LocalContext.current
            )
        }
        composable("profile/{userId}") { backStackEntry ->
            val userId = backStackEntry.arguments?.getString("userId") ?: ""
            ProfileScreen(
                navController = navController,
                userId = userId
            )
        }
    }
}

