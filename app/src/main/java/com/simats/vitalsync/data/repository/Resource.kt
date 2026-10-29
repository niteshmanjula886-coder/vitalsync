package com.simats.vitalsync.data.repository

/**
 * Generic Resource class to represent network / data states.
 */
sealed class Resource<out T> {
    data class Success<out T>(val data: T) : Resource<T>()
    data class Error(val message: String, val code: Int? = null, val throwable: Throwable? = null) : Resource<Nothing>()
    data object Loading : Resource<Nothing>()
}

