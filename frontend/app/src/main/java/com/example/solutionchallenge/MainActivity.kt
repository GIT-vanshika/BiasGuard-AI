package com.example.solutionchallenge

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.navigation.compose.rememberNavController
import com.example.solutionchallenge.data.api.ApiConfig
import com.example.solutionchallenge.data.api.BiasGuardApiService
import com.example.solutionchallenge.data.repository.RetrofitBiasGuardRepository
import com.example.solutionchallenge.ui.navigation.AppNavigation
import com.example.solutionchallenge.ui.theme.SolutionChallengeTheme
import com.example.solutionchallenge.ui.viewmodel.ThemeViewModel
import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        val okHttpClient = OkHttpClient.Builder()
            .connectTimeout(5, TimeUnit.MINUTES)
            .readTimeout(5, TimeUnit.MINUTES)
            .writeTimeout(5, TimeUnit.MINUTES)
            .build()

        val backendApi = Retrofit.Builder()
            .baseUrl(ApiConfig.BASE_URL)
            .client(okHttpClient)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(BiasGuardApiService::class.java)

        val repository = RetrofitBiasGuardRepository(backendApi)

        enableEdgeToEdge()
        setContent {
            val themeViewModel = remember { ThemeViewModel(applicationContext) }
            val themeMode by themeViewModel.themeMode.collectAsState()

            SolutionChallengeTheme(themeMode = themeMode) {
                Scaffold(
                    modifier = Modifier.fillMaxSize(),
                    containerColor = MaterialTheme.colorScheme.background
                ) { innerPadding ->
                    Box(modifier = Modifier.padding(innerPadding)) {
                        val navController = rememberNavController()
                        AppNavigation(
                            navController = navController, 
                            repository = repository,
                            themeViewModel = themeViewModel
                        )
                    }
                }
            }
        }
    }
}