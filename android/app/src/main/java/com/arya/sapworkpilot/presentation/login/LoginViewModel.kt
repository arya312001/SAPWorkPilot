package com.arya.sapworkpilot.presentation.login

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.arya.sapworkpilot.data.local.SettingsStore
import com.arya.sapworkpilot.domain.repository.AuthRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

data class LoginUiState(
    val email: String = "",
    val password: String = "",
    val isLoading: Boolean = false,
    val errorMessage: String? = null,
    val isLoggedIn: Boolean = false
)

@HiltViewModel
class LoginViewModel @Inject constructor(
    private val authRepository: AuthRepository,
    settingsStore: SettingsStore
) : ViewModel() {

    private val _uiState = MutableStateFlow(LoginUiState())
    val uiState: StateFlow<LoginUiState> = _uiState.asStateFlow()

    val backendUrl: StateFlow<String> = settingsStore.baseUrl
        .stateIn(viewModelScope, SharingStarted.Eagerly, "")

    fun onEmailChange(value: String) {
        _uiState.value = _uiState.value.copy(email = value, errorMessage = null)
    }

    fun onPasswordChange(value: String) {
        _uiState.value = _uiState.value.copy(password = value, errorMessage = null)
    }

    private fun isValidEmail(email: String): Boolean {
        return email.contains("@") && email.substringAfter("@").contains(".") && !email.startsWith("@")
    }

    fun onLoginClick() {
        val current = _uiState.value

        when {
            current.email.isBlank() || current.password.isBlank() -> {
                _uiState.value = current.copy(errorMessage = "Enter your email and password")
                return
            }
            !isValidEmail(current.email) -> {
                _uiState.value = current.copy(errorMessage = "Enter a valid email address")
                return
            }
            current.isLoading -> return
        }

        _uiState.value = current.copy(isLoading = true, errorMessage = null)
        viewModelScope.launch {
            val result = authRepository.login(current.email, current.password)
            _uiState.value = result.fold(
                onSuccess = { _uiState.value.copy(isLoading = false, isLoggedIn = true) },
                onFailure = {
                    val message = it.message ?: "Login failed"
                    val hint = if (message.contains("reach", ignoreCase = true)) {
                        "$message Check the backend address below."
                    } else {
                        message
                    }
                    _uiState.value.copy(isLoading = false, errorMessage = hint)
                }
            )
        }
    }
}