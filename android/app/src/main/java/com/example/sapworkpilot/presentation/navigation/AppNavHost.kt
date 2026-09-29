package com.example.sapworkpilot.presentation.navigation

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.sapworkpilot.presentation.audit.AuditLogScreen
import com.example.sapworkpilot.presentation.chat.ChatScreen
import com.example.sapworkpilot.presentation.dashboard.DashboardScreen
import com.example.sapworkpilot.presentation.documents.DocumentUploadScreen
import com.example.sapworkpilot.presentation.heatmap.HeatmapScreen
import com.example.sapworkpilot.presentation.login.LoginScreen
import com.example.sapworkpilot.presentation.meetings.MeetingsScreen
import com.example.sapworkpilot.presentation.profile.ProfileScreen
import com.example.sapworkpilot.presentation.risks.RisksScreen
import com.example.sapworkpilot.presentation.session.SessionViewModel
import com.example.sapworkpilot.presentation.settings.SettingsScreen
import com.example.sapworkpilot.sync.SyncScheduler

sealed class Screen(val route: String) {
    object Login : Screen("login")
    object Dashboard : Screen("dashboard")
    object Chat : Screen("chat")
    object Heatmap : Screen("heatmap")
    object Risks : Screen("risks")
    object Meetings : Screen("meetings")
    object Documents : Screen("documents")
    object Settings : Screen("settings")
    object AuditLog : Screen("audit_log")
    object Profile : Screen("profile")
}

@Composable
fun AppNavHost(sessionViewModel: SessionViewModel = hiltViewModel()) {
    val isLoggedIn by sessionViewModel.isLoggedIn.collectAsState()
    val loggedIn = isLoggedIn

    if (loggedIn == null) {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            CircularProgressIndicator()
        }
    } else {
        val context = LocalContext.current
        LaunchedEffect(loggedIn) {
            if (loggedIn) SyncScheduler.schedule(context)
        }
        val startDestination = remember {
            if (loggedIn) Screen.Dashboard.route else Screen.Login.route
        }
        val role by sessionViewModel.role.collectAsState()
        val currentUser by sessionViewModel.currentUser.collectAsState()
        AppNavigation(
            startDestination = startDestination,
            role = role,
            userName = currentUser?.name,
            userEmail = currentUser?.email,
            onLogout = { sessionViewModel.logout() }
        )
    }
}

@Composable
private fun AppNavigation(
    startDestination: String,
    role: String?,
    userName: String?,
    userEmail: String?,
    onLogout: () -> Unit
) {
    val navController = rememberNavController()

    NavHost(
        navController = navController,
        startDestination = startDestination
    ) {
        composable(Screen.Login.route) {
            LoginScreen(
                onLoginSuccess = {
                    navController.navigate(Screen.Dashboard.route) {
                        popUpTo(Screen.Login.route) { inclusive = true }
                    }
                }
            )
        }
        composable(Screen.Dashboard.route) {
            DashboardScreen(
                canManageDocuments = role == "admin" || role == "project_manager",
                userName = userName,
                onOpenChat = { navController.navigate(Screen.Chat.route) },
                onOpenHeatmap = { navController.navigate(Screen.Heatmap.route) },
                onOpenRisks = { navController.navigate(Screen.Risks.route) },
                onOpenMeetings = { navController.navigate(Screen.Meetings.route) },
                onOpenDocuments = { navController.navigate(Screen.Documents.route) },
                onOpenSettings = { navController.navigate(Screen.Settings.route) },
                onOpenAuditLog = { navController.navigate(Screen.AuditLog.route) },
                onOpenProfile = { navController.navigate(Screen.Profile.route) },
                onLogout = {
                    onLogout()
                    navController.navigate(Screen.Login.route) {
                        popUpTo(Screen.Dashboard.route) { inclusive = true }
                    }
                }
            )
        }
        composable(Screen.Chat.route) {
            ChatScreen(onBack = { navController.popBackStack() })
        }
        composable(Screen.Heatmap.route) {
            HeatmapScreen(onBack = { navController.popBackStack() })
        }
        composable(Screen.Risks.route) {
            RisksScreen(onBack = { navController.popBackStack() })
        }
        composable(Screen.Meetings.route) {
            MeetingsScreen(onBack = { navController.popBackStack() })
        }
        composable(Screen.Documents.route) {
            DocumentUploadScreen(onBack = { navController.popBackStack() })
        }
        composable(Screen.Settings.route) {
            SettingsScreen(onBack = { navController.popBackStack() })
        }
        composable(Screen.AuditLog.route) {
            AuditLogScreen(onBack = { navController.popBackStack() })
        }
        composable(Screen.Profile.route) {
            ProfileScreen(
                name = userName,
                email = userEmail,
                role = role,
                onBack = { navController.popBackStack() }
            )
        }
    }
}