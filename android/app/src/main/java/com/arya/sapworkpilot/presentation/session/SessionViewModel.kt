package com.arya.sapworkpilot.presentation.session

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.arya.sapworkpilot.data.local.TokenStore
import com.arya.sapworkpilot.data.remote.AuthApi
import com.arya.sapworkpilot.domain.repository.AuthRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

data class CurrentUser(val name: String, val email: String, val role: String)

@HiltViewModel
class SessionViewModel @Inject constructor(
    private val authRepository: AuthRepository,
    private val tokenStore: TokenStore,
    private val authApi: AuthApi
) : ViewModel() {

    val isLoggedIn: StateFlow<Boolean?> = authRepository.isLoggedIn
        .stateIn(viewModelScope, SharingStarted.Eagerly, null)

    val role: StateFlow<String?> = tokenStore.role
        .stateIn(viewModelScope, SharingStarted.Eagerly, null)

    private val _currentUser = MutableStateFlow<CurrentUser?>(null)
    val currentUser: StateFlow<CurrentUser?> = _currentUser.asStateFlow()

    init {
        viewModelScope.launch {
            isLoggedIn.collect { loggedIn ->
                if (loggedIn == true) loadCurrentUser()
            }
        }
    }

    private suspend fun loadCurrentUser() {
        try {
            val user = authApi.me()
            _currentUser.value = CurrentUser(user.name, user.email, user.role)
        } catch (e: Exception) {
            _currentUser.value = null
        }
    }

    fun logout() {
        viewModelScope.launch { authRepository.logout() }
    }
}