package com.simats.vitalsync.data.network

import com.simats.vitalsync.data.model.profile.AddReminderRequest
import com.simats.vitalsync.data.model.profile.AppSettingsResponse
import com.simats.vitalsync.data.model.profile.AppVersionResponse
import com.simats.vitalsync.data.model.profile.BaseProfileResponse
import com.simats.vitalsync.data.model.profile.CreateSupportTicketRequest
import com.simats.vitalsync.data.model.profile.HealthGoalsResponse
import com.simats.vitalsync.data.model.profile.HealthPermissionsResponse
import com.simats.vitalsync.data.model.profile.LoginSessionsResponse
import com.simats.vitalsync.data.model.profile.LogoutOthersRequest
import com.simats.vitalsync.data.model.profile.NotificationActionRequest
import com.simats.vitalsync.data.model.profile.NotificationsResponse
import com.simats.vitalsync.data.model.profile.ProfileResponse
import com.simats.vitalsync.data.model.profile.RemindersResponse
import com.simats.vitalsync.data.model.profile.SaveHealthGoalRequest
import com.simats.vitalsync.data.model.profile.SupportTicketsResponse
import com.simats.vitalsync.data.model.profile.UpdateAppSettingsRequest
import com.simats.vitalsync.data.model.profile.UpdateHealthPermissionsRequest
import com.simats.vitalsync.data.model.profile.UpdateProfileRequest
import com.simats.vitalsync.data.model.profile.UpdateReminderRequest
import com.simats.vitalsync.data.model.profile.UploadProfileResponse
import okhttp3.MultipartBody
import okhttp3.RequestBody
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.DELETE
import retrofit2.http.GET
import retrofit2.http.Multipart
import retrofit2.http.POST
import retrofit2.http.Part
import retrofit2.http.Query

interface ProfileApiService {

    @GET("auth/profile.php")
    suspend fun getProfile(
        @Query("user_id") userId: Int? = null,
        @Query("email") email: String? = null
    ): Response<ProfileResponse>

    @POST("auth/profile.php")
    suspend fun updateProfile(
        @Body body: UpdateProfileRequest
    ): Response<ProfileResponse>

    @Multipart
    @POST("auth/upload_profile.php")
    suspend fun uploadProfileImage(
        @Part("user_id") userId: RequestBody? = null,
        @Part("email") email: RequestBody? = null,
        @Part profileImage: MultipartBody.Part
    ): Response<UploadProfileResponse>

    @GET("auth/health_goals.php")
    suspend fun getHealthGoals(
        @Query("user_id") userId: Int? = null,
        @Query("email") email: String? = null
    ): Response<HealthGoalsResponse>

    @POST("api/user/update_health_goals.php")
    suspend fun saveHealthGoal(
        @Body body: SaveHealthGoalRequest
    ): Response<HealthGoalsResponse>

    @GET("auth/reminders.php")
    suspend fun getReminders(
        @Query("user_id") userId: Int? = null,
        @Query("email") email: String? = null
    ): Response<RemindersResponse>

    @POST("auth/reminders.php")
    suspend fun addReminder(
        @Body body: AddReminderRequest
    ): Response<RemindersResponse>

    @POST("auth/reminders.php")
    suspend fun updateReminder(
        @Body body: UpdateReminderRequest
    ): Response<BaseProfileResponse>

    @DELETE("auth/reminders.php")
    suspend fun deleteReminder(
        @Query("id") id: Int,
        @Query("user_id") userId: Int
    ): Response<BaseProfileResponse>

    @GET("settings/app_settings.php")
    suspend fun getAppSettings(
        @Query("user_id") userId: Int? = null,
        @Query("email") email: String? = null
    ): Response<AppSettingsResponse>

    @POST("settings/app_settings.php")
    suspend fun updateAppSettings(
        @Body body: UpdateAppSettingsRequest
    ): Response<AppSettingsResponse>

    @GET("settings/permissions.php")
    suspend fun getHealthPermissions(
        @Query("user_id") userId: Int? = null,
        @Query("email") email: String? = null
    ): Response<HealthPermissionsResponse>

    @POST("settings/permissions.php")
    suspend fun updateHealthPermissions(
        @Body body: UpdateHealthPermissionsRequest
    ): Response<HealthPermissionsResponse>

    @GET("auth/login_sessions.php")
    suspend fun getLoginSessions(
        @Query("user_id") userId: Int? = null,
        @Query("email") email: String? = null
    ): Response<LoginSessionsResponse>

    @POST("auth/login_sessions.php")
    suspend fun logoutOtherSessions(
        @Body body: LogoutOthersRequest
    ): Response<BaseProfileResponse>

    @GET("settings/notifications.php")
    suspend fun getNotifications(
        @Query("user_id") userId: Int? = null,
        @Query("email") email: String? = null
    ): Response<NotificationsResponse>

    @POST("settings/notifications.php")
    suspend fun handleNotification(
        @Body body: NotificationActionRequest
    ): Response<BaseProfileResponse>

    @GET("settings/app_version.php")
    suspend fun getAppVersion(): Response<AppVersionResponse>

    @GET("reports/weekly_report.php")
    suspend fun getWeeklyHealthReport(
        @Query("user_id") userId: Int? = null,
        @Query("email") email: String? = null
    ): Response<com.simats.vitalsync.data.model.profile.WeeklyReportResponse>

    @GET("settings/support.php")
    suspend fun getSupportTickets(
        @Query("user_id") userId: Int? = null,
        @Query("email") email: String? = null
    ): Response<SupportTicketsResponse>

    @POST("settings/support.php")
    suspend fun submitSupportTicket(
        @Body body: CreateSupportTicketRequest
    ): Response<BaseProfileResponse>
}