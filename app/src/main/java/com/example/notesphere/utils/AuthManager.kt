package com.example.notesphere.utils

import android.content.Context
import androidx.core.content.edit
import androidx.security.crypto.EncryptedSharedPreferences
import androidx.security.crypto.MasterKeys
import java.util.concurrent.TimeUnit

class AuthManager(context: Context) {
    // Encrypted SharedPreferences for secure storage
    private val masterKeyAlias = MasterKeys.getOrCreate(MasterKeys.AES256_GCM_SPEC)
    private val prefs = EncryptedSharedPreferences.create(
        "NoteSpherePrefs",
        masterKeyAlias,
        context,
        EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
        EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM
    )

    // Keys for storing data
    private companion object {
        const val KEY_TOKEN = "auth_token"
        const val KEY_REFRESH_TOKEN = "refresh_token"
        const val KEY_TOKEN_EXPIRY = "token_expiry"
        const val KEY_USERNAME = "username"
        const val KEY_EMAIL = "email"
        const val KEY_ROLE = "role"
        const val KEY_COLLEGE = "college"
    }

    // Data class to represent user information
    data class User(
        val username: String,
        val email: String,
        val role: String,
        val college: String
    )

    /**
     * Saves the authentication token, refresh token, and token expiry time.
     * @param token The JWT or authentication token.
     * @param refreshToken Optional refresh token for token renewal.
     * @param expiresInSeconds Optional token expiration duration in seconds.
     */
    fun saveToken(token: String, refreshToken: String? = null, expiresInSeconds: Long? = null) {
        prefs.edit {
            putString(KEY_TOKEN, token)
            refreshToken?.let { putString(KEY_REFRESH_TOKEN, it) }
            expiresInSeconds?.let {
                val expiryTime = System.currentTimeMillis() + TimeUnit.SECONDS.toMillis(it)
                putLong(KEY_TOKEN_EXPIRY, expiryTime)
            }
        }
    }

    /**
     * Saves user information.
     * @param username The user's username.
     * @param email The user's email.
     * @param role The user's role (e.g., student, teacher, admin).
     * @param college The user's college name.
     */
    fun saveUserInfo(username: String, email: String, role: String, college: String) {
        prefs.edit {
            putString(KEY_USERNAME, username)
            putString(KEY_EMAIL, email)
            putString(KEY_ROLE, role)
            putString(KEY_COLLEGE, college)
        }
    }

    /**
     * Retrieves the authentication token if it exists and is not expired.
     * @return The token or null if it doesn't exist or is expired.
     */
    fun getToken(): String? {
        val token = prefs.getString(KEY_TOKEN, null)
        val expiryTime = prefs.getLong(KEY_TOKEN_EXPIRY, 0)
        return if (token != null && (expiryTime == 0L || System.currentTimeMillis() < expiryTime)) {
            token
        } else {
            null
        }
    }

    /**
     * Retrieves the refresh token.
     * @return The refresh token or null if it doesn't exist.
     */
    fun getRefreshToken(): String? {
        return prefs.getString(KEY_REFRESH_TOKEN, null)
    }

    /**
     * Retrieves the stored user information.
     * @return A User object or null if user data is incomplete.
     */
    fun getUserInfo(): User? {
        val username = prefs.getString(KEY_USERNAME, null)
        val email = prefs.getString(KEY_EMAIL, null)
        val role = prefs.getString(KEY_ROLE, null)
        val college = prefs.getString(KEY_COLLEGE, null)
        return if (username != null && email != null && role != null && college != null) {
            User(username, email, role, college)
        } else {
            null
        }
    }

    /**
     * Checks if the user is logged in (has a valid, non-expired token).
     * @return True if the user is logged in, false otherwise.
     */
    fun isLoggedIn(): Boolean {
        return getToken() != null
    }

    /**
     * Checks if the user has a specific role.
     * @param role The role to check (e.g., "student", "teacher", "admin").
     * @return True if the user has the specified role, false otherwise.
     */
    fun hasRole(role: String): Boolean {
        return prefs.getString(KEY_ROLE, null)?.equals(role, ignoreCase = true) == true
    }

    /**
     * Logs out the user by clearing all stored data (token, user info, etc.).
     */
    fun logout() {
        prefs.edit {
            clear()
        }
    }

    /**
     * Clears the authentication token and refresh token, retaining user info.
     * Useful for forcing a re-login without losing user data.
     */
    fun clearToken() {
        prefs.edit {
            remove(KEY_TOKEN)
            remove(KEY_REFRESH_TOKEN)
            remove(KEY_TOKEN_EXPIRY)
        }
    }
}