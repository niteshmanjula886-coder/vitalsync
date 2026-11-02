package com.simats.vitalsync.ui.vitals.viewmodel

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.simats.vitalsync.data.local.SessionManager
import com.simats.vitalsync.data.network.RetrofitClient
import com.simats.vitalsync.data.repository.VitalsRepository

class VitalsViewModelFactory(
    private val context: Context
) : ViewModelProvider.Factory {

    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(VitalsViewModel::class.java)) {
            val sessionManager = SessionManager.getInstance(context.applicationContext)
            val repository = VitalsRepository(
                apiService = RetrofitClient.vitalsApiService,
                sessionManager = sessionManager
            )
            return VitalsViewModel(repository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class: ${modelClass.name}")
    }
}

