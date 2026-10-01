package com.arya.sapworkpilot.data.remote

import com.arya.sapworkpilot.data.remote.dto.LoginRequestDto
import com.arya.sapworkpilot.data.remote.dto.TokenResponseDto
import com.arya.sapworkpilot.data.remote.dto.UserPublicDto
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST

interface AuthApi {

    @POST("auth/login")
    suspend fun login(@Body request: LoginRequestDto): TokenResponseDto

    @GET("auth/me")
    suspend fun me(): UserPublicDto
}