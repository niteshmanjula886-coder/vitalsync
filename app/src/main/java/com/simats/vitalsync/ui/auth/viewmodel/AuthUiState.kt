package com.simats.vitalsync.ui.auth.viewmodel

import com.simats.vitalsync.data.model.auth.User

/**
 * UI State representing authentication screens (Login, SignUp, ForgotPassword).
 */
data class AuthUiState(
    val isLoading: Boolean = false,
    val isSuccess: Boolean = false,
    val errorMessage: String? = null,
    val successMessage: String? = null,
    val user: User? = null
)

