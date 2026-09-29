package com.example.sapworkpilot.data.repository

import com.example.sapworkpilot.data.local.TokenStore
import com.example.sapworkpilot.data.remote.AuthApi
import com.example.sapworkpilot.data.remote.dto.LoginRequestDto
import com.example.sapworkpilot.domain.repository.AuthRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import retrofit2.HttpException
import java.io.IOException
import javax.inject.Inject

class AuthRepositoryImpl @Inject constructor(
    private val api: AuthApi,
    private val tokenStore: TokenStore
) : AuthRepository {

    override val isLoggedIn: Flow<Boolean> =
        tokenStore.token.map { !it.isNullOrEmpty() }

    override suspend fun login(email: String, password: String): Result<String> {
        return try {
            val response = api.login(LoginRequestDto(email.trim(), password))
            tokenStore.save(response.accessToken, response.role)
            Result.success(response.role)
        } catch (e: HttpException) {
            if (e.code() == 401) {
                Result.failure(Exception("Invalid email or password"))
            } else {
                Result.failure(Exception("Server error (${e.code()})"))
            }
        } catch (e: IOException) {
            Result.failure(Exception("Cannot reach the server. Is the backend running?"))
        }
    }

    override suspend fun logout() {
        tokenStore.clear()
    }
}