package com.example.solutionchallenge.ui.navigation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import com.example.solutionchallenge.data.repository.BiasGuardRepository
import com.example.solutionchallenge.ui.screens.splash.SplashScreen
import com.example.solutionchallenge.ui.screens.home.HomeDashboardScreen
import com.example.solutionchallenge.ui.screens.upload.UploadScreen
import com.example.solutionchallenge.ui.screens.results.ResultsScreen
import com.example.solutionchallenge.ui.screens.explanation.ExplanationScreen
import com.example.solutionchallenge.ui.screens.monitoring.MonitoringScreen
import com.example.solutionchallenge.ui.screens.alerts.AlertsScreen
import com.example.solutionchallenge.ui.viewmodel.ExplanationViewModelSample
import com.example.solutionchallenge.ui.viewmodel.UploadViewModelSample
import com.example.solutionchallenge.ui.viewmodel.ResultsViewModelSample
import com.example.solutionchallenge.ui.viewmodel.MonitoringViewModelSample
import com.example.solutionchallenge.ui.viewmodel.ThemeViewModel

@Composable
fun AppNavigation(
    navController: NavHostController, 
    repository: BiasGuardRepository,
    themeViewModel: ThemeViewModel
) {
    val monitoringViewModel = remember { MonitoringViewModelSample(repository) }
    
    NavHost(navController = navController, startDestination = Screen.Splash.route) {
        composable(Screen.Splash.route) {
            SplashScreen(onNavigateToHome = { navController.navigate(Screen.Home.route) { popUpTo(Screen.Splash.route) { inclusive = true } } })
        }
        composable(Screen.Home.route) {
            HomeDashboardScreen(
                themeViewModel = themeViewModel,
                onNavigateUpload = { navController.navigate(Screen.Upload.route) },
                onNavigateMonitoring = { navController.navigate(Screen.Monitoring.route) },
                onNavigateAlerts = { navController.navigate(Screen.Alerts.route) }
            )
        }
        composable(Screen.Upload.route) {
            val uploadViewModel = remember { UploadViewModelSample(repository) }
            UploadScreen(onNavigateResults = { target, protected, prediction -> navController.navigate(Screen.Results.createRoute(target, protected, prediction)) }, viewModel = uploadViewModel)
        }
        composable(Screen.Results.route) { backStackEntry ->
            val target = backStackEntry.arguments?.getString("target") ?: ""
            val protected = backStackEntry.arguments?.getString("protected") ?: ""
            val prediction = backStackEntry.arguments?.getString("prediction") ?: ""
            val resultsViewModel = remember { ResultsViewModelSample(repository) }
            ResultsScreen(target = target, protectedAttribute = protected, predictionColumn = prediction, onViewExplanation = { auditId -> navController.navigate(Screen.Explanation.createRoute(auditId)) }, viewModel = resultsViewModel)
        }
        composable(Screen.Explanation.route) {
            val explanationViewModel = remember { ExplanationViewModelSample(repository) }
            ExplanationScreen(viewModel = explanationViewModel)
        }
        composable(Screen.Monitoring.route) {
            MonitoringScreen(viewModel = monitoringViewModel)
        }
        composable(Screen.Alerts.route) {
            AlertsScreen(viewModel = monitoringViewModel)
        }
    }
}
