package com.simats.vitalsync.ui.auth.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.simats.vitalsync.data.model.auth.ChangePasswordRequest
import com.simats.vitalsync.data.model.auth.LoginRequest
import com.simats.vitalsync.data.model.auth.ResetPasswordRequest
import com.simats.vitalsync.data.model.auth.SignUpRequest
import com.simats.vitalsync.data.model.auth.User
import com.simats.vitalsync.data.network.ApiConstants
import com.simats.vitalsync.data.repository.AuthRepository
import com.simats.vitalsync.data.repository.Resource
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

private val EMAIL_REGEX = Regex("^[A-Za-z0-9._%+-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$")

/**
 * ViewModel governing all authentication interactions in the VitalSync app.
 * Follows the Android MVVM architecture pattern.
 */
class AuthViewModel(
    private val authRepository: AuthRepository
) : ViewModel() {

    private val _loginState = MutableStateFlow(AuthUiState())
    val loginState: StateFlow<AuthUiState> = _loginState.asStateFlow()

    private val _signUpState = MutableStateFlow(AuthUiState())
    val signUpState: StateFlow<AuthUiState> = _signUpState.asStateFlow()

    private val _resetPasswordState = MutableStateFlow(AuthUiState())
    val resetPasswordState: StateFlow<AuthUiState> = _resetPasswordState.asStateFlow()

    private val _changePasswordState = MutableStateFlow(AuthUiState())
    val changePasswordState: StateFlow<AuthUiState> = _changePasswordState.asStateFlow()

    private val _sessionExpiredNotice = MutableStateFlow<String?>(null)
    val sessionExpiredNotice: StateFlow<String?> = _sessionExpiredNotice.asStateFlow()

    private val _currentUser = MutableStateFlow<User?>(authRepository.getLoggedInUser())
    val currentUser: StateFlow<User?> = _currentUser.asStateFlow()

    init {
        // Check for any pending session expired message on initialization
        val expiredMsg = authRepository.consumeSessionExpiredMessage()
        if (!expiredMsg.isNullOrBlank()) {
            _sessionExpiredNotice.value = expiredMsg
        }
    }

    /**
     * Checks if the session has expired due to inactivity.
     * If expired, triggers auto-logout and updates sessionExpiredNotice.
     */
    fun checkSessionTimeout(): Boolean {
        val expired = authRepository.checkSessionTimeout()
        if (expired) {
            _sessionExpiredNotice.value = "Your session has expired due to inactivity. Please log in again to continue."
            resetLoginState()
            resetSignUpState()
            resetPasswordState()
        }
        return expired
    }

    /**
     * Records user activity to keep the session alive.
     */
    fun recordActivity() {
        authRepository.updateActivity()
    }

    /**
     * Consumes and dismisses the session expired notice.
     */
    fun clearSessionExpiredNotice() {
        _sessionExpiredNotice.value = null
    }

    fun isAutoLogoutEnabled(): Boolean = authRepository.isAutoLogoutEnabled()

    fun setAutoLogoutEnabled(enabled: Boolean) {
        authRepository.setAutoLogoutEnabled(enabled)
    }

    fun getSessionTimeoutMinutes(): Long = authRepository.getSessionTimeoutMinutes()

    fun setSessionTimeoutMinutes(minutes: Long) {
        authRepository.setSessionTimeoutMinutes(minutes)
    }

    fun login(email: String, password: String, deviceName: String = "Android Device") {
        if (email.isBlank()) {
            _loginState.update { it.copy(errorMessage = "Email cannot be empty") }
            return
        }
        if (!email.contains("@")) {
            _loginState.update { it.copy(errorMessage = "Please enter a valid email address") }
            return
        }
        if (password.isBlank()) {
            _loginState.update { it.copy(errorMessage = "Password cannot be empty") }
            return
        }

        viewModelScope.launch {
            try {
                authRepository.login(
                    LoginRequest(
                        email = email.trim(),
                        password = password,
                        deviceName = deviceName
                    )
                ).collect { resource ->
                    when (resource) {
                        is Resource.Loading -> {
                            _loginState.update {
                                it.copy(isLoading = true, errorMessage = null, isSuccess = false)
                            }
                        }
                        is Resource.Success -> {
                            _sessionExpiredNotice.value = null
                            _currentUser.value = resource.data
                            _loginState.update {
                                it.copy(
                                    isLoading = false,
                                    isSuccess = true,
                                    errorMessage = null,
                                    user = resource.data
                                )
                            }
                        }
                        is Resource.Error -> {
                            _loginState.update {
                                it.copy(
                                    isLoading = false,
                                    isSuccess = false,
                                    errorMessage = resource.message
                                )
                            }
                        }
                    }
                }
            } catch (e: Exception) {
                _loginState.update { it.copy(isLoading = false, errorMessage = ApiConstants.resolveNetworkErrorMessage(e)) }
            } finally {
                _loginState.update { it.copy(isLoading = false) }
            }
        }
    }

    fun signUp(
        name: String,
        email: String,
        password: String,
        confirmPassword: String,
        phone: String? = null,
        dob: String? = null,
        age: Int? = null,
        gender: String? = null,
        height: Float? = null,
        weight: Float? = null,
        activityLevel: String? = null,
        primaryGoal: String? = "Fitness"
    ) {
        if (name.trim().isBlank()) {
            _signUpState.update { it.copy(errorMessage = "Please enter your full name") }
            return
        }
        if (email.trim().isBlank() || !EMAIL_REGEX.matches(email.trim())) {
            _signUpState.update { it.copy(errorMessage = "Please enter a valid email address (e.g., name@example.com)") }
            return
        }
        if (password.length < 6) {
            _signUpState.update { it.copy(errorMessage = "Password must be at least 6 characters") }
            return
        }
        if (password != confirmPassword) {
            _signUpState.update { it.copy(errorMessage = "Passwords do not match") }
            return
        }

        viewModelScope.launch {
            try {
                authRepository.signUp(
                    SignUpRequest(
                        name = name.trim(),
                        email = email.trim(),
                        password = password,
                        confirmPassword = confirmPassword,
                        phone = phone,
                        dob = dob,
                        age = age,
                        gender = gender,
                        height = height,
                        weight = weight,
                        activityLevel = activityLevel,
                        primaryGoal = primaryGoal
                    )
                ).collect { resource ->
                    when (resource) {
                        is Resource.Loading -> {
                            _signUpState.update {
                                it.copy(isLoading = true, errorMessage = null, isSuccess = false)
                            }
                        }
                        is Resource.Success -> {
                            _sessionExpiredNotice.value = null
                            _currentUser.value = resource.data
                            _signUpState.update {
                                it.copy(
                                    isLoading = false,
                                    isSuccess = true,
                                    errorMessage = null,
                                    user = resource.data
                                )
                            }
                        }
                        is Resource.Error -> {
                            _signUpState.update {
                                it.copy(
                                    isLoading = false,
                                    isSuccess = false,
                                    errorMessage = resource.message
                                )
                            }
                        }
                    }
                }
            } catch (e: Exception) {
                _signUpState.update { it.copy(isLoading = false, errorMessage = ApiConstants.resolveNetworkErrorMessage(e)) }
            } finally {
                _signUpState.update { it.copy(isLoading = false) }
            }
        }
    }

    fun resetPassword(email: String, newPassword: String, confirmPassword: String) {
        if (email.isBlank() || !email.contains("@")) {
            _resetPasswordState.update { it.copy(errorMessage = "Please enter a valid email address") }
            return
        }
        if (newPassword.length < 6) {
            _resetPasswordState.update { it.copy(errorMessage = "Password must be at least 6 characters") }
            return
        }
        if (newPassword != confirmPassword) {
            _resetPasswordState.update { it.copy(errorMessage = "Passwords do not match") }
            return
        }

        viewModelScope.launch {
            try {
                authRepository.resetPassword(
                    ResetPasswordRequest(
                        email = email.trim(),
                        newPassword = newPassword,
                        confirmPassword = confirmPassword
                    )
                ).collect { resource ->
                    when (resource) {
                        is Resource.Loading -> {
                            _resetPasswordState.update {
                                it.copy(isLoading = true, errorMessage = null, isSuccess = false)
                            }
                        }
                        is Resource.Success -> {
                            _resetPasswordState.update {
                                it.copy(
                                    isLoading = false,
                                    isSuccess = true,
                                    successMessage = resource.data,
                                    errorMessage = null
                                )
                            }
                        }
                        is Resource.Error -> {
                            _resetPasswordState.update {
                                it.copy(
                                    isLoading = false,
                                    isSuccess = false,
                                    errorMessage = resource.message
                                )
                            }
                        }
                    }
                }
            } catch (e: Exception) {
                _resetPasswordState.update { it.copy(isLoading = false, errorMessage = ApiConstants.resolveNetworkErrorMessage(e)) }
            } finally {
                _resetPasswordState.update { it.copy(isLoading = false) }
            }
        }
    }

    fun changePassword(
        email: String,
        loggedInEmail: String,
        currentPassword: String? = null,
        newPassword: String,
        confirmPassword: String,
        onSuccess: (String) -> Unit = {}
    ) {
        val trimmedEmail = email.trim()
        val trimmedLoggedIn = loggedInEmail.trim()
        if (trimmedEmail.isBlank()) {
            _changePasswordState.update { it.copy(errorMessage = "Please enter your account email") }
            return
        }
        if (!trimmedEmail.equals(trimmedLoggedIn, ignoreCase = true)) {
            _changePasswordState.update { it.copy(errorMessage = "Email does not match your currently logged-in account") }
            return
        }
        if (newPassword.isBlank()) {
            _changePasswordState.update { it.copy(errorMessage = "New password cannot be empty") }
            return
        }
        if (newPassword.length < 6) {
            _changePasswordState.update { it.copy(errorMessage = "New password must be at least 6 characters") }
            return
        }
        if (newPassword != confirmPassword) {
            _changePasswordState.update { it.copy(errorMessage = "Passwords do not match") }
            return
        }

        viewModelScope.launch {
            try {
                authRepository.changePassword(
                    ChangePasswordRequest(
                        email = trimmedEmail,
                        currentPassword = currentPassword?.ifBlank { null },
                        newPassword = newPassword,
                        confirmPassword = confirmPassword
                    )
                ).collect { resource ->
                    when (resource) {
                        is Resource.Loading -> {
                            _changePasswordState.update {
                                it.copy(isLoading = true, errorMessage = null, isSuccess = false)
                            }
                        }
                        is Resource.Success -> {
                            val msg = resource.data.ifBlank { "Password changed successfully" }
                            _changePasswordState.update {
                                it.copy(
                                    isLoading = false,
                                    isSuccess = true,
                                    successMessage = msg,
                                    errorMessage = null
                                )
                            }
                            authRepository.updateActivity()
                            onSuccess(msg)
                        }
                        is Resource.Error -> {
                            _changePasswordState.update {
                                it.copy(
                                    isLoading = false,
                                    isSuccess = false,
                                    errorMessage = resource.message
                                )
                            }
                        }
                    }
                }
            } catch (e: Exception) {
                _changePasswordState.update { it.copy(isLoading = false, errorMessage = ApiConstants.resolveNetworkErrorMessage(e)) }
            } finally {
                _changePasswordState.update { it.copy(isLoading = false) }
            }
        }
    }

    fun clearChangePasswordError() {
        _changePasswordState.update { it.copy(errorMessage = null) }
    }

    fun resetChangePasswordState() {
        _changePasswordState.value = AuthUiState()
    }

    fun clearLoginError() {
        _loginState.update { it.copy(errorMessage = null) }
        _sessionExpiredNotice.value = null
    }

    fun clearSignUpError() {
        _signUpState.update { it.copy(errorMessage = null) }
    }

    fun clearResetPasswordError() {
        _resetPasswordState.update { it.copy(errorMessage = null) }
    }

    fun resetLoginState() {
        _loginState.value = AuthUiState()
    }

    fun resetSignUpState() {
        _signUpState.value = AuthUiState()
    }

    fun resetPasswordState() {
        _resetPasswordState.value = AuthUiState()
    }

    fun getLoggedInUser(): User? = authRepository.getLoggedInUser()

    fun isLoggedIn(): Boolean = authRepository.isLoggedIn()

    fun isOnboardingCompleted(): Boolean = authRepository.isOnboardingCompleted()

    fun setOnboardingCompleted(completed: Boolean) {
        authRepository.setOnboardingCompleted(completed)
    }

    fun isDarkModeEnabled(): Boolean = authRepository.isDarkModeEnabled()

    fun setDarkModeEnabled(enabled: Boolean) {
        authRepository.setDarkModeEnabled(enabled)
    }

    fun updateCurrentUser(user: User) {
        authRepository.saveUser(user)
        _currentUser.value = user
    }

    fun logout() {
        authRepository.logout()
        _currentUser.value = null
        _sessionExpiredNotice.value = null
        resetLoginState()
        resetSignUpState()
        resetPasswordState()
    }
}
