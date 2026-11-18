package com.simats.vitalsync.data.repository

import android.graphics.BitmapFactory
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.asImageBitmap
import com.simats.vitalsync.data.local.SessionManager
import com.simats.vitalsync.data.model.auth.User
import com.simats.vitalsync.data.model.profile.AddReminderRequest
import com.simats.vitalsync.data.model.profile.AppSettingsDto
import com.simats.vitalsync.data.model.profile.AppVersionResponse
import com.simats.vitalsync.data.model.profile.CreateSupportTicketRequest
import com.simats.vitalsync.data.model.profile.HealthGoalsResponse
import com.simats.vitalsync.data.model.profile.HealthPermissionsDto
import com.simats.vitalsync.data.model.profile.LoginSessionItem
import com.simats.vitalsync.data.model.profile.LogoutOthersRequest
import com.simats.vitalsync.data.model.profile.NotificationActionRequest
import com.simats.vitalsync.data.model.profile.NotificationsResponse
import com.simats.vitalsync.data.model.profile.ReminderItem
import com.simats.vitalsync.data.model.profile.SaveHealthGoalRequest
import com.simats.vitalsync.data.model.profile.SupportTicketsResponse
import com.simats.vitalsync.data.model.profile.UpdateAppSettingsRequest
import com.simats.vitalsync.data.model.profile.UpdateHealthPermissionsRequest
import com.simats.vitalsync.data.model.profile.UpdateProfileRequest
import com.simats.vitalsync.data.model.profile.UpdateReminderRequest
import com.simats.vitalsync.data.model.profile.UploadProfileResponse
import com.simats.vitalsync.data.network.ApiConstants
import com.simats.vitalsync.data.network.ProfileApiService
import com.simats.vitalsync.data.network.RetrofitClient
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.asRequestBody
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONObject
import java.io.File

class ProfileRepository(
    private val apiService: ProfileApiService = RetrofitClient.profileApiService,
    val sessionManager: SessionManager? = null
) {

    fun updateLocalProfileFields(
        name: String? = null,
        age: Int? = null,
        height: Float? = null,
        weight: Float? = null,
        gender: String? = null,
        activityLevel: String? = null,
        primaryGoal: String? = null
    ): User? {
        val current = sessionManager?.getUser() ?: return null
        val updated = current.copy(
            name = name?.takeIf { it.isNotBlank() } ?: current.name,
            fullName = name?.takeIf { it.isNotBlank() } ?: current.fullName,
            age = age?.takeIf { it > 0 } ?: current.age,
            height = height?.takeIf { it > 0f } ?: current.height,
            weight = weight?.takeIf { it > 0f } ?: current.weight,
            gender = gender?.takeIf { it.isNotBlank() } ?: current.gender,
            activityLevel = activityLevel?.takeIf { it.isNotBlank() } ?: current.activityLevel,
            primaryGoal = primaryGoal?.takeIf { it.isNotBlank() } ?: current.primaryGoal
        )
        sessionManager.saveUserSession(updated)
        return updated
    }

    fun getProfile(userId: Int? = null, email: String? = null): Flow<Resource<User>> = flow {
        emit(Resource.Loading)
        try {
            val resolvedUserId = userId ?: sessionManager?.getUser()?.id
            val resolvedEmail = email ?: sessionManager?.getUserEmail()
            val response = apiService.getProfile(userId = resolvedUserId, email = resolvedEmail)
            if (response.isSuccessful && response.body() != null) {
                val body = response.body()!!
                if (body.success && body.profile != null) {
                    sessionManager?.saveUserSession(body.profile)
                    val goalVal = body.profile.stepGoal
                    if (goalVal != null && goalVal >= 1000) {
                        sessionManager?.saveStepGoal(goalVal)
                    }
                    emit(Resource.Success(body.profile))
                } else {
                    emit(Resource.Error(body.message ?: "Failed to fetch profile"))
                }
            } else {
                emit(Resource.Error(parseErrorMessage(response.errorBody()?.string())))
            }
        } catch (e: Exception) {
            emit(Resource.Error(ApiConstants.resolveNetworkErrorMessage(e, "Network error fetching profile"), throwable = e))
        }
    }.flowOn(Dispatchers.IO)

    fun updateProfile(request: UpdateProfileRequest): Flow<Resource<User>> = flow {
        emit(Resource.Loading)
        try {
            val response = apiService.updateProfile(request)
            if (response.isSuccessful && response.body() != null) {
                val body = response.body()!!
                if (body.success && body.profile != null) {
                    sessionManager?.saveUserSession(body.profile)
                    val goalVal = body.profile.stepGoal ?: request.stepGoal
                    if (goalVal != null && goalVal >= 1000) {
                        sessionManager?.saveStepGoal(goalVal)
                    }
                    emit(Resource.Success(body.profile))
                } else {
                    emit(Resource.Error(body.message ?: "Failed to update profile"))
                }
            } else {
                emit(Resource.Error(parseErrorMessage(response.errorBody()?.string())))
            }
        } catch (e: Exception) {
            emit(Resource.Error(ApiConstants.resolveNetworkErrorMessage(e), throwable = e))
        }
    }.flowOn(Dispatchers.IO)

    fun uploadProfilePicture(userId: Int? = null, email: String? = null, imageFile: File): Flow<Resource<UploadProfileResponse>> = flow {
        emit(Resource.Loading)
        try {
            val resolvedUid = userId ?: sessionManager?.getUser()?.id ?: 0
            val resolvedEmail = email ?: sessionManager?.getUserEmail() ?: ""

            val uidPart = if (resolvedUid > 0) resolvedUid.toString().toRequestBody("text/plain".toMediaTypeOrNull()) else null
            val emailPart = if (resolvedEmail.isNotEmpty()) resolvedEmail.toRequestBody("text/plain".toMediaTypeOrNull()) else null

            val reqFile = imageFile.asRequestBody("image/*".toMediaTypeOrNull())
            val bodyPart = MultipartBody.Part.createFormData("avatar", imageFile.name, reqFile)

            val response = apiService.uploadProfileImage(
                userId = uidPart,
                email = emailPart,
                profileImage = bodyPart
            )

            if (response.isSuccessful && response.body() != null) {
                val body = response.body()!!
                if (body.success) {
                    val currentUser = sessionManager?.getUser()
                    if (currentUser != null && (body.imageUrl != null || body.profileImage != null)) {
                        sessionManager.saveUserSession(
                            currentUser.copy(
                                imageUrl = body.imageUrl ?: currentUser.imageUrl,
                                profileImage = body.profileImage ?: currentUser.profileImage
                            )
                        )
                    }
                    emit(Resource.Success(body))
                } else {
                    emit(Resource.Error(body.message ?: "Failed to upload image"))
                }
            } else {
                emit(Resource.Error(parseErrorMessage(response.errorBody()?.string())))
            }
        } catch (e: Exception) {
            emit(Resource.Error(ApiConstants.resolveNetworkErrorMessage(e), throwable = e))
        }
    }.flowOn(Dispatchers.IO)

    suspend fun fetchAvatarBitmap(user: User?): ImageBitmap? {
        val rawUrl = user?.imageUrl?.takeIf { it.isNotBlank() }
            ?: user?.profileImage?.takeIf { it.isNotBlank() }
            ?: return null
        val bytes = RetrofitClient.downloadImageBytes(rawUrl) ?: return null
        return try {
            BitmapFactory.decodeByteArray(bytes, 0, bytes.size)?.asImageBitmap()
        } catch (_: Exception) {
            null
        }
    }

    fun getHealthGoals(userId: Int? = null, email: String? = null): Flow<Resource<HealthGoalsResponse>> = flow {
        emit(Resource.Loading)
        try {
            val resolvedUid = userId ?: sessionManager?.getUser()?.id
            val resolvedEmail = email ?: sessionManager?.getUserEmail()
            val response = apiService.getHealthGoals(userId = resolvedUid, email = resolvedEmail)
            if (response.isSuccessful && response.body() != null) {
                val body = response.body()!!
                if (body.success) {
                    val fetchedStepGoal = body.stepGoal ?: body.goals?.targetSteps
                    if (fetchedStepGoal != null && fetchedStepGoal >= 1000) {
                        sessionManager?.saveStepGoal(fetchedStepGoal)
                    }
                    emit(Resource.Success(body))
                } else {
                    emit(Resource.Error(body.message ?: "Failed to load health goals"))
                }
            } else {
                emit(Resource.Error(parseErrorMessage(response.errorBody()?.string())))
            }
        } catch (e: Exception) {
            emit(Resource.Error(ApiConstants.resolveNetworkErrorMessage(e), throwable = e))
        }
    }.flowOn(Dispatchers.IO)

    fun saveHealthGoal(request: SaveHealthGoalRequest): Flow<Resource<HealthGoalsResponse>> = flow {
        emit(Resource.Loading)
        try {
            val response = apiService.saveHealthGoal(request)
            if (response.isSuccessful && response.body() != null) {
                val body = response.body()!!
                if (body.success) {
                    val updatedGoal = body.selectedGoal ?: body.primaryGoal ?: request.goalTitle
                    val updatedSteps = body.stepGoal ?: body.goals?.targetSteps ?: request.stepGoal ?: request.targetSteps
                    if (updatedSteps != null && updatedSteps >= 1000) {
                        sessionManager?.saveStepGoal(updatedSteps)
                    }
                    if (!updatedGoal.isNullOrBlank()) {
                        sessionManager?.getUser()?.let { user ->
                            sessionManager?.saveUserSession(
                                user.copy(
                                    primaryGoal = updatedGoal,
                                    stepGoal = updatedSteps ?: user.stepGoal
                                )
                            )
                        }
                    }
                    emit(Resource.Success(body))
                } else {
                    emit(Resource.Error(body.message ?: "Failed to save health goal"))
                }
            } else {
                emit(Resource.Error(parseErrorMessage(response.errorBody()?.string())))
            }
        } catch (e: Exception) {
            emit(Resource.Error(ApiConstants.resolveNetworkErrorMessage(e), throwable = e))
        }
    }.flowOn(Dispatchers.IO)

    fun getReminders(userId: Int? = null, email: String? = null): Flow<Resource<List<ReminderItem>>> = flow {
        emit(Resource.Loading)
        try {
            val resolvedUid = userId ?: sessionManager?.getUser()?.id
            val resolvedEmail = email ?: sessionManager?.getUserEmail()
            val response = apiService.getReminders(userId = resolvedUid, email = resolvedEmail)
            if (response.isSuccessful && response.body() != null) {
                val body = response.body()!!
                if (body.success) {
                    emit(Resource.Success(body.reminders))
                } else {
                    emit(Resource.Error(body.message ?: "Failed to load reminders"))
                }
            } else {
                emit(Resource.Error(parseErrorMessage(response.errorBody()?.string())))
            }
        } catch (e: Exception) {
            emit(Resource.Error(ApiConstants.resolveNetworkErrorMessage(e), throwable = e))
        }
    }.flowOn(Dispatchers.IO)

    fun addReminder(request: AddReminderRequest): Flow<Resource<List<ReminderItem>>> = flow {
        emit(Resource.Loading)
        try {
            val response = apiService.addReminder(request)
            if (response.isSuccessful && response.body() != null) {
                val body = response.body()!!
                if (body.success) {
                    emit(Resource.Success(body.reminders))
                } else {
                    emit(Resource.Error(body.message ?: "Failed to add reminder"))
                }
            } else {
                emit(Resource.Error(parseErrorMessage(response.errorBody()?.string())))
            }
        } catch (e: Exception) {
            emit(Resource.Error(ApiConstants.resolveNetworkErrorMessage(e), throwable = e))
        }
    }.flowOn(Dispatchers.IO)

    fun updateReminder(request: UpdateReminderRequest): Flow<Resource<Boolean>> = flow {
        emit(Resource.Loading)
        try {
            val response = apiService.updateReminder(request)
            if (response.isSuccessful && response.body() != null) {
                val body = response.body()!!
                if (body.success) {
                    emit(Resource.Success(true))
                } else {
                    emit(Resource.Error(body.message ?: "Failed to update reminder"))
                }
            } else {
                emit(Resource.Error(parseErrorMessage(response.errorBody()?.string())))
            }
        } catch (e: Exception) {
            emit(Resource.Error(ApiConstants.resolveNetworkErrorMessage(e), throwable = e))
        }
    }.flowOn(Dispatchers.IO)

    fun deleteReminder(id: Int, userId: Int): Flow<Resource<Boolean>> = flow {
        emit(Resource.Loading)
        try {
            val response = apiService.deleteReminder(id = id, userId = userId)
            if (response.isSuccessful && response.body() != null) {
                val body = response.body()!!
                if (body.success) {
                    emit(Resource.Success(true))
                } else {
                    emit(Resource.Error(body.message ?: "Failed to delete reminder"))
                }
            } else {
                emit(Resource.Error(parseErrorMessage(response.errorBody()?.string())))
            }
        } catch (e: Exception) {
            emit(Resource.Error(ApiConstants.resolveNetworkErrorMessage(e), throwable = e))
        }
    }.flowOn(Dispatchers.IO)

    // ==========================================
    // App Settings
    // ==========================================
    fun getAppSettings(userId: Int? = null, email: String? = null): Flow<Resource<AppSettingsDto>> = flow {
        emit(Resource.Loading)
        try {
            val resolvedUid = userId ?: sessionManager?.getUser()?.id
            val resolvedEmail = email ?: sessionManager?.getUserEmail()
            val response = apiService.getAppSettings(userId = resolvedUid, email = resolvedEmail)
            if (response.isSuccessful && response.body() != null) {
                val body = response.body()!!
                if (body.success && body.settings != null) {
                    emit(Resource.Success(body.settings))
                } else {
                    emit(Resource.Error(body.message ?: "Failed to load settings"))
                }
            } else {
                emit(Resource.Error(parseErrorMessage(response.errorBody()?.string())))
            }
        } catch (e: Exception) {
            emit(Resource.Error(ApiConstants.resolveNetworkErrorMessage(e), throwable = e))
        }
    }.flowOn(Dispatchers.IO)

    fun updateAppSettings(request: UpdateAppSettingsRequest): Flow<Resource<AppSettingsDto>> = flow {
        emit(Resource.Loading)
        try {
            val response = apiService.updateAppSettings(request)
            if (response.isSuccessful && response.body() != null) {
                val body = response.body()!!
                if (body.success && body.settings != null) {
                    emit(Resource.Success(body.settings))
                } else {
                    emit(Resource.Error(body.message ?: "Failed to update settings"))
                }
            } else {
                emit(Resource.Error(parseErrorMessage(response.errorBody()?.string())))
            }
        } catch (e: Exception) {
            emit(Resource.Error(ApiConstants.resolveNetworkErrorMessage(e), throwable = e))
        }
    }.flowOn(Dispatchers.IO)

    fun getWeeklyHealthReport(userId: Int? = null, email: String? = null): Flow<Resource<com.simats.vitalsync.data.model.profile.WeeklyReportDto>> = flow {
        emit(Resource.Loading)
        try {
            val resolvedUid = userId ?: sessionManager?.getUser()?.id
            val resolvedEmail = email ?: sessionManager?.getUserEmail()
            val response = apiService.getWeeklyHealthReport(userId = resolvedUid, email = resolvedEmail)
            if (response.isSuccessful && response.body() != null) {
                val body = response.body()!!
                if (body.success && body.data != null) {
                    emit(Resource.Success(body.data))
                } else {
                    emit(Resource.Error(body.message.ifBlank { "Failed to load weekly report" }))
                }
            } else {
                emit(Resource.Error("Weekly report server error: ${response.code()}"))
            }
        } catch (e: Exception) {
            emit(Resource.Error(ApiConstants.resolveNetworkErrorMessage(e), throwable = e))
        }
    }.flowOn(Dispatchers.IO)

    // ==========================================
    // Health Permissions
    // ==========================================
    fun getHealthPermissions(userId: Int? = null, email: String? = null): Flow<Resource<HealthPermissionsDto>> = flow {
        emit(Resource.Loading)
        try {
            val resolvedUid = userId ?: sessionManager?.getUser()?.id
            val resolvedEmail = email ?: sessionManager?.getUserEmail()
            val response = apiService.getHealthPermissions(userId = resolvedUid, email = resolvedEmail)
            if (response.isSuccessful && response.body() != null) {
                val body = response.body()!!
                if (body.success && body.permissions != null) {
                    emit(Resource.Success(body.permissions))
                } else {
                    emit(Resource.Error(body.message ?: "Failed to load permissions"))
                }
            } else {
                emit(Resource.Error(parseErrorMessage(response.errorBody()?.string())))
            }
        } catch (e: Exception) {
            emit(Resource.Error(ApiConstants.resolveNetworkErrorMessage(e), throwable = e))
        }
    }.flowOn(Dispatchers.IO)

    fun updateHealthPermissions(request: UpdateHealthPermissionsRequest): Flow<Resource<HealthPermissionsDto>> = flow {
        emit(Resource.Loading)
        try {
            val response = apiService.updateHealthPermissions(request)
            if (response.isSuccessful && response.body() != null) {
                val body = response.body()!!
                if (body.success && body.permissions != null) {
                    emit(Resource.Success(body.permissions))
                } else {
                    emit(Resource.Error(body.message ?: "Failed to update permissions"))
                }
            } else {
                emit(Resource.Error(parseErrorMessage(response.errorBody()?.string())))
            }
        } catch (e: Exception) {
            emit(Resource.Error(ApiConstants.resolveNetworkErrorMessage(e), throwable = e))
        }
    }.flowOn(Dispatchers.IO)

    // ==========================================
    // Login Sessions
    // ==========================================
    fun getLoginSessions(userId: Int? = null, email: String? = null): Flow<Resource<List<LoginSessionItem>>> = flow {
        emit(Resource.Loading)
        try {
            val resolvedUid = userId ?: sessionManager?.getUser()?.id
            val resolvedEmail = email ?: sessionManager?.getUserEmail()
            val response = apiService.getLoginSessions(userId = resolvedUid, email = resolvedEmail)
            if (response.isSuccessful && response.body() != null) {
                val body = response.body()!!
                if (body.success) {
                    emit(Resource.Success(body.sessions))
                } else {
                    emit(Resource.Error(body.message ?: "Failed to load login sessions"))
                }
            } else {
                emit(Resource.Error(parseErrorMessage(response.errorBody()?.string())))
            }
        } catch (e: Exception) {
            emit(Resource.Error(ApiConstants.resolveNetworkErrorMessage(e), throwable = e))
        }
    }.flowOn(Dispatchers.IO)

    fun logoutOtherSessions(userId: Int? = null, email: String? = null): Flow<Resource<Boolean>> = flow {
        emit(Resource.Loading)
        try {
            val resolvedUid = userId ?: sessionManager?.getUser()?.id
            val resolvedEmail = email ?: sessionManager?.getUserEmail()
            val req = LogoutOthersRequest(userId = resolvedUid, email = resolvedEmail)
            val response = apiService.logoutOtherSessions(req)
            if (response.isSuccessful && response.body() != null) {
                val body = response.body()!!
                if (body.success) {
                    emit(Resource.Success(true))
                } else {
                    emit(Resource.Error(body.message ?: "Failed to terminate other sessions"))
                }
            } else {
                emit(Resource.Error(parseErrorMessage(response.errorBody()?.string())))
            }
        } catch (e: Exception) {
            emit(Resource.Error(ApiConstants.resolveNetworkErrorMessage(e), throwable = e))
        }
    }.flowOn(Dispatchers.IO)

    // ==========================================
    // Notifications
    // ==========================================
    fun getNotifications(userId: Int? = null, email: String? = null): Flow<Resource<NotificationsResponse>> = flow {
        emit(Resource.Loading)
        try {
            val resolvedUid = userId ?: sessionManager?.getUser()?.id
            val resolvedEmail = email ?: sessionManager?.getUserEmail()
            val response = apiService.getNotifications(userId = resolvedUid, email = resolvedEmail)
            if (response.isSuccessful && response.body() != null) {
                val body = response.body()!!
                if (body.success) {
                    emit(Resource.Success(body))
                } else {
                    emit(Resource.Error(body.message ?: "Failed to load notifications"))
                }
            } else {
                emit(Resource.Error(parseErrorMessage(response.errorBody()?.string())))
            }
        } catch (e: Exception) {
            emit(Resource.Error(ApiConstants.resolveNetworkErrorMessage(e), throwable = e))
        }
    }.flowOn(Dispatchers.IO)

    fun markAllNotificationsRead(userId: Int? = null, email: String? = null): Flow<Resource<Boolean>> = flow {
        emit(Resource.Loading)
        try {
            val resolvedUid = userId ?: sessionManager?.getUser()?.id
            val resolvedEmail = email ?: sessionManager?.getUserEmail()
            val req = NotificationActionRequest(userId = resolvedUid, email = resolvedEmail, action = "mark_all_read")
            val response = apiService.handleNotification(req)
            if (response.isSuccessful && response.body() != null) {
                val body = response.body()!!
                if (body.success) {
                    emit(Resource.Success(true))
                } else {
                    emit(Resource.Error(body.message ?: "Failed to mark notifications read"))
                }
            } else {
                emit(Resource.Error(parseErrorMessage(response.errorBody()?.string())))
            }
        } catch (e: Exception) {
            emit(Resource.Error(ApiConstants.resolveNetworkErrorMessage(e), throwable = e))
        }
    }.flowOn(Dispatchers.IO)

    // ==========================================
    // App Version
    // ==========================================
    fun getAppVersion(): Flow<Resource<AppVersionResponse>> = flow {
        emit(Resource.Loading)
        try {
            val response = apiService.getAppVersion()
            if (response.isSuccessful && response.body() != null) {
                val body = response.body()!!
                if (body.success) {
                    emit(Resource.Success(body))
                } else {
                    emit(Resource.Error(body.message ?: "Failed to get app version"))
                }
            } else {
                emit(Resource.Error(parseErrorMessage(response.errorBody()?.string())))
            }
        } catch (e: Exception) {
            emit(Resource.Error(ApiConstants.resolveNetworkErrorMessage(e), throwable = e))
        }
    }.flowOn(Dispatchers.IO)

    // ==========================================
    // Support
    // ==========================================
    fun getSupportTickets(userId: Int? = null, email: String? = null): Flow<Resource<SupportTicketsResponse>> = flow {
        emit(Resource.Loading)
        try {
            val resolvedUid = userId ?: sessionManager?.getUser()?.id
            val resolvedEmail = email ?: sessionManager?.getUserEmail()
            val response = apiService.getSupportTickets(userId = resolvedUid, email = resolvedEmail)
            if (response.isSuccessful && response.body() != null) {
                val body = response.body()!!
                if (body.success) {
                    emit(Resource.Success(body))
                } else {
                    emit(Resource.Error(body.message ?: "Failed to load support tickets"))
                }
            } else {
                emit(Resource.Error(parseErrorMessage(response.errorBody()?.string())))
            }
        } catch (e: Exception) {
            emit(Resource.Error(ApiConstants.resolveNetworkErrorMessage(e), throwable = e))
        }
    }.flowOn(Dispatchers.IO)

    fun submitSupportTicket(subject: String, message: String, userId: Int? = null, email: String? = null): Flow<Resource<Boolean>> = flow {
        emit(Resource.Loading)
        try {
            val resolvedUid = userId ?: sessionManager?.getUser()?.id
            val resolvedEmail = email ?: sessionManager?.getUserEmail()
            val req = CreateSupportTicketRequest(userId = resolvedUid, email = resolvedEmail, subject = subject, message = message)
            val response = apiService.submitSupportTicket(req)
            if (response.isSuccessful && response.body() != null) {
                val body = response.body()!!
                if (body.success) {
                    emit(Resource.Success(true))
                } else {
                    emit(Resource.Error(body.message ?: "Failed to submit inquiry"))
                }
            } else {
                emit(Resource.Error(parseErrorMessage(response.errorBody()?.string())))
            }
        } catch (e: Exception) {
            emit(Resource.Error(ApiConstants.resolveNetworkErrorMessage(e), throwable = e))
        }
    }.flowOn(Dispatchers.IO)

    private fun parseErrorMessage(errorBody: String?): String {
        if (errorBody.isNullOrBlank()) return "Unknown server response"
        return try {
            val json = JSONObject(errorBody)
            json.optString("message", "Request failed with server error")
        } catch (_: Exception) {
            "An error occurred while connecting to the server"
        }
    }
}