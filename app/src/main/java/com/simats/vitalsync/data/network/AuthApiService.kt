package com.simats.vitalsync.data.network

import com.simats.vitalsync.data.model.auth.AuthResponse
import com.simats.vitalsync.data.model.auth.BaseResponse
import com.simats.vitalsync.data.model.auth.LoginRequest
import com.simats.vitalsync.data.model.auth.ResetPasswordRequest
import com.simats.vitalsync.data.model.auth.SignUpRequest
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Query

/**
 * Retrofit interface defining the authentication and profile API endpoints.
 */
interface AuthApiService {

    @POST("auth/signup.php")
    suspend fun signUp(
        @Body request: SignUpRequest
    ): Response<AuthResponse>

    @POST("auth/login.php")
    suspend fun login(
        @Body request: LoginRequest
    ): Response<AuthResponse>

    @POST("auth/reset_password.php")
    suspend fun resetPassword(
        @Body request: ResetPasswordRequest
    ): Response<BaseResponse>

    @GET("auth/profile.php")
    suspend fun getProfile(
        @Query("email") email: String
    ): Response<AuthResponse>
}

