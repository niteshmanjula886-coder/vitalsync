package com.simats.vitalsync.data.model.auth

import com.google.gson.annotations.SerializedName

/**
 * Authenticated User data model matching the VitalSync backend schema.
 */
data class User(
    @SerializedName("id")
    val id: Int = 0,

    @SerializedName("name")
    val name: String? = null,

    @SerializedName("full_name")
    val fullName: String? = null,

    @SerializedName("email")
    val email: String = "",

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
    val primaryGoal: String? = "Fitness",

    @SerializedName("profile_image")
    val profileImage: String? = null,

    @SerializedName("image_url")
    val imageUrl: String? = null,

    @SerializedName("created_at")
    val createdAt: String? = null,

    @SerializedName("updated_at")
    val updatedAt: String? = null
) {
    /**
     * Display name fallback
     */
    val displayName: String
        get() = fullName?.ifBlank { null } ?: name?.ifBlank { null } ?: email.substringBefore("@").replaceFirstChar { it.uppercase() }

    /**
     * User initials (e.g. "Alex Morgan" -> "AM")
     */
    val initials: String
        get() {
            val parts = displayName.trim().split(" ").filter { it.isNotBlank() }
            return if (parts.size >= 2) {
                (parts[0].take(1) + parts[1].take(1)).uppercase()
            } else if (parts.isNotEmpty()) {
                parts[0].take(2).uppercase()
            } else {
                "VS"
            }
        }
}

/**
 * Standard API response wrapper for authentication operations.
 */
data class AuthResponse(
    @SerializedName("success")
    val success: Boolean = false,

    @SerializedName("message")
    val message: String = "",

    @SerializedName("user")
    val user: User? = null
)

/**
 * Base API response containing status and message.
 */
data class BaseResponse(
    @SerializedName("success")
    val success: Boolean = false,

    @SerializedName("message")
    val message: String = ""
)

/**
 * Payload for logging in an existing user.
 */
data class LoginRequest(
    @SerializedName("email")
    val email: String,

    @SerializedName("password")
    val password: String,

    @SerializedName("device_name")
    val deviceName: String = "Android Device"
)

/**
 * Payload for registering a new user account.
 */
data class SignUpRequest(
    @SerializedName("name")
    val name: String,

    @SerializedName("email")
    val email: String,

    @SerializedName("password")
    val password: String,

    @SerializedName("confirm_password")
    val confirmPassword: String,

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
    val primaryGoal: String? = "Fitness"
)

/**
 * Payload for resetting a user's password.
 */
data class ResetPasswordRequest(
    @SerializedName("email")
    val email: String,

    @SerializedName("new_password")
    val newPassword: String,

    @SerializedName("confirm_password")
    val confirmPassword: String
)

