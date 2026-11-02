package com.simats.vitalsync.ui.profile.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.simats.vitalsync.data.model.auth.User
import com.simats.vitalsync.data.model.profile.AddReminderRequest
import com.simats.vitalsync.data.model.profile.AppSettingsDto
import com.simats.vitalsync.data.model.profile.AppVersionResponse
import com.simats.vitalsync.data.model.profile.HealthGoalItem
import com.simats.vitalsync.data.model.profile.HealthPermissionsDto
import com.simats.vitalsync.data.model.profile.LoginSessionItem
import com.simats.vitalsync.data.model.profile.NotificationItem
import com.simats.vitalsync.data.model.profile.ReminderItem
import com.simats.vitalsync.data.model.profile.SaveHealthGoalRequest
import com.simats.vitalsync.data.model.profile.SupportTicketItem
import com.simats.vitalsync.data.model.profile.UpdateAppSettingsRequest
import com.simats.vitalsync.data.model.profile.UpdateHealthPermissionsRequest
import com.simats.vitalsync.data.model.profile.UpdateProfileRequest
import com.simats.vitalsync.data.model.profile.UpdateReminderRequest
import com.simats.vitalsync.data.repository.ProfileRepository
import com.simats.vitalsync.data.repository.Resource
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.io.File

data class ProfileUiState(
    val userId: Int = 0,
    val fullName: String = "",
    val initials: String = "--",
    val primaryGoal: String? = null,
    val age: Int? = null,
    val heightCm: Double? = null,
    val weightKg: Double? = null,
    val bloodGroup: String? = null,
    val phone: String? = null,
    val dob: String? = null,
    val gender: String? = null,
    val email: String = "",
    val targetSteps: Int? = null,
    val targetSleepHours: Double? = null,
    val targetWaterMl: Int? = null,
    val targetCalories: Int? = null,
    val pushNotifications: Boolean = true,
    val vitalAlerts: Boolean = true,
    val dailyReminders: Boolean = true,
    val isMetric: Boolean = true,
    val isLoading: Boolean = false,
    val user: User? = null,
    val errorMessage: String? = null,
    val successMessage: String? = null
)

data class HealthGoalsUiState(
    val isLoading: Boolean = false,
    val selectedGoal: String = "Improve Fitness",
    val availableGoals: List<HealthGoalItem> = emptyList(),
    val errorMessage: String? = null
)

data class RemindersUiState(
    val isLoading: Boolean = false,
    val reminders: List<ReminderItem> = emptyList(),
    val errorMessage: String? = null
)

data class UploadAvatarUiState(
    val isUploading: Boolean = false,
    val uploadedImageUrl: String? = null,
    val errorMessage: String? = null
)

data class AppSettingsUiState(
    val isLoading: Boolean = false,
    val pushNotifications: Boolean = true,
    val vitalAlerts: Boolean = true,
    val dailyReminders: Boolean = true,
    val metricUnits: String = "metric",
    val darkMode: Boolean = false,
    val dataSyncInterval: Int = 15,
    val aiInsights: Boolean = true,
    val usageAnalytics: Boolean = false,
    val aiInsightsEnabled: Boolean = true,
    val userAnalyticsEnabled: Boolean = false,
    val reminderWaterInterval: Int = 120,
    val reminderWorkoutEnabled: Boolean = true,
    val reminderSleepEnabled: Boolean = true,
    val errorMessage: String? = null,
    val successMessage: String? = null
)

data class WeeklyReportUiState(
    val isLoading: Boolean = false,
    val report: com.simats.vitalsync.data.model.profile.WeeklyReportDto? = null,
    val errorMessage: String? = null
)

data class HealthPermissionsUiState(
    val isLoading: Boolean = false,
    val vitalSigns: Boolean = true,
    val activity: Boolean = true,
    val sleep: Boolean = true,
    val hydration: Boolean = false,
    val writeHealthData: Boolean = true,
    val errorMessage: String? = null,
    val successMessage: String? = null
)

data class LoginSessionsUiState(
    val isLoading: Boolean = false,
    val sessions: List<LoginSessionItem> = emptyList(),
    val errorMessage: String? = null,
    val successMessage: String? = null
)

data class NotificationsUiState(
    val isLoading: Boolean = false,
    val unreadCount: Int = 0,
    val notifications: List<NotificationItem> = emptyList(),
    val errorMessage: String? = null,
    val successMessage: String? = null
)

data class AppVersionUiState(
    val isLoading: Boolean = false,
    val version: String = "1.0",
    val versionName: String = "1.0 - Stable",
    val buildType: String = "Release",
    val isUpToDate: Boolean = true,
    val checkedAt: String = "Today at 9:38 AM",
    val features: List<String> = emptyList(),
    val errorMessage: String? = null
)

data class SupportUiState(
    val isLoading: Boolean = false,
    val isSubmitting: Boolean = false,
    val tickets: List<SupportTicketItem> = emptyList(),
    val emailSupport: String = "support@vitalsync.com",
    val hours: String = "Mon-Fri, 9 AM - 6 PM",
    val errorMessage: String? = null,
    val successMessage: String? = null
)

class ProfileViewModel(
    private val profileRepository: ProfileRepository
) : ViewModel() {

    private val _profileState = MutableStateFlow(ProfileUiState())
    val profileState: StateFlow<ProfileUiState> = _profileState.asStateFlow()

    private val _healthGoalsState = MutableStateFlow(HealthGoalsUiState())
    val healthGoalsState: StateFlow<HealthGoalsUiState> = _healthGoalsState.asStateFlow()

    private val _remindersState = MutableStateFlow(RemindersUiState())
    val remindersState: StateFlow<RemindersUiState> = _remindersState.asStateFlow()

    private val _uploadAvatarState = MutableStateFlow(UploadAvatarUiState())
    val uploadAvatarState: StateFlow<UploadAvatarUiState> = _uploadAvatarState.asStateFlow()

    private val _appSettingsState = MutableStateFlow(AppSettingsUiState())
    val appSettingsState: StateFlow<AppSettingsUiState> = _appSettingsState.asStateFlow()

    private val _weeklyReportState = MutableStateFlow(WeeklyReportUiState())
    val weeklyReportState: StateFlow<WeeklyReportUiState> = _weeklyReportState.asStateFlow()

    private val _healthPermissionsState = MutableStateFlow(HealthPermissionsUiState())
    val healthPermissionsState: StateFlow<HealthPermissionsUiState> = _healthPermissionsState.asStateFlow()

    private val _loginSessionsState = MutableStateFlow(LoginSessionsUiState())
    val loginSessionsState: StateFlow<LoginSessionsUiState> = _loginSessionsState.asStateFlow()

    private val _notificationsState = MutableStateFlow(NotificationsUiState())
    val notificationsState: StateFlow<NotificationsUiState> = _notificationsState.asStateFlow()

    private val _appVersionState = MutableStateFlow(AppVersionUiState())
    val appVersionState: StateFlow<AppVersionUiState> = _appVersionState.asStateFlow()

    private val _supportState = MutableStateFlow(SupportUiState())
    val supportState: StateFlow<SupportUiState> = _supportState.asStateFlow()

    fun fetchProfile(userId: Int? = null, email: String? = null) {
        viewModelScope.launch {
            profileRepository.getProfile(userId, email).collect { resource ->
                when (resource) {
                    is Resource.Loading -> {
                        _profileState.update { it.copy(isLoading = true, errorMessage = null) }
                    }
                    is Resource.Success -> {
                        val user = resource.data
                        val name = user.name ?: ""
                        val initialsStr = if (name.isNotBlank()) {
                            val parts = name.trim().split(" ")
                            if (parts.size >= 2) {
                                (parts[0].take(1) + parts[1].take(1)).uppercase()
                            } else {
                                parts.firstOrNull()?.take(2)?.uppercase() ?: "--"
                            }
                        } else "--"

                        _profileState.update {
                            it.copy(
                                isLoading = false,
                                userId = user.id,
                                fullName = name,
                                initials = initialsStr,
                                email = user.email ?: it.email,
                                age = user.age ?: it.age,
                                heightCm = user.height?.toDouble() ?: it.heightCm,
                                weightKg = user.weight?.toDouble() ?: it.weightKg,
                                phone = user.phone ?: it.phone,
                                dob = user.dob ?: it.dob,
                                gender = user.gender ?: it.gender,
                                primaryGoal = user.primaryGoal ?: it.primaryGoal,
                                user = user,
                                errorMessage = null
                            )
                        }

                        fetchAppSettings(user.id, user.email)
                        fetchHealthPermissions(user.id, user.email)
                        fetchReminders(user.id, user.email)
                        fetchNotifications(user.id, user.email)
                        fetchLoginSessions(user.id, user.email)
                    }
                    is Resource.Error -> {
                        _profileState.update {
                            it.copy(isLoading = false, errorMessage = resource.message)
                        }
                    }
                }
            }
        }
    }

    fun updatePersonalInformation(
        newName: String,
        newAge: Int,
        newHeightCm: Double,
        newWeightKg: Double,
        newPhone: String = "",
        newDob: String = "",
        newGender: String = "",
        newBloodGroup: String = "",
        userId: Int? = null,
        email: String? = null
    ) {
        val parts = newName.trim().split(" ")
        val initialsStr = if (parts.size >= 2) {
            (parts[0].take(1) + parts[1].take(1)).uppercase()
        } else {
            parts.firstOrNull()?.take(2)?.uppercase() ?: "AM"
        }

        _profileState.update {
            it.copy(
                fullName = newName,
                initials = initialsStr,
                age = newAge,
                heightCm = newHeightCm,
                weightKg = newWeightKg,
                phone = newPhone.ifBlank { it.phone },
                dob = newDob.ifBlank { it.dob },
                gender = newGender.ifBlank { it.gender },
                bloodGroup = newBloodGroup.ifBlank { it.bloodGroup },
                successMessage = "Personal information updated"
            )
        }

        viewModelScope.launch {
            val req = UpdateProfileRequest(
                userId = userId ?: _profileState.value.userId,
                email = email ?: _profileState.value.email,
                name = newName,
                fullName = newName,
                age = newAge,
                height = newHeightCm.toFloat(),
                weight = newWeightKg.toFloat(),
                phone = newPhone,
                dob = newDob,
                gender = newGender
            )
            profileRepository.updateProfile(req).collect { resource ->
                when (resource) {
                    is Resource.Loading -> {}
                    is Resource.Success -> {
                        _profileState.update {
                            it.copy(
                                user = resource.data,
                                successMessage = "Profile synced with backend",
                                errorMessage = null
                            )
                        }
                    }
                    is Resource.Error -> {
                        _profileState.update {
                            it.copy(errorMessage = resource.message)
                        }
                    }
                }
            }
        }
    }

    fun uploadProfileImage(imageFile: File, userId: Int? = null, email: String? = null) {
        viewModelScope.launch {
            profileRepository.uploadProfilePicture(userId, email, imageFile).collect { resource ->
                when (resource) {
                    is Resource.Loading -> {
                        _uploadAvatarState.update { it.copy(isUploading = true, errorMessage = null) }
                    }
                    is Resource.Success -> {
                        val body = resource.data
                        _uploadAvatarState.update {
                            it.copy(
                                isUploading = false,
                                uploadedImageUrl = body.imageUrl ?: body.profileImage,
                                errorMessage = null
                            )
                        }
                        if (body.imageUrl != null) {
                            _profileState.update { ps ->
                                ps.copy(user = ps.user?.copy(imageUrl = body.imageUrl, profileImage = body.profileImage))
                            }
                        }
                    }
                    is Resource.Error -> {
                        _uploadAvatarState.update {
                            it.copy(isUploading = false, errorMessage = resource.message)
                        }
                    }
                }
            }
        }
    }

    fun fetchHealthGoals(userId: Int? = null, email: String? = null) {
        viewModelScope.launch {
            profileRepository.getHealthGoals(userId, email).collect { resource ->
                when (resource) {
                    is Resource.Loading -> {
                        _healthGoalsState.update { it.copy(isLoading = true, errorMessage = null) }
                    }
                    is Resource.Success -> {
                        _healthGoalsState.update {
                            it.copy(
                                isLoading = false,
                                selectedGoal = resource.data.selectedGoal ?: it.selectedGoal,
                                availableGoals = resource.data.availableGoals,
                                errorMessage = null
                            )
                        }
                    }
                    is Resource.Error -> {
                        _healthGoalsState.update {
                            it.copy(isLoading = false, errorMessage = resource.message)
                        }
                    }
                }
            }
        }
    }

    fun saveHealthGoal(goalId: String, goalTitle: String, userId: Int? = null, email: String? = null) {
        _profileState.update { it.copy(primaryGoal = goalTitle) }
        _healthGoalsState.update { it.copy(selectedGoal = goalTitle) }

        viewModelScope.launch {
            val req = SaveHealthGoalRequest(userId = userId, email = email, goalId = goalId, goalTitle = goalTitle)
            profileRepository.saveHealthGoal(req).collect { resource ->
                when (resource) {
                    is Resource.Loading -> {
                        _healthGoalsState.update { it.copy(isLoading = true, errorMessage = null) }
                    }
                    is Resource.Success -> {
                        val selected = resource.data.selectedGoal ?: goalTitle
                        _healthGoalsState.update {
                            it.copy(
                                isLoading = false,
                                selectedGoal = selected,
                                errorMessage = null
                            )
                        }
                        _profileState.update { it.copy(primaryGoal = selected) }
                    }
                    is Resource.Error -> {
                        _healthGoalsState.update {
                            it.copy(isLoading = false, errorMessage = resource.message)
                        }
                    }
                }
            }
        }
    }

    fun fetchReminders(userId: Int? = null, email: String? = null) {
        viewModelScope.launch {
            profileRepository.getReminders(userId, email).collect { resource ->
                when (resource) {
                    is Resource.Loading -> {
                        _remindersState.update { it.copy(isLoading = true, errorMessage = null) }
                    }
                    is Resource.Success -> {
                        _remindersState.update {
                            it.copy(isLoading = false, reminders = resource.data, errorMessage = null)
                        }
                    }
                    is Resource.Error -> {
                        _remindersState.update {
                            it.copy(isLoading = false, errorMessage = resource.message)
                        }
                    }
                }
            }
        }
    }

    fun toggleReminder(reminder: ReminderItem, isEnabled: Boolean, userId: Int? = null, email: String? = null) {
        _remindersState.update { state ->
            val updated = state.reminders.map { r ->
                if (r.id == reminder.id) r.copy(isEnabled = isEnabled) else r
            }
            state.copy(reminders = updated)
        }

        viewModelScope.launch {
            val req = UpdateReminderRequest(
                id = reminder.id,
                userId = userId ?: _profileState.value.userId,
                email = email ?: _profileState.value.email,
                isEnabled = isEnabled
            )
            profileRepository.updateReminder(req).collect { resource ->
                if (resource is Resource.Error) {
                    _remindersState.update { state ->
                        val reverted = state.reminders.map { r ->
                            if (r.id == reminder.id) r.copy(isEnabled = !isEnabled) else r
                        }
                        state.copy(reminders = reverted, errorMessage = resource.message)
                    }
                }
            }
        }
    }

    fun addReminder(title: String, type: String, time: String, frequency: String, userId: Int, email: String? = null) {
        viewModelScope.launch {
            val req = AddReminderRequest(
                userId = userId,
                email = email,
                title = title,
                reminderType = type,
                time = time,
                frequency = frequency
            )
            profileRepository.addReminder(req).collect { resource ->
                when (resource) {
                    is Resource.Loading -> {
                        _remindersState.update { it.copy(isLoading = true, errorMessage = null) }
                    }
                    is Resource.Success -> {
                        _remindersState.update {
                            it.copy(isLoading = false, reminders = resource.data, errorMessage = null)
                        }
                    }
                    is Resource.Error -> {
                        _remindersState.update {
                            it.copy(isLoading = false, errorMessage = resource.message)
                        }
                    }
                }
            }
        }
    }

    fun deleteReminder(reminderId: Int, userId: Int) {
        viewModelScope.launch {
            profileRepository.deleteReminder(reminderId, userId).collect { resource ->
                when (resource) {
                    is Resource.Loading -> {
                        _remindersState.update { it.copy(isLoading = true, errorMessage = null) }
                    }
                    is Resource.Success -> {
                        _remindersState.update {
                            it.copy(
                                isLoading = false,
                                reminders = it.reminders.filter { r -> r.id != reminderId },
                                errorMessage = null
                            )
                        }
                    }
                    is Resource.Error -> {
                        _remindersState.update {
                            it.copy(isLoading = false, errorMessage = resource.message)
                        }
                    }
                }
            }
        }
    }

    // ==========================================
    // App Settings
    // ==========================================
    fun fetchAppSettings(userId: Int? = null, email: String? = null) {
        viewModelScope.launch {
            profileRepository.getAppSettings(userId, email).collect { resource ->
                when (resource) {
                    is Resource.Loading -> {
                        _appSettingsState.update { it.copy(isLoading = true, errorMessage = null) }
                    }
                    is Resource.Success -> {
                        val s = resource.data
                        _appSettingsState.update {
                            it.copy(
                                isLoading = false,
                                pushNotifications = s.pushNotifications,
                                vitalAlerts = s.vitalAlerts,
                                dailyReminders = s.dailyReminders,
                                metricUnits = s.metricUnits,
                                darkMode = s.darkMode,
                                dataSyncInterval = s.dataSyncInterval,
                                aiInsights = s.aiInsights,
                                aiInsightsEnabled = s.aiInsightsEnabled,
                                usageAnalytics = s.usageAnalytics,
                                userAnalyticsEnabled = s.userAnalyticsEnabled,
                                reminderWaterInterval = s.reminderWaterInterval,
                                reminderWorkoutEnabled = s.reminderWorkoutEnabled,
                                reminderSleepEnabled = s.reminderSleepEnabled,
                                errorMessage = null
                            )
                        }
                    }
                    is Resource.Error -> {
                        _appSettingsState.update {
                            it.copy(isLoading = false, errorMessage = resource.message)
                        }
                    }
                }
            }
        }
    }

    fun updateAppSettings(
        pushNotifications: Boolean? = null,
        vitalAlerts: Boolean? = null,
        dailyReminders: Boolean? = null,
        metricUnits: String? = null,
        darkMode: Boolean? = null,
        aiInsights: Boolean? = null,
        usageAnalytics: Boolean? = null,
        reminderWaterInterval: Int? = null,
        reminderWorkoutEnabled: Boolean? = null,
        reminderSleepEnabled: Boolean? = null,
        userId: Int? = null,
        email: String? = null
    ) {
        _appSettingsState.update { current ->
            current.copy(
                pushNotifications = pushNotifications ?: current.pushNotifications,
                vitalAlerts = vitalAlerts ?: current.vitalAlerts,
                dailyReminders = dailyReminders ?: current.dailyReminders,
                metricUnits = metricUnits ?: current.metricUnits,
                darkMode = darkMode ?: current.darkMode,
                aiInsights = aiInsights ?: current.aiInsights,
                aiInsightsEnabled = aiInsights ?: current.aiInsightsEnabled,
                usageAnalytics = usageAnalytics ?: current.usageAnalytics,
                userAnalyticsEnabled = usageAnalytics ?: current.userAnalyticsEnabled,
                reminderWaterInterval = reminderWaterInterval ?: current.reminderWaterInterval,
                reminderWorkoutEnabled = reminderWorkoutEnabled ?: current.reminderWorkoutEnabled,
                reminderSleepEnabled = reminderSleepEnabled ?: current.reminderSleepEnabled,
                successMessage = "Settings updated"
            )
        }

        viewModelScope.launch {
            val req = UpdateAppSettingsRequest(
                userId = userId ?: _profileState.value.userId,
                email = email ?: _profileState.value.email,
                pushNotifications = pushNotifications,
                vitalAlerts = vitalAlerts,
                dailyReminders = dailyReminders,
                metricUnits = metricUnits,
                darkMode = darkMode,
                aiInsights = aiInsights,
                aiInsightsEnabled = aiInsights,
                usageAnalytics = usageAnalytics,
                userAnalyticsEnabled = usageAnalytics,
                reminderWaterInterval = reminderWaterInterval,
                reminderWorkoutEnabled = reminderWorkoutEnabled,
                reminderSleepEnabled = reminderSleepEnabled
            )
            profileRepository.updateAppSettings(req).collect { resource ->
                when (resource) {
                    is Resource.Loading -> {}
                    is Resource.Success -> {
                        val s = resource.data
                        _appSettingsState.update {
                            it.copy(
                                pushNotifications = s.pushNotifications,
                                vitalAlerts = s.vitalAlerts,
                                dailyReminders = s.dailyReminders,
                                metricUnits = s.metricUnits,
                                darkMode = s.darkMode,
                                dataSyncInterval = s.dataSyncInterval,
                                aiInsights = s.aiInsights,
                                aiInsightsEnabled = s.aiInsightsEnabled,
                                usageAnalytics = s.usageAnalytics,
                                userAnalyticsEnabled = s.userAnalyticsEnabled,
                                reminderWaterInterval = s.reminderWaterInterval,
                                reminderWorkoutEnabled = s.reminderWorkoutEnabled,
                                reminderSleepEnabled = s.reminderSleepEnabled,
                                successMessage = "Settings saved to cloud",
                                errorMessage = null
                            )
                        }
                    }
                    is Resource.Error -> {
                        _appSettingsState.update {
                            it.copy(errorMessage = resource.message)
                        }
                    }
                }
            }
        }
    }

    fun fetchWeeklyHealthReport(userId: Int? = null, email: String? = null) {
        viewModelScope.launch {
            _weeklyReportState.update { it.copy(isLoading = true, errorMessage = null) }
            val resolvedUid = userId ?: _profileState.value.userId
            val resolvedEmail = email ?: _profileState.value.email
            profileRepository.getWeeklyHealthReport(userId = resolvedUid, email = resolvedEmail).collect { resource ->
                when (resource) {
                    is Resource.Loading -> _weeklyReportState.update { it.copy(isLoading = true) }
                    is Resource.Success -> _weeklyReportState.update { it.copy(isLoading = false, report = resource.data, errorMessage = null) }
                    is Resource.Error -> _weeklyReportState.update { it.copy(isLoading = false, errorMessage = resource.message) }
                }
            }
        }
    }

    // ==========================================
    // Health Permissions
    // ==========================================
    fun fetchHealthPermissions(userId: Int? = null, email: String? = null) {
        viewModelScope.launch {
            profileRepository.getHealthPermissions(userId, email).collect { resource ->
                when (resource) {
                    is Resource.Loading -> {
                        _healthPermissionsState.update { it.copy(isLoading = true, errorMessage = null) }
                    }
                    is Resource.Success -> {
                        val p = resource.data
                        _healthPermissionsState.update {
                            it.copy(
                                isLoading = false,
                                vitalSigns = p.vitalSigns,
                                activity = p.activity,
                                sleep = p.sleep,
                                hydration = p.hydration,
                                writeHealthData = p.writeHealthData,
                                errorMessage = null
                            )
                        }
                    }
                    is Resource.Error -> {
                        _healthPermissionsState.update {
                            it.copy(isLoading = false, errorMessage = resource.message)
                        }
                    }
                }
            }
        }
    }

    fun saveHealthPermissions(
        vitalSigns: Boolean,
        activity: Boolean,
        sleep: Boolean,
        hydration: Boolean,
        writeHealthData: Boolean,
        userId: Int? = null,
        email: String? = null
    ) {
        _healthPermissionsState.update {
            it.copy(
                vitalSigns = vitalSigns,
                activity = activity,
                sleep = sleep,
                hydration = hydration,
                writeHealthData = writeHealthData,
                isLoading = true,
                errorMessage = null
            )
        }

        viewModelScope.launch {
            val req = UpdateHealthPermissionsRequest(
                userId = userId ?: _profileState.value.userId,
                email = email ?: _profileState.value.email,
                vitalSigns = vitalSigns,
                activity = activity,
                sleep = sleep,
                hydration = hydration,
                writeHealthData = writeHealthData
            )
            profileRepository.updateHealthPermissions(req).collect { resource ->
                when (resource) {
                    is Resource.Loading -> {}
                    is Resource.Success -> {
                        val p = resource.data
                        _healthPermissionsState.update {
                            it.copy(
                                isLoading = false,
                                vitalSigns = p.vitalSigns,
                                activity = p.activity,
                                sleep = p.sleep,
                                hydration = p.hydration,
                                writeHealthData = p.writeHealthData,
                                successMessage = "Permissions saved successfully",
                                errorMessage = null
                            )
                        }
                    }
                    is Resource.Error -> {
                        _healthPermissionsState.update {
                            it.copy(isLoading = false, errorMessage = resource.message)
                        }
                    }
                }
            }
        }
    }

    // ==========================================
    // Login Sessions
    // ==========================================
    fun fetchLoginSessions(userId: Int? = null, email: String? = null) {
        viewModelScope.launch {
            profileRepository.getLoginSessions(userId, email).collect { resource ->
                when (resource) {
                    is Resource.Loading -> {
                        _loginSessionsState.update { it.copy(isLoading = true, errorMessage = null) }
                    }
                    is Resource.Success -> {
                        _loginSessionsState.update {
                            it.copy(isLoading = false, sessions = resource.data, errorMessage = null)
                        }
                    }
                    is Resource.Error -> {
                        _loginSessionsState.update {
                            it.copy(isLoading = false, errorMessage = resource.message)
                        }
                    }
                }
            }
        }
    }

    fun logoutOtherSessions(userId: Int? = null, email: String? = null) {
        viewModelScope.launch {
            profileRepository.logoutOtherSessions(userId, email).collect { resource ->
                when (resource) {
                    is Resource.Loading -> {
                        _loginSessionsState.update { it.copy(isLoading = true, errorMessage = null) }
                    }
                    is Resource.Success -> {
                        _loginSessionsState.update {
                            it.copy(
                                isLoading = false,
                                sessions = it.sessions.filter { s -> s.isCurrent },
                                successMessage = "Logged out from other devices",
                                errorMessage = null
                            )
                        }
                    }
                    is Resource.Error -> {
                        _loginSessionsState.update {
                            it.copy(isLoading = false, errorMessage = resource.message)
                        }
                    }
                }
            }
        }
    }

    // ==========================================
    // Notifications
    // ==========================================
    fun fetchNotifications(userId: Int? = null, email: String? = null) {
        viewModelScope.launch {
            profileRepository.getNotifications(userId, email).collect { resource ->
                when (resource) {
                    is Resource.Loading -> {
                        _notificationsState.update { it.copy(isLoading = true, errorMessage = null) }
                    }
                    is Resource.Success -> {
                        _notificationsState.update {
                            it.copy(
                                isLoading = false,
                                unreadCount = resource.data.unreadCount,
                                notifications = resource.data.notifications,
                                errorMessage = null
                            )
                        }
                    }
                    is Resource.Error -> {
                        _notificationsState.update {
                            it.copy(isLoading = false, errorMessage = resource.message)
                        }
                    }
                }
            }
        }
    }

    fun markAllNotificationsRead(userId: Int? = null, email: String? = null) {
        _notificationsState.update { state ->
            val updated = state.notifications.map { it.copy(isRead = true) }
            state.copy(notifications = updated, unreadCount = 0)
        }

        viewModelScope.launch {
            profileRepository.markAllNotificationsRead(userId, email).collect { resource ->
                if (resource is Resource.Error) {
                    _notificationsState.update { it.copy(errorMessage = resource.message) }
                }
            }
        }
    }

    // ==========================================
    // App Version
    // ==========================================
    fun fetchAppVersion() {
        viewModelScope.launch {
            profileRepository.getAppVersion().collect { resource ->
                when (resource) {
                    is Resource.Loading -> {
                        _appVersionState.update { it.copy(isLoading = true, errorMessage = null) }
                    }
                    is Resource.Success -> {
                        val v = resource.data
                        _appVersionState.update {
                            it.copy(
                                isLoading = false,
                                version = v.version,
                                versionName = v.versionName,
                                buildType = v.buildType,
                                isUpToDate = v.isUpToDate,
                                checkedAt = v.checkedAt ?: "Today at 9:38 AM",
                                features = v.features,
                                errorMessage = null
                            )
                        }
                    }
                    is Resource.Error -> {
                        _appVersionState.update {
                            it.copy(isLoading = false, errorMessage = resource.message)
                        }
                    }
                }
            }
        }
    }

    // ==========================================
    // Support
    // ==========================================
    fun fetchSupportTickets(userId: Int? = null, email: String? = null) {
        viewModelScope.launch {
            profileRepository.getSupportTickets(userId, email).collect { resource ->
                when (resource) {
                    is Resource.Loading -> {
                        _supportState.update { it.copy(isLoading = true, errorMessage = null) }
                    }
                    is Resource.Success -> {
                        val data = resource.data
                        _supportState.update {
                            it.copy(
                                isLoading = false,
                                tickets = data.tickets,
                                emailSupport = data.emailSupport ?: it.emailSupport,
                                hours = data.hours ?: it.hours,
                                errorMessage = null
                            )
                        }
                    }
                    is Resource.Error -> {
                        _supportState.update {
                            it.copy(isLoading = false, errorMessage = resource.message)
                        }
                    }
                }
            }
        }
    }

    fun submitSupportTicket(
        subject: String,
        message: String,
        userId: Int? = null,
        email: String? = null,
        onSuccess: () -> Unit = {}
    ) {
        viewModelScope.launch {
            profileRepository.submitSupportTicket(subject, message, userId, email).collect { resource ->
                when (resource) {
                    is Resource.Loading -> {
                        _supportState.update { it.copy(isSubmitting = true, errorMessage = null) }
                    }
                    is Resource.Success -> {
                        _supportState.update {
                            it.copy(
                                isSubmitting = false,
                                successMessage = "Inquiry submitted successfully",
                                errorMessage = null
                            )
                        }
                        onSuccess()
                        fetchSupportTickets(userId, email)
                    }
                    is Resource.Error -> {
                        _supportState.update {
                            it.copy(isSubmitting = false, errorMessage = resource.message)
                        }
                    }
                }
            }
        }
    }

    fun clearMessages() {
        _profileState.update { it.copy(errorMessage = null, successMessage = null) }
        _healthGoalsState.update { it.copy(errorMessage = null) }
        _remindersState.update { it.copy(errorMessage = null) }
        _uploadAvatarState.update { it.copy(errorMessage = null) }
        _appSettingsState.update { it.copy(errorMessage = null, successMessage = null) }
        _healthPermissionsState.update { it.copy(errorMessage = null, successMessage = null) }
        _loginSessionsState.update { it.copy(errorMessage = null, successMessage = null) }
        _notificationsState.update { it.copy(errorMessage = null, successMessage = null) }
        _supportState.update { it.copy(errorMessage = null, successMessage = null) }
        _weeklyReportState.update { it.copy(errorMessage = null) }
    }
}
