package com.simats.vitalsync.ui.profile.viewmodel

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.simats.vitalsync.data.local.SessionManager
import com.simats.vitalsync.data.network.RetrofitClient
import com.simats.vitalsync.data.repository.ProfileRepository

class ProfileViewModelFactory(
    private val context: Context
) : ViewModelProvider.Factory {

    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(ProfileViewModel::class.java)) {
            val sessionManager = SessionManager.getInstance(context.applicationContext)
            val repository = ProfileRepository(
                apiService = RetrofitClient.profileApiService,
                sessionManager = sessionManager
            )
            return ProfileViewModel(repository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class: ${modelClass.name}")
    }
}

