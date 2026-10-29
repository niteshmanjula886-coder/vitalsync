package com.simats.vitalsync.data.repository

import com.google.gson.Gson
import com.simats.vitalsync.data.local.SessionManager
import com.simats.vitalsync.data.model.auth.AuthResponse
import com.simats.vitalsync.data.model.auth.BaseResponse
import com.simats.vitalsync.data.model.auth.LoginRequest
import com.simats.vitalsync.data.model.auth.ResetPasswordRequest
import com.simats.vitalsync.data.model.auth.SignUpRequest
import com.simats.vitalsync.data.model.auth.User
import com.simats.vitalsync.data.network.AuthApiService
import com.simats.vitalsync.data.network.RetrofitClient
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn
import okhttp3.ResponseBody
import java.io.IOException

class AuthRepository(
    private val apiService: AuthApiService = RetrofitClient.authApiService,
    private val sessionManager: SessionManager? = null
) {

    private val gson = Gson()

    /**
     * Authenticate user with email and password
     */
    fun login(request: LoginRequest): Flow<Resource<User>> = flow {
        emit(Resource.Loading)
        try {
            val response = apiService.login(request)
            if (response.isSuccessful) {
                val body = response.body()
                if (body != null && body.success && body.user != null) {
                    sessionManager?.saveUserSession(body.user)
                    emit(Resource.Success(body.user))
                } else {
                    val message = body?.message?.ifBlank { null } ?: "Invalid credentials"
                    emit(Resource.Error(message, response.code()))
                }
            } else {
                val errorMsg = parseErrorMessage(response.errorBody()) ?: "Login failed (${response.code()})"
                emit(Resource.Error(errorMsg, response.code()))
            }
        } catch (e: IOException) {
            emit(Resource.Error("Network error. Please check your connection and ensure XAMPP Apache is running.", throwable = e))
        } catch (e: Exception) {
            emit(Resource.Error(e.localizedMessage ?: "An unexpected error occurred", throwable = e))
        }
    }.flowOn(Dispatchers.IO)

    /**
     * Register a new user account
     */
    fun signUp(request: SignUpRequest): Flow<Resource<User>> = flow {
        emit(Resource.Loading)
        try {
            val response = apiService.signUp(request)
            if (response.isSuccessful) {
                val body = response.body()
                if (body != null && body.success && body.user != null) {
                    sessionManager?.saveUserSession(body.user)
                    emit(Resource.Success(body.user))
                } else {
                    val message = body?.message?.ifBlank { null } ?: "Registration failed"
                    emit(Resource.Error(message, response.code()))
                }
            } else {
                val errorMsg = parseErrorMessage(response.errorBody()) ?: "Registration failed (${response.code()})"
                emit(Resource.Error(errorMsg, response.code()))
            }
        } catch (e: IOException) {
            emit(Resource.Error("Network error. Please check your connection and ensure XAMPP Apache is running.", throwable = e))
        } catch (e: Exception) {
            emit(Resource.Error(e.localizedMessage ?: "An unexpected error occurred", throwable = e))
        }
    }.flowOn(Dispatchers.IO)

    /**
     * Reset account password
     */
    fun resetPassword(request: ResetPasswordRequest): Flow<Resource<String>> = flow {
        emit(Resource.Loading)
        try {
            val response = apiService.resetPassword(request)
            if (response.isSuccessful) {
                val body = response.body()
                if (body != null && body.success) {
                    emit(Resource.Success(body.message.ifBlank { "Password reset successfully!" }))
                } else {
                    val message = body?.message?.ifBlank { null } ?: "Failed to reset password"
                    emit(Resource.Error(message, response.code()))
                }
            } else {
                val errorMsg = parseErrorMessage(response.errorBody()) ?: "Reset password failed (${response.code()})"
                emit(Resource.Error(errorMsg, response.code()))
            }
        } catch (e: IOException) {
            emit(Resource.Error("Network error. Please verify backend server connectivity.", throwable = e))
        } catch (e: Exception) {
            emit(Resource.Error(e.localizedMessage ?: "An unexpected error occurred", throwable = e))
        }
    }.flowOn(Dispatchers.IO)

    /**
     * Retrieve user profile
     */
    fun getProfile(email: String): Flow<Resource<User>> = flow {
        emit(Resource.Loading)
        try {
            val response = apiService.getProfile(email)
            if (response.isSuccessful) {
                val body = response.body()
                if (body != null && body.success && body.user != null) {
                    sessionManager?.saveUserSession(body.user)
                    emit(Resource.Success(body.user))
                } else {
                    emit(Resource.Error(body?.message ?: "Profile not found", response.code()))
                }
            } else {
                val errorMsg = parseErrorMessage(response.errorBody()) ?: "Failed to retrieve profile"
                emit(Resource.Error(errorMsg, response.code()))
            }
        } catch (e: IOException) {
            emit(Resource.Error("Network error connecting to backend.", throwable = e))
        } catch (e: Exception) {
            emit(Resource.Error(e.localizedMessage ?: "Unexpected error", throwable = e))
        }
    }.flowOn(Dispatchers.IO)

    fun getLoggedInUser(): User? = sessionManager?.getUser()

    fun isLoggedIn(): Boolean = sessionManager?.isLoggedIn() ?: false

    /**
     * Check if the session has expired due to inactivity, and if so, perform automatic logout.
     */
    fun checkSessionTimeout(): Boolean = sessionManager?.checkAndPerformAutoLogout() ?: false

    fun updateActivity() {
        sessionManager?.updateLastActiveTime()
    }

    fun consumeSessionExpiredMessage(): String? = sessionManager?.consumeSessionExpiredMessage()

    fun isAutoLogoutEnabled(): Boolean = sessionManager?.isAutoLogoutEnabled() ?: true

    fun setAutoLogoutEnabled(enabled: Boolean) {
        sessionManager?.setAutoLogoutEnabled(enabled)
    }

    fun getSessionTimeoutMinutes(): Long = sessionManager?.getSessionTimeoutMinutes() ?: 30L

    fun setSessionTimeoutMinutes(minutes: Long) {
        sessionManager?.setSessionTimeoutMinutes(minutes)
    }

    /**
     * Explicit user logout
     */
    fun logout() {
        sessionManager?.clearSession()
    }

    private fun parseErrorMessage(errorBody: ResponseBody?): String? {
        return try {
            val jsonString = errorBody?.string() ?: return null
            val baseResp = gson.fromJson(jsonString, BaseResponse::class.java)
            baseResp?.message?.ifBlank { null }
        } catch (_: Exception) {
            null
        }
    }
}
