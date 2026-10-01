package com.arya.sapworkpilot.presentation.dashboard

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.arya.sapworkpilot.data.local.entity.CachedDashboardDao
import com.arya.sapworkpilot.data.local.entity.CachedDashboardEntity
import com.arya.sapworkpilot.domain.model.Milestone
import com.arya.sapworkpilot.domain.model.Project
import com.arya.sapworkpilot.domain.model.RiskLevel
import com.arya.sapworkpilot.domain.model.TicketStatus
import com.arya.sapworkpilot.domain.repository.ProjectRepository
import com.google.gson.Gson
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

data class DashboardUiState(
    val isLoading: Boolean = true,
    val isRefreshing: Boolean = false,
    val projects: List<Project> = emptyList(),
    val milestones: List<Milestone> = emptyList(),
    val aiSummary: String = "",
    val highRiskCount: Int = 0,
    val blockedCount: Int = 0,
    val overdueCount: Int = 0,
    val isOffline: Boolean = false,
    val lastUpdated: Long = 0L
)
private data class CachedPayload(
    val projects: List<Project>,
    val milestones: List<Milestone>,
    val aiSummary: String,
    val highRiskCount: Int,
    val blockedCount: Int,
    val overdueCount: Int
)

@HiltViewModel
class DashboardViewModel @Inject constructor(
    private val repository: ProjectRepository,
    private val cacheDao: CachedDashboardDao
) : ViewModel() {

    private val _uiState = MutableStateFlow(DashboardUiState())
    val uiState: StateFlow<DashboardUiState> = _uiState.asStateFlow()
    private val gson = Gson()

    init {
        load(isRefresh = false)
    }

    fun onRefresh() {
        if (_uiState.value.isRefreshing) return
        load(isRefresh = true)
    }

    private fun load(isRefresh: Boolean) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(
                isLoading = !isRefresh,
                isRefreshing = isRefresh
            )

            try {
                val projects = repository.getProjects()
                val heatmap = repository.getHeatmap()
                val tickets = repository.getAllTickets()
                val payload = CachedPayload(
                    projects = projects,
                    milestones = repository.getMilestones(),
                    aiSummary = repository.getAiSummary(),
                    highRiskCount = heatmap.count { it.level == RiskLevel.HIGH },
                    blockedCount = tickets.count { it.status == TicketStatus.BLOCKED },
                    overdueCount = tickets.count { it.isOverdue }
                )

                cacheDao.save(
                    CachedDashboardEntity(
                        json = gson.toJson(payload),
                        cachedAt = System.currentTimeMillis()
                    )
                )

                _uiState.value = DashboardUiState(
                    isLoading = false,
                    isRefreshing = false,
                    projects = payload.projects,
                    milestones = payload.milestones,
                    aiSummary = payload.aiSummary,
                    highRiskCount = payload.highRiskCount,
                    blockedCount = payload.blockedCount,
                    overdueCount = payload.overdueCount,
                    isOffline = false,
                    lastUpdated = System.currentTimeMillis()
                )
            } catch (e: Exception) {
                loadFromCacheOrFallback()
            }
        }
    }

    private suspend fun loadFromCacheOrFallback() {
        val cached = cacheDao.get()
        if (cached != null) {
            val payload = gson.fromJson(cached.json, CachedPayload::class.java)
            _uiState.value = DashboardUiState(
                isLoading = false,
                isRefreshing = false,
                projects = payload.projects,
                milestones = payload.milestones,
                aiSummary = payload.aiSummary,
                highRiskCount = payload.highRiskCount,
                blockedCount = payload.blockedCount,
                overdueCount = payload.overdueCount,
                isOffline = true,
                lastUpdated = cached.cachedAt
            )
        } else {
            // No cache and backend unreachable: show an empty offline state rather than
            // retrying the same failing calls. The user can pull-to-refresh once the backend is up.
            _uiState.value = DashboardUiState(
                isLoading = false,
                isRefreshing = false,
                aiSummary = "Backend unreachable. Configure the server address in Settings and pull to refresh.",
                isOffline = true
            )
        }
    }
}