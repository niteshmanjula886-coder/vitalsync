package com.simats.vitalsync.ui.activity.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.simats.vitalsync.data.network.RetrofitClient
import com.simats.vitalsync.data.repository.ActivityRepository

class ActivityViewModelFactory(
    private val repository: ActivityRepository = ActivityRepository(RetrofitClient.activityApiService)
) : ViewModelProvider.Factory {

    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(ActivityViewModel::class.java)) {
            return ActivityViewModel(repository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class: ${modelClass.name}")
    }
}

