package com.simats.vitalsync.ui.gym.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.simats.vitalsync.data.network.RetrofitClient
import com.simats.vitalsync.data.repository.GymRepository

class GymViewModelFactory(
    private val repository: GymRepository = GymRepository(RetrofitClient.gymApiService)
) : ViewModelProvider.Factory {

    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(GymViewModel::class.java)) {
            return GymViewModel(repository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class: ${modelClass.name}")
    }
}
