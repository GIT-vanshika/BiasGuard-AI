package com.example.solutionchallenge.ui.navigation

sealed class Screen(val route: String) {
    object Splash : Screen("splash")
    object Login : Screen("login")
    object Home : Screen("home")
    object Upload : Screen("upload")
    object Results : Screen("results/{target}/{protected}/{prediction}") {
        fun createRoute(target: String, protected: String, prediction: String) = "results/$target/$protected/$prediction"
    }
    object Explanation : Screen("explanation/{auditId}") {
        fun createRoute(auditId: String) = "explanation/$auditId"
    }
    object Monitoring : Screen("monitoring")
    object Alerts : Screen("alerts")
}
