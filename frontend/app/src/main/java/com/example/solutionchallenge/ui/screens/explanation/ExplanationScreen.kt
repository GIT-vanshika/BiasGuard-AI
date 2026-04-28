package com.example.solutionchallenge.ui.screens.explanation

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.solutionchallenge.ui.components.MetricCard

import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.LaunchedEffect
import com.example.solutionchallenge.ui.viewmodel.ExplanationViewModelSample

@Composable
fun ExplanationScreen(viewModel: ExplanationViewModelSample) {
    val explanationState by viewModel.explanationResult.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()

    LaunchedEffect(Unit) {
        viewModel.fetchExplanation()
    }

    Column(modifier = Modifier
        .fillMaxSize()
        .padding(16.dp)
        .verticalScroll(rememberScrollState())
    ) {
        Text("Explanation Insights", style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.Bold)
        Text("Live Audit Data", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
        
        Spacer(modifier = Modifier.height(24.dp))
        
        if (isLoading) {
            CircularProgressIndicator(modifier = Modifier.padding(32.dp))
            Text("Communicating with python Fairness Engine...")
        } else if (explanationState != null) {
            Text("Why did this decision happen?", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
            Spacer(modifier = Modifier.height(8.dp))
            MetricCard(title = "Executive Summary", value = explanationState!!.executive_summary)
            
            Spacer(modifier = Modifier.height(16.dp))
            Text("Counterfactual Insight", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
            Spacer(modifier = Modifier.height(8.dp))
            MetricCard(title = "What if...", value = explanationState!!.counterfactual_insight)
            
            if (explanationState!!.fallback_used) {
                Spacer(modifier = Modifier.height(16.dp))
                Text("Suggested Fixes", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                Spacer(modifier = Modifier.height(8.dp))
                MetricCard(title = "Fallback Invoked", value = "Standard fairness routines skipped due to constraint violations. Consider reweighing training variables to compensate.")
            }
        } else {
            Text("No explanation data received from backend.", color = MaterialTheme.colorScheme.error)
        }
    }
}
