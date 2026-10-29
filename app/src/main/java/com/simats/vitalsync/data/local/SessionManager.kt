package com.simats.vitalsync.data.local

import android.content.Context
import android.content.SharedPreferences
import com.google.gson.Gson
import com.simats.vitalsync.data.model.auth.User
import com.simats.vitalsync.data.network.RetrofitClient

/**
 * Manages user authentication session, SharedPreferences persistence,
 * and automatic logout based on inactivity timeout.
 */
class SessionManager(context: Context) {

    private val prefs: SharedPreferences =
        context.applicationContext.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)
    private val gson = Gson()

    companion object {
        private const val PREF_NAME = "vitalsync_auth_session"
        private const val KEY_IS_LOGGED_IN = "key_is_logged_in"
        private const val KEY_USER_JSON = "key_user_json"
        private const val KEY_USER_EMAIL = "key_user_email"
        private const val KEY_USER_NAME = "key_user_name"
        private const val KEY_CUSTOM_BASE_URL = "key_custom_base_url"

        // Auto Logout & Session Timestamp Keys
        private const val KEY_LOGIN_TIMESTAMP = "key_login_timestamp"
        private const val KEY_LAST_ACTIVE_TIMESTAMP = "key_last_active_timestamp"
        private const val KEY_AUTO_LOGOUT_ENABLED = "key_auto_logout_enabled"
        private const val KEY_SESSION_TIMEOUT_MINUTES = "key_session_timeout_minutes"
        private const val KEY_SESSION_EXPIRED_MESSAGE = "key_session_expired_message"

        // Default inactivity timeout: 30 minutes
        const val DEFAULT_TIMEOUT_MINUTES = 30L

        @Volatile
        private var INSTANCE: SessionManager? = null

        fun getInstance(context: Context): SessionManager {
            return INSTANCE ?: synchronized(this) {
                INSTANCE ?: SessionManager(context.applicationContext).also {
                    INSTANCE = it
                    // Sync custom base URL if configured previously
                    val savedUrl = it.getCustomBaseUrl()
                    if (!savedUrl.isNullOrBlank()) {
                        RetrofitClient.updateBaseUrl(savedUrl)
                    }
                }
            }
        }
    }

    /**
     * Save active user session with current timestamp.
     */
    fun saveUserSession(user: User) {
        val now = System.currentTimeMillis()
        prefs.edit()
            .putBoolean(KEY_IS_LOGGED_IN, true)
            .putString(KEY_USER_JSON, gson.toJson(user))
            .putString(KEY_USER_EMAIL, user.email)
            .putString(KEY_USER_NAME, user.displayName)
            .putLong(KEY_LOGIN_TIMESTAMP, now)
            .putLong(KEY_LAST_ACTIVE_TIMESTAMP, now)
            .remove(KEY_SESSION_EXPIRED_MESSAGE)
            .apply()
    }

    /**
     * Update the last user active timestamp.
     * Call this on user interaction or screen navigation.
     */
    fun updateLastActiveTime() {
        if (isLoggedIn()) {
            prefs.edit()
                .putLong(KEY_LAST_ACTIVE_TIMESTAMP, System.currentTimeMillis())
                .apply()
        }
    }

    /**
     * Check if the user session has expired due to inactivity.
     */
    fun isSessionExpired(): Boolean {
        if (!isLoggedIn()) return false
        if (!isAutoLogoutEnabled()) return false

        val lastActive = prefs.getLong(KEY_LAST_ACTIVE_TIMESTAMP, 0L)
        if (lastActive == 0L) return false

        val timeoutMillis = getSessionTimeoutMinutes() * 60 * 1000L
        val inactiveDuration = System.currentTimeMillis() - lastActive
        return inactiveDuration > timeoutMillis
    }

    /**
     * Checks if session expired, and if so, performs automatic logout.
     * Returns true if auto-logout was executed.
     */
    fun checkAndPerformAutoLogout(): Boolean {
        if (isSessionExpired()) {
            performAutoLogout("Your session has expired due to inactivity. Please log in again to continue.")
            return true
        }
        return false
    }

    /**
     * Executes automatic logout: clears active session and sets expired reason banner.
     */
    fun performAutoLogout(reasonMessage: String = "Session expired. Please log in again.") {
        prefs.edit()
            .putBoolean(KEY_IS_LOGGED_IN, false)
            .remove(KEY_USER_JSON)
            .remove(KEY_USER_EMAIL)
            .remove(KEY_USER_NAME)
            .remove(KEY_LOGIN_TIMESTAMP)
            .remove(KEY_LAST_ACTIVE_TIMESTAMP)
            .putString(KEY_SESSION_EXPIRED_MESSAGE, reasonMessage)
            .apply()
    }

    /**
     * Retrieves the session expired notice and clears it so it is shown only once.
     */
    fun consumeSessionExpiredMessage(): String? {
        val message = prefs.getString(KEY_SESSION_EXPIRED_MESSAGE, null)
        if (!message.isNullOrBlank()) {
            prefs.edit().remove(KEY_SESSION_EXPIRED_MESSAGE).apply()
        }
        return message
    }

    fun isAutoLogoutEnabled(): Boolean {
        return prefs.getBoolean(KEY_AUTO_LOGOUT_ENABLED, true)
    }

    fun setAutoLogoutEnabled(enabled: Boolean) {
        prefs.edit().putBoolean(KEY_AUTO_LOGOUT_ENABLED, enabled).apply()
    }

    fun getSessionTimeoutMinutes(): Long {
        return prefs.getLong(KEY_SESSION_TIMEOUT_MINUTES, DEFAULT_TIMEOUT_MINUTES)
    }

    fun setSessionTimeoutMinutes(minutes: Long) {
        prefs.edit().putLong(KEY_SESSION_TIMEOUT_MINUTES, minutes).apply()
    }

    fun getLoginTimestamp(): Long {
        return prefs.getLong(KEY_LOGIN_TIMESTAMP, 0L)
    }

    fun getLastActiveTimestamp(): Long {
        return prefs.getLong(KEY_LAST_ACTIVE_TIMESTAMP, 0L)
    }

    fun getUser(): User? {
        val json = prefs.getString(KEY_USER_JSON, null) ?: return null
        return try {
            gson.fromJson(json, User::class.java)
        } catch (_: Exception) {
            null
        }
    }

    fun getUserEmail(): String {
        return prefs.getString(KEY_USER_EMAIL, "") ?: ""
    }

    fun getUserName(): String {
        return prefs.getString(KEY_USER_NAME, "Alex Morgan") ?: "Alex Morgan"
    }

    fun isLoggedIn(): Boolean {
        return prefs.getBoolean(KEY_IS_LOGGED_IN, false)
    }

    /**
     * Explicit user logout (e.g., clicking Logout button in Profile screen).
     */
    fun clearSession() {
        prefs.edit()
            .putBoolean(KEY_IS_LOGGED_IN, false)
            .remove(KEY_USER_JSON)
            .remove(KEY_USER_EMAIL)
            .remove(KEY_USER_NAME)
            .remove(KEY_LOGIN_TIMESTAMP)
            .remove(KEY_LAST_ACTIVE_TIMESTAMP)
            .remove(KEY_SESSION_EXPIRED_MESSAGE)
            .apply()
    }

    fun setCustomBaseUrl(url: String) {
        prefs.edit().putString(KEY_CUSTOM_BASE_URL, url).apply()
        RetrofitClient.updateBaseUrl(url)
    }

    fun getCustomBaseUrl(): String? {
        return prefs.getString(KEY_CUSTOM_BASE_URL, null)
    }
}
