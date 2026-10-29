package com.simats.vitalsync.ui.auth.viewmodel

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.simats.vitalsync.data.local.SessionManager
import com.simats.vitalsync.data.network.RetrofitClient
import com.simats.vitalsync.data.repository.AuthRepository

class AuthViewModelFactory(
    private val context: Context
) : ViewModelProvider.Factory {

    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(AuthViewModel::class.java)) {
            val sessionManager = SessionManager.getInstance(context)
            val authRepository = AuthRepository(
                apiService = RetrofitClient.authApiService,
                sessionManager = sessionManager
            )
            return AuthViewModel(authRepository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class: ${modelClass.name}")
    }
}

