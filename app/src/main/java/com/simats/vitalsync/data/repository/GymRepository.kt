package com.simats.vitalsync.data.repository

import com.simats.vitalsync.data.model.gym.CreateExerciseRequest
import com.simats.vitalsync.data.model.gym.DeleteNutritionRequest
import com.simats.vitalsync.data.model.gym.DeleteWorkoutRequest
import com.simats.vitalsync.data.model.gym.ExerciseDto
import com.simats.vitalsync.data.model.gym.GymAnalyticsDto
import com.simats.vitalsync.data.model.gym.GymNutritionLogDto
import com.simats.vitalsync.data.model.gym.GymNutritionResponse
import com.simats.vitalsync.data.model.gym.GymProgressDto
import com.simats.vitalsync.data.model.gym.LogNutritionRequest
import com.simats.vitalsync.data.model.gym.SaveGymWorkoutRequest
import com.simats.vitalsync.data.model.gym.SaveGymWorkoutResponse
import com.simats.vitalsync.data.model.gym.SaveWorkoutSessionRequest
import com.simats.vitalsync.data.model.gym.SaveWorkoutSessionResponse
import com.simats.vitalsync.data.model.gym.WorkoutSessionDto
import com.simats.vitalsync.data.model.gym.WorkoutSplitDto
import com.simats.vitalsync.data.network.ApiConstants
import com.simats.vitalsync.data.network.GymApiService
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn
import org.json.JSONObject

class GymRepository(
    private val apiService: GymApiService
) {

    fun getSplits(): Flow<Resource<List<WorkoutSplitDto>>> = flow {
        emit(Resource.Loading)
        try {
            val response = apiService.getSplits()
            if (response.isSuccessful && response.body() != null) {
                val body = response.body()!!
                if (body.success) {
                    emit(Resource.Success(body.splits))
                } else {
                    emit(Resource.Error("Failed to fetch splits"))
                }
            } else {
                emit(Resource.Error(parseErrorMessage(response.errorBody()?.string())))
            }
        } catch (e: Exception) {
            emit(Resource.Error(ApiConstants.resolveNetworkErrorMessage(e), throwable = e))
        }
    }.flowOn(Dispatchers.IO)

    fun getExercises(email: String): Flow<Resource<List<ExerciseDto>>> = flow {
        emit(Resource.Loading)
        try {
            val response = apiService.getExercises(email)
            if (response.isSuccessful && response.body() != null) {
                val body = response.body()!!
                if (body.success) {
                    emit(Resource.Success(body.exercises))
                } else {
                    emit(Resource.Error("Failed to fetch exercises"))
                }
            } else {
                emit(Resource.Error(parseErrorMessage(response.errorBody()?.string())))
            }
        } catch (e: Exception) {
            emit(Resource.Error(ApiConstants.resolveNetworkErrorMessage(e), throwable = e))
        }
    }.flowOn(Dispatchers.IO)

    fun addCustomExercise(request: CreateExerciseRequest): Flow<Resource<ExerciseDto>> = flow {
        emit(Resource.Loading)
        try {
            val response = apiService.addCustomExercise(request)
            if (response.isSuccessful && response.body() != null) {
                val body = response.body()!!
                if (body.success && body.exercise != null) {
                    emit(Resource.Success(body.exercise))
                } else {
                    emit(Resource.Error(body.message.ifBlank { "Failed to create exercise" }))
                }
            } else {
                emit(Resource.Error(parseErrorMessage(response.errorBody()?.string())))
            }
        } catch (e: Exception) {
            emit(Resource.Error(ApiConstants.resolveNetworkErrorMessage(e), throwable = e))
        }
    }.flowOn(Dispatchers.IO)

    fun getWorkoutSessions(email: String): Flow<Resource<List<WorkoutSessionDto>>> = flow {
        emit(Resource.Loading)
        try {
            val response = apiService.getWorkoutSessions(email)
            if (response.isSuccessful && response.body() != null) {
                val body = response.body()!!
                if (body.success) {
                    emit(Resource.Success(body.workoutSessions))
                } else {
                    emit(Resource.Error("Failed to fetch workout sessions"))
                }
            } else {
                emit(Resource.Error(parseErrorMessage(response.errorBody()?.string())))
            }
        } catch (e: Exception) {
            emit(Resource.Error(ApiConstants.resolveNetworkErrorMessage(e), throwable = e))
        }
    }.flowOn(Dispatchers.IO)

    fun saveWorkoutSession(request: SaveWorkoutSessionRequest): Flow<Resource<SaveWorkoutSessionResponse>> = flow {
        emit(Resource.Loading)
        try {
            val response = apiService.saveWorkoutSession(request)
            if (response.isSuccessful && response.body() != null) {
                val body = response.body()!!
                if (body.success) {
                    emit(Resource.Success(body))
                } else {
                    emit(Resource.Error(body.message.ifBlank { "Failed to save workout session" }))
                }
            } else {
                emit(Resource.Error(parseErrorMessage(response.errorBody()?.string())))
            }
        } catch (e: Exception) {
            emit(Resource.Error(ApiConstants.resolveNetworkErrorMessage(e), throwable = e))
        }
    }.flowOn(Dispatchers.IO)

    fun getGymProgress(email: String): Flow<Resource<GymProgressDto>> = flow {
        emit(Resource.Loading)
        try {
            val response = apiService.getGymProgress(email)
            if (response.isSuccessful && response.body() != null) {
                val body = response.body()!!
                if (body.success) {
                    emit(Resource.Success(body.progress))
                } else {
                    emit(Resource.Error("Failed to fetch gym progress"))
                }
            } else {
                emit(Resource.Error(parseErrorMessage(response.errorBody()?.string())))
            }
        } catch (e: Exception) {
            emit(Resource.Error(ApiConstants.resolveNetworkErrorMessage(e), throwable = e))
        }
    }.flowOn(Dispatchers.IO)

    fun saveGymWorkout(request: SaveGymWorkoutRequest): Flow<Resource<SaveGymWorkoutResponse>> = flow {
        emit(Resource.Loading)
        try {
            val response = apiService.saveGymWorkout(request)
            if (response.isSuccessful && response.body() != null) {
                val body = response.body()!!
                if (body.success) {
                    emit(Resource.Success(body))
                } else {
                    emit(Resource.Error(body.message.ifBlank { "Failed to save gym workout" }))
                }
            } else {
                emit(Resource.Error(parseErrorMessage(response.errorBody()?.string())))
            }
        } catch (e: Exception) {
            emit(Resource.Error(ApiConstants.resolveNetworkErrorMessage(e), throwable = e))
        }
    }.flowOn(Dispatchers.IO)

    fun getGymWorkouts(userId: Int? = null, email: String? = null, date: String? = null): Flow<Resource<List<WorkoutSessionDto>>> = flow {
        emit(Resource.Loading)
        try {
            val response = apiService.getGymWorkouts(userId = userId, email = email, date = date)
            if (response.isSuccessful && response.body() != null) {
                val body = response.body()!!
                if (body.success) {
                    emit(Resource.Success(body.workoutSessions))
                } else {
                    emit(Resource.Error("Failed to fetch gym workouts"))
                }
            } else {
                emit(Resource.Error(parseErrorMessage(response.errorBody()?.string())))
            }
        } catch (e: Exception) {
            emit(Resource.Error(ApiConstants.resolveNetworkErrorMessage(e), throwable = e))
        }
    }.flowOn(Dispatchers.IO)

    fun deleteGymWorkout(id: Int, userId: Int? = null, email: String? = null): Flow<Resource<Boolean>> = flow {
        emit(Resource.Loading)
        try {
            val response = apiService.deleteGymWorkout(DeleteWorkoutRequest(id = id, userId = userId, email = email))
            if (response.isSuccessful && response.body() != null) {
                val body = response.body()!!
                if (body.success) {
                    emit(Resource.Success(true))
                } else {
                    emit(Resource.Error(body.message.ifBlank { "Failed to delete workout session" }))
                }
            } else {
                emit(Resource.Error(parseErrorMessage(response.errorBody()?.string())))
            }
        } catch (e: Exception) {
            emit(Resource.Error(ApiConstants.resolveNetworkErrorMessage(e), throwable = e))
        }
    }.flowOn(Dispatchers.IO)

    fun analyzeAndLogNutrition(request: LogNutritionRequest): Flow<Resource<GymNutritionLogDto>> = flow {
        emit(Resource.Loading)
        try {
            val response = apiService.analyzeAndLogNutrition(request)
            if (response.isSuccessful && response.body() != null) {
                val body = response.body()!!
                if (body.success && body.data != null) {
                    emit(Resource.Success(body.data))
                } else {
                    emit(Resource.Error(body.message.ifBlank { "Failed to analyze and log nutrition" }))
                }
            } else {
                emit(Resource.Error(parseErrorMessage(response.errorBody()?.string())))
            }
        } catch (e: Exception) {
            emit(Resource.Error(ApiConstants.resolveNetworkErrorMessage(e), throwable = e))
        }
    }.flowOn(Dispatchers.IO)

    fun getGymNutrition(userId: Int? = null, email: String? = null, date: String? = null): Flow<Resource<GymNutritionResponse>> = flow {
        emit(Resource.Loading)
        try {
            val response = apiService.getGymNutrition(userId = userId, email = email, date = date)
            if (response.isSuccessful && response.body() != null) {
                val body = response.body()!!
                if (body.success) {
                    emit(Resource.Success(body))
                } else {
                    emit(Resource.Error("Failed to fetch gym nutrition"))
                }
            } else {
                emit(Resource.Error(parseErrorMessage(response.errorBody()?.string())))
            }
        } catch (e: Exception) {
            emit(Resource.Error(ApiConstants.resolveNetworkErrorMessage(e), throwable = e))
        }
    }.flowOn(Dispatchers.IO)

    fun deleteGymNutrition(id: Int, userId: Int? = null, email: String? = null): Flow<Resource<Boolean>> = flow {
        emit(Resource.Loading)
        try {
            val response = apiService.deleteGymNutrition(DeleteNutritionRequest(id = id, userId = userId, email = email))
            if (response.isSuccessful && response.body() != null) {
                val body = response.body()!!
                if (body.success) {
                    emit(Resource.Success(true))
                } else {
                    emit(Resource.Error(body.message.ifBlank { "Failed to delete meal" }))
                }
            } else {
                emit(Resource.Error(parseErrorMessage(response.errorBody()?.string())))
            }
        } catch (e: Exception) {
            emit(Resource.Error(ApiConstants.resolveNetworkErrorMessage(e), throwable = e))
        }
    }.flowOn(Dispatchers.IO)

    fun getGymAnalytics(userId: Int? = null, email: String? = null, date: String? = null): Flow<Resource<GymAnalyticsDto>> = flow {
        emit(Resource.Loading)
        try {
            val response = apiService.getGymAnalytics(userId = userId, email = email, date = date)
            if (response.isSuccessful && response.body() != null) {
                val body = response.body()!!
                if (body.success && body.data != null) {
                    emit(Resource.Success(body.data))
                } else {
                    emit(Resource.Error(body.message ?: "Failed to fetch gym analytics"))
                }
            } else {
                emit(Resource.Error(parseErrorMessage(response.errorBody()?.string())))
            }
        } catch (e: Exception) {
            emit(Resource.Error(ApiConstants.resolveNetworkErrorMessage(e), throwable = e))
        }
    }.flowOn(Dispatchers.IO)

    private fun parseErrorMessage(errorJson: String?): String {
        if (errorJson.isNullOrBlank()) return "An unexpected server error occurred."
        return try {
            val obj = JSONObject(errorJson)
            obj.optString("message", "Request failed. Please try again.")
        } catch (_: Exception) {
            "Request failed. Please try again."
        }
    }
}
