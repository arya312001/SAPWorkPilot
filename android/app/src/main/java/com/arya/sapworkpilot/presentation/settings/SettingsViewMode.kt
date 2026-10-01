package com.arya.sapworkpilot.presentation.settings

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.arya.sapworkpilot.data.local.SettingsStore
import com.arya.sapworkpilot.data.remote.HealthApi
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import okhttp3.OkHttpClient
import okhttp3.Request
import java.util.concurrent.TimeUnit
import javax.inject.Inject

data class SettingsUiState(
    val currentUrl: String = "",
    val input: String = "",
    val savedMessage: String? = null,
    val isTesting: Boolean = false,
    val testResult: String? = null,
    val testSucceeded: Boolean = false,
    val jiraConfigured: Boolean? = null,
    val llmConfigured: Boolean? = null
)

@HiltViewModel
class SettingsViewModel @Inject constructor(
    private val settingsStore: SettingsStore,
    private val healthApi: HealthApi
) : ViewModel() {

    private val _uiState = MutableStateFlow(SettingsUiState())
    val uiState: StateFlow<SettingsUiState> = _uiState.asStateFlow()

    init {
        viewModelScope.launch {
            val current = settingsStore.baseUrl.first()
            _uiState.value = SettingsUiState(currentUrl = current, input = current)
            loadHealthStatus()
        }
    }

    private suspend fun loadHealthStatus() {
        try {
            val health = healthApi.getHealth()
            _uiState.value = _uiState.value.copy(
                jiraConfigured = health.jiraConfigured,
                llmConfigured = health.llmConfigured
            )
        } catch (e: Exception) {
            // Backend unreachable: leave status as unknown, no error shown here
        }
    }

    private fun validateUrl(url: String): String? {
        if (url.isBlank()) return "Enter a backend URL"
        if (!url.startsWith("http://") && !url.startsWith("https://")) {
            return "URL must start with http:// or https://"
        }
        return null
    }

    fun onInputChange(text: String) {
        _uiState.value = _uiState.value.copy(input = text, savedMessage = null, testResult = null)
    }

    fun onSave() {
        val url = _uiState.value.input.trim()
        val error = validateUrl(url)
        if (error != null) {
            _uiState.value = _uiState.value.copy(savedMessage = null, testResult = error, testSucceeded = false)
            return
        }

        viewModelScope.launch {
            settingsStore.saveBaseUrl(url)
            _uiState.value = _uiState.value.copy(
                currentUrl = url,
                savedMessage = "Saved. Restart requests will use the new address."
            )
            loadHealthStatus()
        }
    }

    fun onTestConnection() {
        if (_uiState.value.isTesting) return
        val url = _uiState.value.input.trim()
        val error = validateUrl(url)
        if (error != null) {
            _uiState.value = _uiState.value.copy(testResult = error, testSucceeded = false)
            return
        }

        _uiState.value = _uiState.value.copy(isTesting = true, testResult = null)

        viewModelScope.launch {
            val normalized = if (url.endsWith("/")) url else "$url/"
            val healthUrl = normalized + "health"

            val result = try {
                val client = OkHttpClient.Builder()
                    .connectTimeout(5, TimeUnit.SECONDS)
                    .readTimeout(5, TimeUnit.SECONDS)
                    .build()
                val request = Request.Builder().url(healthUrl).build()
                kotlinx.coroutines.withContext(kotlinx.coroutines.Dispatchers.IO) {
                    client.newCall(request).execute().use { response ->
                        if (response.isSuccessful) "success" else "failed: HTTP ${response.code}"
                    }
                }
            } catch (e: Exception) {
                "failed: ${e.message ?: "unreachable"}"
            }

            _uiState.value = if (result == "success") {
                _uiState.value.copy(
                    isTesting = false,
                    testResult = "Connected successfully",
                    testSucceeded = true
                )
            } else {
                _uiState.value.copy(
                    isTesting = false,
                    testResult = "Could not connect: ${result.removePrefix("failed: ")}",
                    testSucceeded = false
                )
            }

            if (result == "success") loadHealthStatus()
        }
    }
}