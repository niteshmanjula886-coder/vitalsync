package com.simats.vitalsync.data.model.profile

import com.google.gson.annotations.SerializedName
import com.simats.vitalsync.data.model.auth.User

data class ProfileResponse(
    @SerializedName("success")
    val success: Boolean = false,
    @SerializedName("message")
    val message: String? = null,
    @SerializedName("profile")
    val profile: User? = null
)

data class UpdateProfileRequest(
    @SerializedName("user_id")
    val userId: Int? = null,
    @SerializedName("email")
    val email: String? = null,
    @SerializedName("name")
    val name: String? = null,
    @SerializedName("full_name")
    val fullName: String? = null,
    @SerializedName("phone")
    val phone: String? = null,
    @SerializedName("dob")
    val dob: String? = null,
    @SerializedName("age")
    val age: Int? = null,
    @SerializedName("gender")
    val gender: String? = null,
    @SerializedName("height")
    val height: Float? = null,
    @SerializedName("weight")
    val weight: Float? = null,
    @SerializedName("activity_level")
    val activityLevel: String? = null,
    @SerializedName("primary_goal")
    val primaryGoal: String? = null
)

data class UploadProfileResponse(
    @SerializedName("success")
    val success: Boolean = false,
    @SerializedName("message")
    val message: String? = null,
    @SerializedName("profile_image")
    val profileImage: String? = null,
    @SerializedName("image_url")
    val imageUrl: String? = null
)

data class HealthGoalItem(
    @SerializedName("id")
    val id: String = "",
    @SerializedName("title")
    val title: String = "",
    @SerializedName("badge")
    val badge: String = "",
    @SerializedName("description")
    val description: String = "",
    @SerializedName("icon_type")
    val iconType: String = "FITNESS"
)

data class HealthGoalsResponse(
    @SerializedName("success")
    val success: Boolean = false,
    @SerializedName("message")
    val message: String? = null,
    @SerializedName("selected_goal")
    val selectedGoal: String? = null,
    @SerializedName("available_goals")
    val availableGoals: List<HealthGoalItem> = emptyList()
)

data class SaveHealthGoalRequest(
    @SerializedName("user_id")
    val userId: Int? = null,
    @SerializedName("email")
    val email: String? = null,
    @SerializedName("goal_id")
    val goalId: String? = null,
    @SerializedName("goal_title")
    val goalTitle: String? = null
)

data class ReminderItem(
    @SerializedName("id")
    val id: Int = 0,
    @SerializedName("user_id")
    val userId: Int = 0,
    @SerializedName("title")
    val title: String = "",
    @SerializedName("reminder_type")
    val reminderType: String? = null,
    @SerializedName("time")
    val time: String = "08:00 AM",
    @SerializedName("frequency")
    val frequency: String = "Every day",
    @SerializedName("repeat_days")
    val repeatDays: String? = null,
    @SerializedName("sound_vibration")
    val soundVibration: Boolean = true,
    @SerializedName("is_enabled")
    val isEnabled: Boolean = true,
    @SerializedName("notes")
    val notes: String? = null,
    @SerializedName("created_at")
    val createdAt: String? = null,
    @SerializedName("updated_at")
    val updatedAt: String? = null
)

data class RemindersResponse(
    @SerializedName("success")
    val success: Boolean = false,
    @SerializedName("message")
    val message: String? = null,
    @SerializedName("count")
    val count: Int = 0,
    @SerializedName("reminders")
    val reminders: List<ReminderItem> = emptyList()
)

data class AddReminderRequest(
    @SerializedName("user_id")
    val userId: Int = 0,
    @SerializedName("email")
    val email: String? = null,
    @SerializedName("title")
    val title: String,
    @SerializedName("reminder_type")
    val reminderType: String = "General",
    @SerializedName("time")
    val time: String = "08:00 AM",
    @SerializedName("frequency")
    val frequency: String = "Every day",
    @SerializedName("repeat_days")
    val repeatDays: String = "0,1,2,3,4,5,6",
    @SerializedName("sound_vibration")
    val soundVibration: Boolean = true,
    @SerializedName("is_enabled")
    val isEnabled: Boolean = true,
    @SerializedName("notes")
    val notes: String? = null
)

data class UpdateReminderRequest(
    @SerializedName("id")
    val id: Int,
    @SerializedName("user_id")
    val userId: Int? = null,
    @SerializedName("email")
    val email: String? = null,
    @SerializedName("title")
    val title: String? = null,
    @SerializedName("time")
    val time: String? = null,
    @SerializedName("frequency")
    val frequency: String? = null,
    @SerializedName("is_enabled")
    val isEnabled: Boolean? = null
)

data class AppSettingsDto(
    @SerializedName("user_id")
    val userId: Int = 0,
    @SerializedName("push_notifications")
    val pushNotifications: Boolean = true,
    @SerializedName("vital_alerts")
    val vitalAlerts: Boolean = true,
    @SerializedName("daily_reminders")
    val dailyReminders: Boolean = true,
    @SerializedName("metric_units")
    val metricUnits: String = "metric",
    @SerializedName("dark_mode")
    val darkMode: Boolean = false,
    @SerializedName("data_sync_interval")
    val dataSyncInterval: Int = 15,
    @SerializedName("ai_insights")
    val aiInsights: Boolean = true,
    @SerializedName("usage_analytics")
    val usageAnalytics: Boolean = false,
    @SerializedName("updated_at")
    val updatedAt: String? = null
)

data class AppSettingsResponse(
    @SerializedName("success")
    val success: Boolean = false,
    @SerializedName("status")
    val status: String? = null,
    @SerializedName("message")
    val message: String? = null,
    @SerializedName("settings")
    val settings: AppSettingsDto? = null
)

data class UpdateAppSettingsRequest(
    @SerializedName("user_id")
    val userId: Int? = null,
    @SerializedName("email")
    val email: String? = null,
    @SerializedName("push_notifications")
    val pushNotifications: Boolean? = null,
    @SerializedName("vital_alerts")
    val vitalAlerts: Boolean? = null,
    @SerializedName("daily_reminders")
    val dailyReminders: Boolean? = null,
    @SerializedName("metric_units")
    val metricUnits: String? = null,
    @SerializedName("dark_mode")
    val darkMode: Boolean? = null,
    @SerializedName("data_sync_interval")
    val dataSyncInterval: Int? = null,
    @SerializedName("ai_insights")
    val aiInsights: Boolean? = null,
    @SerializedName("usage_analytics")
    val usageAnalytics: Boolean? = null
)

data class HealthPermissionsDto(
    @SerializedName("vital_signs")
    val vitalSigns: Boolean = true,
    @SerializedName("activity")
    val activity: Boolean = true,
    @SerializedName("sleep")
    val sleep: Boolean = true,
    @SerializedName("hydration")
    val hydration: Boolean = false,
    @SerializedName("write_health_data")
    val writeHealthData: Boolean = true
)

data class HealthPermissionsResponse(
    @SerializedName("success")
    val success: Boolean = false,
    @SerializedName("message")
    val message: String? = null,
    @SerializedName("permissions")
    val permissions: HealthPermissionsDto? = null
)

data class UpdateHealthPermissionsRequest(
    @SerializedName("user_id")
    val userId: Int? = null,
    @SerializedName("email")
    val email: String? = null,
    @SerializedName("vital_signs")
    val vitalSigns: Boolean? = null,
    @SerializedName("activity")
    val activity: Boolean? = null,
    @SerializedName("sleep")
    val sleep: Boolean? = null,
    @SerializedName("hydration")
    val hydration: Boolean? = null,
    @SerializedName("write_health_data")
    val writeHealthData: Boolean? = null
)

data class LoginSessionItem(
    @SerializedName("id")
    val id: Int = 0,
    @SerializedName("device_name")
    val deviceName: String = "",
    @SerializedName("device_os")
    val deviceOs: String? = null,
    @SerializedName("ip_address")
    val ipAddress: String? = null,
    @SerializedName("location")
    val location: String? = null,
    @SerializedName("is_current")
    val isCurrent: Boolean = false,
    @SerializedName("last_active")
    val lastActive: String? = null
)

data class LoginSessionsResponse(
    @SerializedName("success")
    val success: Boolean = false,
    @SerializedName("message")
    val message: String? = null,
    @SerializedName("count")
    val count: Int = 0,
    @SerializedName("sessions")
    val sessions: List<LoginSessionItem> = emptyList()
)

data class LogoutOthersRequest(
    @SerializedName("user_id")
    val userId: Int? = null,
    @SerializedName("email")
    val email: String? = null,
    @SerializedName("action")
    val action: String = "logout_others"
)

data class NotificationItem(
    @SerializedName("id")
    val id: Int = 0,
    @SerializedName("title")
    val title: String = "",
    @SerializedName("description")
    val description: String = "",
    @SerializedName("icon")
    val icon: String? = null,
    @SerializedName("is_read")
    val isRead: Boolean = false,
    @SerializedName("created_at")
    val createdAt: String? = null,
    @SerializedName("time_ago")
    val timeAgo: String? = null
)

data class NotificationsResponse(
    @SerializedName("success")
    val success: Boolean = false,
    @SerializedName("message")
    val message: String? = null,
    @SerializedName("unread_count")
    val unreadCount: Int = 0,
    @SerializedName("notifications")
    val notifications: List<NotificationItem> = emptyList()
)

data class NotificationActionRequest(
    @SerializedName("user_id")
    val userId: Int? = null,
    @SerializedName("email")
    val email: String? = null,
    @SerializedName("action")
    val action: String? = null,
    @SerializedName("id")
    val id: Int? = null
)

data class AppVersionResponse(
    @SerializedName("success")
    val success: Boolean = false,
    @SerializedName("status")
    val status: String? = null,
    @SerializedName("message")
    val message: String? = null,
    @SerializedName("version")
    val version: String = "1.0",
    @SerializedName("version_name")
    val versionName: String = "1.0 - Stable",
    @SerializedName("build_type")
    val buildType: String = "Release",
    @SerializedName("is_up_to_date")
    val isUpToDate: Boolean = true,
    @SerializedName("checked_at")
    val checkedAt: String? = null,
    @SerializedName("features")
    val features: List<String> = emptyList()
)

data class SupportTicketItem(
    @SerializedName("id")
    val id: Int = 0,
    @SerializedName("subject")
    val subject: String = "",
    @SerializedName("message")
    val message: String = "",
    @SerializedName("status")
    val status: String = "open",
    @SerializedName("created_at")
    val createdAt: String? = null
)

data class SupportTicketsResponse(
    @SerializedName("success")
    val success: Boolean = false,
    @SerializedName("status")
    val status: String? = null,
    @SerializedName("message")
    val message: String? = null,
    @SerializedName("email_support")
    val emailSupport: String? = null,
    @SerializedName("hours")
    val hours: String? = null,
    @SerializedName("tickets")
    val tickets: List<SupportTicketItem> = emptyList()
)

data class CreateSupportTicketRequest(
    @SerializedName("user_id")
    val userId: Int? = null,
    @SerializedName("email")
    val email: String? = null,
    @SerializedName("subject")
    val subject: String = "General In-App Inquiry",
    @SerializedName("message")
    val message: String
)

data class BaseProfileResponse(
    @SerializedName("success")
    val success: Boolean = false,
    @SerializedName("message")
    val message: String? = null
)