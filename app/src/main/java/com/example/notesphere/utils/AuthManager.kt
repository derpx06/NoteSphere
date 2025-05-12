package com.example.notesphere.utils

import android.content.Context
import androidx.core.content.edit
import androidx.security.crypto.EncryptedSharedPreferences
import androidx.security.crypto.MasterKeys
import com.example.notesphere.data.User
import java.util.concurrent.TimeUnit

class AuthManager(context: Context) {
    private val masterKeyAlias = MasterKeys.getOrCreate(MasterKeys.AES256_GCM_SPEC)
    private val prefs = EncryptedSharedPreferences.create(
        "NoteSphereSecurePrefs",
        masterKeyAlias,
        context,
        EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
        EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM
    )

    private companion object {
        const val KEY_TOKEN = "auth_token"
        const val KEY_REFRESH_TOKEN = "refresh_token"
        const val KEY_TOKEN_EXPIRY = "token_expiry"
        const val KEY_USER_ID = "user_id"
        const val KEY_USERNAME = "username"
        const val KEY_EMAIL = "email"
        const val KEY_ROLE = "role"
        const val KEY_COLLEGE = "college"
        const val KEY_PROFILE_PHOTO = "profile_photo"
        const val KEY_DESCRIPTION = "description"
        const val KEY_STARS = "stars"
        const val KEY_SEMESTER = "semester"
    }

    fun saveAuthState(token: String, user: User, expiresInSeconds: Long = 3600) {
        prefs.edit {
            putString(KEY_TOKEN, token)
            putString(KEY_USER_ID, user.id)
            putString(KEY_USERNAME, user.username)
            putString(KEY_EMAIL, user.email)
            putString(KEY_ROLE, user.role)
            putString(KEY_COLLEGE, user.college)
            putString(KEY_PROFILE_PHOTO, user.profilePhotoPath)
            putString(KEY_DESCRIPTION, user.description)
            putInt(KEY_STARS, user.stars)
            putInt(KEY_SEMESTER, user.semester ?: 1)
            val expiryTime = System.currentTimeMillis() + TimeUnit.SECONDS.toMillis(expiresInSeconds)
            putLong(KEY_TOKEN_EXPIRY, expiryTime)
        }
    }
    fun getUserId(): String? {
        // Example: Retrieve user ID from SharedPreferences
        return prefs.getString("user_id", null)
        // Alternatively, if user ID is in the JWT token:
        // val token = getToken() ?: return null
        // return parseJwt(token)?.getString("userId")
    }    fun getAuthenticatedUser(): User? {
        return if (isLoggedIn()) {
            User(
                id = prefs.getString(KEY_USER_ID, null) ?: return null,
                username = prefs.getString(KEY_USERNAME, null) ?: return null,
                email = prefs.getString(KEY_EMAIL, null) ?: return null,
                college = prefs.getString(KEY_COLLEGE, null) ?: return null,
                role = prefs.getString(KEY_ROLE, null),
                profilePhotoPath = prefs.getString(KEY_PROFILE_PHOTO, null),
                description = prefs.getString(KEY_DESCRIPTION, null),
                stars = prefs.getInt(KEY_STARS, 0),
                semester = prefs.getInt(KEY_SEMESTER, 0).takeIf { it > 0 }
            )
        } else {
            null
        }
    }

    fun updateProfileInfo(
        profilePhotoPath: String? = null,
        description: String? = null,
        semester: Int? = null
    ) {
        prefs.edit {
            profilePhotoPath?.let { putString(KEY_PROFILE_PHOTO, it) }
            description?.let { putString(KEY_DESCRIPTION, it) }
            semester?.let { putInt(KEY_SEMESTER, it) }
        }
    }

    fun isLoggedIn(): Boolean {
        val token = prefs.getString(KEY_TOKEN, null)
        val expiryTime = prefs.getLong(KEY_TOKEN_EXPIRY, 0)
        return token != null && System.currentTimeMillis() < expiryTime
    }

    fun getToken(): String? {
        val token = prefs.getString(KEY_TOKEN, null)
        val expiryTime = prefs.getLong(KEY_TOKEN_EXPIRY, 0)
        return if (token != null && System.currentTimeMillis() < expiryTime) token else null
    }

    fun logout() {
        prefs.edit {
            clear()
            apply()
        }
    }

    fun getRefreshToken(): String? {
        return prefs.getString(KEY_REFRESH_TOKEN, null)
    }

    fun updateTokens(newToken: String, newRefreshToken: String, expiresInSeconds: Long) {
        prefs.edit {
            putString(KEY_TOKEN, newToken)
            putString(KEY_REFRESH_TOKEN, newRefreshToken)
            putLong(KEY_TOKEN_EXPIRY, System.currentTimeMillis() + TimeUnit.SECONDS.toMillis(expiresInSeconds))
        }
    }
}